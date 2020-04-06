(ns hakukohderyhmapalvelu.system
  (:require [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.cas-client :as cas-client]
            [hakukohderyhmapalvelu.config :as c]
            [hakukohderyhmapalvelu.db :as db]
            [hakukohderyhmapalvelu.migrations :as migrations]
            [hakukohderyhmapalvelu.organisaatio-service :as organisaatio-service]
            [hakukohderyhmapalvelu.server :as http]
            [hakukohderyhmapalvelu.audit-log :as audit-log]))

(defn hakukohderyhmapalvelu-system []
  (let [config (c/make-config)]
    (component/system-map
      :organisaatio-service-cas-client (cas-client/map->CasClient {:service :organisaatio-service
                                                                   :config  config})

      :audit-logger (audit-log/map->OpintopolkuAuditLogger {:config config})

      :db (db/map->DbPool {:config config})

      :migrations (component/using
                    (migrations/map->Migrations {})
                    [:db])

      :organisaatio-service (component/using
                              (organisaatio-service/map->OrganisaatioService {:config config})
                              [:organisaatio-service-cas-client])

      :http-server (component/using
                     (http/map->HttpServer {:config config})
                     [:db
                      :migrations
                      :organisaatio-service]))))
