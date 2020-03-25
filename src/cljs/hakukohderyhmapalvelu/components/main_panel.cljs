(ns hakukohderyhmapalvelu.components.main-panel
  (:require [hakukohderyhmapalvelu.views.hakukohderyhmapalvelu-view :as h]
            [re-frame.core :as re-frame]
            [stylefy.core :as stylefy]))

(def ^:private main-panel-style
  {:font-family "'Roboto', sans-serif"})

(defn- panels [panel-name]
  (case panel-name
    :hakukohderyhmapalvelu-panel [h/hakukohderyhmapalvelu]
    [:div]))

(defn- show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  [:div (stylefy/use-style main-panel-style)
   (let [active-panel (re-frame/subscribe [:active-panel])]
     [show-panel @active-panel])])


