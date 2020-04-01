(ns hakukohderyhmapalvelu.subs.hakukohderyhma-create-subs
  (:require [re-frame.core :as re-frame]))


(re-frame/reg-sub
  :hakukohderyhma-create/create-grid-visible?
  (fn []
    [(re-frame/subscribe [:state-query [:create-hakukohderyhma :visible?] false])])
  (fn [[visible?]]
    visible?))

(re-frame/reg-sub
  :hakukohderyhma-create/ongoing-request?
  (fn []
    [(re-frame/subscribe [:state-query [:requests :hakukohderyhma-create/save-hakukohderyhma]])])
  (fn [[ongoing-request?]]
    (some? ongoing-request?)))
