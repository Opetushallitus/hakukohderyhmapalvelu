(ns hakukohderyhmapalvelu.components.common.popup
  (:require [stylefy.core :as stylefy]
            [schema.core :as s]))


(def popup-background
  {:position "fixed"
   :z-index  1
   :left     0
   :top      0
   :width    "100%"
   :height   "100%"})

(def popup-style
  {:position         "absolute"
   :z-index          1
   :filter           "drop-shadow(0 1px 3px rgba(0, 0, 0, 0.3))"
   :background-color "white"
   :padding          "1rem"
   ::stylefy/mode    [[:after {:bottom        "100%"
                               :right         "80px"
                               :margin-right  "-8px"
                               :border-left   "8px solid transparent"
                               :border-right  "8px solid transparent"
                               :border-bottom "8px solid #ffffff"}]
                      [:before {:bottom              "100%"
                                :content             "\"\""
                                :position            "absolute"
                                :right               "80px"
                                :margin-right        "-8px"
                                :border-left         "8px solid transparent"
                                :border-right        "8px solid transparent"
                                :border-bottom       "8px solid"
                                :border-bottom-color "#ffffff"}]]})

(s/defschema PopupProps
  {:on-close  s/Any
   :style     {s/Any s/Any}
   :cypressid s/Str})

(s/defn popup [{:keys [style on-close cypressid]} :- PopupProps & children]
  [:<>
   [:div (stylefy/use-style popup-background {:cypressid (str cypressid "-close") :on-click #(on-close)})]
   [:div (stylefy/use-style (merge popup-style style) {:cypressid cypressid})
    children]])
