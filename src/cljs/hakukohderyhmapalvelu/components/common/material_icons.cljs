(ns hakukohderyhmapalvelu.components.common.material-icons
  (:require [stylefy.core :as stylefy]))

(def ^:private material-icon-styles
  {:font-family             "Material Icons"
   :font-feature-settings   "liga"
   :font-weight             "normal"
   :font-style              "normal"
   :font-size               "16px"
   :display                 "inline-block"
   :line-height             1
   :text-transform          "none"
   :letter-spacing          "normal"
   :text-rendering          "optimizeLegibility"
   :word-wrap               "normal"
   :white-space             "nowrap"
   :direction               "ltr"
   :-webkit-font-smoothing  "antialiased"
   :-moz-osx-font-smoothing "grayscale"})

(defn- material-icon [icon]
  [:i (stylefy/use-style material-icon-styles)
   icon])

(defn arrow-drop-down []
  [material-icon "arrow_drop_down"])

(defn arrow-drop-up []
  [material-icon "arrow_drop_up"])

(defn done []
  [material-icon "done"])
