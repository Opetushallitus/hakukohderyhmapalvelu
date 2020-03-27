(ns hakukohderyhmapalvelu.handler
  (:require [compojure.api.sweet :as api]
            [compojure.route :as route]
            [ring.util.response :as response]
            [ring.middleware.defaults :as defaults]
            [ring.middleware.json :as json]
            [ring.middleware.reload :as reload]
            [hakukohderyhmapalvelu.health-check :as health-check]))

(defn- redirect-routes []
  (api/undocumented
    (api/GET "/" []
      (response/redirect "/hakukohderyhmapalvelu"))))

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

(def routes
  (api/api
    {:swagger
     {:ui   "/hakukohderyhmapalvelu/api-docs"
      :spec "/hakukohderyhmapalvelu/swagger.json"
      :data {:info        "Hakukohderyhmäpalvelu"
             :description "Hakukohderyhmäpalvelu"
             :consumes    ["application/json"]
             :produces    ["application/json"]}}}
    (redirect-routes)
    (api/context "/hakukohderyhmapalvelu" []
      (index-route)
      (api/context "/api" []
        (health-check-route))
      (resource-route))
    (not-found-route)))

(def handler (-> #'routes
                 (json/wrap-json-response)
                 (defaults/wrap-defaults (dissoc defaults/site-defaults :static))
                 (reload/wrap-reload {:dirs ["src/clj"]})))
