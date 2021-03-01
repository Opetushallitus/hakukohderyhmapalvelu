(ns hakukohderyhmapalvelu.organisaatio.organisaatio-service
  (:require [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.api-schemas :as api-schemas]
            [hakukohderyhmapalvelu.cas.cas-authenticating-client-protocol :as authenticating-client-protocol]
            [hakukohderyhmapalvelu.http :as http]
            [hakukohderyhmapalvelu.oph-url-properties :as url]
            [hakukohderyhmapalvelu.organisaatio.organisaatio-protocol :as organisaatio-service-protocol]
            [hakukohderyhmapalvelu.schemas.organisaatio-service-schemas :as schemas]
            [schema.core :as s]
            [hakukohderyhmapalvelu.config :as c]
            [schema-tools.core :as st]))

(def debug-hakukohderyhmat-delete-this-later ["r1" "r2" "r3"])

(defn- parse-and-validate [response]
  (http/parse-and-validate response schemas/PostNewOrganisaatioResponse))

(defrecord OrganisaatioService [organisaatio-service-authenticating-client config]
  component/Lifecycle

  (start [this]
    (s/validate c/HakukohderyhmaConfig config)
    this)

  (stop [this]
    this)

  organisaatio-service-protocol/OrganisaatioServiceProtocol

  (get-all-hakukohderyhmas [_]
    (let [url           "https://virkailija.testiopintopolku.fi/organisaatio-service/rest/organisaatio/1.2.246.562.24.00000000001/ryhmat?includeImage=fals"
          parent-oid    (-> config :oph-organisaatio-oid)
          ;response-body (-> (authenticating-client-protocol/get organisaatio-service-authenticating-client
          ;                                                      url
          ;                                                      nil)
          ;                  parse-and-validate)
          ]
      debug-hakukohderyhmat-delete-this-later))

  (find-by-oids [_ oid-list]
    (if (not-empty oid-list)
      (let [url (url/resolve-url :organisaatio-service.organisaatio.v4.findbyoids config)
            response-body (-> (authenticating-client-protocol/post organisaatio-service-authenticating-client
                                                                   {:url  url
                                                                    :body oid-list}
                                                                   {:request-schema  schemas/FindByOidsRequest
                                                                    :response-schema schemas/FindByOidsResponse})
                              (http/parse-and-validate schemas/FindByOidsResponse))]
        (map #(st/select-schema % api-schemas/Organisaatio) response-body))
      []))

  (post-new-organisaatio [_ hakukohderyhma]
    (s/validate api-schemas/HakukohderyhmaPostRequest hakukohderyhma)
    (let [url           (url/resolve-url :organisaatio-service.organisaatio.v4 config)
          parent-oid    (-> config :oph-organisaatio-oid)
          body          (merge hakukohderyhma
                               {:parentOid    parent-oid
                                :tyypit       ["Ryhma"]
                                :ryhmatyypit  ["ryhmatyypit_2#1"]
                                :kayttoryhmat ["kayttoryhmat_1#1"]})
          response-body (-> (authenticating-client-protocol/post organisaatio-service-authenticating-client
                                                                 {:url  url
                                                                  :body body}
                                                                 {:request-schema  schemas/PostNewOrganisaatioRequest
                                                                  :response-schema schemas/PostNewOrganisaatioResponse})
                            (http/parse-and-validate schemas/PostNewOrganisaatioResponse))]
      (-> response-body
          :organisaatio
          (select-keys [:oid :nimi])))))
