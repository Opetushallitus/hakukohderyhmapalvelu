(ns hakukohderyhmapalvelu.organisaatio.organisaatio-service
  (:require [hakukohderyhmapalvelu.api-schemas :as api-schema]
            [hakukohderyhmapalvelu.oph-url-properties :as url]
            [hakukohderyhmapalvelu.schemas.organisaatio-service-schemas :as schemas]
            [schema.core :as s]
            [hakukohderyhmapalvelu.config :as c])
  (:import [hakukohderyhmapalvelu.organisaatio.organisaatio_protocol OrganisaatioServiceProtocol]))

(defrecord OrganisaatioService [organisaatio-service-cas-client config]
  OrganisaatioServiceProtocol

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
          response-body (.post organisaatio-service-cas-client
                               {:url  url
                                :body body}
                               {:request-schema  schemas/PostNewOrganisaatioRequest
                                :response-schema schemas/PostNewOrganisaatioResponse})]
      (-> response-body
          :organisaatio
          (select-keys [:oid :nimi])))))
