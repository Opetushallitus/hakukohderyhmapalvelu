(ns hakukohderyhmapalvelu.cas.cas-ticket-client
  (:require [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.cas.cas-ticket-client-protocol :as cas-ticket-client-protocol]
            [hakukohderyhmapalvelu.oph-url-properties :as url]
            [taoensso.timbre :as log]
            [hakukohderyhmapalvelu.cas.cas-utils :as cas-utils])
  (:import [fi.vm.sade.javautils.nio.cas CasClient]
           java.util.UUID))

(defrecord CasTicketClient [config]
  component/Lifecycle
  (start [this]
    (let [cas-client (cas-utils/create-cas-client config "" "")
          service-parameter (url/resolve-url :hakukohderyhmapalvelu.login-success config)
          ]
      (-> this
          (assoc :cas-client cas-client)
          (assoc :service-parameter service-parameter))))

  (stop [this]
    (assoc this :service-parameter nil))

  cas-ticket-client-protocol/CasTicketClientProtocol
  (validate-service-ticket [this ticket]
    (log/info "Validating service ticket" ticket)
    (let [username (.validateServiceTicketWithVirkailijaUsernameBlocking ^CasClient (:cas-client this) (:service-parameter this) ticket)]
      [username ticket])))

(defrecord FakeCasTicketClient []
  cas-ticket-client-protocol/CasTicketClientProtocol
  (validate-service-ticket [_ ticket]
    (log/info "Validating service ticket" ticket)
    [(if (= ticket "USER-WITH-HAKUKOHDE-ORGANIZATION")
       "1.2.246.562.11.22222222222"
       "1.2.246.562.11.11111111111")
     (str (UUID/randomUUID))]))
