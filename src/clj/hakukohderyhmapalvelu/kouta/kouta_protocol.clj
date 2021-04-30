(ns hakukohderyhmapalvelu.kouta.kouta-protocol)

(defprotocol KoutaServiceProtocol
  (list-haun-tiedot [this is-all tarjoajat])
  (list-haun-hakukohteet [this haku-oid tarjoajat])
  (find-hakukohteet-by-oids [this oidit tarjoajat]))
