(ns hakukohderyhmapalvelu.clj-timbre-audit-log
  (:require [taoensso.timbre :as timbre]
            [environ.core :refer [env]]
            [taoensso.timbre.appenders.3rd-party.rolling :refer [rolling-appender]])
  (:import [fi.vm.sade.auditlog
            Logger
            Audit
            ApplicationType]
           java.util.TimeZone))

(defn create-audit-logger [service-name base-path ^ApplicationType application-type]
  (let [audit-log-config (assoc timbre/example-config
                           :appenders {:standard-out     {:enabled? false
                                                          :async?   true}
                                       :println          {:enabled? false
                                                          :async?   true}
                                       :file-appender   (-> (rolling-appender
                                                              {:path    (str base-path
                                                                             "/audit_" service-name
                                                                             ;; Hostname will differentiate files in actual environments
                                                                             (when (:hostname env) (str "_" (:hostname env))))
                                                               :pattern :daily})
                                                            (assoc :output-fn (fn [data] (force (:msg_ data))))
                                                            (assoc :async? true))}
                           :timestamp-opts {:pattern  "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
                                            :timezone (TimeZone/getTimeZone "Europe/Helsinki")})
        logger           (proxy [Logger] [] (log [s]
                                              (timbre/log* audit-log-config :info s)))]
    (new Audit logger service-name application-type)))
