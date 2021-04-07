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

(defn hakukohde-oidit-by-hakukohderyhma-oid [db hakukohderyhma-oid]
  (->> {:oid hakukohderyhma-oid}
       (hakukohteet-by-hakukohderyhma-oid db)
       (map :hakukohde-oid)))

(defn update-hakukohderyhma-hakukohteet! [db hakukohderyhma-oid hakukohteet]
  (with-db-transaction [tx db]
                       (let [incoming (map :oid hakukohteet)
                             current (hakukohde-oidit-by-hakukohderyhma-oid db hakukohderyhma-oid)
                             [to-delete to-add _] (diff current incoming)
                             to-delete' (remove nil? to-delete)
                             hakukohteet-to-add-tuple (->> (remove nil? to-add)
                                                           (map (fn [hk-oid] [hakukohderyhma-oid hk-oid])))]
                         (when-not (empty? to-delete')
                           (delete-hakukohteet-from-hakukohderyhma! tx {:oid         hakukohderyhma-oid
                                                                        :hakukohteet to-delete'}))
                         (when-not (empty? hakukohteet-to-add-tuple)
                           (add-hakukohteet-into-hakukohderyhma! tx {:hakukohderyhmat hakukohteet-to-add-tuple})))))

(defn hakukohderyhmat-by-hakukohteet-and-hakukohderyhmat [db hakukohderyhma-oids hakukohde-oids]
  (hakukohderyhma-by-hakukohteet-and-hakukohderyhmat db {:hakukohderyhmat hakukohderyhma-oids
                                                         :hakukohteet     hakukohde-oids}))

(defn get-hakukohderyhma-oids-by-hakukohde-oid [db hakukohde-oid]
  (->> {:hakukohde-oid hakukohde-oid}
       (hakukohderyhma-oids-by-hakukohde-oid db)
       (mapv :hakukohderyhma-oid)))
