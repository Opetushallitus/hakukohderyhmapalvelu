(ns hakukohderyhmapalvelu.authentication.auth-routes
  (:require [clj-ring-db-session.authentication.login :as crdsa-login]
            [clj-ring-db-session.session.session-store :as oph-session]
            [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.audit-logger-protocol :as audit]
            [hakukohderyhmapalvelu.authentication.schema :as schema]
            [hakukohderyhmapalvelu.cas.cas-ticket-client-protocol :as cas-ticket-client-protocol]
            [hakukohderyhmapalvelu.config :as c]
            [hakukohderyhmapalvelu.kayttooikeus.kayttooikeus-protocol :as kayttooikeus-protocol]
            [hakukohderyhmapalvelu.onr.onr-protocol :as onr-protocol]
            [hakukohderyhmapalvelu.oph-url-properties :as url]
            [hakukohderyhmapalvelu.organisaatio.organisaatio-protocol :as organisaatio-protocol]
            [hakukohderyhmapalvelu.schemas.class-pred :as p]
            [ring.util.http-response :refer [ok]]
            [ring.util.response :as resp]
            [schema.core :as s]
            [taoensso.timbre :as log])
  (:import javax.sql.DataSource
           [fi.vm.sade.javautils.nio.cas CasLogout]))

(defprotocol AuthRoutesSource
  (login [this ticket request])
  (cas-logout [this request])
  (logout [this session]))

(def kirjautuminen (audit/->operation "kirjautuminen"))

(defn- merged-session [request response virkailija]
  (let [organisaatiot (map :organisaatioOid (:organisaatiot virkailija))
        superuser? (boolean (:superuser virkailija))
        request-session (:session request)
        response-session (:session response)]
    (-> response-session
        (merge (select-keys request-session [:key :user-agent]))
        (assoc-in [:identity :organizations] organisaatiot)
        (assoc :superuser superuser?))))

(defn- login-succeeded [organisaatio-service audit-logger request response virkailija henkilo username ticket]
  (log/info "user" username "logged in. Superuser?" (:superuser virkailija))
  (let [session (merged-session request response virkailija)
        henkilo-oid (:oidHenkilo henkilo)]
    (s/validate (p/extends-class-pred organisaatio-protocol/OrganisaatioServiceProtocol) organisaatio-service)
    (s/validate (p/extends-class-pred audit/AuditLoggerProtocol) audit-logger)
    (s/validate kayttooikeus-protocol/Virkailija virkailija)
    (s/validate s/Str henkilo-oid)
    (s/validate s/Str ticket)
    (s/validate schema/Session session)
    (audit/log audit-logger
               (audit/->user session)
               kirjautuminen
               (audit/->target {:henkiloOid henkilo-oid})
               (audit/->changes {} {:ticket ticket}))
    (assoc response :session session)))

(defn- login-failed
  ([login-failed-url e]
   (log/error e "Error in login ticket handling")
   (resp/redirect login-failed-url))
  ([login-failed-url]
   (resp/redirect login-failed-url)))

(defn- cas-initiated-logout [session-store logout-request]
  (log/info "cas-initiated logout")
  (let [ticket (-> (CasLogout.)
                   (.parseTicketFromLogoutRequest logout-request))]
    (log/info "logging out ticket" ticket)
    (if (.isEmpty ticket)
      (log/error "Could not parse ticket from CAS request" logout-request)
      (crdsa-login/cas-initiated-logout (.get ticket) session-store))
    (ok)))

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
    (s/validate (p/extends-class-pred audit/AuditLoggerProtocol) audit-logger)
    (s/validate s/Str (get-in config [:urls :hakukohderyhmapalvelu-url]))
    (s/validate s/Str (url/resolve-url :cas.failure config))
    (assoc this
      :hakukohderyhmapalvelu-url (get-in config [:urls :hakukohderyhmapalvelu-url])
      :login-failure-url (url/resolve-url :cas.failure config)
      :session-store (oph-session/create-session-store (:datasource db))))

  (stop [this]
    (assoc this
      :hakukohderyhmapalvelu-url nil
      :login-failure-url nil
      :session-store nil))

  AuthRoutesSource

  (login [this ticket request]
    (try
      (if-let [[username _] (cas-ticket-client-protocol/validate-service-ticket cas-ticket-validator ticket)]
        (let [redirect-url (or (get-in request [:session :original-url])
                               (:hakukohderyhmapalvelu-url this))
              virkailija (kayttooikeus-protocol/virkailija-by-username kayttooikeus-service username)
              henkilo (onr-protocol/get-person person-service (:oidHenkilo virkailija))
              response (crdsa-login/login
                         {:username             username
                          :henkilo              henkilo
                          :ticket               ticket
                          :success-redirect-url redirect-url
                          :datasource           (:datasource db)})]
          (login-succeeded organisaatio-service audit-logger request response virkailija henkilo username ticket))
        (login-failed (:login-failure-url this)))
      (catch Exception e
        (login-failed (:login-failure-url this) e))))

  (cas-logout [this request]
    (-> this
        :session-store
        (cas-initiated-logout request)))

  (logout [_ session]
    (crdsa-login/logout (url/resolve-url :cas.logout config))))
