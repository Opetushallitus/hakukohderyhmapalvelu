(ns hakukohderyhmapalvelu.kayttooikeus.kayttooikeus-service
  (:require [hakukohderyhmapalvelu.kayttooikeus.kayttooikeus-protocol :as kayttooikeus-protocol]
            [hakukohderyhmapalvelu.oph-url-properties :as url]
            [hakukohderyhmapalvelu.cas.cas-protocol :as cas-client]))

(defrecord HttpKayttooikeusService [kayttooikeus-cas-client config]

  kayttooikeus-protocol/KayttooikeusService
  (virkailija-by-username [_ username]
    (let [url                 (url/resolve-url :kayttooikeus-service.kayttooikeus.kayttaja config {"username" username})
          virkailija-response (cas-client/get kayttooikeus-cas-client url [kayttooikeus-protocol/Virkailija])]
      (if-let [virkailija (first virkailija-response)]
        virkailija
        (throw (new RuntimeException
                    (str "No virkailija found by username '" username "', response was '" virkailija-response "'")))))))

(def fake-virkailija-value
  {"1.2.246.562.11.11111111111"
   {:oidHenkilo     "1.2.246.562.11.11111111012"
    :username       "1.2.246.562.11.11111111111"
    :kayttajaTyyppi "VIRKAILIJA"
    :organisaatiot  [{:organisaatioOid "1.2.246.562.10.0439845"
                      :kayttooikeudet  [{:palvelu "ATARU_EDITORI"
                                         :oikeus  "CRUD"}
                                        {:palvelu "ATARU_HAKEMUS"
                                         :oikeus  "CRUD"}]}
                     {:organisaatioOid "1.2.246.562.28.1"
                      :kayttooikeudet  [{:palvelu "ATARU_EDITORI"
                                         :oikeus  "CRUD"}
                                        {:palvelu "ATARU_HAKEMUS"
                                         :oikeus  "CRUD"}]}]}
   "1.2.246.562.11.22222222222"
   {:oidHenkilo     "1.2.246.562.11.11111111000"
    :username       "1.2.246.562.11.22222222222"
    :kayttajaTyyppi "VIRKAILIJA"
    :organisaatiot  [{:organisaatioOid "1.2.246.562.10.0439846"
                      :kayttooikeudet  [{:palvelu "ATARU_EDITORI"
                                         :oikeus  "CRUD"}
                                        {:palvelu "ATARU_HAKEMUS"
                                         :oikeus  "CRUD"}]}
                     {:organisaatioOid "1.2.246.562.28.2"
                      :kayttooikeudet  [{:palvelu "ATARU_EDITORI"
                                         :oikeus  "CRUD"}
                                        {:palvelu "ATARU_HAKEMUS"
                                         :oikeus  "CRUD"}]}
                     {:organisaatioOid "1.2.246.562.10.10826252480"
                      :kayttooikeudet  [{:palvelu "ATARU_EDITORI"
                                         :oikeus  "CRUD"}]}]}})

(defrecord FakeKayttooikeusService []
  kayttooikeus-protocol/KayttooikeusService
  (virkailija-by-username [_ username]
    (get fake-virkailija-value username (get fake-virkailija-value "1.2.246.562.11.11111111111"))))
