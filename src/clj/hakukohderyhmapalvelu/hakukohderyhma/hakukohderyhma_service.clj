(ns hakukohderyhmapalvelu.hakukohderyhma.hakukohderyhma-service
  (:require [hakukohderyhmapalvelu.hakukohderyhma.hakukohderyhma-service-protocol :as hakukohderyhma-service-protocol]
            [hakukohderyhmapalvelu.organisaatio.organisaatio-protocol :as organisaatio]))

(defrecord HakukohderyhmaService [organisaatio-service]
  hakukohderyhma-service-protocol/HakukohderyhmaServiceProtocol

  (create [_ hakukohderyhma]
    (organisaatio/post-new-organisaatio organisaatio-service hakukohderyhma)))
