(ns hakukohderyhmapalvelu.hakukohderyhma.hakukohderyhma-service-protocol)

(defprotocol HakukohderyhmaServiceProtocol
  (create [this session hakukohderyhma])
  (rename [this session hakukohderyhma])
  (find-hakukohderyhmat-by-hakukohteet-oids [this session hakukohde-oids include-empty])
  (list-haun-tiedot [this session is-all])
  (list-haun-hakukohteet [this session haku-oid])
  (update-hakukohderyhma-hakukohteet [this session oid hakukohdeet])
  (get-hakukohderyhma [this session hakukohderyhma-oid]))
