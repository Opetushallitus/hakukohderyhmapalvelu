(ns hakukohderyhmapalvelu.handler
  (:require [clj-http.client :as http]
            [clojure.string :as string]
            [compojure.core :as api]
            [compojure.route :as route]
            [config.core :as config]
            [ring.util.response :as response]
            [ring.middleware.defaults :as defaults]
            [ring.middleware.json :as json]
            [ring.middleware.reload :as reload]))

(defn- hakukohderyhmapalvelu-env []
  (let [env (config/env :hakukohderyhmapalvelu-env)]
    (if (or (not env)
            (= env "production"))
      :production
      :development)))

(defonce environment (hakukohderyhmapalvelu-env))

(defn- production-resources []
  (if (= environment :production)
    (route/resources "/hakukohderyhmapalvelu")))

(api/defroutes routes
  (api/GET "/" []
    (response/redirect "/hakukohderyhmapalvelu"))
  (api/context "/hakukohderyhmapalvelu" []
    (api/GET "/" []
      (-> (response/resource-response "index.html" {:root "public"})
          (response/content-type "text/html"))))
  (production-resources)
  (route/not-found "<h1>Not found</h1>"))

(defn- wrap-css-proxy [handler]
  (if (= environment :development)
    (letfn [(is-css-uri? [uri]
              (= uri "/hakukohderyhmapalvelu/css/hakukohderyhmapalvelu.css"))]
      (fn proxy-css [request]
        (if (-> request :uri is-css-uri?)
          (-> (http/get "http://localhost:9032/css/hakukohderyhmapalvelu.css")
              :body
              response/response)
          (handler request))))
    handler))

(defn- wrap-js-proxy [handler prefix]
  (if (= environment :development)
    (let [path-start-idx (count prefix)
          is-js-uri?     (fn [uri]
                           (string/starts-with? uri prefix))
          js-uri-path    (fn [uri]
                           (str "http://localhost:9032/js"
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
                 (wrap-css-proxy)
                 (wrap-js-proxy "/hakukohderyhmapalvelu/js")
                 (wrap-js-proxy "/js")))
