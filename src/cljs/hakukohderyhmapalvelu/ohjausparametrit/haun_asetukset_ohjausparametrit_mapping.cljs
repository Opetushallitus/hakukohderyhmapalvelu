(ns hakukohderyhmapalvelu.ohjausparametrit.haun-asetukset-ohjausparametrit-mapping)

(defn haun-asetus-key->ohjausparametri [haun-asetus-key]
  (case haun-asetus-key
    :haun-asetukset/hakukohteiden-maara-rajoitettu
    :hakutoiveidenMaaraRajoitettu

    :haun-asetukset/hakukohteiden-maara-rajoitus
    :hakutoiveidenEnimmaismaara

    :haun-asetukset/jarjestetyt-hakutoiveet
    :jarjestetytHakutoiveet

    :haun-asetukset/useita-hakemuksia
    :useitaHakemuksia))

(defn- parse-int [value]
  (.parseInt js/Number value 10))

(defn- boolean-value? [haun-asetus-key _]
  (some #{haun-asetus-key}
        #{:haun-asetukset/hakukohteiden-maara-rajoitettu
          :haun-asetukset/jarjestetyt-hakutoiveet
          :haun-asetukset/useita-hakemuksia}))

(defn- >0-number-value? [haun-asetus-key value]
  (and (= haun-asetus-key :haun-asetukset/hakukohteiden-maara-rajoitus)
       (> (parse-int value) 0)))

(defn- useita-hakemuksia? [haun-asetus-key _]
  (= haun-asetus-key :haun-asetukset/useita-hakemuksia))

(defn- value-map-fn [haun-asetus-key
                     value
                     mappings]
  (->> mappings
       (filter (fn [[pred]]
                 (pred haun-asetus-key value)))
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
            ohjausparametri-value
            ohjausparametri-value->haun-asetus-value-mappings)]
    (f ohjausparametri-value)))

(def ^:private haun-asetus-value->ohjausparametri-value-mappings
  [[>0-number-value? parse-int]
   [useita-hakemuksia? not]
   [(constantly true) identity]])

(defn haun-asetus-value->ohjausparametri-value [haun-asetus-value
                                                haun-asetus-key]
  (let [f (value-map-fn
            haun-asetus-key
            haun-asetus-value
            haun-asetus-value->ohjausparametri-value-mappings)]
    (f haun-asetus-value)))
