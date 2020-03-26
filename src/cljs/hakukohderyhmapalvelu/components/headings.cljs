(ns hakukohderyhmapalvelu.components.headings
  (:require [hakukohderyhmapalvelu.styles.styles-colors :as colors]
            [hakukohderyhmapalvelu.styles.styles-fonts :as vars]
            [stylefy.core :as stylefy]))

(def ^:private h2-styles
  {:color       colors/black
   :font-size   "20px"
   :font-weight vars/font-weight-medium
   :line-height "24px"})

(defn h2
  ([heading]
   [h2 {} heading])
  ([{:keys [id]} heading]
   [:h2 (stylefy/use-style h2-styles {:id id})
    heading]))
