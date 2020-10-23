(ns hakukohderyhmapalvelu.components.common.label
  (:require [hakukohderyhmapalvelu.styles.styles-colors :as colors]
            [schema.core :as s]
            [stylefy.core :as stylefy]))


(def ^:private label-styles
  {:color       colors/gray-lighten-1
   :cursor      "pointer"
   :user-select "none"})

(s/defn label
  [{:keys [id
           cypressid
           label
           for
           hidden]} :- {(s/optional-key :id)        s/Str
                        (s/optional-key :cypressid) s/Str
                        :label                      s/Str
                        (s/optional-key :for)       s/Str
                        (s/optional-key :hidden)    s/Bool}]
  [:label (stylefy/use-style
            label-styles
            {:cypressid cypressid
             :for       for
             :id        id
             :hidden    hidden})
   label])
