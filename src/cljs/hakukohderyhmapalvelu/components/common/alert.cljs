(ns hakukohderyhmapalvelu.components.common.alert
  (:require [hakukohderyhmapalvelu.styles.styles-colors :as colors]
            [hakukohderyhmapalvelu.components.common.svg :as svg]
            [hakukohderyhmapalvelu.events.alert-events :as alert-events]
            [hakukohderyhmapalvelu.subs.alert-subs :as alert-subs]
            [re-frame.core :as re-frame]))

(def alert-style
  {:position "fixed"
   :right "0px"
   :top "35px"
   :z-index "1"
   :font-size "12px"
   :padding "15px"
   :color "white"
   :background-color colors/red-dark-1
   :border-radius "4px 0px 0px 4px"
   :display "flex"
   :flex-direction "row"
   :justify-content "flex-start"})

(defn alert-icon []
  [svg/icon :alert {:width "20px" :height "20px" :margin-right "10px"} {:fill "white"}])

(defn- close-button [on-close]
  [:span {:on-click on-close
          :style    {:margin-left "10px"
                     :position    "relative"
                     :bottom      "5px"
                     :cursor      "pointer"}}
   [svg/icon :cross {:width "8px" :height "8px"} {:fill "white" :width "8" :height "8" :view-box "0 0 18 18"}]])

(defn alert []
  (let [message  @(re-frame/subscribe [alert-subs/alert-message])
        on-close #(re-frame/dispatch [alert-events/alert-closed])]
    (when (seq message)
      [:div {:style alert-style}
       [alert-icon]
       [:span message]
       [close-button on-close]])))
