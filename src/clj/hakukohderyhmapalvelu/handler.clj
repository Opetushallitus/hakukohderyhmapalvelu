(ns hakukohderyhmapalvelu.handler
  (:require [clj-http.client :as http]
            [clojure.string :as string]
            [compojure.core :as api]
            [compojure.route :as route]
            [hakukohderyhmapalvelu.config :as c]
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
  (route/resources "/hakukohderyhmapalvelu")
  (route/not-found "<h1>Not found</h1>"))

(defn- wrap-css-proxy [handler]
  (if (= (c/config :environment) :development)
    (letfn [(is-css-uri? [uri]
              (= uri "/hakukohderyhmapalvelu/css/hakukohderyhmapalvelu.css"))]
      (fn proxy-css [request]
        (if (-> request :uri is-css-uri?)
          (-> (http/get (str (c/config :server :shadow-cljs-server-url)
                             "/css/hakukohderyhmapalvelu.css"))
              :body
              response/response)
          (handler request))))
    handler))

(defn- wrap-js-proxy [handler prefix]
  (if (= (c/config :environment) :development)
    (let [path-start-idx (count prefix)
          is-js-uri?     (fn [uri]
                           (string/starts-with? uri prefix))
          js-uri-path    (fn [uri]
                           (str (c/config :server :shadow-cljs-server-url)
                                "/js"
                                (subs uri path-start-idx)))]
      (fn proxy-js [request]
        (if (-> request :uri is-js-uri?)
          (-> request :uri js-uri-path http/get :body response/response)
          (handler request))))
    handler))

(def handler (-> #'routes
                 (json/wrap-json-response)
                 (defaults/wrap-defaults (dissoc defaults/site-defaults :static))
                 (reload/wrap-reload {:dirs ["src/clj"]})
                 (wrap-js-proxy "/hakukohderyhmapalvelu/js")
                 (wrap-js-proxy "/js")
                 (wrap-css-proxy)))
