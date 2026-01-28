(ns hakukohderyhmapalvelu.db
  (:require [cheshire.core :as json]
            [clojure.java.jdbc :as jdbc]
            [clojure.set :as cs]
            [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.config :as c]
            [hikari-cp.core :as hikari]
            [schema.core :as s])
  (:import [java.sql Date PreparedStatement Timestamp]
           [java.time Instant LocalDate LocalDateTime OffsetDateTime ZoneId ZonedDateTime]
           [org.postgresql.util PGobject]))

(def ^:private system-zone (ZoneId/systemDefault))

(extend-protocol jdbc/ISQLValue
  clojure.lang.IPersistentCollection
  (sql-value [value]
    (doto (PGobject.)
      (.setType "jsonb")
      (.setValue (json/generate-string value)))))

(extend-protocol jdbc/IResultSetReadColumn
  PGobject
  (result-set-read-column [pgobj _ _]
    (let [type  (.getType pgobj)
          value (.getValue pgobj)]
      (case type
        "json" (json/parse-string value true)
        "jsonb" (json/parse-string value true)
        :else value))))

(extend-protocol jdbc/IResultSetReadColumn
  java.sql.Date
  (result-set-read-column [v _ _] (.toLocalDate v))

  java.sql.Timestamp
  (result-set-read-column [v _ _]
    (OffsetDateTime/ofInstant (.toInstant v) system-zone))

  org.postgresql.jdbc.PgArray
  (result-set-read-column [v _ _]
    (vec (.getArray v))))

(extend-protocol jdbc/ISQLParameter
  OffsetDateTime
  (set-parameter [v ^PreparedStatement stmt idx]
    (.setObject stmt idx v))

  ZonedDateTime
  (set-parameter [v ^PreparedStatement stmt idx]
    (.setObject stmt idx (.toOffsetDateTime v)))

  LocalDateTime
  (set-parameter [v ^PreparedStatement stmt idx]
    (.setTimestamp stmt idx (Timestamp/valueOf v)))

  LocalDate
  (set-parameter [v ^PreparedStatement stmt idx]
    (.setDate stmt idx (Date/valueOf v)))

  Instant
  (set-parameter [v ^PreparedStatement stmt idx]
    (.setTimestamp stmt idx (Timestamp/from v))))

(defrecord DbPool [config]
  component/Lifecycle

  (start [this]
    (s/validate c/HakukohderyhmaConfig config)
    (let [datasource-options (merge
                               {:auto-commit        true
                                :read-only          false
                                :connection-timeout 30000
                                :validation-timeout 5000
                                :idle-timeout       600000
                                :max-lifetime       1800000
                                :minimum-idle       10
                                :maximum-pool-size  10
                                :pool-name          "db-pool"
                                :adapter            "postgresql"
                                :register-mbeans    false}
                               (-> config
                                   :db
                                   (select-keys [:username
                                                 :password
                                                 :database-name
                                                 :host
                                                 :port])
                                   (cs/rename-keys {:host :server-name
                                                    :port :port-number})))
          datasource         (hikari/make-datasource datasource-options)]
      (assoc this :datasource datasource)))

  (stop [this]
    (when-let [datasource (:datasource this)]
      (hikari/close-datasource datasource))
    (assoc this :datasource nil)))
