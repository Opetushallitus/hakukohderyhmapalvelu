(ns hakukohderyhmapalvelu.ohjausparametrit.haun-asetukset-ohjausparametrit-mapping
  (:require [hakukohderyhmapalvelu.dates.date-parser :as d]))

(defn haun-asetus-key->ohjausparametri [haun-asetus-key]
  (case haun-asetus-key
    :haun-asetukset/hakukohteiden-maara-rajoitettu
    :hakutoiveidenMaaraRajoitettu

    :haun-asetukset/hakukohteiden-maara-rajoitus
    :hakutoiveidenEnimmaismaara

    :haun-asetukset/jarjestetyt-hakutoiveet
    :jarjestetytHakutoiveet

    :haun-asetukset/useita-hakemuksia
    :useitaHakemuksia

    :haun-asetukset/sijoittelu
    :sijoittelu

    :haun-asetukset/valintatulokset-valmiina-viimeistaan
    :PH_VTSSV))

(defn- parse-int [value]
  (.parseInt js/Number value 10))

(defn- boolean-value? [haun-asetus-key _]
  (some #{haun-asetus-key}
        #{:haun-asetukset/hakukohteiden-maara-rajoitettu
          :haun-asetukset/jarjestetyt-hakutoiveet
          :haun-asetukset/useita-hakemuksia
          :haun-asetukset/sijoittelu}))

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

(defn- date-value? [haun-asetus-key _]
  (= haun-asetus-key :haun-asetukset/valintatulokset-valmiina-viimeistaan))

(defn- local-date->long [date]
  (let [date' (-> date
                  d/iso-date-time-local-str->date
                  d/date->long)]
    {:date date'}))

(defn- long->date [ohjausparametrit-date]
  (some-> ohjausparametrit-date :date d/long->date))

(def ^:private ohjausparametri-value->haun-asetus-value-mappings
  [[date-value? long->date]
   [useita-hakemuksia? not]
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
  [[date-value? local-date->long]
   [>0-number-value? parse-int]
   [useita-hakemuksia? not]
   [(constantly true) identity]])

(defn haun-asetus-value->ohjausparametri-value [haun-asetus-value
                                                haun-asetus-key]
  (let [f (value-map-fn
            haun-asetus-key
            haun-asetus-value
            haun-asetus-value->ohjausparametri-value-mappings)]
    (f haun-asetus-value)))
