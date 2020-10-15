(ns hakukohderyhmapalvelu.subs.panel-subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
  :panel/active-panel
  (fn [db _]
    (:active-panel db)))
