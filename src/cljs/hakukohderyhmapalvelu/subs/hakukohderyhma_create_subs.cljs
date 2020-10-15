(ns hakukohderyhmapalvelu.subs.hakukohderyhma-create-subs
  (:require [re-frame.core :as re-frame]))


(re-frame/reg-sub
  :hakukohderyhmien-hallinta/create-grid-visible?
  (fn []
    [(re-frame/subscribe [:state-query [:create-hakukohderyhma :visible?] false])])
  (fn [[visible?]]
    visible?))

(re-frame/reg-sub
  :hakukohderyhmien-hallinta/ongoing-request?
  (fn []
    [(re-frame/subscribe [:state-query [:requests :hakukohderyhmien-hallinta/save-hakukohderyhma]])])
  (fn [[ongoing-request?]]
    (some? ongoing-request?)))
