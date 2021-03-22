(ns hakukohderyhmapalvelu.hakukohderyhma.hakukohderyhma-service-protocol)

(defprotocol HakukohderyhmaServiceProtocol
  (create [this session hakukohderyhma])
  (rename [this session hakukohderyhma])
  (get-by-haku-oids [this session hakukohde-oids])
  (list-haun-tiedot [this session is-all])
  (list-haun-hakukohteet [this session haku-oid]))
