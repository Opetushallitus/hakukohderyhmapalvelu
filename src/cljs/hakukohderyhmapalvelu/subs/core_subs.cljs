(ns hakukohderyhmapalvelu.subs.core-subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
  :active-panel
  (fn [db _]
    (:active-panel db)))

(re-frame/reg-sub
  :state-query
  (fn [db [_ path default]]
    (get-in db path default)))
