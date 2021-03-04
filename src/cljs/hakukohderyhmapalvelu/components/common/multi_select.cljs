(ns hakukohderyhmapalvelu.components.common.multi-select
  (:require [hakukohderyhmapalvelu.styles.styles-colors :as colors]
            [stylefy.core :as stylefy]
            [schema.core :as s]))


(def ^:private multi-select-style
  {:border-color  colors/gray-lighten-3
   :border-radius "4px"
   :border-style  "solid"
   :border-width  "1px"
   :height        "20rem"
   :overflow-y    "scroll"
   :user-select   "none"})

(def ^:private multi-select-style-empty
  (merge multi-select-style
         {:background-color colors/gray-lighten-6}))

(def ^:private option-style
  {:background-color "#fff"
   :cursor           "pointer"
   :display          "block"
   :padding          "2px 4px"})

(def ^:private option-style-selected
  (merge
    option-style
    {:background-color colors/blue-lighten-3}))

(s/defschema Option
  {:is-selected s/Bool
   :label       s/Str
   :value       s/Any})

(defn- create-multi-select-option [select-fn {:keys [value label is-selected]}]
  (let [style (if is-selected option-style-selected option-style)
        on-click #(select-fn value)]
    (fn []
      ^{:key value} [:div (stylefy/use-style style {:on-click on-click})
                     label])))

(s/defschema Props
  {:options [Option]
   :select-fn s/Any
   (s/optional-key :cypressid) s/Str})

(s/defn multi-select [{:keys [options select-fn cypressid]} :- Props]
  (let [is-empty (empty? options)
        container-style (if is-empty multi-select-style-empty multi-select-style)]
    [:div (stylefy/use-style container-style {:cypressid cypressid})
     (when-not is-empty
       (mapv (partial create-multi-select-option select-fn) options))]))
