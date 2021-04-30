(ns hakukohderyhmapalvelu.subs.hakukohderyhma-subs
  (:require [re-frame.core :as re-frame]
            [hakukohderyhmapalvelu.i18n.utils :as i18n-utils]
            [hakukohderyhmapalvelu.events.hakukohderyhmien-hallinta-events :as hakukohderyhma-evts]))


(def selected-hakukohderyhma-as-option :hakukohderyhmien-hallinta/get-currently-selected-hakukohderyhma-as-option)
(def saved-hakukohderyhmas-as-options :hakukohderyhmien-hallinta/get-saved-hakukohderyhma-names)
(def hakukohderyhman-hakukohteet-as-options :hakukohderyhmien-hallinta/hakukohteet-as-options)
(def hakukohderyhman-hakukohteet :hakukohderyhmien-hallinta/hakukohderyhman-hakukohteet)
(def selected-hakukohderyhmas-hakukohteet :hakukohderyhmien-hallinta/selected-hakukohderyhmas-hakukohteet)
(def selected-hakukohderyhma :hakukohderyhmien-hallinta/selected-hakukohderyhma)
(def is-loading-hakukohderyhmas :hakukohderyhmien-hallinta/is-loading-hakukohderyhmas)

(re-frame/reg-sub
  :hakukohderyhmien-hallinta/create-grid-visible?
  (fn []
    [(re-frame/subscribe [:state-query hakukohderyhma-evts/create-input-is-active false])])
  (fn [[visible?]]
    visible?))

(re-frame/reg-sub
  :hakukohderyhmien-hallinta/ongoing-request?
  (fn []
    [(re-frame/subscribe [:state-query [:requests hakukohderyhma-evts/hakukohderyhma-persisted]])])
  (fn [[ongoing-request?]]
    (some? ongoing-request?)))

(re-frame/reg-sub
  saved-hakukohderyhmas-as-options
  (fn []
    [(re-frame/subscribe [:lang])
     (re-frame/subscribe [:state-query hakukohderyhma-evts/persisted-hakukohderyhmas])])
  (fn [[lang saved-ryhmat]]
    (let [transform-fn (i18n-utils/create-item->option-transformer lang :nimi :oid)]
      (map transform-fn saved-ryhmat))))


(re-frame/reg-sub
  selected-hakukohderyhma
  (fn [db]
    (->> (get-in db hakukohderyhma-evts/persisted-hakukohderyhmas)
         (filter :is-selected)
         first)))

(re-frame/reg-sub
  selected-hakukohderyhma-as-option
  (fn []
    [(re-frame/subscribe [:lang])
     (re-frame/subscribe [selected-hakukohderyhma])])
  (fn [[lang selected-ryhma]]
    (when selected-ryhma
      (let [transform-fn (i18n-utils/create-item->option-transformer lang :nimi :oid)]
        (transform-fn selected-ryhma)))))

(re-frame/reg-sub
  hakukohderyhman-hakukohteet
  (fn []
    [(re-frame/subscribe [selected-hakukohderyhma])])
  (fn [[hakukohderyhma]]
    (:hakukohteet hakukohderyhma)))

(re-frame/reg-sub
  hakukohderyhman-hakukohteet-as-options
  (fn []
    [(re-frame/subscribe [:lang])
     (re-frame/subscribe [hakukohderyhman-hakukohteet])])
  (fn [[lang hakukohteet]]
    (let [transform-fn (i18n-utils/create-item->option-transformer lang :nimi :oid #(-> % :oikeusHakukohteeseen not))]
      (map transform-fn hakukohteet))))

(re-frame/reg-sub
  selected-hakukohderyhmas-hakukohteet
  (fn []
    [(re-frame/subscribe [hakukohderyhman-hakukohteet])])
  (fn [[hakukohteet]]
    (filter :is-selected hakukohteet)))

(re-frame/reg-sub
  is-loading-hakukohderyhmas
  (fn [db]
    (-> (:requests db)
        (:hakukohderyhmien-hallinta/get-all-hakukohderyhma)
        (boolean))))
