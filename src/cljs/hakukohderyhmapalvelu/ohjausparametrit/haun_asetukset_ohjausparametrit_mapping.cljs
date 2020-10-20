(ns hakukohderyhmapalvelu.ohjausparametrit.haun-asetukset-ohjausparametrit-mapping)

(defn haun-asetus-key->ohjausparametri [haun-asetus-key]
  (case haun-asetus-key
    :haun-asetukset/hakukohteiden-maara-rajoitus
    :hakukohteidenMaaraRajoitus

    :haun-asetukset/hakukohteiden-priorisointi
    :hakukohteidenPriorisointi

    :haun-asetukset/vain-yksi-hakemus-rajoitus
    :vainYksiHakemusRajoitus))

(defn ohjausparametri-value->haun-asetus-value [ohjausparametri-value
                                                haun-asetus-key]
  (cond-> ohjausparametri-value
          (some #{haun-asetus-key}
                #{:haun-asetukset/hakukohteiden-maara-rajoitus
                  :haun-asetukset/hakukohteiden-priorisointi
                  :haun-asetukset/vain-yksi-hakemus-rajoitus})
          true?))
