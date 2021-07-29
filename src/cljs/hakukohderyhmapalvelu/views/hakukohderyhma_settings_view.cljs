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
  [selected-ryhma]
  [:div (stylefy/use-style {:display "flex"
                            :flex-direction "row"
                            :align-items "center"})
   (label/label { :id "max-hakukohteet-label"
                 :label @(subscribe [:translation :hakukohderyhma/max-hakukohteet])
                 :for "max-hakukohteet"}
                {:font-size "x-small"
                 :margin-right "0.5rem"})
   [input/input-number {:input-id "max-hakukohteet"
                        :required? false
                        :on-change (fn [event]
                                     (let [settings (:settings selected-ryhma)
                                           updated-settings (assoc settings :max-hakukohteet (js/parseInt event 10))]
                                          (dispatch [hakukohderyhma-events/hakukohderyhma-update-settings updated-settings])
                                       ))
                        :value (str (get-in selected-ryhma [:settings :max-hakukohteet]))
                        :min 1
                        :disabled? false
                        :cypressid "max-hakukohteet"
                        }
                        {:width "3rem"
                         :font-size "small"
                         :height "2rem"}]])

(defn- rajaava-checkbox
  [selected-ryhma]
  [:<>
    [:div (stylefy/use-style {:display "flex"
                              :flex-direction "row"})
      (checkbox/checkbox-slider {:checked?  (get-in selected-ryhma [:settings :rajaava])
                                :cypressid "rajaava-checkbox"
                                :id        "rajaava-checkbox"
                                :aria-labelledby     @(subscribe [:translation :hakukohderyhma/rajaava])
                                :on-change (fn []
                                             (dispatch [hakukohderyhma-events/hakukohderyhma-toggle-rajaava]))})
      (label/label { :id "rajaava-label"
                   :label @(subscribe [:translation :hakukohderyhma/rajaava])
                   :for "rajaava-checkbox"}
                  {:margin-left "1rem"
                   :font-size "small"})]
    (when (get-in selected-ryhma [:settings :rajaava])
      (max-hakukohteet selected-ryhma))])

(defn hakukohderyha-settings-view
  []
  (let [selected-ryhma @(subscribe [hakukohderyhma-subs/selected-hakukohderyhma])]
    [:div (stylefy/use-style settings-view-style {:cypressid "hakukohderyhma-settings-view"})
      (when (not (nil? selected-ryhma))
        (rajaava-checkbox selected-ryhma))
    ]))