(ns hakukohderyhmapalvelu.views
  (:require [hakukohderyhmapalvelu.components.common.alert :as alert]
            [hakukohderyhmapalvelu.views.hakukohderyhmien-hallinta-panel :as h]
            [hakukohderyhmapalvelu.views.haun-asetukset-panel :as a]
            [re-frame.core :as re-frame]))

(defn- panels [panel-name]
  (let [panel (case panel-name
                :panel/hakukohderyhmien-hallinta [h/hakukohderyhmien-hallinta-panel]
                :panel/haun-asetukset [a/haun-asetukset-panel]
                [:div])]
    [:<>
     panel]))

(defn- show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  [:div
   [alert/alert]
   (let [{panel :panel} @(re-frame/subscribe [:panel/active-panel])]
     [show-panel panel])])
