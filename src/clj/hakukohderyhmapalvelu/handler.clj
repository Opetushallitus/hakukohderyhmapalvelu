(ns hakukohderyhmapalvelu.handler
  (:require [compojure.core :as api]
            [compojure.route :as route]
            [ring.util.response :as response]
            [ring.middleware.defaults :as defaults]
            [ring.middleware.json :as json]
            [ring.middleware.reload :as reload]))

(api/defroutes routes
  (api/GET "/" []
    (response/redirect "/hakukohderyhmapalvelu"))
  (api/context "/hakukohderyhmapalvelu" []
    (api/GET "/" []
      (-> (response/resource-response "index.html" {:root "public"})
          (response/content-type "text/html"))))
  (route/resources "/")
  (route/not-found "<h1>Not found</h1>"))

(def handler (-> #'routes
                 (json/wrap-json-response)
                 (defaults/wrap-defaults (dissoc defaults/site-defaults :static))
                 (reload/wrap-reload {:dirs ["src/clj"]})))
