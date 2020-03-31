(ns hakukohderyhmapalvelu.components.common.checkbox
  (:require [hakukohderyhmapalvelu.styles.styles-colors :as colors]
            [hakukohderyhmapalvelu.styles.layout-styles :as layout]
            [schema.core :as s]
            [stylefy.core :as stylefy]))

(defn- make-search-control-styles [style-prefix]
  (merge layout/vertical-align-center-styles
         {:cursor              "pointer"
          :grid-area           style-prefix
          :justify-self        "end"
          ::stylefy/sub-styles {:input
                                {:cursor "pointer"}
                                :label
                                {:color       colors/gray-lighten-1
                                 :cursor      "pointer"
                                 :margin-left "10px"
                                 :user-select "none"}}}))

(s/defschema CheckboxWithLabelProps
  {:checkbox-id  s/Str
   :cypressid    s/Str
   :label        s/Str
   :style-prefix s/Str})

(s/defn checkbox-with-label :- s/Any
  [{:keys [checkbox-id
           cypressid
           label
           style-prefix]} :- CheckboxWithLabelProps]
  (let [search-control-styles (make-search-control-styles style-prefix)]
    [:div (stylefy/use-style search-control-styles)
     [:input (stylefy/use-sub-style
               search-control-styles
               :input
               {:cypressid (str cypressid "-input")
                :id        checkbox-id
                :type      "checkbox"})]
     [:label (stylefy/use-sub-style
               search-control-styles
               :label
               {:cypressid (str cypressid "-label")
                :for       checkbox-id})
      label]]))
