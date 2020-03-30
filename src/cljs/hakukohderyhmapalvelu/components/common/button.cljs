(ns hakukohderyhmapalvelu.components.common.button
  (:require [hakukohderyhmapalvelu.styles.styles-colors :as colors]
            [hakukohderyhmapalvelu.styles.styles-effects :as effects]
            [schema.core :as s]
            [stylefy.core :as stylefy]))

(s/defschema ButtonProps
  {:label                   s/Str
   :style-prefix            s/Str})

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
     ::stylefy/mode      [[:hover hover-styles]
                          [:focus (merge hover-styles
                                         {:filter effects/drop-shadow-effect-blue})]]}))

(s/defn button :- s/Any
  [{:keys [label
           style-prefix]} :- ButtonProps]
  (let [button-styles (make-button-styles style-prefix)]
    [:button (stylefy/use-style button-styles)
     label]))
