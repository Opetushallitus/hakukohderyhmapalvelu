(ns hakukohderyhmapalvelu.server
  (:require [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.config :as c]
            [hakukohderyhmapalvelu.handler :as h]
            [ring.adapter.jetty :as jetty]))

(defrecord HttpServer []
  component/Lifecycle

  (start [this]
    (let [port   (c/config :server :http :port)
          server (jetty/run-jetty h/handler {:port port :join? false})]
      (assoc this :server server)))

  (stop [this]
    (when-let [server (:server this)]
      (.stop server))
    (assoc this :server nil)))

(defn new-http-server []
  (map->HttpServer {}))
