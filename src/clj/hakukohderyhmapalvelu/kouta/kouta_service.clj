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
            [hakukohderyhmapalvelu.config :as c])
  (:import (java.time LocalDateTime)))


(defn- local-date-time? [dt]
  (instance? LocalDateTime dt))

(defn- hakuaika-not-over? [now {:keys [alkaa paattyy]}]
  ;; Haku ei ole päättynyt, jos sillä on tulevaisuudessa alkavia hakuja
  ;; tai alkaneet haut eivät ole vielä päättyneet.
  (or
    (when (local-date-time? alkaa)
      (.isAfter alkaa now))
    (when (local-date-time? paattyy)
      (.isAfter paattyy now))))

(defn- not-over? [now {:keys [hakuajat]}]
  (->> hakuajat
       (map (partial hakuaika-not-over? now))
       (some true?)))

(defn- enrich-with-organisaatio [hakukohde organisaatiot]
  (->> hakukohde
       :organisaatioOid
       (get organisaatiot)
       first
       (assoc hakukohde :organisaatio)))

(defn- hakukohteet-with-organisaatio [hakukohteet organisaatiot]
  (as-> hakukohteet hakukohteet'
        (map #(enrich-with-organisaatio % organisaatiot) hakukohteet')
        (st/select-schema hakukohteet' api-schemas/HakukohdeListResponse)))

(defrecord KoutaService [kouta-authenticating-client organisaatio-service config]
  component/Lifecycle

  (start [this]
    (s/validate c/HakukohderyhmaConfig config)
    this)

  (stop [this]
    this)

  kouta-service-protocol/KoutaServiceProtocol

  (list-haun-tiedot [_ is-all]
    (let [now (LocalDateTime/now)
          organisaatio-oid (:oph-organisaatio-oid config)
          url (oph-url/resolve-url :kouta-internal.haku.search config {:tarjoaja organisaatio-oid})
          filter-fn (if is-all identity (partial not-over? now))]
      (as-> url res'
            (authenticating-client-protocol/get kouta-authenticating-client res' schemas/HaunTiedotListResponse)
            (http/parse-and-validate res' schemas/HaunTiedotListResponse)
            (filter filter-fn res')
            (st/select-schema res' api-schemas/HaunTiedotListResponse))))

  (list-haun-hakukohteet [_ haku-oid]
    (let [url (oph-url/resolve-url :kouta-internal.hakukohde.search config {:haku haku-oid})
          hakukohteet (as-> url res'
                            (authenticating-client-protocol/get kouta-authenticating-client res' schemas/HakukohdeListResponse)
                            (http/parse-and-validate res' schemas/HakukohdeListResponse))
          organisaatiot (->> (map :organisaatioOid hakukohteet)
                             (organisaatio/find-by-oids organisaatio-service)
                             (group-by :oid))]

      (hakukohteet-with-organisaatio hakukohteet organisaatiot)))

  (find-hakukohteet-by-oids [_ oidit]
    (let [url (oph-url/resolve-url :kouta-internal.hakukohde.findbyoids config)
          hakukohteet (as-> url res'
                            (authenticating-client-protocol/post kouta-authenticating-client
                                                                 {:url res'
                                                                  :body oidit}
                                                                 {:request-schema [s/Str]
                                                                  :response-schema schemas/HakukohdeListResponse})
                            (http/parse-and-validate res' schemas/HakukohdeListResponse))
          organisaatiot (->> (map :organisaatioOid hakukohteet)
                             (organisaatio/find-by-oids organisaatio-service)
                             (group-by :oid))]
      (hakukohteet-with-organisaatio hakukohteet organisaatiot))))
