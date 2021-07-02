(ns hakukohderyhmapalvelu.hakukohderyhma.hakukohderyhma-service-protocol)

(defprotocol HakukohderyhmaServiceProtocol
  (create [this session hakukohderyhma])
  (rename [this session hakukohderyhma])
  (delete [this session hakukohderyhma-oid])
  (find-hakukohderyhmat-by-hakukohteet-oids [this session hakukohde-oids include-empty])
  (list-hakukohderyhma-oids-by-hakukohde-oid [this session hakukohde-oid])
  (list-haun-tiedot [this session is-all])
  (list-haun-hakukohteet [this session haku-oid])
  (update-hakukohderyhma-hakukohteet [this session oid hakukohdeet])
  (get-hakukohteet-for-hakukohderyhma-oid [this session hakukohderyhma-oid])
  (get-hakukohderyhma [this session hakukohderyhma-oid])
  (insert-or-update-settings [this session hakukohderyhma-oid settings]))
