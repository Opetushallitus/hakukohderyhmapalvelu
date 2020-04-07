(ns hakukohderyhmapalvelu.system
  (:require [clojure.core.async :as async]
            [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.cas.cas-client :as cas-client]
            [hakukohderyhmapalvelu.config :as c]
            [hakukohderyhmapalvelu.db :as db]
            [hakukohderyhmapalvelu.migrations :as migrations]
            [hakukohderyhmapalvelu.cas.mock.mock-cas-client :as mock-cas-client]
            [hakukohderyhmapalvelu.cas.mock.mock-dispatcher :as mock-dispatcher]
            [hakukohderyhmapalvelu.organisaatio.organisaatio-service :as organisaatio-service]
            [hakukohderyhmapalvelu.server :as http]
            [hakukohderyhmapalvelu.audit-log :as audit-log]))

(defn hakukohderyhmapalvelu-system []
  (let [config            (c/make-config)
        it-profile?       (-> config :public-config :environment (= :it))
        base-system       [:audit-logger (audit-log/map->OpintopolkuAuditLogger {:config config})

                           :db (db/map->DbPool {:config config})

                           :migrations (component/using
                                         (migrations/map->Migrations {})
                                         [:db])

                           :organisaatio-service (component/using
                                                   (organisaatio-service/map->OrganisaatioService {:config config})
                                                   [:organisaatio-service-cas-client])

                           :http-server (component/using
                                          (http/map->HttpServer {:config config})
                                          (cond-> [:db
                                                   :migrations
                                                   :organisaatio-service]
                                                  it-profile?
                                                  (conj :mock-dispatcher)))]
        production-system [:organisaatio-service-cas-client (cas-client/map->CasClient {:service :organisaatio-service
                                                                                        :config  config})]
        mock-system       [:mock-organisaatio-service-cas-chan (async/chan)

                           :organisaatio-service-cas-client (component/using
                                                              (mock-cas-client/map->MockedCasClient {})
                                                              {:chan :mock-organisaatio-service-cas-chan})

                           :mock-dispatcher (component/using
                                              (mock-dispatcher/map->MockDispatcher {})
                                              {:organisaatio-service-chan :mock-organisaatio-service-cas-chan})]
        system            (into base-system
                                (if it-profile?
                                  mock-system
                                  production-system))]
    (apply component/system-map system)))
