(ns hakukohderyhmapalvelu.views.hakukohderyhma-settings-view
  (:require [hakukohderyhmapalvelu.components.common.input :as input]
            [stylefy.core :as stylefy]
            [hakukohderyhmapalvelu.components.common.checkbox :as checkbox]
            [hakukohderyhmapalvelu.subs.hakukohderyhma-subs :as hakukohderyhma-subs]
            [hakukohderyhmapalvelu.events.hakukohderyhmien-hallinta-events :as hakukohderyhma-events]
            [re-frame.core :refer [dispatch subscribe]]
            [hakukohderyhmapalvelu.components.common.label :as label]))

(def ^:private settings-view-style
  {:display  "flex"
   :flex-direction "column"
   :grid-area "hakukohderyhma-settings-view"})

(defn- max-hakukohteet
  []
  [:div (stylefy/use-style {:display "flex"
                            :flex-direction "row"})
   (label/label { :id "max-hakukohteet-label"
                 :label "Ryhmän hakukohteita valittavissa enintään"
                 :for "max-hakukohteet"}
                {:font-size "x-small"})
   [input/input-number {:input-id "max-hakukohteet"
                        :required? false
                        :on-change (fn [event]
                                     (println "Muutettu numeroa " event))
                        :value "1"
                        :min 1
                        :disabled? false
                        :cypressid "max-hakukohteet"
                        }]])

(defn- rajaava-checkbox
  [selected-ryhma]
  [:<>
    [:div (stylefy/use-style {:display "flex"
                              :flex-direction "row"})
      (checkbox/checkbox-slider {:checked?  (get-in selected-ryhma [:settings :rajaava])
                                :cypressid "rajaava-checkbox"
                                :disabled? false
                                :id        "rajaava-checkbox"
                                :aria-labelledby     "Rajaava"
                                :on-change (fn []
                                             (dispatch [hakukohderyhma-events/hakukohderyhma-toggle-rajaava]))})
      (label/label { :id "rajaava-label"
                   :label "Rajaava"
                   :for "rajaava-checkbox"}
                  {:margin-left "1rem"
                   :font-size "small"})]
    (when (get-in selected-ryhma [:settings :rajaava])
      (max-hakukohteet))])

(defn hakukohderyha-settings-view
  []
  (let [selected-ryhma @(subscribe [hakukohderyhma-subs/selected-hakukohderyhma])]
    [:div (stylefy/use-style settings-view-style {:cypressid "hakukohderyhma-settings-view"})
      (when (not (nil? selected-ryhma))
        (rajaava-checkbox selected-ryhma))
    ]))