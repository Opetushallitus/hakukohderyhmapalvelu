(ns hakukohderyhmapalvelu.components.common.alert
  (:require [hakukohderyhmapalvelu.styles.styles-colors :as colors]
            [hakukohderyhmapalvelu.components.common.svg :as svg]
            [hakukohderyhmapalvelu.events.alert-events :as alert-events]
            [hakukohderyhmapalvelu.subs.alert-subs :as alert-subs]
            [re-frame.core :as re-frame]
            [stylefy.core :as stylefy]))

(def ^:private alert-style
  {:position         "fixed"
   :right            "0px"
   :top              "35px"
   :z-index          "1"
   :padding          "15px"
   :color            "white"
   :background-color colors/red-dark-1
   :border-radius    "4px 0px 0px 4px"
   :display          "flex"
   :flex-direction   "row"
   :justify-content  "flex-start"})

(def ^:private close-style {:margin-left "10px"
                            :position    "relative"
                            :cursor      "pointer"})

(defn alert-icon []
  [svg/icon :alert {:width "20px" :height "20px" :margin-right "10px"} {:fill "white"}])

(defn- close-button [on-close]
  [:span (stylefy/use-style close-style {:on-click on-close})
   [svg/icon :cross {:width "14px" :height "14px"} {:fill "white" :width "14" :height "14" :view-box "0 0 18 18"}]])

(defn alert []
  (let [message  @(re-frame/subscribe [alert-subs/alert-message])
        on-close #(re-frame/dispatch [alert-events/alert-closed])]
    (when (seq message)
      [:div (stylefy/use-style alert-style {:cypressid "alert"})
       [alert-icon]
       [:span message]
       [close-button on-close]])))
