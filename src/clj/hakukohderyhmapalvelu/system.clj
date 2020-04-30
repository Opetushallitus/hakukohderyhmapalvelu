(ns hakukohderyhmapalvelu.system
  (:require [clojure.core.async :as async]
            [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.audit-log :as audit-log]
            [hakukohderyhmapalvelu.authentication.auth-routes :as auth-routes]
            [hakukohderyhmapalvelu.cas.cas-authenticating-client :as authenticating-client]
            [hakukohderyhmapalvelu.cas.cas-ticket-client :as cas-ticket-validator]
            [hakukohderyhmapalvelu.cas.mock.mock-authenticating-client :as mock-authenticating-client]
            [hakukohderyhmapalvelu.cas.mock.mock-dispatcher :as mock-dispatcher]
            [hakukohderyhmapalvelu.config :as c]
            [hakukohderyhmapalvelu.db :as db]
            [hakukohderyhmapalvelu.health-check :as health-check]
            [hakukohderyhmapalvelu.kayttooikeus.kayttooikeus-service :as kayttooikeus-service]
            [hakukohderyhmapalvelu.migrations :as migrations]
            [hakukohderyhmapalvelu.onr.onr-service :as onr-service]
            [hakukohderyhmapalvelu.organisaatio.organisaatio-service :as organisaatio-service]
            [hakukohderyhmapalvelu.server :as http]))

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
                                                   [:organisaatio-service-authenticating-client])

                           :health-checker (component/using
                                             (health-check/map->DbHealthChecker {})
                                             [:db])

                           :auth-routes-source (component/using
                                                 (auth-routes/map->AuthRoutesMaker {:config config})
                                                 [:db
                                                  :cas-ticket-validator
                                                  :kayttooikeus-service
                                                  :person-service
                                                  :organisaatio-service
                                                  :audit-logger])

                           :http-server (component/using
                                          (http/map->HttpServer {:config config})
                                          (cond-> [:db
                                                   :migrations
                                                   :health-checker
                                                   :organisaatio-service
                                                   :auth-routes-source]
                                                  it-profile?
                                                  (conj :mock-dispatcher)))]
        production-system [:organisaatio-service-authenticating-client (authenticating-client/map->CasAuthenticatingClient {:service :organisaatio-service
                                                                                                                            :config  config})

                           :kayttooikeus-authenticating-client (authenticating-client/map->CasAuthenticatingClient {:service :kayttooikeus
                                                                                                                    :config  config})

                           :kayttooikeus-service (component/using
                                                   (kayttooikeus-service/map->HttpKayttooikeusService {:config config})
                                                   [:kayttooikeus-authenticating-client])

                           :onr-authenticating-client (authenticating-client/map->CasAuthenticatingClient {:service :oppijanumerorekisteri
                                                                                                           :config  config})

                           :cas-ticket-validator (cas-ticket-validator/map->CasTicketClient {:config config})

                           :person-service (component/using
                                             (onr-service/map->HttpPersonService {:config config})
                                             [:onr-authenticating-client])]
        mock-system       [:mock-organisaatio-service-cas-chan (async/chan)

                           :organisaatio-service-authenticating-client (component/using
                                                                         (mock-authenticating-client/map->MockedCasClient {})
                                                                         {:chan :mock-organisaatio-service-cas-chan})

                           :kayttooikeus-service (kayttooikeus-service/->FakeKayttooikeusService)

                           :person-service (onr-service/->FakePersonService)

                           :cas-ticket-validator (cas-ticket-validator/map->FakeCasTicketClient {})

                           :mock-dispatcher (component/using
                                              (mock-dispatcher/map->MockDispatcher {})
                                              {:organisaatio-service-chan :mock-organisaatio-service-cas-chan})]
        system            (into base-system
                                (if it-profile?
                                  mock-system
                                  production-system))]
    (apply component/system-map system)))
