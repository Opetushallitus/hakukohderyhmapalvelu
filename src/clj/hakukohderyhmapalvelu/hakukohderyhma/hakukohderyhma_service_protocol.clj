(ns hakukohderyhmapalvelu.hakukohderyhma.hakukohderyhma-service-protocol)

(defprotocol HakukohderyhmaServiceProtocol
  (create [this session hakukohderyhma])
  (list-haun-tiedot [this session is-all])
  (list-haun-hakukohteet [this session haku-oid]))
