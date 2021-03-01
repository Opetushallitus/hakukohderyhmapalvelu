(ns hakukohderyhmapalvelu.hakukohderyhma.hakukohderyhma-service
  (:require [hakukohderyhmapalvelu.audit-logger-protocol :as audit]
            [hakukohderyhmapalvelu.hakukohderyhma.hakukohderyhma-service-protocol :as hakukohderyhma-service-protocol]
            [hakukohderyhmapalvelu.organisaatio.organisaatio-protocol :as organisaatio]
            [hakukohderyhmapalvelu.kouta.kouta-protocol :as kouta]))

(def hakukohderyhma-luonti (audit/->operation "HakukohderyhmaLuonti"))

(defrecord HakukohderyhmaService [audit-logger organisaatio-service kouta-service]
  hakukohderyhma-service-protocol/HakukohderyhmaServiceProtocol

  (get-all [_]
    (organisaatio/get-organisaatio-children organisaatio-service))

  (create [_ session hakukohderyhma]
    (let [r (organisaatio/post-new-organisaatio organisaatio-service hakukohderyhma)]
      (audit/log audit-logger
                 (audit/->user session)
                 hakukohderyhma-luonti
                 (audit/->target {:oid (:oid r)})
                 (audit/->changes {} r))
      r))

  (list-haun-tiedot [_ session is-all]
    (kouta/list-haun-tiedot kouta-service is-all))

  (list-haun-hakukohteet [_ session haku-oid]
    (kouta/list-haun-hakukohteet kouta-service haku-oid)))
