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

(def ^:private input-text-styles
  (merge input-container-styles
         {:outline       "none"
          :position      "relative"
          ::stylefy/mode [["::placeholder" {:color colors/gray-lighten-1}]
                          [:focus {:border-color colors/blue-lighten-2
                                   :box-shadow   effects/inset-box-shadow-effect-blue}]]}))


(s/defschema InputTextProps
  {(s/optional-key :cypressid) s/Str
   :input-id                   s/Str
   :on-change                  s/Any
   :placeholder                s/Str
   :aria-label                 s/Str})

(s/defn input-text :- s/Any
  [{:keys [cypressid
           input-id
           on-change
           placeholder
           aria-label]} :- InputTextProps]
  [:input (stylefy/use-style
            input-text-styles
            {:cypressid   cypressid
             :id          input-id
             :on-change   (fn [event]
                            (let [value (.. event -target -value)]
                              (on-change value)))
             :placeholder placeholder
             :type        "text"
             :aria-label  aria-label})])

(def ^:private input-dropdown-styles
  (merge
    layout/vertical-align-center-styles
    layout/horizontal-space-between-styles
    input-container-styles))


(s/defschema InputDropdownProps
  {:cypressid        s/Str
   :unselected-label s/Str})

(s/defn input-dropdown :- s/Any
  [{:keys [cypressid
           unselected-label]} :- InputDropdownProps]
  [:div (stylefy/use-style
          input-dropdown-styles)
   [:span
    {:cypressid (str cypressid "-unselected-label")}
    unselected-label]
   [icon/arrow-drop-down]])
