(ns hakukohderyhmapalvelu.subs.panel-menu-subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
  :panel-menu/active-panel
  (fn [db _]
    (:active-panel db)))
