(ns hakukohderyhmapalvelu.ataru.ataru-protocol)

(defprotocol AtaruServiceProtocol
  (get-forms [service hakukohderyhma-oid]))
