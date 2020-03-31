(ns hakukohderyhmapalvelu.system
  (:require [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.config :as config]
            [hakukohderyhmapalvelu.db :as db]
            [hakukohderyhmapalvelu.migrations :as migrations]
            [hakukohderyhmapalvelu.server :as http]
            [hakukohderyhmapalvelu.audit-log :as audit-log]))

(def hakukohderyhmapalvelu-system
  (component/system-map
    :config (config/map->Config {})

    :audit-logger (component/using
                    (audit-log/map->OpintopolkuAuditLogger {})
                    [:config])

    :db (component/using
          (db/map->DbPool {})
          [:config])

    :migrations (component/using
                  (migrations/map->Migrations {})
                  [:db])

    :http-server (component/using
                   (http/map->HttpServer {})
                   [:config :db :migrations])))
