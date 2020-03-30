(ns hakukohderyhmapalvelu.components.common.input
  (:require [hakukohderyhmapalvelu.components.common.material-icons :as icon]
            [hakukohderyhmapalvelu.styles.styles-colors :as colors]
            [hakukohderyhmapalvelu.styles.styles-effects :as effects]
            [hakukohderyhmapalvelu.styles.styles-fonts :as fonts]
            [hakukohderyhmapalvelu.styles.layout-styles :as layout]
            [schema.core :as s]
            [stylefy.core :as stylefy]))

(def ^:private input-row-height "40px")

(def ^:private input-container-styles
  (let [input-left-right-padding "10px"]
    {:border        (str "2px solid " colors/gray-lighten-3)
     :border-radius "3px"
     :box-sizing    "border-box"
     :box-shadow    effects/inset-box-shadow-effect-black
     :color         colors/black
     :font-size     "16px"
     :font-weight   fonts/font-weight-regular
     :height        input-row-height
     :padding       (str "0 " input-left-right-padding)
     :width         "100%"}))

(defn- make-input-text-styles [style-prefix]
  (merge layout/vertical-align-center-styles
         {:grid-area           style-prefix
          :position            "relative"
          :width               "100%"
          ::stylefy/sub-styles {:input
                                (merge input-container-styles
                                       {:outline       "none"
                                        :position      "relative"
                                        ::stylefy/mode [["::placeholder" {:color colors/gray-lighten-1}]
                                                        [:focus {:border-color colors/blue-lighten-2
                                                                 :box-shadow   effects/inset-box-shadow-effect-blue}]]})}}))

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

(defn- make-input-dropdown-styles [style-prefix]
  (merge layout/vertical-align-center-styles
         {:grid-area           style-prefix
          :width               "100%"
          ::stylefy/sub-styles {:selected-dropdown-item
                                (merge layout/vertical-align-center-styles
                                       layout/horizontal-space-between-styles
                                       input-container-styles
                                       {})}}))

(s/defschema InputDropdownProps
  {:cypressid        s/Str
   :style-prefix     s/Str
   :unselected-label s/Str})

(s/defn input-dropdown :- s/Any
  [{:keys [cypressid
           style-prefix
           unselected-label]} :- InputDropdownProps]
  (let [input-dropdown-styles (make-input-dropdown-styles style-prefix)]
    [:div (stylefy/use-style input-dropdown-styles)
     [:div (stylefy/use-sub-style
             input-dropdown-styles
             :selected-dropdown-item)
      [:span
       {:cypressid (str cypressid "-unselected-label")}
       unselected-label]
      [icon/arrow-drop-down]]]))
