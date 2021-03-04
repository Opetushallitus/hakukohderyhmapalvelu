(ns hakukohderyhmapalvelu.subs.haku-subs
  (:require [re-frame.core :as re-frame]
            [hakukohderyhmapalvelu.events.haku-events :as haku-events]))

;; Oletusjärjestys, jolla haetaan kielistettyä arvoa eri kielillä
(def fi-order [:fi :sv :en])
(def sv-order [:sv :fi :en])
(def en-order [:en :fi :sv])

(defn- get-with-fallback [m order]
  (->> (map #(get m %) order)
       (remove nil?)
       first))

(defn- item->option [order label-field value-field item]
  (let [localized (get item label-field)
        value (get item value-field)
        is-selected (get item :is-selected)]
    {:label (get-with-fallback localized order)
     :value value
     :is-selected is-selected}))

(defn- order-for-lang [lang]
  (case lang
    :fi fi-order
    :sv sv-order
    :en en-order
    :default fi-order))

(defn- create-item->option-transformer [lang label-field value-field]
  (partial
    item->option
    (order-for-lang lang)
    label-field
    value-field))

;; Tilaukset
(def haku-haut :haku/haut)
(def haku-haut-as-options :haku/haut-as-options)
(def haku-is-loading :haku/is-loading)
(def haku-selected-haku :haku/selected-haku)
(def haku-selected-haku-as-option :haku-selected-haku-as-option)
(def haku-hakukohteet :haku/hakukohteet)
(def haku-hakukohteet-as-options :haku/hakukohteet-as-options)

(re-frame/reg-sub
  haku-haut
  (fn [db _]
    (get-in db haku-events/haku-haut)))

(re-frame/reg-sub
  haku-haut-as-options
  (fn []
    [(re-frame/subscribe [:lang])
     (re-frame/subscribe [haku-haut])])
  (fn [[lang haut] _]
    (let [transform-fn (create-item->option-transformer lang :nimi :oid)]
      (map transform-fn haut))))

(re-frame/reg-sub
  haku-selected-haku
  (fn []
    [(re-frame/subscribe [haku-haut])])
  (fn [[haut] _]
    (some #(when (:is-selected %) %) haut)))

(re-frame/reg-sub
  haku-selected-haku-as-option
  (fn []
    [(re-frame/subscribe [:lang])
     (re-frame/subscribe [haku-selected-haku])])
  (fn [[lang selected-haku] _]
    (let [transform-fn (create-item->option-transformer lang :nimi :oid)]
      (transform-fn selected-haku))))

(re-frame/reg-sub
  haku-is-loading
  (fn [db _]
    (let [requests (:requests db)]
      (contains? requests haku-events/get-haut))))

(re-frame/reg-sub
  haku-hakukohteet
  (fn []
    [(re-frame/subscribe [haku-selected-haku])])
  (fn [[selected-haku] _]
    (:hakukohteet selected-haku)))

(re-frame/reg-sub
  haku-hakukohteet-as-options
  (fn []
    [(re-frame/subscribe [:lang])
     (re-frame/subscribe [haku-hakukohteet])])
  (fn [[lang hakukohteet] _]
    (let [transform-fn (create-item->option-transformer lang :nimi :oid)]
      (map transform-fn hakukohteet))))
