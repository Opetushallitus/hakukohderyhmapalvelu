(ns hakukohderyhmapalvelu.organisaatio.fixtures
  (:require
    [hakukohderyhmapalvelu.test-fixtures :refer [organisaatio-1 organisaatio-2]]))


(def hakukohderyhma-response
  {:oid          "1.2.246.562.28.4"
   :nimi         {:fi "Hakukohderyhmä 1"}
   :version      0
   :kayttoryhmat []
   :parentOid    "1.2.246.562.28.01"
   :ryhmatyypit  []
   :tyypit       []})

(def organisaatiot-response
  [organisaatio-1
   organisaatio-2])

(def organisaatio-response
  (first organisaatiot-response))

(def organisaatio-delete-response
  {:message "deleted"})
