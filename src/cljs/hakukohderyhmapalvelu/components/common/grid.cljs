(ns hakukohderyhmapalvelu.components.common.grid
  (:require [goog.string :as gstring]
            [hakukohderyhmapalvelu.styles.styles-colors :as colors]
            [hakukohderyhmapalvelu.styles.layout-styles :as layout]
            [schema.core :as s]
            [stylefy.core :as stylefy]))

(defn- format-grid-row [row style-prefix]
  (->> [(repeat 3 style-prefix) "1fr" style-prefix]
       flatten
       (apply gstring/format row)))

(s/defn make-input-styles :- s/Any
  [style-prefix :- s/Str]
  (let [grid (str (format-grid-row "[%s-heading-row-start] \"%s-heading %s-control\" %s [%s-heading-row-end]" style-prefix)
                  (format-grid-row "[%s-input-row-start] \"%s-input %s-input\" %s [%s-input-row-end]" style-prefix)
                  "/ 50% 50%")]
    {:display             "grid"
     :grid-area           style-prefix
     :grid                grid
     ::stylefy/sub-styles {:heading
                           (merge layout/vertical-align-center-styles
                                  {:color     colors/black
                                   :grid-area (str style-prefix "-heading")})}}))

(s/defschema ^:always-validate InputWithLabelAndControlProps
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
  (let [input-styles (make-input-styles style-prefix)]
    [:div (stylefy/use-style input-styles)
     [:label (stylefy/use-sub-style
               input-styles
               :heading
               {:cypressid (str cypressid "-label")
                :for       input-id})
      label]
     input-component
     control-component]))
