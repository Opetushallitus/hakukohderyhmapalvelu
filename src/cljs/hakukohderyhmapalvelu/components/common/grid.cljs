(ns hakukohderyhmapalvelu.components.common.grid
  (:require [goog.string :as gstring]
            [hakukohderyhmapalvelu.styles.styles-colors :as colors]
            [hakukohderyhmapalvelu.styles.layout-styles :as layout]
            [schema.core :as s]
            [stylefy.core :as stylefy]))

(defn- format-grid-row [row n style-prefix]
  (->> [(repeat n style-prefix) "1fr" style-prefix]
       flatten
       (apply gstring/format row)))

(s/defn make-input-with-label-and-control-styles :- s/Any
  [style-prefix :- s/Str]
  (let [grid (str (format-grid-row "[%s-heading-row-start] \"%s-heading %s-control\" %s [%s-heading-row-end]" 3 style-prefix)
                  (format-grid-row "[%s-input-row-start] \"%s-input %s-input\" %s [%s-input-row-end]" 3 style-prefix)
                  "/ 50% 50%")]
    {:display             "grid"
     :grid-area           style-prefix
     :grid                grid
     ::stylefy/sub-styles {:heading
                           (merge layout/vertical-align-center-styles
                                  {:color     colors/black
                                   :grid-area (str style-prefix "-heading")})}}))

(s/defschema InputWithLabelAndControlProps
  {:control-component [s/Any]
   :cypressid         s/Str
   :input-component   [s/Any]
   :input-id          s/Str
   :label             s/Str
   :style-prefix      s/Str})

(s/defn input-with-label-and-control :- s/Any
  [{:keys [control-component
           cypressid
           input-component
           input-id
           style-prefix
           label]} :- InputWithLabelAndControlProps]
  (let [input-styles (make-input-with-label-and-control-styles style-prefix)]
    [:div (stylefy/use-style input-styles)
     [:label (stylefy/use-sub-style
               input-styles
               :heading
               {:cypressid (str cypressid "-label")
                :for       input-id})
      label]
     input-component
     control-component]))

(defn- make-input-without-top-row-styles [style-prefix]
  (let [grid (str (format-grid-row "[%s-top-row-start] \". .\" %s [%s-top-row-end]" 1 style-prefix)
                  (format-grid-row "[%s-input-row-start] \"%s-input %s-button\" %s [%s-input-row-end]" 3 style-prefix)
                  "/ 50% 50%")]
    {:display "grid"
     :grid    grid}))

(s/defschema InputAndButtonWithoutTopRowProps
  {:button-component [s/Any]
   :input-component  [s/Any]
   :style-prefix     s/Str})

(s/defn input-and-button-without-top-row :- s/Any
  [{:keys [button-component
           input-component
           style-prefix]} :- InputAndButtonWithoutTopRowProps]
  (let [input-styles (make-input-without-top-row-styles style-prefix)]
    [:div (stylefy/use-style input-styles)
     input-component
     button-component]))

