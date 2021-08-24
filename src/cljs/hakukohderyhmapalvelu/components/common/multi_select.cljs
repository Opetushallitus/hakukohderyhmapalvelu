(ns hakukohderyhmapalvelu.components.common.multi-select
  (:require [hakukohderyhmapalvelu.styles.styles-colors :as colors]
            [stylefy.core :as stylefy]
            [schema.core :as s]
            [hakukohderyhmapalvelu.components.common.material-icons :as icon]))


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
    {:color colors/gray-lighten-3
     :cursor "arrow"}))

(def ^:private label-style
  {:display       "block"
   :line-height   "16px"
   :margin-bottom "5px"})

(def ^:private sub-label-style
  {:display   "block"
   :font-size "12px"
   :color     colors/gray-lighten-1})

(def ^:private sub-label-style-disabled
  (dissoc sub-label-style :color))

(s/defschema Option
  {:is-selected s/Bool
   :is-disabled s/Bool
   :label       s/Str
   :sub-label   s/Str
   :value       s/Any
   :tila        s/Str})

(defn- multi-select-option [select-fn {:keys [value label sub-label is-selected is-disabled tila]} cypressid]
  (let [style (cond
                is-disabled option-style-disabled
                is-selected option-style-selected
                :else option-style)
        sub-label-style (if is-disabled sub-label-style-disabled sub-label-style)
        on-click #(when (not is-disabled) (select-fn value))]
    [:div (stylefy/use-style style {:on-click on-click
                                    :cypressid (str cypressid "__" label (when is-selected "--selected"))})
     [:span (stylefy/use-style sub-label-style) sub-label]
     [:span (stylefy/use-style label-style)
      (when (= tila "arkistoitu")
        [icon/archived] )
      label]]))

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
