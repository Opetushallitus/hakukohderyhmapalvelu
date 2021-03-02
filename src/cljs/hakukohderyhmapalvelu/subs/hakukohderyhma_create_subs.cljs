(ns hakukohderyhmapalvelu.subs.hakukohderyhma-create-subs
  (:require [re-frame.core :as re-frame]
            [hakukohderyhmapalvelu.events.hakukohderyhmien-hallinta-events :refer [persisted-hakukohderyhmas
                                                                                   selected-hakukohderyhma
                                                                                   create-hakukohderyhma-is-visible]]))


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
  :hakukohderyhmien-hallinta/get-currently-saved-hakukohderyhmas
  (fn []
    [(re-frame/subscribe [:state-query persisted-hakukohderyhmas])])
  (fn [[saved-ryhmat]]
    saved-ryhmat))

(re-frame/reg-sub
  :hakukohderyhmien-hallinta/get-currently-selected-hakukohderyhma
  (fn []
    [(re-frame/subscribe [:state-query selected-hakukohderyhma])])
  (fn [[selected-ryhma]]
    selected-ryhma))
