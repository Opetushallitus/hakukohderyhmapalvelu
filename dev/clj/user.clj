(ns user
  (:require [reloaded.repl :refer [system init start stop go reset reset-all set-init!]]
            [schema.core :as s]
            [taoensso.timbre.appenders.core :as appenders]
            [taoensso.timbre :as timbre]
            [timbre-ns-pattern-level])
  (:import [java.util TimeZone]))

(defn hkrp-reset! []
  (try
    (reset)
    (catch Exception e
      (.printStackTrace e))))

(timbre/merge-config!
  {:appenders      {:println
                    (appenders/println-appender {:stream :std-out})}
   :middleware     [(timbre-ns-pattern-level/middleware {"com.zaxxer.hikari.HikariConfig" :debug
                                                         "ring.middleware.logger"         :error
                                                         :all                             :info})]
   :timestamp-opts {:pattern  "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
                    :timezone (TimeZone/getTimeZone "Europe/Helsinki")}
   :output-fn      (partial timbre/default-output-fn {:stacktrace-fonts {}})})
(s/set-fn-validation! true)
(set-init! #(do
              (require 'hakukohderyhmapalvelu.system)
              (require 'hakukohderyhmapalvelu.config)
              ((resolve 'hakukohderyhmapalvelu.system/hakukohderyhmapalvelu-system)
               ((resolve 'hakukohderyhmapalvelu.config/make-config)))))
