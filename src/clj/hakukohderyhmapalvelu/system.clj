(ns hakukohderyhmapalvelu.system
  (:require [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.cas-client :as cas-client]
            [hakukohderyhmapalvelu.config :as config]
            [hakukohderyhmapalvelu.db :as db]
            [hakukohderyhmapalvelu.migrations :as migrations]
            [hakukohderyhmapalvelu.organisaatio-service :as organisaatio-service]
            [hakukohderyhmapalvelu.server :as http]
            [hakukohderyhmapalvelu.audit-log :as audit-log]))

(defn hakukohderyhmapalvelu-system []
  (component/system-map
    :organisaatio-service-cas-client (component/using
                                       (cas-client/map->CasClient {:service :organisaatio-service})
                                       [:config])

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

    :organisaatio-service (component/using
                            (organisaatio-service/map->OrganisaatioService {})
                            [:organisaatio-service-cas-client
                             :config])

    :http-server (component/using
                   (http/map->HttpServer {})
                   [:config
                    :db
                    :migrations
                    :organisaatio-service])))
