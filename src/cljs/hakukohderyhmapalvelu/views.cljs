(ns hakukohderyhmapalvelu.views
  (:require [hakukohderyhmapalvelu.views.hakukohderyhmien-hallinta-panel :as h]
            [hakukohderyhmapalvelu.views.haun-asetukset-panel :as a]
            [re-frame.core :as re-frame]))

(defn- panels [panel-name]
  (let [panel (case panel-name
                :panel-menu/hakukohderyhmien-hallinta-panel [h/hakukohderyhmien-hallinta-panel]
                :panel-menu/haun-asetukset-panel [a/haun-asetukset-panel]
                [:div])]
    [:<>
     panel]))

(defn- show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  [:div
   (let [active-panel @(re-frame/subscribe [:panel-menu/active-panel])]
     [show-panel active-panel])])
