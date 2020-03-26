(ns hakukohderyhmapalvelu.components.panel
  (:require [hakukohderyhmapalvelu.components.headings :as h]
            [hakukohderyhmapalvelu.styles.styles-colors :as colors]
            [hakukohderyhmapalvelu.styles.styles-effects :as effects]
            [stylefy.core :as stylefy]))

(def ^:private main-panel-style
  {:filter       effects/drop-shadow-effect
   :margin-left  "auto"
   :margin-right "auto"
   :width        "1150px"})

(def ^:private panel-content-style
  {:background-color colors/white})

(defn panel [heading contents]
  [:div (stylefy/use-style main-panel-style)
   [h/h2 heading]
   [:div (stylefy/use-style panel-content-style)
    contents]])
