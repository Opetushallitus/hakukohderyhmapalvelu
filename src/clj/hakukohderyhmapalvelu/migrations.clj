(ns hakukohderyhmapalvelu.migrations
  (:require [com.stuartsierra.component :as component]
            [schema.core :as s])
  (:import [org.flywaydb.core Flyway]
           [hakukohderyhmapalvelu.db DbPool]))

(defrecord Migrations [db]
  component/Lifecycle

  (start [this]
    (s/validate DbPool db)
    (let [datasource (:datasource db)
          flyway     (-> (Flyway/configure)
                         (.dataSource datasource)
                         (.load))]
      (.migrate flyway))
    this)

  (stop [this]
    this))
