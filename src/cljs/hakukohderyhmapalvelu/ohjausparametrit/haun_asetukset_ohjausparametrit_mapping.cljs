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

    :haun-asetukset/hakijakohtainen-paikan-vastaanottoaika
    :PH_HPVOA

    :haun-asetukset/paikan-vastaanotto-paattyy
    :PH_OPVP

    :haun-asetukset/hakukierros-paattyy
    :PH_HKP

    :haun-asetukset/sijoittelu
    :sijoittelu

    :haun-asetukset/valintatulokset-valmiina-viimeistaan
    :PH_VTSSV

    :haun-asetukset/varasijasaannot-astuvat-voimaan
    :PH_VSSAV

    :haun-asetukset/varasijataytto-paattyy
    :PH_VSTP

    :haun-asetukset/valintatulosten-julkaiseminen-hakijoille
    :PH_VTJH

    :haun-asetukset/liitteiden-muokkauksen-takaraja
    :PH_LMT

    :haun-asetukset/ilmoittautuminen-paattyy
    :PH_IP

    :haun-asetukset/automaattinen-hakukelpoisuus-paattyy
    :PH_AHP

    :haun-asetukset/harkinnanvaraisen-valinnan-paatosten-tallennus-paattyy
    :PH_HVVPTP

    :haun-asetukset/oppilaitosten-virkailijoiden-valintapalvelun-kaytto-estetty
    :PH_OLVVPKE

    :haun-asetukset/valintaesityksen-hyvaksyminen
    :PH_VEH

    :haun-asetukset/koetulosten-tallentaminen
    :PH_KTT))

(defn- parse-int [value]
  (let [i (.parseInt js/Number value 10)]
    (when-not (.isNaN js/Number i)
      i)))

(defn- boolean-value? [haun-asetus-key _]
  (some #{haun-asetus-key}
        #{:haun-asetukset/hakukohteiden-maara-rajoitettu
          :haun-asetukset/jarjestetyt-hakutoiveet
          :haun-asetukset/useita-hakemuksia
          :haun-asetukset/sijoittelu}))

(defn- int-value? [haun-asetus-key _]
  (= haun-asetus-key :haun-asetukset/hakijakohtainen-paikan-vastaanottoaika))

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
  (some #{haun-asetus-key}
        #{:haun-asetukset/paikan-vastaanotto-paattyy
          :haun-asetukset/hakukierros-paattyy
          :haun-asetukset/valintatulokset-valmiina-viimeistaan
          :haun-asetukset/varasijasaannot-astuvat-voimaan
          :haun-asetukset/varasijataytto-paattyy}))

(defn- local-date->long [date]
  (when-not (empty? date)
    (let [date' (-> date
                    d/iso-date-time-local-str->date
                    d/date->long)]
      {:date date'})))

(defn- long->date [ohjausparametrit-date]
  (some-> ohjausparametrit-date :date d/long->date))

(defn- string->int-value [s]
  (when-not (empty? s)
    {:value (parse-int s)}))

(defn- int-value->string [int-value]
  (when-let [value (:value int-value)]
    (str value)))

(def ^:private ohjausparametri-value->haun-asetus-value-mappings
  [[date-value? long->date]
   [>0-number-value? str]
   [useita-hakemuksia? not]
   [boolean-value? true?]
   [int-value? int-value->string]
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
   [int-value? string->int-value]
   [(constantly true) identity]])

(defn haun-asetus-value->ohjausparametri-value [haun-asetus-value
                                                haun-asetus-key]
  (let [f (value-map-fn
            haun-asetus-key
            haun-asetus-value
            haun-asetus-value->ohjausparametri-value-mappings)]
    (f haun-asetus-value)))

(defn clear-keys-on-empty-value [haun-asetus-key]
  (case haun-asetus-key
    :haun-asetukset/sijoittelu
    [:haun-asetukset/valintatulokset-valmiina-viimeistaan
     :haun-asetukset/varasijasaannot-astuvat-voimaan
     :haun-asetukset/varasijataytto-paattyy]
    :haun-asetukset/hakukohteiden-maara-rajoitettu
    [:haun-asetukset/hakukohteiden-maara-rajoitus]
    []))
