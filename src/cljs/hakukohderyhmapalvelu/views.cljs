(ns hakukohderyhmapalvelu.views
  (:require [hakukohderyhmapalvelu.views.hakukohderyhmapalvelu-panel :as h]
            [re-frame.core :as re-frame]))

(defn- panels [panel-name]
  (case panel-name
    :hakukohderyhmapalvelu-panel [h/hakukohderyhmapalvelu-panel]
    [:div]))

(defn- show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  [:div
   (let [active-panel (re-frame/subscribe [:active-panel])]
     [show-panel @active-panel])])
