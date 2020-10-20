(ns hakukohderyhmapalvelu.ohjausparametrit.haun-asetukset-ohjausparametrit-mapping)

(defn haun-asetus-key->ohjausparametri [haun-asetus-key]
  (case haun-asetus-key
    :haun-asetukset/hakukohteiden-maara-rajoitettu
    :hakutoiveidenMaaraRajoitettu

    :haun-asetukset/jarjestetyt-hakutoiveet
    :jarjestetytHakutoiveet

    :haun-asetukset/useita-hakemuksia
    :useitaHakemuksia))

(defn- boolean-value? [haun-asetus-key]
  (some #{haun-asetus-key}
        #{:haun-asetukset/hakukohteiden-maara-rajoitettu
          :haun-asetukset/jarjestetyt-hakutoiveet
          :haun-asetukset/useita-hakemuksia}))

(defn- useita-hakemuksia? [haun-asetus-key]
  (= haun-asetus-key :haun-asetukset/useita-hakemuksia))

(defn- value-map-fn [haun-asetus-key
                     mappings]
  (->> mappings
       (filter (fn [[pred]]
                 (pred haun-asetus-key)))
       (map (fn [[_ ->haun-asetus-value]]
              ->haun-asetus-value))
       (apply comp)))

(def ^:private ohjausparametri-value->haun-asetus-value-mappings
  [[useita-hakemuksia? not]
   [boolean-value? true?]
   [(constantly true) identity]])

(defn ohjausparametri-value->haun-asetus-value [ohjausparametri-value
                                                haun-asetus-key]
  (let [f (value-map-fn
            haun-asetus-key
            ohjausparametri-value->haun-asetus-value-mappings)]
    (f ohjausparametri-value)))

(def ^:private haun-asetus-value->ohjausparametri-value-mappings
  [[useita-hakemuksia? not]
   [(constantly true) identity]])

(defn haun-asetus-value->ohjausparametri-value [haun-asetus-value
                                                haun-asetus-key]
  (let [f (value-map-fn
            haun-asetus-key
            haun-asetus-value->ohjausparametri-value-mappings)]
    (f haun-asetus-value)))
