(ns hakukohderyhmapalvelu.subs.hakukohderyhma-create-subs
  (:require [re-frame.core :as re-frame]))


(re-frame/reg-sub
  :hakukohderyhma-create/create-grid-visible?
  (fn []
    [(re-frame/subscribe [:state-query [:ui :create-hakukohderyhma-grid :visible?] false])])
  (fn [[visible?]]
    visible?))
