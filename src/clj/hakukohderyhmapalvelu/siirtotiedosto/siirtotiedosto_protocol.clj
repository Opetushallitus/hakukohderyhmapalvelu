(ns hakukohderyhmapalvelu.siirtotiedosto.siirtotiedosto-protocol)

(defprotocol SiirtotiedostoProtocol
  (create-siirtotiedosto [this executionId executionSubId ryhmat]))