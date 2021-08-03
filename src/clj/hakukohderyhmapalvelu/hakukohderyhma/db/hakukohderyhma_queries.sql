-- :name hakukohteet-by-hakukohderyhma-oid :? :*
SELECT hakukohde_oid AS "hakukohde-oid"
FROM hakukohderyhma
WHERE hakukohderyhma_oid = :oid

-- :name hakukohderyhma-oids-by-hakukohde-oid :? :*
SELECT DISTINCT hakukohderyhma_oid AS "hakukohderyhma-oid"
    FROM hakukohderyhma
    WHERE hakukohde_oid = :hakukohde-oid
    ORDER BY hakukohderyhma_oid

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
ORDER BY m.hakukohderyhma_oid
;

-- :name delete-hakukohteet-from-hakukohderyhma! :! :n
DELETE FROM hakukohderyhma WHERE hakukohderyhma_oid = :oid
AND hakukohde_oid IN (:v*:hakukohteet)

-- :name add-hakukohteet-into-hakukohderyhma! :! :n
INSERT INTO hakukohderyhma (hakukohderyhma_oid, hakukohde_oid)
VALUES :tuple*:hakukohderyhmat

-- :name delete-by-hakukohderyhma-oid :! :n
DELETE FROM hakukohderyhma WHERE hakukohderyhma_oid = :oid

-- :name delete-settings-by-hakukohderyhma-oid :! :n
DELETE FROM hakukohderyhma_settings WHERE hakukohderyhma_oid = :oid

-- :name settings-by-hakukohderyhma-oids :? :*
SELECT  s.rajaava,
        s.hakukohderyhma_oid AS "hakukohderyhma-oid",
        s.max_hakukohteet AS "max-hakukohteet"
FROM hakukohderyhma_settings s
WHERE s.hakukohderyhma_oid IN (:v*:hakukohderyhma-oids);

-- :name upsert-settings! :! :n
INSERT INTO hakukohderyhma_settings (hakukohderyhma_oid, rajaava, max_hakukohteet)
VALUES (:hakukohderyhma-oid, :rajaava, :max-hakukohteet)
ON CONFLICT (hakukohderyhma_oid)
DO
    UPDATE SET rajaava = :rajaava, max_hakukohteet = :max-hakukohteet;

-- :name grouped-hakukohderyhmas :? :*
SELECT
    hakukohde_oid AS "oid",
    array_agg(hakukohderyhma_oid) AS "hakukohderyhmat"
FROM hakukohderyhma
WHERE hakukohde_oid IN (:v*:hakukohde-oids)
GROUP BY hakukohde_oid;
