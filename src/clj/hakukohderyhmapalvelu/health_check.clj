(ns hakukohderyhmapalvelu.health-check
  (:require [clojure.java.jdbc :as jdbc]
            [schema.core :as s])
  (:import [hakukohderyhmapalvelu.db DbPool]))

(defprotocol HealthChecker
  (check-health [this]))

(defn run-query [db q]
  (jdbc/with-db-transaction [connection {:datasource (:datasource db)}]
    (jdbc/query connection q)))

(defrecord DbHealthChecker [db]
  HealthChecker
  (check-health [_]
    (s/validate DbPool db)
    (assert (=
              '({:arvo 1})
              (run-query db "select 1 as arvo")))
    "Hakukohderyhmäpalvelu vaikuttaa olevan OK."))
