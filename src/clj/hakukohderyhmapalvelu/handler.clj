(ns hakukohderyhmapalvelu.handler
  (:require [cheshire.core :as json]
            [clj-ring-db-session.authentication.auth-middleware :as auth-middleware]
            [clj-ring-db-session.session.session-client :as session-client]
            [clj-ring-db-session.session.session-store :refer [create-session-store]]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [reitit.coercion.schema]
            [reitit.dev.pretty :as pretty]
            [reitit.ring.middleware.muuntaja :as muuntaja-middleware]
            [reitit.ring.middleware.exception :as exception-middleware]
            [reitit.ring.middleware.parameters :as parameters-middleware]
            [reitit.ring.coercion :as coercion]
            [reitit.ring :as ring]
            [environ.core :refer [env]]
            [hakukohderyhmapalvelu.api-schemas :as schema]
            [hakukohderyhmapalvelu.authentication.auth-routes :as auth-routes]
            [hakukohderyhmapalvelu.cas.mock.mock-authenticating-client-schemas :as mock-cas]
            [hakukohderyhmapalvelu.cas.mock.mock-dispatcher-protocol :as mock-dispatcher-protocol]
            [hakukohderyhmapalvelu.config :as c]
            [hakukohderyhmapalvelu.hakukohderyhma.hakukohderyhma-service-protocol :as hakukohderyhma]
            [hakukohderyhmapalvelu.health-check :as health-check]
            [hakukohderyhmapalvelu.oph-url-properties :as oph-urls]
            [hakukohderyhmapalvelu.schemas.class-pred :as p]
            [hakukohderyhmapalvelu.session-timeout :as session-timeout]
            [clj-access-logging]
            [clj-stdout-access-logging]
            [clj-timbre-access-logging]
            [ring.middleware.defaults :as defaults]
            [ring.middleware.json :as wrap-json]
            [ring.middleware.reload :as reload]
            [ring.middleware.session :as ring-session]
            [ring.util.http-response :as response]
            [schema.core :as s]
            [selmer.parser :as selmer]
            [taoensso.timbre :as log]
            [muuntaja.core :as m])
  (:import [javax.sql DataSource]))


(defn- create-index-handler [config]
  (let [public-config (-> config :public-config json/generate-string)
        rendered-page (selmer/render-file
                        "templates/index.html.template"
                        {:frontend-config  public-config
                         :front-properties (oph-urls/front-json config)
                         :apply-raamit     (c/production-environment? config)})]
    (fn [_]
      (-> (response/ok rendered-page)
          (response/content-type "text/html")
          (response/charset "utf-8")))))

(defn- create-wrap-database-backed-session [config datasource]
  (fn [handler] (ring-session/wrap-session handler
                                           {:root         "/hakukohderyhmapalvelu"
                                            :cookie-attrs {:secure (c/production-environment? config)}
                                            :store        (create-session-store datasource)})))

(s/defschema MakeHandlerArgs
  {:config                           c/HakukohderyhmaConfig
   :db                               {:datasource (s/pred #(instance? DataSource %))
                                      :config     c/HakukohderyhmaConfig}
   :health-checker                   (p/extends-class-pred health-check/HealthChecker)
   :auth-routes-source               (p/extends-class-pred auth-routes/AuthRoutesSource)
   :hakukohderyhma-service           s/Any
   (s/optional-key :mock-dispatcher) (p/extends-class-pred mock-dispatcher-protocol/MockDispatcherProtocol)})

(defn auth-middleware [config db]
  [(create-wrap-database-backed-session config (:datasource db))
   clj-access-logging/wrap-session-access-logging
   #(auth-middleware/with-authentication % (oph-urls/resolve-url :cas.login config))
   session-client/wrap-session-client-headers
   (session-timeout/create-wrap-idle-session-timeout config)])

(defn- integration-test-routes [{:keys [mock-dispatcher config]}]
  (when (c/integration-environment? config)
    ["/mock"
     ["/authenticating-client"
      {:post {:summary    "Mockaa yhden CAS-autentikoituvalla clientilla tehdyn HTTP-kutsun"
              :parameters {:body mock-cas/MockCasAuthenticatingClientRequest}
              :handler    (fn [{{spec :body} :parameters}]
                            (.dispatch-mock mock-dispatcher spec)
                            (response/ok {}))}}]
     ["/reset"
      {:post {:summary "Resetoi mockatut HTTP-kutsumääritykset"
              :handler (fn [_]
                         (.reset-mocks mock-dispatcher)
                         (response/ok {}))}}]]))

(defn- routes [{:keys [health-checker config db auth-routes-source hakukohderyhma-service] :as args}]
  (let [auth (auth-middleware config db)]
    [["/"
      {:get {:no-doc  true
             :handler (fn [_] (response/permanent-redirect "/hakukohderyhmapalvelu/"))}}]
     ["/hakukohderyhmapalvelu"
      ["/login-error"
       {:get {:no-doc  true
              :handler (fn [_]
                         (log/warn "Kirjautuminen epäonnistui ja käyttäjä ohjattiin virhesivulle.")
                         (-> (response/internal-server-error "<h1>Virhe sisäänkirjautumisessa.</h1>")
                             (response/content-type "text/html")))}}]
      [""
       {:get {:middleware auth
              :no-doc     true
              :handler    (fn [_] (response/permanent-redirect "/hakukohderyhmapalvelu/"))}}]
      ["/"
       {:get {:middleware auth
              :no-doc     true
              :handler    (create-index-handler config)}}]
      ["/hakukohderyhmien-hallinta"
       {:get {:middleware auth
              :no-doc     true
              :handler    (create-index-handler config)}}]
      ["/haun-asetukset"
       {:get {:middleware auth
              :no-doc     true
              :handler    (create-index-handler config)}}]
      ["/swagger.json"
       {:get {:no-doc  true
              :swagger {:info {:title       "Hakukohderyhmäpalvelu"
                               :description "Hakukohderyhmäpalvelun ulkoinen rajapinta."}}
              :handler (swagger/create-swagger-handler)}}]
      ["/api"
       ["/health"
        {:get {:summary "Terveystarkastus"
               :tags    ["Admin"]
               :handler (fn [_]
                          (s/validate (p/extends-class-pred health-check/HealthChecker) health-checker)
                          (-> (health-check/check-health health-checker)
                              response/ok
                              (response/content-type "text/html")))}}]
       ["/hakukohderyhma"
        [""
         {:post {:middleware auth
                 :tags       ["Hakukohderyhmä"]
                 :summary    "Tallentaa uuden hakukohderyhmän"
                 :responses  {200 {:body schema/Hakukohderyhma}}
                 :parameters {:body schema/HakukohderyhmaPostRequest}
                 :handler    (fn [{session :session {hakukohderyhma :body} :parameters}]
                               (response/ok (hakukohderyhma/create hakukohderyhma-service session hakukohderyhma)))}}]
        ["/:oid/hakukohteet"
         {:put {:middleware auth
                :tags       ["Hakukohderyhmä"]
                :summary    "Päivittää hakukohderyhmän ja hakukohteiden liitoksen"
                :responses  {200 {:body schema/Hakukohderyhma}}
                :parameters {:path {:oid s/Str} :body [schema/Hakukohde]}
                :handler    (fn [{session :session {hakukohteet :body {oid :oid} :path} :parameters}]
                              (response/ok (hakukohderyhma/update-hakukohderyhma-hakukohteet
                                             hakukohderyhma-service session oid hakukohteet)))}}]
         ["/find-by-hakukohde-oids"
          {:post {:middleware auth
                  :tags       ["Hakukohderyhmä"]
                  :summary    "Hakee kaikki talletetut hakukohderyhmät"
                  :responses  {200 {:body schema/HakukohderyhmaListResponse}}
                  :parameters {:body schema/HakukohderyhmaSearchRequest}
                  :handler    (fn [{session :session {{hakukohde-oids :oids} :body} :parameters}]
                                (response/ok
                                  (hakukohderyhma/find-hakukohderyhmat-by-hakukohteet-oids
                                    hakukohderyhma-service session hakukohde-oids)))}}]
        ["/:oid/rename"
         {:post {:middleware auth
                 :tags       ["Hakukohderyhmä"]
                 :summary    "Uudelleennimeää hakukohderyhmän"
                 :responses  {200 {:body s/Any}}
                 :parameters {:path {:oid s/Str} :body schema/HakukohderyhmaPutRequest}
                 :handler    (fn [{session :session {hakukohderyhma :body {oid :oid} :path} :parameters}]
                               (if (= oid (:oid hakukohderyhma))
                                 (response/ok (hakukohderyhma/rename hakukohderyhma-service session hakukohderyhma))
                                 (response/bad-request "Polun oid ei vastaa lähetetyn hakukohderyhmän oid:ia")))}}]]
       ["/haku"
        [""
         {:get {:middleware auth
                :tags       ["Haku"]
                :summary    "Hakee listauksen käyttäjän organisaation hauista"
                :responses  {200 {:body schema/HaunTiedotListResponse}}
                :parameters {:query {(s/optional-key :all) s/Bool}}
                :handler    (fn [{session :session {{is-all :all} :query} :parameters}]
                              (response/ok
                                (hakukohderyhma/list-haun-tiedot hakukohderyhma-service session (boolean is-all))))}}]
        ["/:oid/hakukohde"
         {:get {:middleware auth
                :tags       ["Haku"]
                :summary    "Hakee listauksen haun hakukohteista"
                :responses  {200 {:body schema/HakukohdeListResponse}}
                :parameters {:path {:oid s/Str}}
                :handler    (fn [{session :session {{haku-oid :oid} :path} :parameters}]
                              (response/ok
                                (hakukohderyhma/list-haun-hakukohteet hakukohderyhma-service session haku-oid)))}}]]
       (integration-test-routes args)]
      ["/auth"
       {:middleware (conj auth session-client/wrap-session-client-headers)}
       ["/cas"
        {:get  {:no-doc     true
                :parameters {:query {:ticket s/Str}}
                :handler    (fn [{{{:keys [ticket]} :query} :parameters :as request}]
                              (auth-routes/login auth-routes-source ticket request))}
         :post {:no-doc  true
                :handler (fn [request] (auth-routes/cas-logout auth-routes-source request))}}]
       ["/logout"
        {:get {:no-doc  true
               :handler (fn [{:keys [session]}] (auth-routes/logout auth-routes-source session))}}]]]]))

(defn router [args]
  (ring/router
    (routes args)
    {:exception pretty/exception
     :data      {:coercion   reitit.coercion.schema/coercion
                 :muuntaja   m/instance
                 :middleware [swagger/swagger-feature
                              parameters-middleware/parameters-middleware
                              muuntaja-middleware/format-negotiate-middleware
                              muuntaja-middleware/format-response-middleware
                              exception-middleware/exception-middleware
                              muuntaja-middleware/format-request-middleware
                              coercion/coerce-response-middleware
                              coercion/coerce-request-middleware]}}))

(s/defn create-handler [args :- MakeHandlerArgs]
  (ring/ring-handler
    (router args)
    (ring/routes
      (swagger-ui/create-swagger-ui-handler
        {:config {:validatorUrl     nil
                  :operationsSorter "alpha"}
         :path   "/hakukohderyhmapalvelu/swagger/index.html"
         :url    "/hakukohderyhmapalvelu/swagger.json"})
      (ring/create-resource-handler {:path "/hakukohderyhmapalvelu" :root "public/hakukohderyhmapalvelu"})
      (ring/create-default-handler {:not-found (constantly {:status 404, :body "<h1>Not found</h1>"})}))))

(def reloader #'reload/reloader)

(s/defn make-production-handler
  [args :- MakeHandlerArgs]
  (-> (create-handler args)
      (clj-access-logging/wrap-access-logging)
      (clj-stdout-access-logging/wrap-stdout-access-logging)
      (clj-timbre-access-logging/wrap-timbre-access-logging
        {:path (str (-> args :config :log :base-path)
                    "/access_hakukohderyhmapalvelu"
                    (when (:hostname env) (str "_" (:hostname env))))})
      (wrap-json/wrap-json-response)
      (defaults/wrap-defaults (-> defaults/site-defaults
                                  (dissoc :static)
                                  (update :security dissoc :anti-forgery)))))

(s/defn make-reloading-handler
  [args :- MakeHandlerArgs]
  (let [reload (reloader ["src/clj" "src/cljc"] true)]
    (fn [request]
      (reload)
      (let [handler (make-production-handler args)]
        (handler request)))))

(s/defn make-handler
  [{config :config :as args} :- MakeHandlerArgs]
  (if (c/production-environment? config)
    (make-production-handler args)
    (make-reloading-handler args)))
