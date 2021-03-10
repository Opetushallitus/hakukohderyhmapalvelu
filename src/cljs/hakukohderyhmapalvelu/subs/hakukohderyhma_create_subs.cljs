(ns hakukohderyhmapalvelu.subs.hakukohderyhma-create-subs
  (:require [re-frame.core :as re-frame]
            [hakukohderyhmapalvelu.i18n.utils :as i18n-utils]
            [hakukohderyhmapalvelu.events.hakukohderyhmien-hallinta-events :refer [persisted-hakukohderyhmas
                                                                                   selected-hakukohderyhma
                                                                                   create-hakukohderyhma-is-visible]]))


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
    (let [transform-fn (i18n-utils/create-item->option-transformer lang :nimi :oid)]
      (map transform-fn saved-ryhmat))))

(re-frame/reg-sub
  get-currently-selected-hakukohderyhma
  (fn []
    [(re-frame/subscribe [:lang])
     (re-frame/subscribe [:state-query selected-hakukohderyhma])])
  (fn [[lang selected-ryhma]]
    (let [transform-fn (i18n-utils/create-item->option-transformer lang :nimi :oid)]
      (transform-fn selected-ryhma))))
