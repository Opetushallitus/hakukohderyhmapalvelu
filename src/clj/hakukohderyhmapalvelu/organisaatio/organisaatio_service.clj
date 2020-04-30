(ns hakukohderyhmapalvelu.organisaatio.organisaatio-service
  (:require [hakukohderyhmapalvelu.api-schemas :as api-schema]
            [hakukohderyhmapalvelu.cas.cas-authenticating-client-protocol :as authenticating-client-protocol]
            [hakukohderyhmapalvelu.http :as http]
            [hakukohderyhmapalvelu.oph-url-properties :as url]
            [hakukohderyhmapalvelu.organisaatio.organisaatio-protocol :as organisaatio-service-protocol]
            [hakukohderyhmapalvelu.schemas.organisaatio-service-schemas :as schemas]
            [schema.core :as s]
            [hakukohderyhmapalvelu.config :as c]))

(defn- parse-and-validate [response]
  (http/parse-and-validate response schemas/PostNewOrganisaatioResponse))

(defrecord OrganisaatioService [organisaatio-service-authenticating-client config]
  organisaatio-service-protocol/OrganisaatioServiceProtocol

  (post-new-organisaatio [_ hakukohderyhma]
    (s/validate c/HakukohderyhmaConfig config)
    (s/validate api-schema/HakukohderyhmaRequest hakukohderyhma)
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
                            parse-and-validate)]
      (-> response-body
          :organisaatio
          (select-keys [:oid :nimi])))))
