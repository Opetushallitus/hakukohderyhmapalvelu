(ns hakukohderyhmapalvelu.views.hakukohderyhma-settings-view
  (:require [hakukohderyhmapalvelu.components.common.input :as input]
            [hakukohderyhmapalvelu.components.common.button :as button]
            [stylefy.core :as stylefy]
            [hakukohderyhmapalvelu.components.common.checkbox :as checkbox]
            [hakukohderyhmapalvelu.subs.hakukohderyhma-subs :as hakukohderyhma-subs]
            [hakukohderyhmapalvelu.events.hakukohderyhmien-hallinta-events :as hakukohderyhma-events]
            [re-frame.core :refer [dispatch subscribe]]
            [hakukohderyhmapalvelu.components.common.label :as label]))

(def ^:private settings-view-style
  {:display        "flex"
   :flex-direction "column"
   :grid-area      "hakukohderyhma-settings-view"})

(defn- max-hakukohteet
  [selected-ryhma]
  [:div (stylefy/use-style {:display        "flex"
                            :flex-direction "row"
                            :align-items    "center"})
   (label/label {:id    "max-hakukohteet-label"
                 :label @(subscribe [:translation :hakukohderyhma/max-hakukohteet])
                 :for   "max-hakukohteet"}
                {:font-size    "x-small"
                 :margin-right "0.5rem"})
   [input/input-number {:input-id  "max-hakukohteet"
                        :required? false
                        :on-change (fn [event]
                                     (let [settings (:settings selected-ryhma)
                                           updated-settings (assoc settings :max-hakukohteet (js/parseInt event 10))]
                                       (dispatch [hakukohderyhma-events/hakukohderyhma-update-settings updated-settings])
                                       ))
                        :value     (str (get-in selected-ryhma [:settings :max-hakukohteet]))
                        :min       1
                        :disabled? false
                        :cypressid "max-hakukohteet"}
    {:width     "3rem"
     :font-size "small"
     :height    "2rem"}]])

(defn- yo-amm-autom-hakukelpoisuus-checkbox
  [selected-ryhma]
  [:<>
   [:div (stylefy/use-style {:display        "flex"
                             :flex-direction "row"})
    (checkbox/checkbox-slider {:checked?        (get-in selected-ryhma [:settings :yo-amm-autom-hakukelpoisuus])
                               :cypressid       "yo-amm-autom-hakukelpoisuus-checkbox"
                               :id              "yo-amm-autom-hakukelpoisuus-checkbox"
                               :aria-labelledby @(subscribe [:translation :hakukohderyhma/yo-amm-autom-hakukelpoisuus])
                               :on-change       (fn []
                                                  (let [settings (:settings selected-ryhma)
                                                        updated-settings (assoc settings :yo-amm-autom-hakukelpoisuus (not (:yo-amm-autom-hakukelpoisuus settings)))]
                                                    (dispatch [hakukohderyhma-events/hakukohderyhma-update-settings updated-settings])
                                                    ))})
    (label/label {:id    "yo-amm-autom-hakukelpoisuus-label"
                  :label @(subscribe [:translation :hakukohderyhma/yo-amm-autom-hakukelpoisuus])
                  :for   "yo-amm-autom-hakukelpoisuus-checkbox"}
                 {:margin-left "1rem"
                  :font-size   "small"})]])

(defn- save-button [selected-ryhma]
  [:div (stylefy/use-style {:display        "flex"
                            :flex-direction "row"})
   [button/button {:cypressid    "save-asetukset"
                   :disabled?    false
                   :label        @(subscribe [:translation :hakukohderyhma/tallenna-jarjestys])
                   :on-click      (fn []
                                    (let [settings (:settings selected-ryhma)]
                                      (js/console.log (str "Saving settings: " selected-ryhma))
                                      (dispatch [hakukohderyhma-events/hakukohderyhma-update-settings settings])))
                   :style-prefix "save-asetukset-btn"
                   :custom-style {:is-danger  false
                                  :margin-left "15px"
                                  }}]])

(defn- priorisoiva-checkbox
  [selected-ryhma disabled?]
  (let [priorisoiva-checked? (boolean (get-in selected-ryhma [:settings :priorisoiva])) ;fixme
        changed? (boolean (get selected-ryhma :prioriteettijarjestys-changed))]
    [:<>
     [:div (stylefy/use-style {:display        "flex"
                               :flex-direction "row"})
      (checkbox/checkbox-slider {:checked?        priorisoiva-checked?
                                 :cypressid       "priorisoiva-checkbox"
                                 :id              "priorisoiva-checkbox"
                                 :disabled?        disabled?
                                 :aria-labelledby @(subscribe [:translation :hakukohderyhma/priorisoiva])
                                 :on-change       (fn []
                                                    (dispatch [hakukohderyhma-events/hakukohderyhma-toggle-priorisoiva]))})
      (label/label {:id    "priorisoiva-label"
                    :label @(subscribe [:translation :hakukohderyhma/priorisoiva])
                    :for   "priorisoiva-checkbox"}
                   {:margin-left "1rem"
                    :font-size   "small"})
      (when (and priorisoiva-checked? changed?) (save-button selected-ryhma))]]))

(defn- rajaava-checkbox
  [selected-ryhma disabled?]
  (let [rajaava-checked? (get-in selected-ryhma [:settings :rajaava])]
    [:<>
     [:div (stylefy/use-style {:display        "flex"
                               :flex-direction "row"
                               :margin-top "1em"})
      (checkbox/checkbox-slider {:checked?        rajaava-checked?
                                 :cypressid       "rajaava-checkbox"
                                 :id              "rajaava-checkbox"
                                 :disabled?        disabled?
                                 :aria-labelledby @(subscribe [:translation :hakukohderyhma/rajaava])
                                 :on-change       (fn []
                                                    (dispatch [hakukohderyhma-events/hakukohderyhma-toggle-rajaava]))})
      (label/label {:id    "rajaava-label"
                    :label @(subscribe [:translation :hakukohderyhma/rajaava])
                    :for   "rajaava-checkbox"}
                   {:margin-left "1rem"
                    :font-size   "small"})]
     (when (get-in selected-ryhma [:settings :rajaava])
       (max-hakukohteet selected-ryhma))]
    )
  )

(defn- priorisoiva-and-rajaava-checkboxes [selected-ryhma]
  (let [rajaava-checked? (boolean (get-in selected-ryhma [:settings :rajaava]))
        priorisoiva-checked? (boolean (get-in selected-ryhma [:settings :priorisoiva]))]
    [:div
     (priorisoiva-checkbox selected-ryhma rajaava-checked?)
     (rajaava-checkbox selected-ryhma priorisoiva-checked?)]))

(defn- jyemp-checkbox
  [selected-ryhma]
  [:div (stylefy/use-style {:display        "flex"
                            :flex-direction "row"
                            :margin-top     "1rem"})
    (checkbox/checkbox-slider {:checked?        (get-in selected-ryhma [:settings :jos-ylioppilastutkinto-ei-muita-pohjakoulutusliitepyyntoja])
                               :cypressid       "jyemp-checkbox"
                               :id              "jyemp-checkbox"
                               :aria-labelledby @(subscribe [:translation :hakukohderyhma/jyemp])
                               :on-change       (fn []
                                                  (let [settings (:settings selected-ryhma)
                                                        jyemp (not (:jos-ylioppilastutkinto-ei-muita-pohjakoulutusliitepyyntoja settings))
                                                        updated-settings (assoc settings :jos-ylioppilastutkinto-ei-muita-pohjakoulutusliitepyyntoja jyemp)]
                                                    (dispatch [hakukohderyhma-events/hakukohderyhma-update-settings updated-settings])))})
    (label/label {:id    "jyemp-label"
                  :label @(subscribe [:translation :hakukohderyhma/jyemp])
                  :for   "jyemp-checkbox"}
                 {:margin-left "0.5rem"
                  :font-size   "small"})])

(defn hakukohderyhma-settings-view
  []
  (let [selected-ryhma @(subscribe [hakukohderyhma-subs/selected-hakukohderyhma])]
    [:div (stylefy/use-style settings-view-style {:cypressid "hakukohderyhma-settings-view"})
     (when selected-ryhma
       [:<>
        (priorisoiva-and-rajaava-checkboxes selected-ryhma)
        (jyemp-checkbox selected-ryhma)
        (yo-amm-autom-hakukelpoisuus-checkbox selected-ryhma)])]))
