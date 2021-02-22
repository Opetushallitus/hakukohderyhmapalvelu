(ns hakukohderyhmapalvelu.components.common.dropdown
  (:require [hakukohderyhmapalvelu.components.common.input :refer [input-container-styles input-row-height]]
            [hakukohderyhmapalvelu.components.common.material-icons :as icon]
            [hakukohderyhmapalvelu.styles.styles-colors :as colors]
            [hakukohderyhmapalvelu.styles.layout-styles :as layout]
            [reagent.core :as reagent]
            [schema.core :as s]
            [stylefy.core :as stylefy]))

(def ^:private input-dropdown-container-styles
  (merge
    layout/vertical-align-center-styles
    {:flex-direction "column"
     :width "100%"
     ::stylefy/mode {:hover {:cursor "pointer"}}}))

(def ^:private input-dropdown-selector-styles
  (merge
    layout/vertical-align-center-styles
    layout/horizontal-space-between-styles
    input-container-styles))

(def ^:private input-dropdown-item-container-styles
  {:display "flex"
   :flex-direction "column"
   :position "absolute"
   :background-color "white"
   :width "540px"
   :margin-top input-row-height
   :border-style "solid"
   :border-width "1px"
   :border-color colors/gray-lighten-3
   :border-radius "3px"})


(s/defschema InputDropdownProps
  {:cypressid        s/Str
   :unselected-label s/Str
   :dropdown-items s/Any
   :selected-dropdown-item s/Any})

(defn dropdown-option
  [{:keys [item-str selection-fn]}]
  [:div (stylefy/use-style
          {:padding "6px 9px"
           ::stylefy/mode {:hover {:background-color colors/blue-lighten-3}}}
          {:on-click #(selection-fn)})
   item-str])

(defn dropdown-item-container
  [{:keys [dropdown-items
           selection-fn]}]
  [:div (stylefy/use-style input-dropdown-item-container-styles)
   (for [item dropdown-items]
     ^{:key item} (dropdown-option
                    {:item-str item
                     :selection-fn (fn [_]
                                     (println "wtf123")
                                     (selection-fn item))}))])

(s/defn input-dropdown :- s/Any
  [{:keys [cypressid
           unselected-label
           dropdown-items
           selected-dropdown-item]} :- InputDropdownProps]
  (let [is-active (reagent/atom false)]
    (fn []
      (let [arrow-icon (if @is-active icon/arrow-drop-up icon/arrow-drop-down)]

        [:div (stylefy/use-style
                input-dropdown-container-styles
                {:on-click #(swap! is-active not)})
         [:div (stylefy/use-style
                 input-dropdown-selector-styles)
          [:span
           {:cypressid (str cypressid "-unselected-label")};TODO: cypressid should be (str cypressid "-label"), make sure tests are okay first
           (or @selected-dropdown-item unselected-label)]
          [arrow-icon]]
         (when @is-active
           (dropdown-item-container {:dropdown-items dropdown-items
                                      :selection-fn  (partial reset! selected-dropdown-item)}))]))))
