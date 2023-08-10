(ns hakukohderyhmapalvelu.ataru.ataru-service
  (:require [com.stuartsierra.component :as component]
            [schema.core :as s]
            [hakukohderyhmapalvelu.config :as c]
            [hakukohderyhmapalvelu.cas.cas-authenticating-client-protocol :as authenticating-client-protocol]
            [hakukohderyhmapalvelu.ataru.ataru-protocol :as ataru-service-protocol]
            [hakukohderyhmapalvelu.oph-url-properties :as oph-url]
            [hakukohderyhmapalvelu.schemas.organisaatio-service-schemas :as schemas]
            [hakukohderyhmapalvelu.http :as http]))


(defrecord AtaruService [ataru-authenticating-client config]
  component/Lifecycle

  (start [this]
    (s/validate c/HakukohderyhmaConfig config)
    this)

  (stop [this]
    this)

  ataru-service-protocol/AtaruServiceProtocol

  (get-forms [_ hakukohderyhma-oid]
    (let [params (if hakukohderyhma-oid {:hakukohderyhma-oid hakukohderyhma-oid} {})
          url (oph-url/resolve-url :ataru.forms config params)]
      (-> (authenticating-client-protocol/http-get ataru-authenticating-client url)
          (http/parse-and-validate schemas/GetFormsResponse)
          :forms))))
