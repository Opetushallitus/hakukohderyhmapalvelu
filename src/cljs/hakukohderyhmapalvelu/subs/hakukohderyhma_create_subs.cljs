(ns hakukohderyhmapalvelu.subs.hakukohderyhma-create-subs
  (:require [re-frame.core :as re-frame]
            [hakukohderyhmapalvelu.events.hakukohderyhmien-hallinta-events :refer [persisted-hakukohderyhmas
                                                                                   selected-hakukohderyhma-name
                                                                                   create-hakukohderyhma-is-visible]]))

(def get-saved-hakukohderyhma-names :hakukohderyhmien-hallinta/get-saved-hakukohderyhma-names)
(def get-currently-selected-hakukohderyhma-name :hakukohderyhmien-hallinta/get-currently-selected-hakukohderyhma-name)

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
  get-saved-hakukohderyhma-names
  (fn []
    [(re-frame/subscribe [:state-query persisted-hakukohderyhmas])])
  (fn [[saved-ryhmat]]
    (mapv :name saved-ryhmat)))

(re-frame/reg-sub
  get-currently-selected-hakukohderyhma-name
  (fn []
    [(re-frame/subscribe [:state-query selected-hakukohderyhma-name])])
  (fn [[selected-ryhma]]
    selected-ryhma))
