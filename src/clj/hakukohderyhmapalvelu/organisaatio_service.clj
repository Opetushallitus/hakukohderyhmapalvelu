(ns hakukohderyhmapalvelu.organisaatio-service
  (:require [clojure.pprint]
            [hakukohderyhmapalvelu.oph-url-properties :as url]
            [hakukohderyhmapalvelu.schemas.organisaatio-service-schemas :as schemas]
            [schema.core :as s]
            [hakukohderyhmapalvelu.config :as c]))

(defprotocol OrganisaatioServiceProtocol
  (post-new-organisaatio [service hakukohderyhma]))

(defrecord OrganisaatioService [organisaatio-service-cas-client config]
  OrganisaatioServiceProtocol

  (post-new-organisaatio [_ hakukohderyhma]
    (s/validate c/HakukohderyhmaConfig config)
    (let [url           (url/resolve-url :organisaatio-service.organisaatio.v4 config)
          parent-oid    (-> config :oph-organisaatio-oid)
          body          (merge hakukohderyhma
                               {:parentOid    parent-oid
                                :tyypit       ["Ryhma"]
                                :ryhmatyypit  ["ryhmatyypit_2#1"]
                                :kayttoryhmat ["kayttoryhmat_1#1"]})
          response-body (.post organisaatio-service-cas-client
                               {:url  url
                                :body body}
                               {:request-schema  schemas/PostNewOrganisaatioRequestSchema
                                :response-schema schemas/PostNewOrganisaatioResponseSchema})]
      (-> response-body
          :organisaatio
          (select-keys [:oid :nimi])))))
