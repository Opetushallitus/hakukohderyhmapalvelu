-- :name hakukohteet-by-hakukohderyhma-oid :? :*
SELECT hakukohde_oid AS "hakukohde-oid" FROM hakukohderyhma WHERE hakukohderyhma_oid = :oid

-- :name hakukohderyhma-by-hakukohteet-and-hakukohderyhmat :? :*
WITH hakukohderyhma_oid_table AS (
    SELECT *
    FROM jsonb_array_elements_text(:hakukohderyhmat)
),
     matching_hakukohderyhma AS (
         SELECT DISTINCT(h2.value) AS hakukohderyhma_oid
         FROM hakukohderyhma_oid_table h2
                  LEFT JOIN hakukohderyhma h ON h.hakukohderyhma_oid = h2.value
         WHERE
--~ (if (empty? (:hakukohteet params)) "FALSE" "hakukohde_oid IN (:v*:hakukohteet)")
            OR hakukohde_oid IS NULL
     )
SELECT m.hakukohderyhma_oid                         AS "hakukohderyhma-oid",
       array_remove(array_agg(hakukohde_oid), NULL) AS "hakukohde-oids"
FROM matching_hakukohderyhma m
         LEFT JOIN hakukohderyhma h ON m.hakukohderyhma_oid = h.hakukohderyhma_oid
GROUP BY m.hakukohderyhma_oid
;

-- :name delete-hakukohteet-from-hakukohderyhma! :! :n
DELETE FROM hakukohderyhma WHERE hakukohderyhma_oid = :oid
AND hakukohde_oid IN (:v*:hakukohteet)

-- :name add-hakukohteet-into-hakukohderyhma! :! :n
INSERT INTO hakukohderyhma (hakukohderyhma_oid, hakukohde_oid)
VALUES :tuple*:hakukohderyhmat
