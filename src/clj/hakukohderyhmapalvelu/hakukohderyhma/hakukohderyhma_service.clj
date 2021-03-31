(ns hakukohderyhmapalvelu.hakukohderyhma.hakukohderyhma-service
  (:require [hakukohderyhmapalvelu.audit-logger-protocol :as audit]
            [hakukohderyhmapalvelu.hakukohderyhma.hakukohderyhma-service-protocol :as hakukohderyhma-service-protocol]
            [hakukohderyhmapalvelu.hakukohderyhma.db.hakukohderyhma-queries :as hakukohderyhma-queries]
            [hakukohderyhmapalvelu.organisaatio.organisaatio-protocol :as organisaatio]
            [hakukohderyhmapalvelu.kouta.kouta-protocol :as kouta]))

(def hakukohderyhmapalvelu-ryhmatyyppi "ryhmatyypit_6#1")
(def default-hakukohderyhma {:tyypit       ["Ryhma"]
                             :ryhmatyypit  [hakukohderyhmapalvelu-ryhmatyyppi]
                             :kayttoryhmat ["kayttoryhmat_1#1"]})

(def hakukohderyhma-luonti (audit/->operation "HakukohderyhmaLuonti"))
(def hakukohderyhma-uudelleennimeaminen (audit/->operation "HakukohderyhmaUudelleennimeaminen"))
(def hakukohderyhma-hakukohteet-edit (audit/->operation "HakukohderyhmaLiitosMuokkaus"))

(defn- create-merge-hakukohderyhma-with-hakukohteet-fn [hakukohderyhmat hakukohteet]
  (let [grouped-hakukohderyhmat (group-by :oid hakukohderyhmat)
        grouped-hakukohteet (group-by :oid hakukohteet)]
    (fn [{:keys [hakukohderyhma-oid hakukohde-oids]}]
      (when-let [hakukohderyhma (first (get grouped-hakukohderyhmat hakukohderyhma-oid))]
        (->> hakukohde-oids
             (map #(first (get grouped-hakukohteet %)))
             (assoc hakukohderyhma :hakukohteet))))))

(defrecord HakukohderyhmaService [audit-logger organisaatio-service kouta-service db]
  hakukohderyhma-service-protocol/HakukohderyhmaServiceProtocol

  (find-hakukohderyhmat-by-hakukohteet-oids [_ session hakukohde-oids include-empty]
    ;; TODO: Tarkista käyttäjän oikeudet hakukohteisiin ja hakukohderyhmään (organisaatioon)
    (if-not (empty? hakukohde-oids)
      (let [hakukohderyhmat (organisaatio/get-organisaatio-children organisaatio-service hakukohderyhmapalvelu-ryhmatyyppi)
            hakukohteet (kouta/find-hakukohteet-by-oids kouta-service hakukohde-oids)
            joins (hakukohderyhma-queries/hakukohderyhmat-by-hakukohteet-and-hakukohderyhmat
                    db
                    (map :oid hakukohderyhmat)
                    (map :oid hakukohteet))
            merge-fn (create-merge-hakukohderyhma-with-hakukohteet-fn hakukohderyhmat hakukohteet)
            hakukohderyhmat-with-hakukohteet (map merge-fn joins)]
        (cond->> hakukohderyhmat-with-hakukohteet
                 (not include-empty) (remove #(empty? (:hakukohteet %)))))
      []))

  (create [_ session hakukohderyhma-name]
    (let [hkr (organisaatio/post-new-organisaatio organisaatio-service (merge default-hakukohderyhma
                                                                              hakukohderyhma-name))]
      (audit/log audit-logger
                 (audit/->user session)
                 hakukohderyhma-luonti
                 (audit/->target {:oid (:oid hkr)})
                 (audit/->changes {} hkr))
      (assoc hkr :hakukohteet [])))

  (rename [this session hakukohderyhma]
    (let [previous-hkr (organisaatio/get-organisaatio organisaatio-service (:oid hakukohderyhma))
          renamed-hkr (organisaatio/put-organisaatio organisaatio-service hakukohderyhma)]
      (audit/log audit-logger
                 (audit/->user session)
                 hakukohderyhma-uudelleennimeaminen
                 (audit/->target {:oid (:oid renamed-hkr)})
                 (audit/->changes (:nimi previous-hkr) (:nimi renamed-hkr)))
      renamed-hkr))

  (list-haun-tiedot [_ session is-all]
    (kouta/list-haun-tiedot kouta-service is-all))

  (list-haun-hakukohteet [_ session haku-oid]
    (kouta/list-haun-hakukohteet kouta-service haku-oid))

  (update-hakukohderyhma-hakukohteet [this session oid hakukohteet]
    ;; TODO: Tarkista käyttäjän oikeudet hakukohteisiin ja hakukohderyhmään (organisaatioon)
    (let [current-hakukohderyhma (hakukohderyhma-service-protocol/get-hakukohderyhma this session oid)
          hakukohteet' (->> (map :oid hakukohteet)
                            (kouta/find-hakukohteet-by-oids kouta-service))
          updated-hakukohderyhma (assoc current-hakukohderyhma :hakukohteet hakukohteet')
          distinct-haut (distinct (map :hakuOid hakukohteet'))]
      (if (< (count distinct-haut) 2)
        (do
          (hakukohderyhma-queries/update-hakukohderyhma-hakukohteet! db oid hakukohteet')
          (audit/log audit-logger
                     (audit/->user session)
                     hakukohderyhma-hakukohteet-edit
                     (audit/->target {:oid oid})
                     (audit/->changes current-hakukohderyhma updated-hakukohderyhma))
          updated-hakukohderyhma)
        (throw (Exception. "Hakukohteet eivät kuulu samaan hakuun.")))))

  (get-hakukohderyhma [_ session hakukohderyhma-oid]
    (let [hakukohderyhma (organisaatio/get-organisaatio organisaatio-service hakukohderyhma-oid)
          hakukohde-oidit (hakukohderyhma-queries/hakukohde-oidit-by-hakukohderyhma-oid db (:oid hakukohderyhma))]
      (assoc hakukohderyhma :hakukohteet (kouta/find-hakukohteet-by-oids kouta-service hakukohde-oidit)))))
