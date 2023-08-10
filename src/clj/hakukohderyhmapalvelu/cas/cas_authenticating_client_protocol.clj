(ns hakukohderyhmapalvelu.cas.cas-authenticating-client-protocol
  (:refer-clojure :exclude [get]))

(defprotocol CasAuthenticatingClientProtocol
  (post [this opts])
  (http-get [this url])
  (http-put [this opts])
  (delete [this url]))
