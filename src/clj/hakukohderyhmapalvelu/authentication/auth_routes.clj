(ns hakukohderyhmapalvelu.authentication.auth-routes
  (:require [clj-ring-db-session.authentication.login :as crdsa-login]
            [clj-ring-db-session.session.session-client :as session-client]
            [clojure.string :as string]
            [compojure.api.core :as compojure-core]
            [compojure.api.sweet :as api]
            [com.stuartsierra.component :as component]
            [environ.core :refer [env]]
            [hakukohderyhmapalvelu.audit-log :as audit-log]
            [hakukohderyhmapalvelu.cas.cas-ticket-validator :as cas-ticket-validator]
            [hakukohderyhmapalvelu.config :as c]
            [hakukohderyhmapalvelu.kayttooikeus.kayttooikeus-protocol :as kayttooikeus-protocol]
            [hakukohderyhmapalvelu.onr.onr-protocol :as onr-protocol]
            [hakukohderyhmapalvelu.oph-url-properties :as url]
            [hakukohderyhmapalvelu.organisaatio.organisaatio-protocol :as organisaatio-protocol]
            [hakukohderyhmapalvelu.schemas.class-pred :as p]
            [ring.util.response :as resp]
            [schema.core :as s]
            [taoensso.timbre :as log])
  (:import [hakukohderyhmapalvelu.audit_log AuditLogger]
           [hakukohderyhmapalvelu.kayttooikeus.kayttooikeus_protocol KayttooikeusService]
           [hakukohderyhmapalvelu.onr.onr_protocol PersonService]
           [hakukohderyhmapalvelu.organisaatio.organisaatio_protocol OrganisaatioServiceProtocol]
           [javax.sql DataSource]))

(defprotocol AuthRoutesSource
  (create-auth-routes [this]))

(defn- rewrite-url-for-environment
  "Ensure that https is used when available (due to https termination on
   non-development environments)"
  [url-from-session]
  (if (:dev? env)
    url-from-session
    (string/replace url-from-session #"^http://" "https://")))

(defn- cas-login [cas-ticket-validator ticket]
  (fn []
    (when ticket
      [(cas-ticket-validator/validate-service-ticket cas-ticket-validator ticket)
       ticket])))

(defn- fake-login-provider [ticket]
  (fn []
    (let [username      (if (= ticket "USER-WITH-HAKUKOHDE-ORGANIZATION")
                          "1.2.246.562.11.22222222222"
                          "1.2.246.562.11.11111111111")
          unique-ticket (str (System/currentTimeMillis) "-" (rand-int (Integer/MAX_VALUE)))]
      [username unique-ticket])))

(defn- create-login-success-handler [organisaatio-service audit-logger session]
  (fn [response virkailija henkilo username ticket]
    (log/info "user" username "logged in")
    (s/validate (p/extends-class-pred organisaatio-protocol/OrganisaatioServiceProtocol) organisaatio-service)
    (s/validate (p/extends-class-pred audit-log/AuditLogger) audit-logger)
    (s/validate kayttooikeus-protocol/Virkailija virkailija)
    (s/validate s/Str (:oidHenkilo henkilo))
    (s/validate s/Str ticket)
    (s/validate s/Any session)

    ; TODO : add audit-logging

    (update-in
      response
      [:session :identity]
      assoc
      :user-right-organizations []                          ; TODO : Add organisations
      :superuser false                                      ; TODO : Add organisations
      :organizations {}                                     ; TODO : Add organisations
      )))

(defn- create-login-failed-handler [login-failed-url]
  (fn
    ([e]
     (.printStackTrace e)
     (log/error e "Error in login ticket handling")
     (resp/redirect login-failed-url))
    ([]
     (resp/redirect login-failed-url))))

(s/defn login [login-provider
               kayttooikeus-service :- KayttooikeusService
               person-service :- PersonService
               organization-service :- OrganisaatioServiceProtocol
               db
               audit-logger :- AuditLogger
               config :- c/HakukohderyhmaConfig
               redirect-url :- s/Str
               session]
  (crdsa-login/login {:login-provider       login-provider
                      :virkailija-finder    #(kayttooikeus-protocol/virkailija-by-username kayttooikeus-service %)
                      :henkilo-finder       #(onr-protocol/get-person person-service %)
                      :success-redirect-url redirect-url
                      :do-on-success        (create-login-success-handler organization-service audit-logger session)
                      :login-failed-handler (create-login-failed-handler (url/resolve-url :cas.failure config))
                      :datasource           (:datasource db)}))

(defn- cas-initiated-logout [logout-request]
  (log/warn (str "Saatiin logout-pyyntö'" logout-request "', mutta cas-initiated logoutia ei ole vielä toteutettu!"))
  (throw (new RuntimeException "CASin tekemää logouttia ei ole vielä toteutettu.")))

(defrecord AuthRoutesMaker [config
                            db
                            cas-ticket-validator
                            kayttooikeus-service
                            person-service
                            organisaatio-service
                            audit-logger]
  component/Lifecycle
  (start [this]
    (s/validate (s/pred #(instance? DataSource %)) (:datasource db))
    (s/validate c/HakukohderyhmaConfig config)
    (s/validate (p/extends-class-pred cas-ticket-validator/CasTicketClientProtocol) cas-ticket-validator)
    (s/validate (p/extends-class-pred kayttooikeus-protocol/KayttooikeusService) kayttooikeus-service)
    (s/validate (p/extends-class-pred onr-protocol/PersonService) person-service)
    (s/validate (p/extends-class-pred organisaatio-protocol/OrganisaatioServiceProtocol) organisaatio-service)
    (s/validate (p/extends-class-pred audit-log/AuditLogger) audit-logger)
    (assoc this :hakukohderyhmapalvelu-url (get-in config [:urls :hakukohderyhmapalvelu-url])))

  (stop [this]
    (assoc this :hakukohderyhmapalvelu-url nil))

  AuthRoutesSource
  (create-auth-routes [this]
    (api/context "/auth" []
      (compojure-core/route-middleware [session-client/wrap-session-client-headers]
                                       (api/undocumented
                                         (api/GET "/cas" [ticket :as request]
                                           (let [redirect-url   (if-let [url-from-session (get-in request [:session :original-url])]
                                                                  (rewrite-url-for-environment url-from-session)
                                                                  (:hakukohderyhmapalvelu-url this))
                                                 environment    (-> config :public-config :environment)
                                                 login-provider (if (= :it environment)
                                                                  (fake-login-provider ticket)
                                                                  (cas-login cas-ticket-validator ticket))]
                                             (login login-provider
                                                    kayttooikeus-service
                                                    person-service
                                                    organisaatio-service
                                                    db
                                                    audit-logger
                                                    config
                                                    redirect-url
                                                    (:session request))))
                                         (api/POST "/cas" [logout-request]
                                           (cas-initiated-logout logout-request))
                                         (api/GET "/logout" {session :session}
                                           (crdsa-login/logout session (url/resolve-url :cas.logout config) (:datasource db))))))))