(ns hakukohderyhmapalvelu.organisaatio.organisaatio-service
  (:require [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.api-schemas :as api-schemas]
            [hakukohderyhmapalvelu.cas.cas-authenticating-client-protocol :as authenticating-client-protocol]
            [hakukohderyhmapalvelu.http :as http]
            [hakukohderyhmapalvelu.oph-url-properties :as oph-url]
            [hakukohderyhmapalvelu.organisaatio.organisaatio-protocol :as organisaatio-service-protocol]
            [hakukohderyhmapalvelu.schemas.organisaatio-service-schemas :as schemas]
            [schema.core :as s]
            [hakukohderyhmapalvelu.config :as c]
            [schema-tools.core :as st]))

(def hakukohderyhma-keys [:oid :nimi])

(defrecord OrganisaatioService [organisaatio-service-authenticating-client config]
  component/Lifecycle

  (start [this]
    (s/validate c/HakukohderyhmaConfig config)
    this)

  (stop [this]
    this)

  organisaatio-service-protocol/OrganisaatioServiceProtocol

  (get-organisaatio-children [_]
    (let [url (oph-url/resolve-url :organisaatio-service.organisaatio.v3.ryhmat config)
          response-body (as-> url res'
                              (authenticating-client-protocol/get organisaatio-service-authenticating-client
                                                                  res'
                                                                  s/Any)
                              (http/parse-and-validate res' schemas/GetRyhmatResponse))]
      (map #(select-keys % hakukohderyhma-keys)
           response-body)))

  (find-by-oids [_ oid-list]
    (if (not-empty oid-list)
      (let [url (oph-url/resolve-url :organisaatio-service.organisaatio.v4.findbyoids config)
            response-body (-> (authenticating-client-protocol/post organisaatio-service-authenticating-client
                                                                   {:url  url
                                                                    :body oid-list}
                                                                   {:request-schema  schemas/FindByOidsRequest
                                                                    :response-schema schemas/FindByOidsResponse})
                              (http/parse-and-validate schemas/FindByOidsResponse))]
        (map #(st/select-schema % api-schemas/Organisaatio) response-body))
      []))

  (post-new-organisaatio [_ hakukohderyhma]
    (s/validate api-schemas/HakukohderyhmaRequest hakukohderyhma)
    (let [url           (oph-url/resolve-url :organisaatio-service.organisaatio.v4 config)
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
          (select-keys hakukohderyhma-keys)))))
