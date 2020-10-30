(ns hakukohderyhmapalvelu.components.common.headings
  (:require [hakukohderyhmapalvelu.styles.styles-colors :as colors]
            [hakukohderyhmapalvelu.styles.styles-fonts :as vars]
            [stylefy.core :as stylefy]))

(def ^:private h2-styles
  {:color       colors/black
   :font-size   "20px"
   :font-weight vars/font-weight-medium
   :line-height "24px"})

(def ^:private h3-styles
  {:color       colors/black
   :font-size   "18px"
   :font-weight vars/font-weight-regular
   :line-height "24px"})

(defn heading [{:keys [cypressid
                       level
                       id]} heading-text]
  (let [[element styles] (case level
                           :h2 [:h2 h2-styles]
                           :h3 [:h3 h3-styles])]
    [element (stylefy/use-style
               styles
               {:id        id
                :cypressid cypressid})
     heading-text]))
