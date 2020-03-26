(ns hakukohderyhmapalvelu.components.common.input
  (:require [hakukohderyhmapalvelu.styles.styles-colors :as colors]
            [hakukohderyhmapalvelu.styles.styles-effects :as effects]
            [hakukohderyhmapalvelu.styles.layout-styles :as layout]
            [schema.core :as s]
            [stylefy.core :as stylefy]))

(defn- make-input-text-styles [style-prefix]
  (let [row-height               "40px"
        input-left-right-padding "10px"]
    (merge layout/vertical-align-center-styles
           {:grid-area           style-prefix
            :position            "relative"
            ::stylefy/sub-styles {:input
                                  {:border        (str "2px solid " colors/gray-lighten-3)
                                   :border-radius "3px"
                                   :box-sizing    "border-box"
                                   :box-shadow    effects/inset-box-shadow-effect-black
                                   :color         colors/black
                                   :font-size     "16px"
                                   :height        row-height
                                   :outline       "none"
                                   :padding       (str "0 " input-left-right-padding)
                                   :position      "relative"
                                   :width         "100%"
                                   ::stylefy/mode [["::placeholder" {:color colors/gray-lighten-1}]
                                                   [:focus {:border-color colors/blue-lighten-2
                                                            :box-shadow   effects/inset-box-shadow-effect-blue}]]}}})))

(s/defschema InputTextProps
  {:cypressid    s/Str
   :input-id     s/Str
   :placeholder  s/Str
   :style-prefix s/Str})

(s/defn input-text :- s/Any
  [{:keys [cypressid
           input-id
           placeholder
           style-prefix]} :- InputTextProps]
  (let [input-text-styles (make-input-text-styles style-prefix)]
    [:div (stylefy/use-style input-text-styles)
     [:input (stylefy/use-sub-style
               input-text-styles
               :input
               {:cypressid   cypressid
                :id          input-id
                :placeholder placeholder
                :type        "text"})]]))
