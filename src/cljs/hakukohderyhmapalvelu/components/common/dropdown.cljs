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
     :width          "100%"
     ::stylefy/mode  {:hover {:cursor "pointer"}}}))

(def ^:private input-dropdown-selector-styles
  (merge
    layout/vertical-align-center-styles
    layout/horizontal-space-between-styles
    input-container-styles))

(def ^:private input-dropdown-item-container-styles
  {:display          "flex"
   :flex-direction   "column"
   :position         "absolute"
   :background-color "white"
   :width            "540px"
   :margin-top       input-row-height
   :border-style     "solid"
   :border-width     "1px"
   :border-color     colors/gray-lighten-3
   :border-radius    "3px"})


(s/defschema InputDropdownProps
  {:cypressid              s/Str
   :unselected-label       s/Str
   :dropdown-items         s/Any
   :selected-dropdown-item s/Any
   :selection-fn           s/Any})

(defn dropdown-main-body
  [{:keys [cypressid
           selected-dropdown-item
           unselected-label
           is-dropped-down]}]
  [:div (stylefy/use-style
          input-dropdown-selector-styles)
   [:span
    {:cypressid (str cypressid "-label" (when is-dropped-down "--dropped"))}
    (or @selected-dropdown-item unselected-label)]
   [(if is-dropped-down icon/arrow-drop-up icon/arrow-drop-down)]])

(defn dropdown-item
  [{:keys [item-str selection-fn]}]
  [:div (stylefy/use-style
          {:padding       "6px 9px"
           ::stylefy/mode {:hover {:background-color colors/blue-lighten-3}}}
          {:cypressid (str "dropdown-selector--" item-str)
           :on-click  #(selection-fn)
           :key       item-str})
   item-str])

(defn dropdown-item-container
  [{:keys [dropdown-items
           selection-fn]}]
  [:div (stylefy/use-style input-dropdown-item-container-styles)
   (for [item dropdown-items]
     (dropdown-item
       {:item-str     item
        :selection-fn #(selection-fn item)}))])

(s/defn input-dropdown :- s/Any
  [{:keys [cypressid
           unselected-label
           dropdown-items
           selected-dropdown-item
           selection-fn]} :- InputDropdownProps]
  (let [is-active (reagent/atom false)]
    (fn []
      (let [dereffed-items @dropdown-items
            is-dropped-down @is-active]
        [:div (stylefy/use-style
                input-dropdown-container-styles
                {:on-click #(when (seq dereffed-items) (swap! is-active not))})
         (dropdown-main-body {:cypressid              cypressid
                              :selected-dropdown-item selected-dropdown-item
                              :unselected-label       unselected-label
                              :is-dropped-down        is-dropped-down})
         (when is-dropped-down
           (dropdown-item-container {:dropdown-items dereffed-items
                                     :selection-fn   selection-fn}))]))))
