(ns hakukohderyhmapalvelu.kouta.kouta-service
  (:require [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.cas.cas-authenticating-client-protocol :as authenticating-client-protocol]
            [hakukohderyhmapalvelu.http :as http]
            [hakukohderyhmapalvelu.oph-url-properties :as oph-url]
            [hakukohderyhmapalvelu.organisaatio.organisaatio-protocol :as organisaatio]
            [hakukohderyhmapalvelu.kouta.kouta-protocol :as kouta-service-protocol]
            [hakukohderyhmapalvelu.schemas.kouta-service-schemas :as schemas]
            [hakukohderyhmapalvelu.api-schemas :as api-schemas]
            [schema.core :as s]
            [schema-tools.core :as st]
            [hakukohderyhmapalvelu.config :as c]
            [clojure.string :as str])
  (:import (java.time LocalDateTime)))


(defn- local-date-time? [dt]
  (instance? LocalDateTime dt))

(defn- hakuaika-not-over? [now {paattyy :paattyy}]
  ;; Haku ei ole päättynyt, jos sillä on hakuja joilla ei ole
  ;; päättymispäivää tai alkaneet haut eivät ole vielä päättyneet.
  (or
    (nil? paattyy)
    (when (local-date-time? paattyy)
      (.isAfter paattyy now))))

(defn- not-over? [now {:keys [hakuajat]}]
  (->> hakuajat
       (map (partial hakuaika-not-over? now))
       (some true?)))

(defn- enrich-with-tarjoaja [hakukohde organisaatiot]
  (->> hakukohde
    :tarjoaja
    (get organisaatiot)
    first
    (assoc hakukohde :tarjoaja)))

(defn- set-has-valintakoe [hakukohde]
  (assoc hakukohde
    :hasValintakoe
    (-> (:valintakokeet hakukohde) seq boolean)))

(defn- create-internal-hakukohteet [hakukohteet organisaatiot]
  (as-> hakukohteet hakukohteet'
        (map
          (comp
            #(enrich-with-tarjoaja % organisaatiot)
            set-has-valintakoe)
          hakukohteet')
        (st/select-schema hakukohteet' api-schemas/HakukohdeListResponse)))

(defn- get-organisations-for-hakukohteet [organisaatio-service hakukohteet]
  (->> (map :tarjoaja hakukohteet)
       distinct
       (organisaatio/find-by-oids organisaatio-service)
       (group-by :oid)))

(defrecord KoutaService [kouta-authenticating-client organisaatio-service config]
  component/Lifecycle

  (start [this]
    (s/validate c/HakukohderyhmaConfig config)
    this)

  (stop [this]
    this)

  kouta-service-protocol/KoutaServiceProtocol

  (list-haun-tiedot [_ is-all tarjoajat]
    (let [now (LocalDateTime/now)
          tarjoaja (str/join "," tarjoajat) ;fixme, korjaus luultavasti kouta-internalin päähän
          url (oph-url/resolve-url :kouta-internal.haku.search config {:tarjoaja tarjoaja})
          filter-fn (if is-all identity (partial not-over? now))]
      (as-> url res'
            (authenticating-client-protocol/get kouta-authenticating-client res' schemas/HaunTiedotListResponse)
            (http/parse-and-validate res' schemas/HaunTiedotListResponse)
            (filter filter-fn res')
            (st/select-schema res' api-schemas/HaunTiedotListResponse))))

  (list-haun-hakukohteet [_ haku-oid tarjoajat]
    (let [tarjoaja (str/join "," tarjoajat)
          url (oph-url/resolve-url :kouta-internal.hakukohde.search config {:haku     haku-oid
                                                                            :tarjoaja tarjoaja
                                                                            :all      "true"})
          hakukohteet (as-> url res'
                            (authenticating-client-protocol/get kouta-authenticating-client res' schemas/HakukohdeListResponse)
                            (http/parse-and-validate res' schemas/HakukohdeListResponse))
          organisaatiot (get-organisations-for-hakukohteet organisaatio-service hakukohteet)]
      (create-internal-hakukohteet hakukohteet organisaatiot)))

  (find-hakukohteet-by-oids [_ oids tarjoajat]
    (if (seq oids)
      (let [tarjoaja (str/join "," tarjoajat)
            url (oph-url/resolve-url :kouta-internal.hakukohde.findbyoids config {:tarjoaja tarjoaja})
            hakukohteet (as-> url res'
                              (authenticating-client-protocol/post kouta-authenticating-client
                                                                   {:url  res'
                                                                    :body oids}
                                                                   {:request-schema  [s/Str]
                                                                    :response-schema schemas/HakukohdeListResponse})
                              (http/parse-and-validate res' schemas/HakukohdeListResponse))
            organisaatiot (get-organisations-for-hakukohteet organisaatio-service hakukohteet)]
        (create-internal-hakukohteet hakukohteet organisaatiot))
      [])))
