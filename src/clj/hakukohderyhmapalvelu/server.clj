(ns hakukohderyhmapalvelu.server
  (:require [hakukohderyhmapalvelu.handler :refer [handler]]
            [config.core :as config]
            [ring.adapter.jetty :refer [run-jetty]]
            [taoensso.timbre.appenders.core :as appenders]
            [taoensso.timbre :as timbre])
  (:gen-class))

(defn- configure-logging []
  (timbre/merge-config!
    {:level     :info
     :appenders {:println (appenders/println-appender
                            {:stream :std-out})}}))

(defn -main [& _args]
  (let [port (or (config/env :hakukohderyhmapalvelu-service-port) 8080)]
    (configure-logging)
    (run-jetty handler {:port port :join? false})))
