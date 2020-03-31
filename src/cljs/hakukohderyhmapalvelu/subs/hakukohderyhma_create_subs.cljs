(ns hakukohderyhmapalvelu.subs.hakukohderyhma-create-subs
  (:require [re-frame.core :as re-frame]))


(re-frame/reg-sub
  :hakukohderyhma-create/create-grid-visible?
  (fn []
    [(re-frame/subscribe [:state-query [:ui :create-hakukohderyhma-grid :visible?] false])])
  (fn [[visible?]]
    visible?))

(re-frame/reg-sub
  :hakukohderyhma-create/hakukohderyhma-name
  (fn []
    [(re-frame/subscribe [:state-query [:ui :create-hakukohderyhma-grid :hakukohderyhma-name]])])
  (fn [[hakukohderyhma-name]]
    hakukohderyhma-name))

(re-frame/reg-sub
  :hakukohderyhma-create/save-button-enabled?
  (fn []
    [(re-frame/subscribe [:state-query [:requests :hakukohderyhma-create/save-hakukohderyhma] false])
     (re-frame/subscribe [:hakukohderyhma-create/hakukohderyhma-name])])
  (fn [[ongoing-request? hakukohderyhma-name]]
    (and (not ongoing-request?)
         (seq hakukohderyhma-name))))
