(ns hakukohderyhmapalvelu.hakukohderyhma.hakukohderyhma-service
  (:require [hakukohderyhmapalvelu.audit-logger-protocol :as audit]
            [hakukohderyhmapalvelu.hakukohderyhma.hakukohderyhma-service-protocol :as hakukohderyhma-service-protocol]
            [hakukohderyhmapalvelu.organisaatio.organisaatio-protocol :as organisaatio]
            [hakukohderyhmapalvelu.kouta.kouta-protocol :as kouta]))

(def hakukohderyhma-luonti (audit/->operation "HakukohderyhmaLuonti"))

(defrecord HakukohderyhmaService [audit-logger organisaatio-service kouta-service]
  hakukohderyhma-service-protocol/HakukohderyhmaServiceProtocol

  (create [_ session hakukohderyhma]
    (let [r (organisaatio/post-new-organisaatio organisaatio-service hakukohderyhma)]
      (audit/log audit-logger
                 (audit/->user session)
                 hakukohderyhma-luonti
                 (audit/->target {:oid (:oid r)})
                 (audit/->changes {} r))
      r))

  (list-haun-tiedot [_ session is-all]
    (kouta/list-haun-tiedot kouta-service is-all)))
