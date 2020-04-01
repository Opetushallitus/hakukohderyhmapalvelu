(ns hakukohderyhmapalvelu.server
  (:require [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.handler :as h]
            [ring.adapter.jetty :as jetty]))

(defrecord HttpServer [config]
  component/Lifecycle

  (start [this]
    (let [port   (-> config :config :server :http :port)
          server (jetty/run-jetty (h/make-handler (:config config)) {:port port :join? false})]
      (assoc this :server server)))

  (stop [this]
    (when-let [server (:server this)]
      (.stop server))
    (assoc this :server nil)))
