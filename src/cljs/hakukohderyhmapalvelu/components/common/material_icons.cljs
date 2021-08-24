(ns hakukohderyhmapalvelu.components.common.material-icons
  (:require [stylefy.core :as stylefy]
            [re-frame.core :refer [subscribe]]))

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

(defn- material-icon [icon additional-styles options]
  (let [ styles (merge material-icon-styles additional-styles)]
    [:i (stylefy/use-style styles options)
     icon]))

(defn arrow-drop-down []
  [material-icon "arrow_drop_down" {}])

(defn arrow-drop-up []
  [material-icon "arrow_drop_up" {}])

(defn archived []
  [material-icon "book"
   {:vertical-align "text-top"}
   {:title @(subscribe [:translation :yleiset/arkistoitu])}])

(defn done []
  [material-icon "done" {}])
