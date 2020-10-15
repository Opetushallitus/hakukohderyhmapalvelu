(ns hakukohderyhmapalvelu.handler
  (:require [cheshire.core :as json]
            [clj-ring-db-session.authentication.auth-middleware :as auth-middleware]
            [clj-ring-db-session.session.session-client :as session-client]
            [clj-ring-db-session.session.session-store :refer [create-session-store]]
            [compojure.api.core :as compojure-core]
            [compojure.api.sweet :as api]
            [compojure.route :as route]
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
            [taoensso.timbre :as log])
  (:import [javax.sql DataSource]))

(defn- redirect-routes []
  (api/undocumented
    (api/GET "/" []
      (response/permanent-redirect "/hakukohderyhmapalvelu"))))

(defn- production-environment? [config]
  (-> config :public-config :environment (= :production)))

(defn- index-route [config]
  (let [public-config  (-> config :public-config json/generate-string)
        rendered-page  (selmer/render-file
                         "templates/index.html.template"
                         {:frontend-config  public-config
                          :front-properties (oph-urls/front-json config)
                          :apply-raamit     (production-environment? config)})
        index-response (fn []
                         (-> (response/ok rendered-page)
                             (response/content-type "text/html")
                             (response/charset "utf-8")))]
    (api/undocumented
      (api/GET "/" []
        (index-response))
      (api/GET "/hakukohderyhmien-hallinta" []
        (index-response)))))

(defn- health-check-route [health-checker]
  (s/validate (p/extends-class-pred health-check/HealthChecker) health-checker)
  (api/undocumented
    (api/GET "/api/health" []
      (health-check/check-health health-checker))))

(defn- resource-route []
  (api/undocumented
    (route/resources "/" {:root "public/hakukohderyhmapalvelu"})))

(defn- error-routes []
  (api/undocumented
    (api/GET "/login-error" []
      (do
        (log/warn "Kirjautuminen epäonnistui ja käyttäjä ohjattiin virhesivulle.")
        (-> (response/internal-server-error "<h1>Virhe sisäänkirjautumisessa.</h1>")
            (response/content-type "text/html"))))))

(defn- not-found-route []
  (api/undocumented
    (route/not-found "<h1>Not found</h1>")))

(defn- integration-test-routes [mock-dispatcher]
  (api/context "/api/mock" []
    (api/POST "/authenticating-client" []
      :summary "Mockaa yhden CAS-autentikoituvalla clientilla tehdyn HTTP-kutsun"
      :body [spec mock-cas/MockCasAuthenticatingClientRequest]
      (.dispatch-mock mock-dispatcher spec)
      (response/ok {}))

    (api/POST "/reset" []
      :summary "Resetoi mockatut HTTP-kutsumääritykset"
      (.reset-mocks mock-dispatcher)
      (response/ok {}))))

(defn- create-wrap-database-backed-session [config datasource]
  (fn [handler] (ring-session/wrap-session handler
                                           {:root         "/hakukohderyhmapalvelu"
                                            :cookie-attrs {:secure (= :production (-> config :public-config :environment))}
                                            :store        (create-session-store datasource)})))

(s/defschema MakeHandlerArgs
  {:config                           c/HakukohderyhmaConfig
   :db                               {:datasource (s/pred #(instance? DataSource %))
                                      :config     c/HakukohderyhmaConfig}
   :health-checker                   (p/extends-class-pred health-check/HealthChecker)
   :auth-routes-source               (p/extends-class-pred auth-routes/AuthRoutesSource)
   :hakukohderyhma-service           s/Any
   (s/optional-key :mock-dispatcher) (p/extends-class-pred mock-dispatcher-protocol/MockDispatcherProtocol)})

(s/defn ^:private make-authenticated-routes [hakukohderyhma-service
                                             config]
  (api/routes
    (index-route config)
    (api/context "/api" []
      :tags ["api"]
      (api/POST "/hakukohderyhma" {session :session}
        :summary "Tallentaa uuden hakukohderyhmän"
        :body [hakukohderyhma schema/HakukohderyhmaRequest]
        :return schema/HakukohderyhmaResponse
        (response/ok (hakukohderyhma/create hakukohderyhma-service session hakukohderyhma))))))

(s/defn make-routes
  [{:keys [config
           db
           health-checker
           auth-routes-source
           hakukohderyhma-service
           mock-dispatcher]} :- MakeHandlerArgs]
  (api/api
    {:swagger
     {:ui   "/hakukohderyhmapalvelu/api-docs"
      :spec "/hakukohderyhmapalvelu/swagger.json"
      :data {:info     {:title       "Hakukohderyhmäpalvelu"
                        :description "Hakukohderyhmäpalvelu"}
             :tags     [{:name "api" :description "Hakukohderyhmäpalvelu API"}]
             :consumes ["application/json"]
             :produces ["application/json"]}}}
    (redirect-routes)
    (api/context "/hakukohderyhmapalvelu" []
      (compojure-core/route-middleware [(create-wrap-database-backed-session config (:datasource db))
                                        clj-access-logging/wrap-session-access-logging
                                        #(auth-middleware/with-authentication % (oph-urls/resolve-url :cas.login config))
                                        session-client/wrap-session-client-headers
                                        (session-timeout/create-wrap-idle-session-timeout config)]
                                       (-> (make-authenticated-routes hakukohderyhma-service config)
                                           wrap-json/wrap-json-response
                                           api/undocumented)
                                       (auth-routes/create-auth-routes auth-routes-source))
      (when (-> config :public-config :environment (= :it))
        (integration-test-routes mock-dispatcher))
      (health-check-route health-checker)
      (error-routes)
      (resource-route))
    (not-found-route)))

(def reloader #'reload/reloader)

(s/defn make-production-handler
  [args :- MakeHandlerArgs]
  (-> (make-routes args)
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
  (if (production-environment? config)
    (make-production-handler args)
    (make-reloading-handler args)))
