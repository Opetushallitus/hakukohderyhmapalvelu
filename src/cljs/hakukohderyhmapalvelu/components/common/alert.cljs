(ns hakukohderyhmapalvelu.components.common.alert
  (:require [hakukohderyhmapalvelu.styles.styles-colors :as colors]
            [hakukohderyhmapalvelu.components.common.svg :as svg]))

(def alert-style
  {:position "fixed"
   :right "0px"
   :top "35px"
   :z-index "1"
   :font-size "12px"
   :padding "15px"
   :color "white"
   :background-color colors/red-dark-5
   :border-radius "4px 0px 0px 4px"
   :display "flex"
   :flex-direction "row"
   :justify-content "flex-start"})

(defn alert-icon []
  [svg/icon "alert" {:width "20px" :height "20px" :margin-right "6px" :fill "white"}])

(defn- close-button [on-close]
  [:span {:on-click on-close
          :style {:margin-left "10px"
                  :position "relative"
                  :bottom "5px"
                  :cursor "pointer"}}
   [svg/icon "cross" {:width "8px" :height "8px"}]])

(defn alert [{:keys [message on-close]}]
  (when (seq message)
    [:div {:style alert-style}
     [alert-icon]
     [:span message]
     [close-button on-close]]))
