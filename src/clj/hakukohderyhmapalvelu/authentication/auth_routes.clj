(ns hakukohderyhmapalvelu.authentication.auth-routes
  (:require [clj-ring-db-session.authentication.login :as crdsa-login]
            [clj-ring-db-session.session.session-client :as session-client]
            [compojure.api.core :as compojure-core]
            [compojure.api.sweet :as api]
            [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.audit-log :as audit-log]
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

(defn- create-login-success-handler [organisaatio-service audit-logger session]
  (fn [response virkailija henkilo username ticket]
    (log/info "user" username "logged in")
    (s/validate (p/extends-class-pred organisaatio-protocol/OrganisaatioServiceProtocol) organisaatio-service)
    (s/validate (p/extends-class-pred audit-log/AuditLogger) audit-logger)
    (s/validate kayttooikeus-protocol/Virkailija virkailija)
    (s/validate s/Str (:oidHenkilo henkilo))
    (s/validate s/Str ticket)
    (s/validate schema/Session session)

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
                                           (let [redirect-url (or (get-in request [:session :original-url])
                                                                  (:hakukohderyhmapalvelu-url this))]
                                             (crdsa-login/login
                                              {:login-provider       #(cas-ticket-client-protocol/validate-service-ticket cas-ticket-validator ticket)
                                               :virkailija-finder    #(kayttooikeus-protocol/virkailija-by-username kayttooikeus-service %)
                                               :henkilo-finder       #(onr-protocol/get-person person-service %)
                                               :success-redirect-url redirect-url
                                               :do-on-success        (create-login-success-handler organisaatio-service audit-logger (:session request))
                                               :login-failed-handler (create-login-failed-handler (url/resolve-url :cas.failure config))
                                               :datasource           (:datasource db)})))
                                         (api/POST "/cas" [logout-request]
                                           (cas-initiated-logout logout-request))
                                         (api/GET "/logout" {session :session}
                                           (crdsa-login/logout session (url/resolve-url :cas.logout config) (:datasource db))))))))
