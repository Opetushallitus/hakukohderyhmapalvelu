(ns hakukohderyhmapalvelu.organisaatio.organisaatio-protocol)

(defprotocol OrganisaatioServiceProtocol
  (get-all-hakukohderyhmas [service])
  (post-new-organisaatio [service hakukohderyhma])
  (find-by-oids [service oid-list]))
