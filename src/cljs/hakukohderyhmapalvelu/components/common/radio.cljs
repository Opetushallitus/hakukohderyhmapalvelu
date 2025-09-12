(ns hakukohderyhmapalvelu.components.common.radio
  (:require [hakukohderyhmapalvelu.components.common.material-icons :as mi]
            [hakukohderyhmapalvelu.styles.styles-colors :as colors]
            [schema.core :as s]
            [stylefy.core :as stylefy]
            [hakukohderyhmapalvelu.styles.layout-styles :as layout]
            [hakukohderyhmapalvelu.components.common.label :as label-component]))

(def ^:private radio-styles
  (merge
   (layout/flex-row-styles "center" "center")
   {:border-radius "50%"
    :border-width  "1px"
    :border-style  "solid"
    :color         colors/white
    :cursor        "pointer"
    :height        "16px"
    :user-select   "none"
    :width         "16px"}))

(def ^:private radio-unchecked-styles
  {:border-color colors/gray-lighten-3})

(def ^:private radio-checked-styles
  {:background-color colors/blue-lighten-1
   :border-color     colors/blue-lighten-1})

(def ^:private radio-checked-disabled-styles
  {:color colors/gray-lighten-3})

(def ^:private radio-disabled-styles
  {:background-color colors/gray-lighten-5
   :border-color     colors/gray-lighten-3
   :cursor           "default"
   :pointer-events   "none"})

(def ^:private radio-with-label-styles
  (merge
   layout/vertical-align-center-styles
   {:cursor          "pointer"
    :justify-self    "end"
    ::stylefy/manual [["div + label"
                       {:margin-left "10px"}]]}))

(s/defn radio
  [{:keys [id
           name
           on-change
           checked?
           value
           disabled?
           cypressid
           aria-labelledby]} :- {:id                         s/Str
                                 :name                       s/Str
                                 :on-change                  s/Any
                                 :checked?                   s/Bool
                                 :value                      s/Str
                                 (s/optional-key :disabled?) s/Bool
                                 (s/optional-key :cypressid) s/Str
                                 :aria-labelledby            s/Str}]
  [:div
   (cond-> (stylefy/use-style
            radio-styles
            {:id              id
             :name            name
             :checked         checked?
             :value           value
             :role            "radio"
             :aria-labelledby aria-labelledby
             :tabIndex        0
             :aria-checked    checked?
             :cypressid       cypressid
             :aria-disabled   disabled?})
     (not checked?)
     (update :style merge radio-unchecked-styles)
     (and checked? (not disabled?))
     (update :style merge radio-checked-styles)
     (and checked? disabled?)
     (update :style merge radio-checked-disabled-styles)
     (not disabled?)
     (assoc :on-click on-change
            :on-key-press (fn [e]
                            (when (= " " (.-key e))
                              (.preventDefault e)
                              (on-change))))
     disabled?
     (update :style merge radio-disabled-styles))
   (when checked?
     [mi/radio-button-checked])])

(s/defn radio-with-label
  [{:keys [checked?
           name
           value
           cypressid
           disabled?
           id
           label
           on-change]} :- {:id        s/Str
                           :name      s/Str
                           :value     s/Str
                           :checked?  s/Bool
                           :cypressid s/Str
                           :disabled? s/Bool
                           :label     s/Str
                           :on-change s/Any}]
  (let [label-id (str id "-label")]
    [:div (stylefy/use-style radio-with-label-styles)
     [radio
      {:aria-labelledby label-id
       :name            name
       :value           value
       :checked?        checked?
       :cypressid       (str cypressid "-input")
       :disabled?       disabled?
       :id              id
       :on-change       on-change}]
     [label-component/label
      {:cypressid (str cypressid "-label")
       :id        label-id
       :label     label}]]))

