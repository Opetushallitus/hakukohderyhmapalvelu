(ns hakukohderyhmapalvelu.core
  (:require [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.system :as system]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.core :as appenders])
  (:gen-class))

(defn- configure-logging []
  (timbre/merge-config!
    {:level     :info
     :appenders {:println (appenders/println-appender
                            {:stream :std-out})}}))

(defn -main [& _args]
  (configure-logging)
  (component/start-system system/hakukohderyhmapalvelu-system))
