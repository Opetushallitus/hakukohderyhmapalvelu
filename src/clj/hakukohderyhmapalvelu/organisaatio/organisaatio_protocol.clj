(ns hakukohderyhmapalvelu.organisaatio.organisaatio-protocol)

(defprotocol OrganisaatioServiceProtocol
  (get-organisaatio-children [service])
  (post-new-organisaatio [service hakukohderyhma])
  (find-by-oids [service oid-list]))
