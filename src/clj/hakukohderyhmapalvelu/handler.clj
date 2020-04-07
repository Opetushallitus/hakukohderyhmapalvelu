(ns hakukohderyhmapalvelu.handler
  (:require [cheshire.core :as json]
            [compojure.api.sweet :as api]
            [compojure.route :as route]
            [hakukohderyhmapalvelu.config :as c]
            [hakukohderyhmapalvelu.cas.mock.mock-cas-client-schemas :as mock-cas]
            [hakukohderyhmapalvelu.cas.mock.mock-dispatcher-protocol :as mock-dispatcher-protocol]
            [hakukohderyhmapalvelu.organisaatio.organisaatio-protocol :as organisaatio-service-protocol]
            [hakukohderyhmapalvelu.oph-url-properties :as oph-urls]
            [hakukohderyhmapalvelu.schemas.class-pred :as p]
            [ring.util.http-response :as response]
            [ring.middleware.defaults :as defaults]
            [ring.middleware.json :as wrap-json]
            [ring.middleware.logger :as logger]
            [ring.middleware.reload :as reload]
            [schema.core :as s]
            [selmer.parser :as selmer]
            [hakukohderyhmapalvelu.health-check :as health-check]
            [hakukohderyhmapalvelu.api-schemas :as schema]))

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

(defn- health-check-route []
  (api/undocumented
    (api/GET "/health" []
      (health-check/check-health))))

(defn- resource-route []
  (api/undocumented
    (route/resources "/" {:root "public/hakukohderyhmapalvelu"})))

(defn- not-found-route []
  (api/undocumented
    (route/not-found "<h1>Not found</h1>")))

(defn- integration-test-routes [mock-dispatcher]
  (api/context "/mock" []
    (api/POST "/cas-client" []
      :summary "Mockaa yhden CAS -clientilla tehdyn HTTP-kutsun"
      :body [spec mock-cas/MockCasClientRequest]
      (.dispatch-mock mock-dispatcher spec)
      (response/ok {}))

    (api/POST "/reset" []
      :summary "Resetoi mockatut HTTP-kutsumääritykset"
      (.reset-mocks mock-dispatcher)
      (response/ok {}))))

(s/defschema MakeHandlerArgs
  {:config                           c/HakukohderyhmaConfig
   :organisaatio-service             (p/extends-class-pred organisaatio-service-protocol/OrganisaatioServiceProtocol)
   (s/optional-key :mock-dispatcher) (p/extends-class-pred mock-dispatcher-protocol/MockDispatcherProtocol)})

(s/defn make-routes
  [{:keys [config
           organisaatio-service
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
      (index-route config)
      (api/context "/api" []
        :tags ["api"]
        (api/POST "/hakukohderyhma" []
          :summary "Tallentaa uuden hakukohderyhmän"
          :body [hakukohderyhma schema/HakukohderyhmaRequest]
          :return schema/HakukohderyhmaResponse
          (response/ok (.post-new-organisaatio organisaatio-service hakukohderyhma)))
        (when (-> config :public-config :environment (= :it))
          (integration-test-routes mock-dispatcher))
        (health-check-route))
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
