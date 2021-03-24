(ns hakukohderyhmapalvelu.organisaatio.fixtures)


(def hakukohderyhma-response
  {:oid "1.2.246.562.28.4"
   :nimi {:fi "Hakukohderyhmä 1"}
   :kayttoryhmat []
   :parentOid    "1.2.246.562.28.01"
   :ryhmatyypit  []
   :tyypit       []})

(def organisaatiot-response
  [{:oid          "1.2.246.562.28.1"
    :kayttoryhmat []
    :parentOid    "1.2.246.562.28.01"
    :ryhmatyypit  []
    :tyypit       []
    :nimi         {:fi "Organisaatio 1"}}
   {:oid          "1.2.246.562.28.2"
    :kayttoryhmat []
    :parentOid    "1.2.246.562.28.02"
    :ryhmatyypit  []
    :tyypit       []
    :nimi         {:fi "Organisaatio 2"}}])

(def organisaatio-response
  (first organisaatiot-response))
