(ns hakukohderyhmapalvelu.cas.cas-authenticating-client-protocol
  (:refer-clojure :exclude [get]))

(defprotocol CasAuthenticatingClientProtocol
  (post [this opts schemas])
  (get [this url response-schema])
  (http-put [this opts schemas]))
