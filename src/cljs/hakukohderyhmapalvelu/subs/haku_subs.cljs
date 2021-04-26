(ns hakukohderyhmapalvelu.subs.haku-subs
  (:require [re-frame.core :as re-frame]
            [hakukohderyhmapalvelu.i18n.utils :as i18n-utils]
            [hakukohderyhmapalvelu.events.haku-events :as haku-events]
            [hakukohderyhmapalvelu.haku-utils :as u]
            [hakukohderyhmapalvelu.subs.hakukohderyhma-subs :as hakukohderyhma-subs]))

;; Tilaukset
(def haku-haut :haku/haut)
(def haku-haut-as-options :haku/haut-as-options)
(def haku-is-loading :haku/is-loading)
(def haku-selected-haku :haku/selected-haku)
(def haku-selected-haku-as-option :haku-selected-haku-as-option)
(def haku-hakukohteet :haku/hakukohteet)
(def haku-hakukohteet-filter :haku/hakukohteet-filter)
(def haku-hakukohteet-is-empty :haku/hakukohteet-is-empty)
(def haku-hakukohteet-as-options :haku/hakukohteet-as-options)
(def haku-selected-hakukohteet :haku/selected-hakukohteet)
(def haku-hakukohteet-not-in-hakukohderyhma :haku/haku-hakukohteet-not-in-hakukohderyhma)

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
    (let [transform-fn (i18n-utils/create-item->option-transformer lang :nimi :oid)]
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
    (let [transform-fn (i18n-utils/create-item->option-transformer lang :nimi :oid)]
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
  haku-hakukohteet-is-empty
  (fn []
    [(re-frame/subscribe [haku-hakukohteet])])
  (fn [[hakukohteet] _]
    (empty? hakukohteet)))

(re-frame/reg-sub
  haku-hakukohteet-filter
  (fn [db _]
    (get-in db haku-events/haku-hakukohteet-filter)))


(re-frame/reg-sub
  haku-hakukohteet-not-in-hakukohderyhma
  (fn []
    [(re-frame/subscribe [haku-hakukohteet])
     (re-frame/subscribe [hakukohderyhma-subs/hakukohderyhman-hakukohteet])])
  (fn [[hakukohteet hakukohderyhman-hakukohteet]]
    (let [hakukohderyhman-hakukohteet-oids (set (map :oid hakukohderyhman-hakukohteet))]
      (remove #(hakukohderyhman-hakukohteet-oids (:oid %)) hakukohteet))))

(re-frame/reg-sub
  haku-hakukohteet-as-options
  (fn []
    [(re-frame/subscribe [:lang])
     (re-frame/subscribe [haku-hakukohteet-filter])
     (re-frame/subscribe [haku-hakukohteet-not-in-hakukohderyhma])])
  (fn [[lang filter-text hakukohteet] _]
    (let [transform-fn (i18n-utils/create-item->option-transformer lang :nimi :oid #(-> % :oikeusHakukohteeseen not))]
      (->> hakukohteet
           (filter #(u/hakukohde-includes-string? % filter-text lang))
           (map transform-fn)
           (remove :is-disabled)))))

(re-frame/reg-sub
  haku-selected-hakukohteet
  (fn []
    [(re-frame/subscribe [haku-hakukohteet-not-in-hakukohderyhma])])
  (fn [[hakukohteet]]
    (filter :is-selected hakukohteet)))
