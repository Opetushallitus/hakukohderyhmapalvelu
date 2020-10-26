(ns hakukohderyhmapalvelu.components.common.checkbox
  (:require [hakukohderyhmapalvelu.styles.styles-colors :as colors]
            [schema.core :as s]
            [stylefy.core :as stylefy]
            [hakukohderyhmapalvelu.styles.layout-styles :as layout]))

(def ^:private checkbox-styles
  {:cursor "pointer"})

(s/defn checkbox
  [{:keys [id
           on-change
           checked?
           disabled?
           cypressid]} :- {:id                         s/Str
                           :on-change                  s/Any
                           :checked?                   s/Bool
                           (s/optional-key :disabled?) s/Bool
                           (s/optional-key :cypressid) s/Str}]

  [:input
   (stylefy/use-style
     checkbox-styles
     {:id        id
      :cypressid cypressid
      :checked   checked?
      :disabled  disabled?
      :on-change on-change
      :type      "checkbox"})])

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
              :on-click        on-change
              :role            "checkbox"
              :tabIndex        0
              :aria-checked    checked?
              :aria-labelledby aria-labelledby})
           (and checked? (not disabled?))
           (merge {:style checkbox-slider-container-checked-styles}))
   [:div
    (cond-> (stylefy/use-sub-style
              checkbox-slider-styles
              :checkbox)
            checked?
            (merge {:style checkbox-slider-checkbox-checked-styles})
            disabled?
            (update :style merge checkbox-slider-checkbox-disabled-styles))]])
