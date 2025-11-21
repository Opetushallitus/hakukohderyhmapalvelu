(ns hakukohderyhmapalvelu.cas.cas-ticket-client-protocol
  (:require [schema.core :as s]))

(s/defschema Virkailija
             {:oidHenkilo                 s/Str
              :username                   s/Str
              :organisaatiot              [s/Str]
              :superuser                  s/Bool})

(defprotocol CasTicketClientProtocol
  (validate-service-ticket [this ticket]))
