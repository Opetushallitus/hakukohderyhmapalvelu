(ns hakukohderyhmapalvelu.db
  (:require [cheshire.core :as json]
            [clj-time.coerce :as clj-time-coerce]
            [clojure.java.jdbc :as jdbc]
            [clojure.set :as cs]
            [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.config :as c]
            [hikari-cp.core :as hikari]
            [schema.core :as s])
  (:import [java.sql PreparedStatement]
           [org.postgresql.util PGobject]))

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
  (result-set-read-column [v _ _] (clj-time-coerce/from-sql-date v))

  java.sql.Timestamp
  (result-set-read-column [v _ _] (clj-time-coerce/from-sql-time v))

  org.postgresql.jdbc.PgArray
  (result-set-read-column [v _ _]
    (vec (.getArray v))))

(extend-type org.joda.time.DateTime
  jdbc/ISQLParameter
  (set-parameter [v ^PreparedStatement stmt idx]
    (.setTimestamp stmt idx (clj-time-coerce/to-sql-time v))))

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
