(ns hakukohderyhmapalvelu.cas.cas-ticket-client
  (:require [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.cas.cas-ticket-client-protocol :as cas-ticket-client-protocol]
            [hakukohderyhmapalvelu.cas.cas-utils :as cas-utils]
            [hakukohderyhmapalvelu.oph-url-properties :as url]
            [taoensso.timbre :as log])
  (:import (fi.vm.sade.javautils.nio.cas CasClient UserDetails)))

(def oph-organisaatio-oid "1.2.246.562.10.00000000001")
(def crud-regex #"^ROLE_APP_HAKUKOHDERYHMAPALVELU_CRUD_(1.2.246.562.\d+.\d+)$")

(defn- role->org [role] (nth (re-matches crud-regex role) 1))
(defn- hakukohderyhmapalvelu-allowed-orgs [roles]
  (->> roles
       (keep role->org)
       (set)))

(defn has-oph-org? [orgs]
  (contains? orgs oph-organisaatio-oid))

(defn- get-authorized-virkailija [^UserDetails user-details]
  (let [allowed-orgs (hakukohderyhmapalvelu-allowed-orgs (.getRoles user-details))]
    (if (seq allowed-orgs)
      {:oidHenkilo     (.getHenkiloOid user-details)
       :username       (.getUser user-details)
       :organisaatiot  allowed-orgs
       :superuser      (has-oph-org? allowed-orgs)}
      (log/warn "No required permission found for user" user-details))))

(defn- get-user-details ^UserDetails [^CasClient cas-client service-parameter ticket]
  (.validateServiceTicketWithVirkailijaUserDetailsBlocking cas-client service-parameter ticket))

(defrecord CasTicketClient [config]
  component/Lifecycle
  (start [this]
    (let [cas-client (cas-utils/create-cas-client config "" "")
          service-parameter (url/resolve-url :hakukohderyhmapalvelu.login-success config)]
      (-> this
          (assoc :cas-client cas-client)
          (assoc :service-parameter service-parameter))))

  (stop [this]
    (assoc this :service-parameter nil))

  cas-ticket-client-protocol/CasTicketClientProtocol
  (validate-service-ticket [this ticket]
    (log/info "Validating service ticket" ticket)
    (let [user-details ^UserDetails (get-user-details (:cas-client this) (:service-parameter this) ticket)]
      (get-authorized-virkailija user-details))))

(defrecord FakeCasTicketClient []
  cas-ticket-client-protocol/CasTicketClientProtocol
  (validate-service-ticket [_ ticket]
    (log/info "Validating service ticket" ticket)
    {:oidHenkilo     "1.2.246.562.11.11111111111"
     :username       "testuser"
     :organisaatiot  ["1.2.246.562.10.0439845", "1.2.246.562.28.1"]
     :superuser      true}))
