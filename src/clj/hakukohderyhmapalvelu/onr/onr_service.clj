(ns hakukohderyhmapalvelu.onr.onr-service
  (:require [hakukohderyhmapalvelu.cas.cas-authenticating-client-protocol :as authenticating-client]
            [hakukohderyhmapalvelu.onr.onr-protocol :as onr-protocol]
            [hakukohderyhmapalvelu.oph-url-properties :as url]
            [schema.core :as s]))

(defrecord HttpPersonService [onr-authenticating-client config]

  onr-protocol/PersonService

  (get-person [_ oid]
    (let [url (url/resolve-url :oppijanumerorekisteri.get-person config oid)]
      (authenticating-client/get onr-authenticating-client url s/Any))))

(def fake-onr-person {:oidHenkilo   "1.2.3.4.5.6"
                      :hetu         "020202A0202"
                      :etunimet     "Testi"
                      :kutsumanimi  "Testi"
                      :sukunimi     "Ihminen"
                      :syntymaaika  "1941-06-16"
                      :sukupuoli    "2"
                      :kansalaisuus [{:kansalaisuusKoodi "246"}]
                      :aidinkieli   {:id          "742310"
                                     :kieliKoodi  "fi"
                                     :kieliTyyppi "suomi"}
                      :turvakielto  false
                      :yksiloity    false
                      :yksiloityVTJ false})

(defrecord FakePersonService []
  onr-protocol/PersonService

  (get-person [this oid]
    (condp = oid
      "2.2.2" (merge fake-onr-person
                     {:oidHenkilo "2.2.2"
                      :turvakielto true
                      :yksiloity   true
                      :etunimet    "Ari"
                      :kutsumanimi "Ari"
                      :sukunimi    "Vatanen"
                      :hetu         "141196-933S"})
      (merge fake-onr-person
             {:oidHenkilo oid}))))
