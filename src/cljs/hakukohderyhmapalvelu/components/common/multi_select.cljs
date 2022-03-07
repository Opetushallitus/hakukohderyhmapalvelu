(ns hakukohderyhmapalvelu.components.common.multi-select
  (:require [hakukohderyhmapalvelu.styles.styles-colors :as colors]
            [stylefy.core :as stylefy]
            [schema.core :as s]
            [hakukohderyhmapalvelu.components.common.material-icons :as icon]
            [hakukohderyhmapalvelu.events.hakukohderyhmien-hallinta-events :as hakukohderyhma-events]
            [re-frame.core :refer [dispatch]]))


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
   (s/optional-key :priorisointi) s/Bool
   (s/optional-key :icon) s/Any})

(defn- multi-select-option-priorisoiva [select-fn {:keys [value label sub-label is-selected is-disabled icon index is-last priorisointi]} cypressid]
  (let [style (cond
                is-disabled option-style-disabled
                is-selected option-style-selected
                :else option-style)
        arrow-style {:float "left"
                     :padding-left "3px"}
        swap-fn #(dispatch [hakukohderyhma-events/swap-hakukohtees %])
        sub-label-style (if is-disabled sub-label-style-disabled sub-label-style)
        on-click #(when (not is-disabled) (select-fn value))
        swap-down #(swap-fn index)
        swap-up #(swap-fn (- index 1))]
    [:div (stylefy/use-style style {:cypressid (str cypressid "__" label (when is-selected "--selected"))})
     [:div (stylefy/use-style arrow-style)
      [:div (stylefy/use-style arrow-style {:on-click swap-up}) (when (and priorisointi (> index 0)) (icon/arrow-drop-up))]
      [:div (stylefy/use-style arrow-style {:on-click swap-down}) (when (and (not is-last)
                                                                             priorisointi) (icon/arrow-drop-down)) is-last]]
     [:div (stylefy/use-style style {:on-click on-click
                                     :cypressid (str cypressid "__" label (when is-selected "--selected"))})
      [:span (stylefy/use-style sub-label-style) sub-label]
      [:span (stylefy/use-style label-style)
       (when icon
         [icon])
       label]]]))

(defn- multi-select-option [select-fn {:keys [value label sub-label is-selected is-disabled icon]} cypressid]
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
      (when icon
        [icon])
      label]]))

(s/defschema Props
  {:options                    [Option]
   :select-fn                  s/Any
   (s/optional-key :cypressid) s/Str})

(s/defn multi-select-priorisoiva [{:keys [options select-fn cypressid]} :- Props]
  (let [is-empty (empty? options)
        container-style (if is-empty multi-select-style-empty multi-select-style)
        last-option-index (- (count options) 1)
        indexed-options (map-indexed (fn [i x] (-> x
                                                   (assoc :index i)
                                                   (assoc :is-last (= i last-option-index))))
                                     options)]
    [:div (stylefy/use-style container-style {:cypressid cypressid})
     (for [option indexed-options]
       ^{:key (:value option)} [multi-select-option-priorisoiva select-fn option cypressid])]))

(s/defn multi-select [{:keys [options select-fn cypressid]} :- Props]
  (let [is-empty (empty? options)
        container-style (if is-empty multi-select-style-empty multi-select-style)]
    [:div (stylefy/use-style container-style {:cypressid cypressid})
     (for [option options]
       ^{:key (:value option)} [multi-select-option select-fn option cypressid])]))

