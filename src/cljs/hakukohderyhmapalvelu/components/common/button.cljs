(ns hakukohderyhmapalvelu.components.common.button
  (:require [hakukohderyhmapalvelu.styles.styles-colors :as colors]
            [hakukohderyhmapalvelu.styles.styles-effects :as effects]
            [schema.core :as s]
            [stylefy.core :as stylefy]))

(s/defschema ButtonProps
  {:cypressid                  s/Str
   (s/optional-key :disabled?) s/Bool
   (s/optional-key :custom-style) s/Any
   :label                      s/Any
   :on-click                   s/Any
   :style-prefix               s/Str})

(defn- make-button-styles [style-prefix {:keys [is-danger] :as custom-style}]
  (let [bg-color-1 (if is-danger colors/red-dark-3 colors/blue-lighten-1)
        bg-color-2 (if is-danger colors/red-dark-4 colors/blue-lighten-2)
        hover-styles {:background-color bg-color-2
                      :border-color     bg-color-2}

        default-style {:background-color   bg-color-1
                       :border-color       bg-color-1
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
                                                           {:filter effects/drop-shadow-effect-blue})]]}]
    (merge
      default-style
      custom-style)))

(defn- make-text-button-styles [style-prefix]
  {:background      "none"
   :border          "none"
   :color           colors/blue
   :cursor          "pointer"
   :font-size       "1rem"
   :grid-area       style-prefix
   :margin          0
   :padding         0
   :text-decoration "underline"
   ::stylefy/mode   [[":hover:not(:disabled)" {:color colors/blue-lighten-1}]
                     [:disabled {:cursor "default"
                                 :color colors/blue-lighten-3}]]})

(defn- create-button [{:keys [cypressid disabled? label on-click style]}]
  [:button (stylefy/use-style
             style
             {:cypressid cypressid
              :disabled  disabled?
              :on-click  (fn []
                           (on-click))})
   label])

(s/defn button :- s/Any
  [{:keys [cypressid
           disabled?
           label
           on-click
           style-prefix
           custom-style]} :- ButtonProps]
  (let [button-styles (make-button-styles style-prefix custom-style)]
    (create-button {:cypressid cypressid
                    :disabled? disabled?
                    :label     label
                    :on-click  on-click
                    :style     button-styles})))

(s/defn text-button
  [{:keys [cypressid
           disabled?
           label
           on-click
           style-prefix]} :- ButtonProps]
  (let [button-styles (make-text-button-styles style-prefix)]
    (create-button {:cypressid cypressid
                    :disabled? disabled?
                    :label     label
                    :on-click  on-click
                    :style     button-styles})))
