(ns hakukohderyhmapalvelu.kouta.kouta-protocol)

(defprotocol KoutaServiceProtocol
  (list-haun-tiedot [this is-all])
  (list-haun-hakukohteet [this haku-oid]))
