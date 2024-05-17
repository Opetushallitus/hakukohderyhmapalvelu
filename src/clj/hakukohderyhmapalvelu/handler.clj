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
            [reitit.ring.middleware.parameters :as parameters-middleware]
            [reitit.ring.coercion :as coercion]
            [reitit.ring :as ring]
            [environ.core :refer [env]]
            [hakukohderyhmapalvelu.api-schemas :as schema]
            [hakukohderyhmapalvelu.authentication.auth-routes :as auth-routes]
            [hakukohderyhmapalvelu.cas.mock.mock-authenticating-client-schemas :as mock-cas]
            [hakukohderyhmapalvelu.cas.mock.mock-dispatcher-protocol :as mock-dispatcher-protocol]
            [hakukohderyhmapalvelu.config :as c]
            [hakukohderyhmapalvelu.exception :as exception]
            [hakukohderyhmapalvelu.hakukohderyhma.hakukohderyhma-service-protocol :as hakukohderyhma]
            [hakukohderyhmapalvelu.health-check :as health-check]
            [hakukohderyhmapalvelu.oph-url-properties :as oph-urls]
            [hakukohderyhmapalvelu.schemas.class-pred :as p]
            [hakukohderyhmapalvelu.session-timeout :as session-timeout]
            [hakukohderyhmapalvelu.siirtotiedosto.siirtotiedosto-protocol :as siirtotiedosto]
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
            [muuntaja.core :as m]
            [clj-time.core :as t]
            [clj-time.format :as f])
  (:import [javax.sql DataSource]))


(defn- random-lowercase-string [n]
  (reduce (fn [acc _] (str acc (char (+ 97 (rand-int 26))))) "" (range n)))

(def ^:private cache-fingerprint (random-lowercase-string 10))

(defn- create-index-handler [config]
  (let [public-config (-> config :public-config json/generate-string)
        rendered-page (selmer/render-file
                        "templates/index.html.template"
                        {:frontend-config   public-config
                         :front-properties  (oph-urls/front-json config)
                         :apply-raamit      (c/production-environment? config)
                         :cache-fingerprint cache-fingerprint})]
    (fn [_]
      (-> (response/ok rendered-page)
          (response/content-type "text/html")
          (response/charset "utf-8")))))

(defn- create-error-handler [config]
  (let [rendered-page (selmer/render-file
                        "templates/login-error.html.template"
                        {:apply-raamit (c/production-environment? config)})]
    (fn [_]
      (log/warn "Kirjautuminen epäonnistui ja käyttäjä ohjattiin virhesivulle.")
      (-> (response/forbidden rendered-page)
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
   :siirtotiedosto-service           s/Any
   (s/optional-key :mock-dispatcher) (p/extends-class-pred mock-dispatcher-protocol/MockDispatcherProtocol)})

(defn auth-middleware [config db]
  [(create-wrap-database-backed-session config (:datasource db))
   clj-access-logging/wrap-session-access-logging
   #(auth-middleware/with-authentication % (oph-urls/resolve-url :cas.login config))
   session-client/wrap-session-client-headers
   (session-timeout/create-wrap-absolute-session-timeout config)])

(def datetime-format "yyyy-MM-dd'T'HH:mm:ss")
(def datetime-parser (f/formatter datetime-format (t/default-time-zone)))

(defn- parseDatetime
  ([datetimeStr fieldDesc]
   (parseDatetime datetimeStr fieldDesc nil))
  ([datetimeStr fieldDesc default]
   (if-not (nil? datetimeStr)
     (try (f/parse datetime-parser datetimeStr)
          (catch java.lang.IllegalArgumentException _
            (response/bad-request!
              {:msg  (str "Illegal " fieldDesc " '" datetimeStr "', allowed format: '" datetime-format "'")})))
     default
     )))

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

(defn- routes [{:keys [health-checker config db auth-routes-source hakukohderyhma-service siirtotiedosto-service]
                :as args}]
  (let [auth (auth-middleware config db)]
    [["/"
      {:get {:no-doc  true
             :handler (fn [_] (response/permanent-redirect "/hakukohderyhmapalvelu/"))}}]
     ["/hakukohderyhmapalvelu"
      ["/login-error"
       {:get {:no-doc  true
              :handler (create-error-handler config)}}]
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
       ["/siirtotiedosto"
        {:get {:middleware auth
               :tags        ["Siirtotiedosto"]
               :summary     "Tallentaa annetulla aikavälillä luodut tai muokatut hakukohderyhmät siirtotiedostoon"
               :responses   {200 {:body schema/SiirtotiedostoResponse}
                             400 {:body s/Str}}
               :parameters  {:query {(s/optional-key :start-datetime) (s/maybe s/Str)
                                     (s/optional-key :end-datetime) (s/maybe s/Str)}}
               :handler    (fn [{session :session {{startDatetime :start-datetime
                                                    endDatetime   :end-datetime} :query} :parameters}]
                             (let [start (parseDatetime startDatetime "startDatetime")
                                   end (parseDatetime endDatetime "endDatetime" (t/now))
                                   chunk-size (-> config
                                                  :siirtotiedosto
                                                  :max-kohderyhmacount-in-file)
                                   all-oids (hakukohderyhma/get-hakukohderyhma-oid-chunks-by-timerange
                                              hakukohderyhma-service
                                              session
                                              start
                                              end)
                                   create-siirtotiedosto (fn [oid-chunk] (->> {:hakukohderyhma-oids oid-chunk}
                                                                              (hakukohderyhma/list-hakukohteet-and-settings
                                                                              hakukohderyhma-service session)
                                                                              (siirtotiedosto/create-siirtotiedosto siirtotiedosto-service)
                                                                              ))
                                   s3-keys (map create-siirtotiedosto (partition chunk-size chunk-size nil all-oids))]
                             (response/ok {:keys s3-keys
                                           :count (count all-oids)
                                           :success true})))}}]
       ["/hakukohderyhma"
        [""
         {:post {:middleware auth
                 :tags       ["Hakukohderyhmä"]
                 :summary    "Tallentaa uuden hakukohderyhmän"
                 :responses  {200 {:body schema/Hakukohderyhma}}
                 :parameters {:body schema/HakukohderyhmaPostRequest}
                 :handler    (fn [{session :session {hakukohderyhma :body} :parameters}]
                               (response/ok (hakukohderyhma/create hakukohderyhma-service session hakukohderyhma)))}}]
        ["/search/find-by-hakukohde-oids"
         {:post {:middleware auth
                 :tags       ["Hakukohderyhmä"]
                 :summary    "Hakee kaikki talletetut hakukohderyhmät"
                 :responses  {200 {:body schema/HakukohderyhmaListResponse}}
                 :parameters {:body schema/HakukohderyhmaSearchRequest}
                 :handler    (fn [{session :session {{hakukohde-oids :oids include-empty :includeEmpty} :body} :parameters}]
                               (response/ok (hakukohderyhma/find-hakukohderyhmat-by-hakukohteet-oids
                                              hakukohderyhma-service session hakukohde-oids include-empty)))}}]
        ["/search/by-hakukohteet"
         {:post {:middleware auth
                 :tags       ["Hakukohderyhmä"]
                 :summary    "Hakee hakukohderyhmät hakukohteittain"
                 :response   {200 {:body schema/GroupedHakukohderyhmaResponse}}
                 :parameters {:body [s/Str]}
                 :handler    (fn [{session :session {hakukohde-oids :body} :parameters}]
                               (response/ok (hakukohderyhma/get-hakukohderyhmat-by-hakukohteet
                                              hakukohderyhma-service session hakukohde-oids)))}}]
        ["/:oid"
         [""
          {:delete {:middleware auth
                    :tags       ["Hakukohderyhmä"]
                    :summary    "Poistaa hakukohderyhmän ja hakukohderyhmän liitokset"
                    :responses  {200 {:body schema/HakukohderyhmaDeleteResponse}
                                 400 {:body schema/HakukohderyhmaDeleteResponse}}
                    :parameters {:path {:oid s/Str}}
                    :handler    (fn [{session :session {{oid :oid} :path} :parameters}]
                                  (condp = (hakukohderyhma/delete hakukohderyhma-service session oid)
                                    schema/StatusDeleted (response/ok {:status schema/StatusDeleted})
                                    schema/StatusInUse (response/conflict {:status schema/StatusInUse})))}}]
         ["/hakukohteet"
          {:put {:middleware auth
                 :tags       ["Hakukohderyhmä"]
                 :summary    "Päivittää hakukohderyhmän ja hakukohteiden liitoksen"
                 :responses  {200 {:body schema/Hakukohderyhma}}
                 :parameters {:path {:oid s/Str} :body [schema/Hakukohde]}
                 :handler    (fn [{session :session {hakukohteet :body {oid :oid} :path} :parameters}]
                               (response/ok (hakukohderyhma/update-hakukohderyhma-hakukohteet
                                              hakukohderyhma-service session oid hakukohteet)))}
           :get {:middleware auth
                 :tags       ["Hakukohderyhmä"]
                 :summary    ["Hakee hakukohderyhmän tiedot"]
                 :responses {200 {:body [s/Str]}}
                 :parameters {:path {:oid s/Str}}
                 :handler    (fn [{{{oid :oid} :path} :parameters}]
                               (response/ok (hakukohderyhma/get-hakukohde-oids-for-hakukohderyhma-oid
                                              hakukohderyhma-service oid)))}}]
         ["/rename"
          {:post {:middleware auth
                  :tags       ["Hakukohderyhmä"]
                  :summary    "Uudelleennimeää hakukohderyhmän"
                  :responses  {200 {:body s/Any}}
                  :parameters {:path {:oid s/Str} :body schema/HakukohderyhmaPutRequest}
                  :handler    (fn [{session :session {hakukohderyhma :body {oid :oid} :path} :parameters}]
                                (if (= oid (:oid hakukohderyhma))
                                  (response/ok (hakukohderyhma/rename hakukohderyhma-service session hakukohderyhma))
                                  (response/bad-request "Polun oid ei vastaa lähetetyn hakukohderyhmän oid:ia")))}}]
         ["/settings"
          {:get {:middleware auth
                 :tags       ["Hakukohderyhmä", "Asetukset"]
                 :summary    ["Hakee hakukohderyhmän asetukset"]
                 :responses  {200 {:body schema/HakukohderyhmaSettings}}
                 :parameters {:path {:oid s/Str}}
                 :handler    (fn [{session :session {{oid :oid} :path}  :parameters}]
                               (response/ok (hakukohderyhma/get-settings hakukohderyhma-service session oid)))}
           :put {:middleware auth
                  :tags       ["Hakukohderyhmä", "Asetukset"]
                  :summary    ["Asettaa hakukohderyhmän asetukset"]
                  :responses  {200 {:body schema/HakukohderyhmaSettings}}
                  :parameters {:path {:oid s/Str} :body schema/HakukohderyhmaSettings}
                  :handler    (fn [{session :session {settings :body {oid :oid} :path}  :parameters}]
                                (response/ok (hakukohderyhma/insert-or-update-settings hakukohderyhma-service session oid settings)))}}]]]



       ["/hakukohde/:oid/hakukohderyhmat"
        {:get {:middleware auth
               :tags       ["Hakukohde"]
               :responses  {200 {:body [s/Str]}}
               :summary    "Hakee listauksen annetun hakukohteen hakukohderyhmistä"
               :parameters {:path {:oid s/Str}}
               :handler    (fn [{session :session {{hakukohde-oid :oid} :path} :parameters}]
                             (response/ok
                               (hakukohderyhma/list-hakukohderyhma-oids-by-hakukohde-oid hakukohderyhma-service session hakukohde-oid)))}}]
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
         :post {:no-doc     true
                :parameters {:form {:logoutRequest s/Str}}
                :handler    (fn [{{logout-request :logoutRequest} :params}]
                              (auth-routes/cas-logout auth-routes-source logout-request))}}]
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
                              exception/exception-middleware
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
         :path   "/hakukohderyhmapalvelu/swagger"
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
