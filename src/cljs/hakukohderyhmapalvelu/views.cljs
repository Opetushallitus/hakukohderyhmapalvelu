(ns hakukohderyhmapalvelu.views
  (:require [hakukohderyhmapalvelu.components.common.alert :as alert]
            [hakukohderyhmapalvelu.events.alert-events :as alert-events]
            [hakukohderyhmapalvelu.subs.alert-subs :as alert-subs]
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

(defn- app-alert []
  [alert/alert {:message  @(re-frame/subscribe [alert-subs/alert-message])
                :on-close #(re-frame/dispatch [alert-events/alert-closed])}])

(defn main-panel []
  [:div
   [app-alert]
   (let [{panel :panel} @(re-frame/subscribe [:panel/active-panel])]
     [show-panel panel])])
