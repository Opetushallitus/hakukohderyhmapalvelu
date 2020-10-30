(ns hakukohderyhmapalvelu.components.common.checkbox
  (:require [hakukohderyhmapalvelu.components.common.material-icons :as mi]
            [hakukohderyhmapalvelu.styles.styles-colors :as colors]
            [schema.core :as s]
            [stylefy.core :as stylefy]
            [hakukohderyhmapalvelu.styles.layout-styles :as layout]))

(def ^:private checkbox-styles
  (merge
    (layout/flex-row-styles "center" "center")
    {:border-radius "5px"
     :border-width  "1px"
     :border-style  "solid"
     :color         colors/white
     :cursor        "pointer"
     :height        "16px"
     :user-select   "none"
     :width         "16px"}))

(def ^:private checkbox-unchecked-styles
  {:border-color colors/gray-lighten-3})

(def ^:private checkbox-checked-styles
  {:background-color colors/blue-lighten-1
   :border-color     colors/blue-lighten-1})

(def ^:private checkbox-checked-disabled-styles
  {:color colors/gray-lighten-3})

(def ^:private checkbox-disabled-styles
  {:background-color colors/gray-lighten-5
   :border-color     colors/gray-lighten-3
   :cursor           "default"
   :pointer-events   "none"})

(s/defn checkbox
  [{:keys [id
           on-change
           checked?
           disabled?
           cypressid
           aria-labelledby]} :- {:id                         s/Str
                                 :on-change                  s/Any
                                 :checked?                   s/Bool
                                 (s/optional-key :disabled?) s/Bool
                                 (s/optional-key :cypressid) s/Str
                                 :aria-labelledby            s/Str}]
  [:div
   (cond-> (stylefy/use-style
             checkbox-styles
             {:id              id
              :role            "checkbox"
              :aria-labelledby aria-labelledby
              :tabIndex        0
              :aria-checked    checked?
              :cypressid       cypressid
              :aria-disabled   disabled?})
           (not checked?)
           (update :style merge checkbox-unchecked-styles)
           (and checked? (not disabled?))
           (update :style merge checkbox-checked-styles)
           (and checked? disabled?)
           (update :style merge checkbox-checked-disabled-styles)
           (not disabled?)
           (assoc :on-click on-change
                  :on-key-press (fn [e]
                                  (when (= " " (.-key e))
                                    (.preventDefault e)
                                    (on-change))))
           disabled?
           (update :style merge checkbox-disabled-styles))
   (when checked?
     [mi/done])])

(def ^:private checkbox-slider-styles
  (merge
    (layout/flex-row-styles "center" "flex-start")
    {:background-color    colors/gray-lighten-3
     :border-radius       "11px"
     :cursor              "pointer"
     :height              "22px"
     :transition          "all 0.2s ease-in-out"
     :width               "40px"
     ::stylefy/sub-styles {:checkbox
                           {:background-color colors/white
                            :border-radius    "50%"
                            :height           "20px"
                            :margin           "0 1px"
                            :transition       "all 0.2s ease-in-out"
                            :width            "20px"}}}))

(def ^:private checkbox-slider-container-checked-styles
  {:background-color colors/blue-lighten-1})

(def ^:private checkbox-slider-checkbox-checked-styles
  {:transform "translateX(18px)"})

(def ^:private checkbox-slider-checkbox-disabled-styles
  {:background-color colors/gray-lighten-5})

(s/defn checkbox-slider
  [{:keys [id
           aria-labelledby
           checked?
           disabled?
           on-change]} :- {:id                         s/Str
                           :aria-labelledby            s/Str
                           :checked?                   s/Bool
                           (s/optional-key :disabled?) s/Bool
                           :on-change                  s/Any}]
  [:div
   (cond-> (stylefy/use-style
             checkbox-slider-styles
             {:id              id
              :role            "checkbox"
              :tabIndex        0
              :aria-checked    checked?
              :aria-labelledby aria-labelledby})
           (and checked? (not disabled?))
           (merge {:style checkbox-slider-container-checked-styles})
           (not disabled?)
           (merge {:on-click     on-change
                   :on-key-press (fn [e]
                                   (when (= " " (.-key e))
                                     (.preventDefault e)
                                     (on-change)))}))
   [:div
    (cond-> (stylefy/use-sub-style
              checkbox-slider-styles
              :checkbox)
            checked?
            (merge {:style checkbox-slider-checkbox-checked-styles})
            disabled?
            (update :style merge checkbox-slider-checkbox-disabled-styles))]])
