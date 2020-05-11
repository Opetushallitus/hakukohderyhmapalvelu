(ns hakukohderyhmapalvelu.authentication.auth-routes
  (:require [clj-ring-db-session.authentication.login :as crdsa-login]
            [clj-ring-db-session.session.session-client :as session-client]
            [compojure.api.core :as compojure-core]
            [compojure.api.sweet :as api]
            [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.audit-logger-protocol :as audit-logger-protocol]
            [hakukohderyhmapalvelu.authentication.schema :as schema]
            [hakukohderyhmapalvelu.cas.cas-ticket-client-protocol :as cas-ticket-client-protocol]
            [hakukohderyhmapalvelu.config :as c]
            [hakukohderyhmapalvelu.kayttooikeus.kayttooikeus-protocol :as kayttooikeus-protocol]
            [hakukohderyhmapalvelu.onr.onr-protocol :as onr-protocol]
            [hakukohderyhmapalvelu.oph-url-properties :as url]
            [hakukohderyhmapalvelu.organisaatio.organisaatio-protocol :as organisaatio-protocol]
            [hakukohderyhmapalvelu.schemas.class-pred :as p]
            [ring.util.response :as resp]
            [schema.core :as s]
            [taoensso.timbre :as log])
  (:import javax.sql.DataSource))

(defprotocol AuthRoutesSource
  (create-auth-routes [this]))

(defn- login-succeeded [organisaatio-service audit-logger response virkailija henkilo username ticket]
  (log/info "user" username "logged in")
  (s/validate (p/extends-class-pred organisaatio-protocol/OrganisaatioServiceProtocol) organisaatio-service)
  (s/validate (p/extends-class-pred audit-logger-protocol/AuditLoggerProtocol) audit-logger)
  (s/validate kayttooikeus-protocol/Virkailija virkailija)
  (s/validate s/Str (:oidHenkilo henkilo))
  (s/validate s/Str ticket)

  (s/validate schema/Session (:session response))
  ; TODO : add audit-logging

  response)

(defn- login-failed
  ([login-failed-url e]
   (log/error e "Error in login ticket handling")
   (resp/redirect login-failed-url))
  ([login-failed-url]
   (resp/redirect login-failed-url)))

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
    (s/validate (p/extends-class-pred cas-ticket-client-protocol/CasTicketClientProtocol) cas-ticket-validator)
    (s/validate (p/extends-class-pred kayttooikeus-protocol/KayttooikeusService) kayttooikeus-service)
    (s/validate (p/extends-class-pred onr-protocol/PersonService) person-service)
    (s/validate (p/extends-class-pred organisaatio-protocol/OrganisaatioServiceProtocol) organisaatio-service)
    (s/validate (p/extends-class-pred audit-logger-protocol/AuditLoggerProtocol) audit-logger)
    (assoc this :hakukohderyhmapalvelu-url (get-in config [:urls :hakukohderyhmapalvelu-url]))
    (assoc this :login-failure-url (url/resolve-url :cas.failure config))
    (s/validate s/Str (get-in config [:urls :hakukohderyhmapalvelu-url]))
    (s/validate s/Str (url/resolve-url :cas.failure config))
    (assoc this
      :hakukohderyhmapalvelu-url (get-in config [:urls :hakukohderyhmapalvelu-url])
      :login-failure-url (url/resolve-url :cas.failure config)))

  (stop [this]
    (assoc this
      :hakukohderyhmapalvelu-url nil
      :login-failure-url nil))

  AuthRoutesSource
  (create-auth-routes [this]
    (api/context "/auth" []
      (compojure-core/route-middleware [session-client/wrap-session-client-headers]
                                       (api/undocumented
                                         (api/GET "/cas" [ticket :as request]
                                           (try
                                             (if-let [[username _] (cas-ticket-client-protocol/validate-service-ticket cas-ticket-validator ticket)]
                                               (let [redirect-url (or (get-in request [:session :original-url])
                                                                      (:hakukohderyhmapalvelu-url this))
                                                     virkailija   (kayttooikeus-protocol/virkailija-by-username kayttooikeus-service username)
                                                     henkilo      (onr-protocol/get-person person-service (:oidHenkilo virkailija))
                                                     response     (crdsa-login/login
                                                                    {:username             username
                                                                     :henkilo              henkilo
                                                                     :ticket               ticket
                                                                     :success-redirect-url redirect-url
                                                                     :datasource           (:datasource db)})]
                                                 (login-succeeded organisaatio-service audit-logger response virkailija henkilo username ticket))
                                               (login-failed (:login-failure-url this)))
                                             (catch Exception e
                                               (login-failed (:login-failure-url this) e))))
                                         (api/POST "/cas" [logout-request]
                                           (cas-initiated-logout logout-request))
                                         (api/GET "/logout" {session :session}
                                           (crdsa-login/logout session (url/resolve-url :cas.logout config) (:datasource db))))))))
