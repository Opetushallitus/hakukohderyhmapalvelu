(ns hakukohderyhmapalvelu.views
  (:require
    [re-frame.core :as re-frame]
    [hakukohderyhmapalvelu.subs.core-subs]
    [hakukohderyhmapalvelu.views.hakukohderyhmapalvelu-view :as h]))

(defn- panels [panel-name]
  (case panel-name
    :hakukohderyhmapalvelu-panel [h/hakukohderyhmapalvelu]
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [:active-panel])]
    [show-panel @active-panel]))
