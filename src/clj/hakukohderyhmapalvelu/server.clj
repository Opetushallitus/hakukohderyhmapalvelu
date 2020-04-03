(ns hakukohderyhmapalvelu.server
  (:require [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.handler :as h]
            [ring.adapter.jetty :as jetty]
            [schema.core :as s]
            [hakukohderyhmapalvelu.config :as c]))

(defrecord HttpServer [config
                       organisaatio-service]
  component/Lifecycle

  (start [this]
    (s/validate c/HakukohderyhmaConfig config)
    (let [port   (-> config :server :http :port)
          server (jetty/run-jetty (h/make-handler
                                    {:config               config
                                     :organisaatio-service organisaatio-service})
                                  {:port port :join? false})]
      (assoc this :server server)))

  (stop [this]
    (when-let [server (:server this)]
      (.stop server))
    (assoc this :server nil)))
