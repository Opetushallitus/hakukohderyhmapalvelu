(ns hakukohderyhmapalvelu.server
  (:require [hakukohderyhmapalvelu.config :as c]
            [hakukohderyhmapalvelu.handler :refer [handler]]
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
  (let [port (c/config :server :http :port)]
    (configure-logging)
    (run-jetty handler {:port port :join? false})))
