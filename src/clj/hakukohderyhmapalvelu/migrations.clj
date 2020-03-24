(ns hakukohderyhmapalvelu.migrations
  (:require [com.stuartsierra.component :as component])
  (:import [org.flywaydb.core Flyway]))

(defrecord Migrations [db]
  component/Lifecycle

  (start [this]
    (let [datasource (:datasource db)
          flyway (-> (Flyway/configure)
                     (.dataSource datasource)
                     (.load))]
      (.migrate flyway))
    this)

  (stop [this]
    this))
