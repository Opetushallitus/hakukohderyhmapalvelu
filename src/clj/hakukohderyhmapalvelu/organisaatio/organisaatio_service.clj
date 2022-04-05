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
            [schema-tools.core :as st]
            [taoensso.timbre :as log]))

(def hakukohderyhma-keys [:oid :nimi :version :parentOid :tyypit :ryhmatyypit :kayttoryhmat])

(defn call-find-by-oids
  [config organisaatio-service-authenticating-client oid-list]
  (if (not-empty oid-list)
    (let [url           (oph-url/resolve-url :organisaatio-service.organisaatio.v4.findbyoids config)
          response-body (-> (authenticating-client-protocol/post organisaatio-service-authenticating-client
                              {:url  url
                               :body oid-list}
                              {:request-schema  schemas/FindByOidsRequest
                               :response-schema schemas/FindByOidsResponse})
                          (http/parse-and-validate schemas/FindByOidsResponse))]
      (map #(st/select-schema % api-schemas/Organisaatio) response-body))
    []))

(defn do-chunked
  [chunk-size f list]
  (if (> (count list) chunk-size)
    (let [[chunk rest] (split-at chunk-size list)]
      (concat (f chunk) (do-chunked chunk-size f rest)))
    (f list)))

(defrecord OrganisaatioService [organisaatio-service-authenticating-client config]
  component/Lifecycle

  (start [this]
    (s/validate c/HakukohderyhmaConfig config)
    this)

  (stop [this]
    this)

  organisaatio-service-protocol/OrganisaatioServiceProtocol

  (get-organisaatio-children [_ ryhmatyyppi]
    (let [url (oph-url/resolve-url :organisaatio-service.organisaatio.v3.ryhmat config {:ryhmatyyppi ryhmatyyppi})
          response-body (as-> url res'
                              (authenticating-client-protocol/get organisaatio-service-authenticating-client
                                                                  res'
                                                                  schemas/GetRyhmatResponse)
                              (http/parse-and-validate res' schemas/GetRyhmatResponse))
          orgs (->> response-body
                    (map #(st/select-schema % api-schemas/Organisaatio)))]
      orgs))

  (get-organisaatio [_ oid]
    (let [url (oph-url/resolve-url :organisaatio-service.organisaatio.v4.get config oid)
          organisaatio (-> (authenticating-client-protocol/get organisaatio-service-authenticating-client
                                                               url
                                                               {:response-schema schemas/Organisaatio})
                           (http/parse-and-validate schemas/Organisaatio))]
      (st/select-schema organisaatio api-schemas/Organisaatio)))

  (find-by-oids [_ oid-list]
    (let [f (partial call-find-by-oids config organisaatio-service-authenticating-client)]
      ; Organisaatio service limits requests to have at most 1000 oids
      (do-chunked 1000 f oid-list)))

  (post-new-organisaatio [_ hakukohderyhma]
    (s/validate schemas/PostOrganisaatioHakukohderyhmaParameter hakukohderyhma)
    (let [url           (oph-url/resolve-url :organisaatio-service.organisaatio.v4 config)
          parent-oid    (-> config :oph-organisaatio-oid)
          body          (merge hakukohderyhma
                               {:parentOid    parent-oid})]
      (log/info (str "Creating new hakukohderyhmä to organisaatiopalvelu. Request body: " body))
      (let [response-body-unparsed (-> (authenticating-client-protocol/post organisaatio-service-authenticating-client
                                                                   {:url  url
                                                                    :body body}
                                                                   {:request-schema  schemas/PostNewOrganisaatioRequest
                                                                    :response-schema schemas/PostNewOrganisaatioResponse}))]
        (log/info (str "Creating new hakukohderyhmä to organisaatiopalvelu. Response body: " response-body-unparsed))
        (-> (http/parse-and-validate response-body-unparsed schemas/PostNewOrganisaatioResponse)
            :organisaatio
            (st/select-schema api-schemas/Organisaatio)))))

  (put-organisaatio [_ hakukohderyhma]
    (s/validate api-schemas/HakukohderyhmaPutRequest hakukohderyhma)
    (let [base-url (oph-url/resolve-url :organisaatio-service.organisaatio.v4 config)
          url (str base-url "/" (:oid hakukohderyhma))
          response-unparsed (authenticating-client-protocol/http-put organisaatio-service-authenticating-client
                                                                          {:url  url
                                                                           :body hakukohderyhma}
                                                                          {:request-schema  schemas/Organisaatio
                                                                           :response-schema schemas/PutNewOrganisaatioResponse})
          response-body (http/parse-and-validate response-unparsed s/Any)]
      (-> response-body
          :organisaatio
          (st/select-schema api-schemas/Organisaatio))))

  (delete-organisaatio [_ oid]
    (let [url (oph-url/resolve-url :organisaatio-service.organisaatio.v4.delete config oid)]
      (-> (authenticating-client-protocol/delete organisaatio-service-authenticating-client
                                                 url
                                                 schemas/DeleteOrganisaatioResponse)
          (http/parse-and-validate schemas/DeleteOrganisaatioResponse))
      (log/info (str "Deleted organisation with oid " oid)))))
