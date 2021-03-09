(ns hakukohderyhmapalvelu.subs.hakukohderyhma-create-subs
  (:require [re-frame.core :as re-frame]
            [hakukohderyhmapalvelu.events.hakukohderyhmien-hallinta-events :refer [persisted-hakukohderyhmas
                                                                                   selected-hakukohderyhma
                                                                                   create-hakukohderyhma-is-visible]]))

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
    {:label       (get-with-fallback localized order)
     :value       value
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
    value-field))                                           ;TODO: siirrä keskitettyyn paikkaan (copypastettu hakukohderyhmapalvelu.subs.haku-subs :sta)

(def get-saved-hakukohderyhmas-as-options :hakukohderyhmien-hallinta/get-saved-hakukohderyhma-names)
(def get-currently-selected-hakukohderyhma :hakukohderyhmien-hallinta/get-currently-selected-hakukohderyhma-name)

(re-frame/reg-sub
  :hakukohderyhmien-hallinta/create-grid-visible?
  (fn []
    [(re-frame/subscribe [:state-query create-hakukohderyhma-is-visible false])])
  (fn [[visible?]]
    visible?))

(re-frame/reg-sub
  :hakukohderyhmien-hallinta/ongoing-request?
  (fn []
    [(re-frame/subscribe [:state-query [:requests :hakukohderyhmien-hallinta/save-hakukohderyhma]])])
  (fn [[ongoing-request?]]
    (some? ongoing-request?)))

(re-frame/reg-sub
  get-saved-hakukohderyhmas-as-options
  (fn []
    [(re-frame/subscribe [:lang])
     (re-frame/subscribe [:state-query persisted-hakukohderyhmas])])
  (fn [[lang saved-ryhmat]]
    (let [transform-fn (create-item->option-transformer lang :nimi :oid)]
      (map transform-fn saved-ryhmat))))

(re-frame/reg-sub
  get-currently-selected-hakukohderyhma
  (fn []
    [(re-frame/subscribe [:lang])
     (re-frame/subscribe [:state-query selected-hakukohderyhma])])
  (fn [[lang selected-ryhma]]
    (let [transform-fn (create-item->option-transformer lang :nimi :oid)]
      (transform-fn selected-ryhma))))
