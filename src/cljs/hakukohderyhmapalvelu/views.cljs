(ns hakukohderyhmapalvelu.views
  (:require [hakukohderyhmapalvelu.views.panel-menu :as m]
            [hakukohderyhmapalvelu.views.hakukohderyhmien-hallinta-panel :as h]
            [re-frame.core :as re-frame]))

(defn- panels [panel-name]
  (let [panel (case panel-name
                :panel-menu/hakukohderyhmien-hallinta-panel [h/hakukohderyhmien-hallinta-panel]
                [:div])]
    [:<>
     [m/panel-menu]
     panel]))

(defn- show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  [:div
   (let [active-panel @(re-frame/subscribe [:panel-menu/active-panel])]
     [show-panel active-panel])])
