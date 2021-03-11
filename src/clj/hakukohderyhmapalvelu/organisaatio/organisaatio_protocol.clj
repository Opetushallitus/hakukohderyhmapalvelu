(ns hakukohderyhmapalvelu.organisaatio.organisaatio-protocol)

(defprotocol OrganisaatioServiceProtocol
  (post-new-organisaatio [service hakukohderyhma])
  (find-by-oids [service oid-list]))
