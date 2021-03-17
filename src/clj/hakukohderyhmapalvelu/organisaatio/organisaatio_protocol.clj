(ns hakukohderyhmapalvelu.organisaatio.organisaatio-protocol)

(defprotocol OrganisaatioServiceProtocol
  (get-organisaatio-children [service])
  (get-organisaatio [service oid])
  (post-new-organisaatio [service hakukohderyhma])
  (find-by-oids [service oid-list]))
