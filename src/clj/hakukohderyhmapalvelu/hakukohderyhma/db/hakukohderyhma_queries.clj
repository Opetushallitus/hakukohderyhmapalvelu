(ns hakukohderyhmapalvelu.hakukohderyhma.db.hakukohderyhma-queries
  (:require [hugsql.core :as hugsql]
            [clojure.java.jdbc :refer [with-db-transaction]]
            [clojure.data :refer [diff]]))


(hugsql/def-db-fns "hakukohderyhmapalvelu/hakukohderyhma/db/hakukohderyhma_queries.sql")

;; Esittele sql-kyselyt
(declare hakukohteet-by-hakukohderyhma-oid)
(declare hakukohderyhma-oids-by-hakukohde-oid)
(declare delete-hakukohteet-from-hakukohderyhma!)
(declare add-hakukohteet-into-hakukohderyhma!)
(declare hakukohderyhma-by-hakukohteet-and-hakukohderyhmat)
(declare delete-by-hakukohderyhma-oid)
(declare delete-settings-by-hakukohderyhma-oid)
(declare settings-by-hakukohderyhma-oids)
(declare upsert-settings!)
(declare grouped-hakukohderyhmas)

(def initial-settings
  {:rajaava false
   :max-hakukohteet nil
   :jos-ylioppilastutkinto-ei-muita-pohjakoulutusliitepyyntoja false
   :yo-amm-autom-hakukelpoisuus false})

(defn hakukohde-oidit-by-hakukohderyhma-oid [db hakukohderyhma-oid]
  (->> {:oid hakukohderyhma-oid}
       (hakukohteet-by-hakukohderyhma-oid db)
       (map :hakukohde-oid)))

(defn update-hakukohderyhma-hakukohteet! [db hakukohderyhma-oid current-hakukohteet new-hakukohteet]
  (with-db-transaction [tx db]
                       (let [get-authorized-oids (fn [hakukohteet]
                                                   (->> hakukohteet
                                                        (filter :oikeusHakukohteeseen)
                                                        (map :oid)))
                             new (get-authorized-oids new-hakukohteet)
                             current (get-authorized-oids current-hakukohteet)
                             [to-delete to-add _] (diff current new)
                             to-delete' (remove nil? to-delete)
                             hakukohteet-to-add-tuple (->> (remove nil? to-add)
                                                           (map (fn [hk-oid] [hakukohderyhma-oid hk-oid])))]
                         (when-not (empty? to-delete')
                           (delete-hakukohteet-from-hakukohderyhma! tx {:oid         hakukohderyhma-oid
                                                                        :hakukohteet to-delete'}))
                         (when-not (empty? hakukohteet-to-add-tuple)
                           (add-hakukohteet-into-hakukohderyhma! tx {:hakukohderyhmat hakukohteet-to-add-tuple}))
                         (hakukohde-oidit-by-hakukohderyhma-oid tx hakukohderyhma-oid))))

(defn hakukohderyhmat-by-hakukohteet-and-hakukohderyhmat [db hakukohderyhma-oids hakukohde-oids]
  (hakukohderyhma-by-hakukohteet-and-hakukohderyhmat db {:hakukohderyhmat hakukohderyhma-oids
                                                         :hakukohteet     hakukohde-oids}))

(defn get-hakukohderyhma-oids-by-hakukohde-oid [db hakukohde-oid]
  (->> {:hakukohde-oid hakukohde-oid}
       (hakukohderyhma-oids-by-hakukohde-oid db)
       (mapv :hakukohderyhma-oid)))

(defn delete-hakukohderyhma [db hakukohderyhma-oid]
  (with-db-transaction [tx db]
    (delete-settings-by-hakukohderyhma-oid tx {:oid hakukohderyhma-oid})
    (delete-by-hakukohderyhma-oid tx {:oid hakukohderyhma-oid})))

(defn find-settings-by-hakukohderyhma-oids
  [db hakukohderyhma-oids]
  (let [settings (->> {:hakukohderyhma-oids hakukohderyhma-oids}
                      (settings-by-hakukohderyhma-oids db)
                      (group-by :hakukohderyhma-oid))]
    (->> hakukohderyhma-oids
         (map (fn [hakukohderyhma-oid]
                (if-let [matching-settings (get settings hakukohderyhma-oid)]
                  (first matching-settings)
                  initial-settings))))))

(defn insert-or-update-settings
  [db hakukohderyhma-oid settings]
  (with-db-transaction [tx db]
    (let [rajaava (:rajaava settings)
          max-hakukohteet (:max-hakukohteet settings)
          jyemp (:jos-ylioppilastutkinto-ei-muita-pohjakoulutusliitepyyntoja settings)
          yo-amm-autom-hakukelpoisuus (:yo-amm-autom-hakukelpoisuus settings)]
      (upsert-settings! tx {:hakukohderyhma-oid hakukohderyhma-oid :rajaava rajaava :max-hakukohteet max-hakukohteet :jos-ylioppilastutkinto-ei-muita-pohjakoulutusliitepyyntoja jyemp :yo-amm-autom-hakukelpoisuus yo-amm-autom-hakukelpoisuus})
      (dissoc (first (find-settings-by-hakukohderyhma-oids tx [hakukohderyhma-oid])) :hakukohderyhma-oid))))

(defn get-hakukohderyhmat-by-hakukohteet [db hakukohde-oids]
  (grouped-hakukohderyhmas db {:hakukohde-oids hakukohde-oids}))
