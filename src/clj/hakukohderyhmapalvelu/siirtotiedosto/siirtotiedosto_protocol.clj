(ns hakukohderyhmapalvelu.siirtotiedosto.siirtotiedosto-protocol)

(defprotocol SiirtotiedostoProtocol
  (create-siirtotiedosto [this executionId executionSubId ryhmat])
  (create-siirtotiedostot-by-params [this session params])
  (create-next-siirtotiedostot [this]))