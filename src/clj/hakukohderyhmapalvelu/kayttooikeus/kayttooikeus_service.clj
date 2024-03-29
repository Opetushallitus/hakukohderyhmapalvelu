(ns hakukohderyhmapalvelu.kayttooikeus.kayttooikeus-service
  (:require [hakukohderyhmapalvelu.cas.cas-authenticating-client-protocol :as authenticating-client]
            [hakukohderyhmapalvelu.http :as http]
            [hakukohderyhmapalvelu.kayttooikeus.kayttooikeus-protocol :as kayttooikeus-protocol]
            [hakukohderyhmapalvelu.oph-url-properties :as url]
            [schema.core :as s]))

(def hakukohderyhmapalvelu-crud-permission
  {:palvelu "HAKUKOHDERYHMAPALVELU"
   :oikeus  "CRUD"})

(def oph-organisaatio-oid "1.2.246.562.10.00000000001")

(defn- hakukohderyhmapalvelu-allowed-orgs [orgs]
  (vec (filter #(contains? (set (:kayttooikeudet %)) hakukohderyhmapalvelu-crud-permission) orgs)))

(s/defn has-permission [virkailija :- kayttooikeus-protocol/Virkailija
                        permission :- kayttooikeus-protocol/Kayttooikeus]
  (let [permissions (set (mapcat :kayttooikeudet (:organisaatiot virkailija)))]
    (contains? permissions permission)))

(defn has-oph-org? [virkailija]
  (some? (not-empty (filter #(= oph-organisaatio-oid (:organisaatioOid %)) (:organisaatiot virkailija)))))

(defn- virkailija-with-hakukohderyhma-permission [response]
  (let [virkailija (first (http/parse-and-validate response [kayttooikeus-protocol/Virkailija]))
        virkailija-with-hakukohderyhma-access (update virkailija :organisaatiot hakukohderyhmapalvelu-allowed-orgs)
        superuser? (has-oph-org? virkailija-with-hakukohderyhma-access)]
    (if (not-empty (:organisaatiot virkailija-with-hakukohderyhma-access))
      (assoc virkailija-with-hakukohderyhma-access :superuser superuser?)
      (throw (new RuntimeException
                  (str "No required permission found for username " (:username virkailija)))))))

(defrecord HttpKayttooikeusService [kayttooikeus-authenticating-client config]

  kayttooikeus-protocol/KayttooikeusService
  (virkailija-by-username [_ username]
    (let [url      (url/resolve-url :kayttooikeus-service.kayttooikeus.kayttaja config {:username username})
          response (authenticating-client/http-get kayttooikeus-authenticating-client url)]
      (if-let [virkailija (virkailija-with-hakukohderyhma-permission response)]
        virkailija
        (throw (new RuntimeException
                    (str "No virkailija found by username " username)))))))

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
