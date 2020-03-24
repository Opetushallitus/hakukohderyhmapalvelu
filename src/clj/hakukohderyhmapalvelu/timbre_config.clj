(ns hakukohderyhmapalvelu.timbre-config
  (:require [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.core :refer [println-appender]]
            [taoensso.timbre.appenders.3rd-party.rolling :refer [rolling-appender]]
            [environ.core :refer [env]]
            [hakukohderyhmapalvelu.config :as hkr-config])
  (:import [java.util TimeZone]))

(defn- create-config []
  (hkr-config/map->Config {}))

(defn configure-logging! []
  (timbre/merge-config!
    {:appenders
                     {:println
                      (println-appender {:stream :std-out})
                      :file-appender
                      (rolling-appender
                        {:path    (str (-> (create-config) :log :base-path)
                                       "/app_hakukohderyhmapalvelu"
                                       (when (:hostname env) (str "_" (:hostname env))))
                         :pattern :daily})}
     :timestamp-opts {:pattern  "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
                      :timezone (TimeZone/getTimeZone "Europe/Helsinki")}
     :output-fn      (partial timbre/default-output-fn {:stacktrace-fonts {}})}))
