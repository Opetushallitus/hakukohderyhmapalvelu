(ns hakukohderyhmapalvelu.subs.hakukohderyhma-subs
  (:require [re-frame.core :as re-frame]
            [hakukohderyhmapalvelu.i18n.utils :as i18n-utils]
            [hakukohderyhmapalvelu.events.hakukohderyhmien-hallinta-events :as hakukohderyhma-evts]
            [hakukohderyhmapalvelu.components.common.material-icons :as icon]))


(def selected-hakukohderyhma-as-option :hakukohderyhmien-hallinta/get-currently-selected-hakukohderyhma-as-option)
(def saved-hakukohderyhmas-as-options :hakukohderyhmien-hallinta/get-saved-hakukohderyhma-names)
(def hakukohderyhman-hakukohteet-as-options :hakukohderyhmien-hallinta/hakukohteet-as-options)
(def hakukohderyhman-hakukohteet :hakukohderyhmien-hallinta/hakukohderyhman-hakukohteet)
(def hakukohderyhman-hakukohteet-prioriteettijarjestyksessa :hakukohderyhmien-hallinta/hakukohderyhman-hakukohteet-prioriteettijarjestyksessa)
(def selected-hakukohderyhmas-hakukohteet :hakukohderyhmien-hallinta/selected-hakukohderyhmas-hakukohteet)
(def selected-hakukohderyhma :hakukohderyhmien-hallinta/selected-hakukohderyhma)
(def is-loading-hakukohderyhmas :hakukohderyhmien-hallinta/is-loading-hakukohderyhmas)
(def hakukohderyhma-is-priorisoiva :hakukohderyhmien-hallinta/hakukohderyhma-is-priorisoiva)

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
    (let [transform-fn (i18n-utils/create-item->option-transformer lang {:label [:nimi]} :oid)]
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
      (let [transform-fn (i18n-utils/create-item->option-transformer lang {:label [:nimi]} :oid)]
        (transform-fn selected-ryhma)))))

(re-frame/reg-sub
  hakukohderyhman-hakukohteet-prioriteettijarjestyksessa
  (fn []
    [(re-frame/subscribe [selected-hakukohderyhma])])
  (fn [[hakukohderyhma]]
    (let [hakukohteet (:hakukohteet hakukohderyhma)
          priorisointi?  (get-in hakukohderyhma [:settings :priorisoiva])
          prioriteettijarjestys (vec (get-in hakukohderyhma [:settings :prioriteettijarjestys]))
          result (if priorisointi? (vec (sort-by #(.indexOf prioriteettijarjestys (:oid %)) hakukohteet)) hakukohteet)]
      result)))

(re-frame/reg-sub
  hakukohderyhman-hakukohteet
  (fn []
    [(re-frame/subscribe [selected-hakukohderyhma])])
  (fn [[hakukohderyhma]]
    (:hakukohteet hakukohderyhma)))

(re-frame/reg-sub
  hakukohderyhma-is-priorisoiva
  (fn []
    [(re-frame/subscribe [selected-hakukohderyhma])])
  (fn [[hakukohderyhma]]
    (get-in hakukohderyhma [:settings :priorisoiva])))

(re-frame/reg-sub
  hakukohderyhman-hakukohteet-as-options
  (fn []
    [(re-frame/subscribe [:lang])
     (re-frame/subscribe [hakukohderyhman-hakukohteet-prioriteettijarjestyksessa])
     (re-frame/subscribe [hakukohderyhma-is-priorisoiva])])
  (fn [[lang hakukohteet priorisoiva?]]
    (let [labels {:label     [:nimi]
                  :sub-label [:tarjoaja :nimi]}
          transform-fn (i18n-utils/create-item->option-transformer lang labels :oid #(-> % :oikeusHakukohteeseen not))
          add-icon (fn [option hakukohde] (if (= "arkistoitu" (:tila hakukohde))
                                            (assoc option :icon icon/archived)
                                            option))
          transform-and-add-icon (fn [hakukohde] (-> hakukohde
                                                     (transform-fn)
                                                     (assoc :priorisointi priorisoiva?)
                                                     (add-icon hakukohde)))]
      (map transform-and-add-icon hakukohteet))))

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
