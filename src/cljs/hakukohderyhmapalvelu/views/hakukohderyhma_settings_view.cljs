(ns hakukohderyhmapalvelu.views.hakukohderyhma-settings-view
  (:require [hakukohderyhmapalvelu.components.common.input :as input]
            [stylefy.core :as stylefy]
            [hakukohderyhmapalvelu.components.common.checkbox :as checkbox]
            [hakukohderyhmapalvelu.components.common.label :as label]))

(def ^:private settings-view-style
  {:display  "flex"
   :flex-direction "column"})



(defn hakukohderyha-settings-view []
 [:div (stylefy/use-style settings-view-style {:cypressid "hakukohderyhma-settings-view"})
  [:<>
    (checkbox/checkbox-slider {:checked?  false
                                :cypressid "rajaava-checkbox"
                                :disabled? false
                                :id        "rajaava-checkbox"
                                :aria-labelledby     "Rajaava"
                                :on-change (fn [event]
                                             (println "Muutettu" event))})]
    (label/label { :id "rajaava-label"
                :label "Rajaava"
                :for "rajaava-checkbox"})
  [:<>
   (label/label { :id "max-hakukohteet-label"
                  :label "Ryhmän hakukohteita valittavissa enintään"
                  :for "max-hakukohteet"})
   [input/input-number {:input-id "max-hakukohteet"
                        :required? false
                        :on-change (fn [event]
                                     (println "Muutettu numeroa " event))
                        :value "1"
                        :min 1
                        :disabled? false
                        :cypressid "max-hakukohteet"
                        }]]
  ])