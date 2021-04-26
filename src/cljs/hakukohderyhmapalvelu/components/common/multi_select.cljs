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

(def ^:private option-style-disabled
  (merge
    option-style
    {:color "#b8b8b8"
     :cursor "arrow"}))

(s/defschema Option
  {:is-selected s/Bool
   :is-disabled s/Bool
   :label       s/Str
   :value       s/Any})

(defn- multi-select-option [select-fn {:keys [value label is-selected is-disabled]} cypressid]
  (let [style (cond
                is-disabled option-style-disabled
                is-selected option-style-selected
                :else option-style)
        on-click #(when (not is-disabled) (select-fn value))]
    [:div (stylefy/use-style style {:on-click on-click
                                    :cypressid (str cypressid "__" label (when is-selected "--selected"))})
     label]))

(s/defschema Props
  {:options                    [Option]
   :select-fn                  s/Any
   (s/optional-key :cypressid) s/Str})

(s/defn multi-select [{:keys [options select-fn cypressid]} :- Props]
  (let [is-empty (empty? options)
        container-style (if is-empty multi-select-style-empty multi-select-style)]
    [:div (stylefy/use-style container-style {:cypressid cypressid})
     (for [option options]
       ^{:key (:value option)} [multi-select-option select-fn option cypressid])]))
