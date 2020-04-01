(ns hakukohderyhmapalvelu.handler
  (:require [compojure.api.sweet :as api]
            [compojure.route :as route]
            [hakukohderyhmapalvelu.config :as c]
            [ring.util.http-response :as response]
            [ring.middleware.defaults :as defaults]
            [ring.middleware.json :as json]
            [ring.middleware.logger :as logger]
            [ring.middleware.reload :as reload]
            [schema.core :as s]
            [hakukohderyhmapalvelu.health-check :as health-check]
            [hakukohderyhmapalvelu.api-schemas :as schema]))

(defn- redirect-routes []
  (api/undocumented
    (api/GET "/" []
      (response/permanent-redirect "/hakukohderyhmapalvelu"))))

(defn- index-route []
  (api/undocumented
    (api/GET "/" []
      (-> (response/resource-response "index.html" {:root "public/hakukohderyhmapalvelu"})
          (response/content-type "text/html")
          (response/charset "utf-8")))))

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

(defn make-routes []
  (api/api
    {:swagger
     {:ui   "/hakukohderyhmapalvelu/api-docs"
      :spec "/hakukohderyhmapalvelu/swagger.json"
      :data {:info        "Hakukohderyhmäpalvelu"
             :description "Hakukohderyhmäpalvelu"
             :tags        [{:name "api" :description "Hakukohderyhmäpalvelu API"}]
             :consumes    ["application/json"]
             :produces    ["application/json"]}}}
    (redirect-routes)
    (api/context "/hakukohderyhmapalvelu" []
      (index-route)
      (api/context "/api" []
        :tags ["api"]
        (api/POST "/hakukohderyhma" []
          :summary "Tallentaa uuden hakukohderyhmän"
          :body [hakukohderyhma schema/Hakukohderyhma]
          :return schema/Hakukohderyhma
          (Thread/sleep 2000)
          (response/ok hakukohderyhma))
        (health-check-route))
      (resource-route))
    (not-found-route)))

(def reloader #'reload/reloader)

(s/defn make-production-handler
  [config :- c/HakukohderyhmaConfig]
  (-> (make-routes config)
      (logger/wrap-with-logger)
      (json/wrap-json-response)
      (defaults/wrap-defaults (-> defaults/site-defaults
                                  (dissoc :static)
                                  (update :security dissoc :anti-forgery)))))

(defn- make-reloading-handler [config]
  (let [reload (reloader ["src/clj" "src/cljc"] true)]
    (fn [request]
      (reload)
      (let [handler (make-production-handler config)]
        (handler request)))))

(defn make-handler [config]
  (if (-> config :public-config :environment (= :production))
    (make-production-handler config)
    (make-reloading-handler config)))
