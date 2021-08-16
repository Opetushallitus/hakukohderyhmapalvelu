(ns hakukohderyhmapalvelu.hakukohderyhma.hakukohderyhma-service
  (:require [hakukohderyhmapalvelu.audit-logger-protocol :as audit]
            [hakukohderyhmapalvelu.hakukohderyhma.hakukohderyhma-service-protocol :as hakukohderyhma-protocol]
            [hakukohderyhmapalvelu.hakukohderyhma.db.hakukohderyhma-queries :as hakukohderyhma-queries]
            [hakukohderyhmapalvelu.organisaatio.organisaatio-protocol :as organisaatio]
            [hakukohderyhmapalvelu.ataru.ataru-protocol :as ataru]
            [hakukohderyhmapalvelu.kouta.kouta-protocol :as kouta]
            [hakukohderyhmapalvelu.api-schemas :as api-schemas]))

(def hakukohderyhmapalvelu-ryhmatyyppi "ryhmatyypit_6#1")
(def default-hakukohderyhma {:tyypit       ["Ryhma"]
                             :ryhmatyypit  [hakukohderyhmapalvelu-ryhmatyyppi]
                             :kayttoryhmat ["kayttoryhmat_1#1"]})

(def hakukohderyhma-luonti (audit/->operation "HakukohderyhmaLuonti"))
(def hakukohderyhma-uudelleennimeaminen (audit/->operation "HakukohderyhmaUudelleennimeaminen"))
(def hakukohderyhma-hakukohteet-edit (audit/->operation "HakukohderyhmaLiitosMuokkaus"))
(def hakukohderyhma-poisto (audit/->operation "HakukohderyhmaPoisto"))

(defn- create-merge-hakukohderyhma-with-hakukohteet-fn [hakukohderyhmat hakukohteet]
  (let [grouped-hakukohderyhmat (group-by :oid hakukohderyhmat)
        grouped-hakukohteet (group-by :oid hakukohteet)]
    (fn [{:keys [hakukohderyhma-oid hakukohde-oids]}]
      (when-let [hakukohderyhma (first (get grouped-hakukohderyhmat hakukohderyhma-oid))]
        (->> hakukohde-oids
             (keep #(first (get grouped-hakukohteet %)))
             (assoc hakukohderyhma :hakukohteet))))))

(defn- session-organizations [session]
  (get-in session [:identity :organizations]))

(defn- apply-default-settings-if-missing
  [hakukohderyhma settings]
    (if-let [matching-settings (first (filter #(= (:hakukohderyhma-oid %) (:oid hakukohderyhma)) settings))]
      (assoc hakukohderyhma :settings (dissoc matching-settings :hakukohderyhma-oid))
      (assoc hakukohderyhma :settings hakukohderyhma-queries/initial-settings))
  )

(defrecord HakukohderyhmaService [audit-logger organisaatio-service kouta-service ataru-service db]
  hakukohderyhma-protocol/HakukohderyhmaServiceProtocol

  (find-hakukohderyhmat-by-hakukohteet-oids [_ session hakukohde-oids include-empty]
    (if-not (empty? hakukohde-oids)
      (let [user-organisaatiot (session-organizations session)
            hakukohderyhmat (organisaatio/get-organisaatio-children organisaatio-service hakukohderyhmapalvelu-ryhmatyyppi)
            hakukohteet (kouta/find-hakukohteet-by-oids kouta-service hakukohde-oids user-organisaatiot)
            joins (hakukohderyhma-queries/hakukohderyhmat-by-hakukohteet-and-hakukohderyhmat
                    db
                    (map :oid hakukohderyhmat)
                    (map :oid hakukohteet))
            merge-fn (create-merge-hakukohderyhma-with-hakukohteet-fn hakukohderyhmat hakukohteet)
            hakukohderyhmat-with-hakukohteet (map merge-fn joins)
            settings (hakukohderyhma-queries/find-settings-by-hakukohderyhma-oids db (map :oid hakukohderyhmat))
            hakukohde-ryhmat-with-settings (map #(apply-default-settings-if-missing % settings) hakukohderyhmat-with-hakukohteet)]
        (cond->> hakukohde-ryhmat-with-settings
                 (not include-empty) (remove #(empty? (:hakukohteet %)))))
      []))

  (list-hakukohderyhma-oids-by-hakukohde-oid [_ session hakukohde-oid]
    (when hakukohde-oid
      (hakukohderyhma-queries/get-hakukohderyhma-oids-by-hakukohde-oid db hakukohde-oid)))

  (create [_ session hakukohderyhma-name]
    (let [hkr (organisaatio/post-new-organisaatio organisaatio-service (merge default-hakukohderyhma
                                                                              hakukohderyhma-name))]
      (audit/log audit-logger
                 (audit/->user session)
                 hakukohderyhma-luonti
                 (audit/->target {:oid (:oid hkr)})
                 (audit/->changes {} hkr))
      (assoc hkr :hakukohteet []
                 :settings hakukohderyhma-queries/initial-settings)))

  (delete [this session hakukohderyhma-oid]
    (let [forms (ataru/get-forms ataru-service hakukohderyhma-oid)
          not-in-ataru-use (empty? forms)
          owns-all (->> (hakukohderyhma-protocol/get-hakukohteet-for-hakukohderyhma-oid this session hakukohderyhma-oid)
                        (every? :oikeusHakukohteeseen))]
      (if (and not-in-ataru-use owns-all)
        (do
          (organisaatio/delete-organisaatio organisaatio-service hakukohderyhma-oid)
          (hakukohderyhma-queries/delete-hakukohderyhma db hakukohderyhma-oid)
          (audit/log audit-logger
                     (audit/->user session)
                     hakukohderyhma-poisto
                     (audit/->target {:oid hakukohderyhma-oid})
                     (audit/->changes nil nil))
          api-schemas/StatusDeleted)
        api-schemas/StatusInUse)))

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
    (let [user-organisaatiot (session-organizations session)]
      (kouta/list-haun-tiedot kouta-service is-all user-organisaatiot)))

  (list-haun-hakukohteet [_ session haku-oid]
    (let [user-organisaatiot (session-organizations session)]
      (kouta/list-haun-hakukohteet kouta-service haku-oid user-organisaatiot)))

  (update-hakukohderyhma-hakukohteet [this session oid hakukohteet]
    (let [user-organisaatiot (session-organizations session)
          hakukohde-oidit (map :oid hakukohteet)
          new-hakukohteet (kouta/find-hakukohteet-by-oids kouta-service hakukohde-oidit user-organisaatiot)
          hakukohderyhma (hakukohderyhma-protocol/get-hakukohderyhma this session oid)
          current-hakukohteet (:hakukohteet hakukohderyhma)
          distinct-haut (distinct (map :hakuOid new-hakukohteet))]
      (if (< (count distinct-haut) 2)                       ;; Hakukohteet kuuluvat samaan hakuun
        (let [updated-hakukohde-oids (hakukohderyhma-queries/update-hakukohderyhma-hakukohteet! db oid current-hakukohteet new-hakukohteet)
              all-hakukohteet (merge (group-by :oid new-hakukohteet) (group-by :oid current-hakukohteet))
              updated-hakukohteet (map #(first (get all-hakukohteet %)) updated-hakukohde-oids)
              settings (hakukohderyhma-protocol/get-settings this session oid)
              hakukohderyhma' (-> hakukohderyhma
                                  (assoc :hakukohteet updated-hakukohteet)
                                  (assoc :settings settings))]
          (audit/log audit-logger
                     (audit/->user session)
                     hakukohderyhma-hakukohteet-edit
                     (audit/->target {:oid oid})
                     (audit/->changes hakukohderyhma hakukohderyhma'))
          hakukohderyhma')
        (throw (Exception. "Hakukohteet eivät kuulu samaan hakuun.")))))

  (insert-or-update-settings
    [_ _ hakukohderyhma-oid settings]
      (hakukohderyhma-queries/insert-or-update-settings db hakukohderyhma-oid settings))

  (get-settings
    [_ _ hakukohderyhma-oid]
    (-> (hakukohderyhma-queries/find-settings-by-hakukohderyhma-oids db [hakukohderyhma-oid])
         (first)
         (dissoc :hakukohderyhma-oid)))

  (get-hakukohteet-for-hakukohderyhma-oid [_ session hakukohderyhma-oid]
    (let [user-organisaatiot (session-organizations session)
          hakukohde-oidit (hakukohderyhma-queries/hakukohde-oidit-by-hakukohderyhma-oid db hakukohderyhma-oid)]
      (kouta/find-hakukohteet-by-oids kouta-service hakukohde-oidit user-organisaatiot)))

  (get-hakukohde-oids-for-hakukohderyhma-oid [_ hakukohderyhma-oid]
    (hakukohderyhma-queries/hakukohde-oidit-by-hakukohderyhma-oid db hakukohderyhma-oid))

  (get-hakukohderyhma [this session hakukohderyhma-oid]
    (let [hakukohderyhma (organisaatio/get-organisaatio organisaatio-service hakukohderyhma-oid)]
      (->> (hakukohderyhma-protocol/get-hakukohteet-for-hakukohderyhma-oid this session hakukohderyhma-oid)
           (assoc hakukohderyhma :hakukohteet))))

  (get-hakukohderyhmat-by-hakukohteet [_ _ hakukohde-oids]
    (if-not (empty? hakukohde-oids)
      (hakukohderyhma-queries/get-hakukohderyhmat-by-hakukohteet db hakukohde-oids)
      [])))
