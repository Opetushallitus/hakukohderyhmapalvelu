(ns hakukohderyhmapalvelu.handler
  (:require [cheshire.core :as json]
            [clj-ring-db-session.authentication.auth-middleware :as auth-middleware]
            [clj-ring-db-session.session.session-client :as session-client]
            [clj-ring-db-session.session.session-store :refer [create-session-store]]
            [compojure.api.core :as compojure-core]
            [compojure.api.sweet :as api]
            [compojure.route :as route]
            [hakukohderyhmapalvelu.api-schemas :as schema]
            [hakukohderyhmapalvelu.authentication.auth-routes :as auth-routes]
            [hakukohderyhmapalvelu.cas.mock.mock-cas-client-schemas :as mock-cas]
            [hakukohderyhmapalvelu.cas.mock.mock-dispatcher-protocol :as mock-dispatcher-protocol]
            [hakukohderyhmapalvelu.config :as c]
            [hakukohderyhmapalvelu.health-check :as health-check]
            [hakukohderyhmapalvelu.oph-url-properties :as oph-urls]
            [hakukohderyhmapalvelu.organisaatio.organisaatio-protocol :as organisaatio-service-protocol]
            [hakukohderyhmapalvelu.schemas.class-pred :as p]
            [hakukohderyhmapalvelu.session-timeout :as session-timeout]
            [ring.middleware.defaults :as defaults]
            [ring.middleware.json :as wrap-json]
            [ring.middleware.logger :as logger]
            [ring.middleware.reload :as reload]
            [ring.middleware.session :as ring-session]
            [ring.util.http-response :as response]
            [schema.core :as s]
            [selmer.parser :as selmer])
  (:import [javax.sql DataSource]))

(defn- redirect-routes []
  (api/undocumented
    (api/GET "/" []
      (response/permanent-redirect "/hakukohderyhmapalvelu"))))

(defn- index-route [config]
  (let [public-config (-> config :public-config json/generate-string)
        rendered-page (selmer/render-file
                        "templates/index.html.template"
                        {:config           public-config
                         :front-properties (oph-urls/front-json config)})]
    (api/undocumented
      (api/GET "/" []
        (-> (response/ok rendered-page)
            (response/content-type "text/html")
            (response/charset "utf-8"))))))

(defn- health-check-route [health-checker]
  (s/validate (p/extends-class-pred health-check/HealthChecker) health-checker)
  (api/undocumented
    (api/GET "/api/health" []
      (health-check/check-health health-checker))))

(defn- resource-route []
  (api/undocumented
    (route/resources "/" {:root "public/hakukohderyhmapalvelu"})))

(defn- not-found-route []
  (api/undocumented
    (route/not-found "<h1>Not found</h1>")))

(defn- integration-test-routes [mock-dispatcher]
  (api/context "/api/mock" []
    (api/POST "/cas-client" []
      :summary "Mockaa yhden CAS -clientilla tehdyn HTTP-kutsun"
      :body [spec mock-cas/MockCasClientRequest]
      (.dispatch-mock mock-dispatcher spec)
      (response/ok {}))

    (api/POST "/reset" []
      :summary "Resetoi mockatut HTTP-kutsumääritykset"
      (.reset-mocks mock-dispatcher)
      (response/ok {}))))

(defn- create-wrap-database-backed-session [config datasource]
  (fn [handler] (ring-session/wrap-session handler
                               {:root "/hakukohderyhmapalvelu"
                                :cookie-attrs {:secure (= :production (-> config :public-config :environment))}
                                :store (create-session-store datasource)})))


(s/defschema MakeHandlerArgs
  {:config                           c/HakukohderyhmaConfig
   :db                               {:datasource (s/pred #(instance? DataSource %))
                                      :config c/HakukohderyhmaConfig}
   :health-checker                   (p/extends-class-pred health-check/HealthChecker)
   :organisaatio-service             (p/extends-class-pred organisaatio-service-protocol/OrganisaatioServiceProtocol)
   :auth-routes-source               (p/extends-class-pred auth-routes/AuthRoutesSource)
   (s/optional-key :mock-dispatcher) (p/extends-class-pred mock-dispatcher-protocol/MockDispatcherProtocol)})

(s/defn ^:private make-authenticated-routes [organisaatio-service
                                             config]
  (api/routes
    (index-route config)
    (api/context "/api" []
      :tags ["api"]
      (api/POST "/hakukohderyhma" []
        :summary "Tallentaa uuden hakukohderyhmän"
        :body [hakukohderyhma schema/HakukohderyhmaRequest]
        :return schema/HakukohderyhmaResponse
        (let [result (.post-new-organisaatio organisaatio-service hakukohderyhma)]
          (response/ok result))))))

(s/defn make-routes
  [{:keys [config
           db
           health-checker
           organisaatio-service
           auth-routes-source
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
                                        #(auth-middleware/with-authentication % (oph-urls/resolve-url :cas.login config) (:datasource db))
                                        session-client/wrap-session-client-headers
                                        (session-timeout/create-wrap-idle-session-timeout config)]
                                       (-> (make-authenticated-routes organisaatio-service config)
                                           wrap-json/wrap-json-response
                                           api/undocumented)
                                       (auth-routes/create-auth-routes auth-routes-source))
      (when (-> config :public-config :environment (= :it))
        (integration-test-routes mock-dispatcher))
      (health-check-route health-checker)
      (resource-route))
    (not-found-route)))

(def reloader #'reload/reloader)

(s/defn make-production-handler
  [args :- MakeHandlerArgs]
  (-> (make-routes args)
      (logger/wrap-with-logger)
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
  (if (-> config :public-config :environment (= :production))
    (make-production-handler args)
    (make-reloading-handler args)))
