(ns hakukohderyhmapalvelu.cas.cas-protocol)

(defprotocol CasClientProtocol
  (post [this opts schemas]))
