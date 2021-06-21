(ns hakukohderyhmapalvelu.system
  (:require [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.audit-logger :as audit-logger]
            [hakukohderyhmapalvelu.authentication.auth-routes :as auth-routes]
            [hakukohderyhmapalvelu.cas.cas-authenticating-client :as authenticating-client]
            [hakukohderyhmapalvelu.cas.cas-ticket-client :as cas-ticket-validator]
            [hakukohderyhmapalvelu.cas.mock.mock-authenticating-client :as mock-authenticating-client]
            [hakukohderyhmapalvelu.cas.mock.mock-dispatcher :as mock-dispatcher]
            [hakukohderyhmapalvelu.config :as c]
            [hakukohderyhmapalvelu.db :as db]
            [hakukohderyhmapalvelu.hakukohderyhma.hakukohderyhma-service :as hakukohderyhma-service]
            [hakukohderyhmapalvelu.health-check :as health-check]
            [hakukohderyhmapalvelu.kayttooikeus.kayttooikeus-service :as kayttooikeus-service]
            [hakukohderyhmapalvelu.migrations :as migrations]
            [hakukohderyhmapalvelu.onr.onr-service :as onr-service]
            [hakukohderyhmapalvelu.organisaatio.organisaatio-service :as organisaatio-service]
            [hakukohderyhmapalvelu.kouta.kouta-service :as kouta-service]
            [hakukohderyhmapalvelu.ataru.ataru-service :as ataru-service]
            [hakukohderyhmapalvelu.server :as http]))

(defn hakukohderyhmapalvelu-system [config]
  (let [it-profile?       (c/integration-environment? config)
        base-system       [:audit-logger (audit-logger/map->AuditLogger {:config config})

                           :db (db/map->DbPool {:config config})

                           :migrations (component/using
                                         (migrations/map->Migrations {})
                                         [:db])

                           :ataru-service (component/using
                                            (ataru-service/map->AtaruService {:config config})
                                            [:ataru-authenticating-client])

                           :organisaatio-service (component/using
                                                   (organisaatio-service/map->OrganisaatioService {:config config})
                                                   [:organisaatio-service-authenticating-client])

                           :kouta-service (component/using
                                            (kouta-service/map->KoutaService {:config config})
                                            [:kouta-authenticating-client :organisaatio-service])

                           :hakukohderyhma-service (component/using
                                                    (hakukohderyhma-service/map->HakukohderyhmaService {})
                                                    [:audit-logger
                                                     :organisaatio-service
                                                     :kouta-service
                                                     :ataru-service
                                                     :db])

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
                                                   :hakukohderyhma-service
                                                   :auth-routes-source]
                                                  it-profile?
                                                  (conj :mock-dispatcher)))]
        production-system [:organisaatio-service-authenticating-client (authenticating-client/map->CasAuthenticatingClient {:service :organisaatio-service
                                                                                                                            :config  config})

                           :ataru-authenticating-client (authenticating-client/map->CasAuthenticatingClient {:service :ataru
                                                                                                             :config  config})

                           :kouta-authenticating-client (authenticating-client/map->CasAuthenticatingClient {:service :kouta-internal
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
        mock-system       [:mock-organisaatio-service-cas-request-map (atom {})

                           :organisaatio-service-authenticating-client (component/using
                                                                         (mock-authenticating-client/map->MockedCasClient {})
                                                                         {:request-map :mock-organisaatio-service-cas-request-map})

                           :mock-ataru-cas-request-map (atom {})

                           :ataru-authenticating-client (component/using
                                                          (mock-authenticating-client/map->MockedCasClient {})
                                                          {:request-map :mock-ataru-cas-request-map})

                           :mock-kouta-cas-request-map (atom {})

                           :kouta-authenticating-client (component/using
                                                          (mock-authenticating-client/map->MockedCasClient {})
                                                          {:request-map :mock-kouta-cas-request-map})

                           :kayttooikeus-service (kayttooikeus-service/->FakeKayttooikeusService)

                           :person-service (onr-service/->FakePersonService)

                           :cas-ticket-validator (cas-ticket-validator/map->FakeCasTicketClient {})

                           :mock-dispatcher (component/using
                                              (mock-dispatcher/map->MockDispatcher {:config config})
                                              {:organisaatio-service-request-map :mock-organisaatio-service-cas-request-map
                                               :kouta-service-request-map        :mock-kouta-cas-request-map
                                               :ataru-service-request-map        :mock-ataru-cas-request-map})]
        system            (into base-system
                                (if it-profile?
                                  mock-system
                                  production-system))]
    (apply component/system-map system)))
