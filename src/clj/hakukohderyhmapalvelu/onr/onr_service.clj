(ns hakukohderyhmapalvelu.onr.onr-service
  (:require [cheshire.core :as json]
            [clojure.core.match :as match]
            [hakukohderyhmapalvelu.cas.cas-authenticating-client-protocol :as authenticating-client]
            [hakukohderyhmapalvelu.onr.onr-protocol :as onr-protocol]
            [hakukohderyhmapalvelu.oph-url-properties :as url]
            [schema.core :as s]))

(defrecord HttpPersonService [onr-authenticating-client config]

  onr-protocol/PersonService
  (get-person [_ oid]
    (let [url      (url/resolve-url :oppijanumerorekisteri.get-person config oid)
          response (authenticating-client/get onr-authenticating-client url s/Any)]
      (match/match response
                   {:status 200 :body body}
                   (json/parse-string body true)

                   :else (throw (RuntimeException. (str "Got non-200 response when fetching person by oid " oid " "
                                                        "from url " url ", "
                                                        "status: " (:status response) ", "
                                                        "response body: "
                                                        (:body response))))))))

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
