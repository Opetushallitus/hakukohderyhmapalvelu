(ns hakukohderyhmapalvelu.ohjausparametrit.haun-asetukset-ohjausparametrit-mapping)

(defn haun-asetus-key->ohjausparametri [haun-asetus-key]
  (case haun-asetus-key
    :haun-asetukset/hakukohteiden-maara-rajoitettu
    :hakutoiveidenMaaraRajoitettu

    :haun-asetukset/jarjestetyt-hakutoiveet
    :jarjestetytHakutoiveet

    :haun-asetukset/vain-yksi-hakemus-rajoitus
    :vainYksiHakemusRajoitus))

(defn ohjausparametri-value->haun-asetus-value [ohjausparametri-value
                                                haun-asetus-key]
  (cond-> ohjausparametri-value
          (some #{haun-asetus-key}
                #{:haun-asetukset/hakukohteiden-maara-rajoitettu
                  :haun-asetukset/jarjestetyt-hakutoiveet
                  :haun-asetukset/vain-yksi-hakemus-rajoitus})
          true?))
