(ns hakukohderyhmapalvelu.components.common.panel
  (:require [hakukohderyhmapalvelu.components.common.headings :as h]
            [hakukohderyhmapalvelu.styles.styles-colors :as colors]
            [hakukohderyhmapalvelu.styles.styles-effects :as effects]
            [stylefy.core :as stylefy]))

(def ^:private main-panel-style
  {:margin-left  "auto"
   :margin-right "auto"
   :width        "1150px"})

(def ^:private panel-content-style
  {:background-color colors/white
   :filter           effects/drop-shadow-effect-black
   :padding          "15px 25px"})

(defn panel [{:keys [cypressid]} heading contents]
  [:div (stylefy/use-style main-panel-style {:cypressid cypressid})
   [h/heading {:cypressid (str cypressid "-heading")
               :level     :h2}
    heading]
   [:div (stylefy/use-style panel-content-style)
    contents]])

