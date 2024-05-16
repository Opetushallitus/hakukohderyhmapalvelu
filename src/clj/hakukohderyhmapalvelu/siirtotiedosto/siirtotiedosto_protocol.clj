(ns hakukohderyhmapalvelu.siirtotiedosto.siirtotiedosto-protocol)

(defprotocol SiirtotiedostoProtocol
  (create-siirtotiedosto [this ryhmat]))