(ns hakukohderyhmapalvelu.components.common.button
  (:require [hakukohderyhmapalvelu.styles.styles-colors :as colors]
            [hakukohderyhmapalvelu.styles.styles-effects :as effects]
            [schema.core :as s]
            [stylefy.core :as stylefy]))

(s/defschema ButtonProps
  {:cypressid                  s/Str
   (s/optional-key :disabled?) s/Bool
   :label                      s/Str
   :on-click                   s/Any
   :style-prefix               s/Str})

(defn- make-button-styles [style-prefix]
  (let [hover-styles {:background-color colors/blue-lighten-2
                      :border-color     colors/blue-lighten-2}]
    {:background-color   colors/blue-lighten-1
     :border-color       colors/blue-lighten-1
     :border-radius      "3px"
     :color              "white"
     :cursor             "pointer"
     :font-family        "inherit"
     :font-size          "100%"
     :line-height        1.15
     :margin             0
     :outline            "none"
     :overflow           "visible"
     :padding            "0 20px"
     :text-transform     "none"
     :-webkit-appearance "button"
     :grid-area          style-prefix
     ::stylefy/mode      [[:disabled {:background-color colors/blue-lighten-3
                                      :border-color     colors/blue-lighten-3
                                      :cursor           "default"}]
                          [":hover:not(:disabled)" hover-styles]
                          [:focus (merge hover-styles
                                         {:filter effects/drop-shadow-effect-blue})]]}))

(s/defn button :- s/Any
  [{:keys [cypressid
           disabled?
           label
           on-click
           style-prefix]} :- ButtonProps]
  (let [button-styles (make-button-styles style-prefix)]
    [:button (stylefy/use-style
               button-styles
               {:cypressid cypressid
                :disabled  disabled?
                :on-click  (fn []
                             (on-click))})

     label]))
