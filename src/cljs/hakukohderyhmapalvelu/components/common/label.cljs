(ns hakukohderyhmapalvelu.components.common.label
  (:require [hakukohderyhmapalvelu.styles.styles-colors :as colors]
            [schema.core :as s]
            [stylefy.core :as stylefy]))


(def ^:private label-styles
  {:color       colors/gray-lighten-1
   :cursor      "pointer"
   :user-select "none"})

(s/defn label
  [{:keys [cypressid
           label
           for]} :- {(s/optional-key :cypressid) s/Str
                     :label                      s/Str
                     :for                        s/Str}]
  [:label (stylefy/use-style
            label-styles
            {:cypressid cypressid
             :for       for})
   label])
