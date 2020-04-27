(ns hakukohderyhmapalvelu.cas.cas-protocol
  (:refer-clojure :exclude [get]))

(defprotocol CasClientProtocol
  (post [this opts schemas])
  (get [this url response-schema]))
