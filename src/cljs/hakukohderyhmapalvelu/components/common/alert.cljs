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

(defn alert []
  [:div {:style alert-style}
   [svg/icon "alert" {:width "20px" :height "20px" :margin-right "6px" :fill "white"}]
   [:span "Jokin asia epäonnistui. Kokeile jonkin ajan kuluttua uudestaan."]
   [svg/icon "cross" {:width "10px" :height "10px" :margin-left "3px"}]])
