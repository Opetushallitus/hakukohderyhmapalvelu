(ns hakukohderyhmapalvelu.onr.onr-protocol)

(defprotocol PersonService
  (get-person [this oid]))
