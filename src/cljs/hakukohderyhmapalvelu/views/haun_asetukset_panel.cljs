(ns hakukohderyhmapalvelu.views.haun-asetukset-panel
  (:require [hakukohderyhmapalvelu.components.common.checkbox :as c]
            [hakukohderyhmapalvelu.components.common.headings :as h]
            [hakukohderyhmapalvelu.components.common.input :as i]
            [hakukohderyhmapalvelu.components.common.label :as l]
            [hakukohderyhmapalvelu.components.common.panel :as p]
            [hakukohderyhmapalvelu.dates.datetime-local :as dl]
            [hakukohderyhmapalvelu.dates.date-parser :as d]
            [hakukohderyhmapalvelu.styles.styles-colors :as colors]
            [hakukohderyhmapalvelu.styles.layout-styles :as layout]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [stylefy.core :as stylefy]))

(def ^:private haun-asetukset-grid-styles
  {:display               "grid"
   :grid-template-columns "[haun-asetukset-label] 2fr [haun-asetukset-input] 4fr [end]"
   :grid-auto-rows        "minmax(min-content, max-content)"})

(defn- get-id-prefix [haun-asetus-key]
  (str (namespace haun-asetus-key)
       "-"
       (name haun-asetus-key)))

(def ^:private haun-asetukset-grid-item-layout-styles
  (merge
    (layout/flex-column-styles "flex-start" "center")
    {:padding-left   "20px"
     :padding-top    "5px"
     :padding-bottom "5px"
     :width          "350px"}))

(def ^:private haun-asetukset-label-styles
  (merge
    haun-asetukset-grid-item-layout-styles
    {:border-left       (str "1px solid " colors/gray-lighten-3)
     :grid-column-start "haun-asetukset-label"}))

(def ^:private haun-asetukset-input-styles
  (merge
    haun-asetukset-grid-item-layout-styles
    {:grid-column-start "haun-asetukset-input"}))

(def ^:private haun-asetukset-required-legend-styles
  {:color   colors/gray-lighten-1
   :padding-top "1em"})

(defn- haun-asetukset-label-container [{:keys [component
                                               bold-left-label-margin?]}]
  [:div
   (cond-> (stylefy/use-style haun-asetukset-label-styles)
           bold-left-label-margin?
           (merge {:style {:border-left (str "2px solid " colors/blue-lighten-1)}}))
   component])

(defn- haun-asetukset-label [{:keys [id
                                     label
                                     for
                                     required?
                                     bold-left-label-margin?]}]
  [haun-asetukset-label-container
   {:component               [l/label
                              (cond-> {:id    id
                                       :label (str label (when required? " *"))}
                                      for
                                      (assoc :for for))]
    :bold-left-label-margin? bold-left-label-margin?}])

(defn- haun-asetukset-input [{:keys [input-component]}]
  [:div
   (stylefy/use-style haun-asetukset-input-styles)
   input-component])

(defn- haun-asetukset-checkbox [{:keys [haku-oid
                                        haun-asetus-key
                                        type
                                        bold-left-label-margin?]}]
  (let [id-prefix   (get-id-prefix haun-asetus-key)
        checkbox-id (str id-prefix "-checkbox")
        label-id    (str id-prefix "-label")
        checked?    @(re-frame/subscribe [:haun-asetukset/haun-asetus haku-oid haun-asetus-key])
        disabled?   @(re-frame/subscribe [:haun-asetukset/haun-asetukset-disabled? haku-oid])
        label       @(re-frame/subscribe [:translation haun-asetus-key])
        checkbox-fn (case type
                      :checkbox c/checkbox
                      :slider c/checkbox-slider)]
    [:<>
     [haun-asetukset-label
      {:id                      label-id
       :label                   label
       :bold-left-label-margin? bold-left-label-margin?}]
     [haun-asetukset-input
      {:input-component [checkbox-fn
                         {:id              checkbox-id
                          :checked?        checked?
                          :disabled?       disabled?
                          :on-change       (fn []
                                             (re-frame/dispatch [:haun-asetukset/set-haun-asetus
                                                                 haku-oid
                                                                 haun-asetus-key
                                                                 (not checked?)]))
                          :aria-labelledby label-id}]}]]))

(defn- hakukohteiden-maara-rajoitettu [{:keys [haku-oid]}]
  (let [checkbox-haun-asetus-key :haun-asetukset/hakukohteiden-maara-rajoitettu
        id-prefix                (get-id-prefix checkbox-haun-asetus-key)
        enabled?                 @(re-frame/subscribe [:haun-asetukset/haun-asetus haku-oid checkbox-haun-asetus-key])
        text-input-id            (str id-prefix "-input")
        text-input-label-id      (str id-prefix "-input-label")
        text-input-label         @(re-frame/subscribe [:translation :haun-asetukset/hakukohteiden-maara])
        disabled?                @(re-frame/subscribe [:haun-asetukset/haun-asetukset-disabled? haku-oid])
        value                    @(re-frame/subscribe [:haun-asetukset/haun-asetus haku-oid :haun-asetukset/hakukohteiden-maara-rajoitus])]
    [:<>
     [haun-asetukset-checkbox
      {:haku-oid                haku-oid
       :haun-asetus-key         checkbox-haun-asetus-key
       :type                    :slider
       :bold-left-label-margin? enabled?}]
     (when enabled?
       [:<>
        [haun-asetukset-label
         {:id                      text-input-label-id
          :label                   text-input-label
          :required?               true
          :bold-left-label-margin? true}]
        [haun-asetukset-input
         {:input-component [i/input-number
                            {:input-id   text-input-id
                             :value      value
                             :required?  true
                             :on-empty   (fn []
                                           (re-frame/dispatch [:haun-asetukset/unset-haun-asetus
                                                               haku-oid
                                                               :haun-asetukset/hakukohteiden-maara-rajoitus]))
                             :on-change  (fn [value]
                                           (re-frame/dispatch [:haun-asetukset/set-haun-asetus
                                                               haku-oid
                                                               :haun-asetukset/hakukohteiden-maara-rajoitus
                                                               value]))
                             :aria-label text-input-label
                             :min        1
                             :disabled?  disabled?}]}]])]))

(defn- haun-asetukset-date-time [{:keys [haku-oid
                                         haun-asetus-key
                                         required?
                                         bold-left-label-margin?]}]
  (let [id-prefix                 (get-id-prefix haun-asetus-key)
        label-id                  (str id-prefix "-label")
        date-time-picker-id       (str id-prefix "-date-time-picker")
        label                     @(re-frame/subscribe [:translation haun-asetus-key])
        datetime-local-supported? (dl/datetime-local-supported?)
        datetime-local-value      (some-> @(re-frame/subscribe
                                             [:haun-asetukset/haun-asetus haku-oid haun-asetus-key])
                                          d/date->iso-date-time-local-str)
        get-date-value            (fn get-date-value [datetime-local-value]
                                    (some-> datetime-local-value (subs 0 10)))
        get-time-value            (fn get-time-value [datetime-local-value]
                                    (some-> datetime-local-value (subs 11)))
        date-value                (reagent/atom (get-date-value
                                                  datetime-local-value))
        time-value                (reagent/atom (get-time-value
                                                  datetime-local-value))
        set-datetime-local        (fn set-datetime-local []
                                    (when (and @date-value
                                               @time-value)
                                      (let [datetime-local-value (str @date-value "T" @time-value)]
                                        (re-frame/dispatch [:haun-asetukset/set-haun-asetus
                                                            haku-oid
                                                            haun-asetus-key
                                                            datetime-local-value]))))]

    [:<>
     [haun-asetukset-label
      (cond-> {:id                      label-id
               :label                   label
               :required?               required?
               :bold-left-label-margin? bold-left-label-margin?}
              datetime-local-supported?
              (assoc :for date-time-picker-id))]
     [haun-asetukset-input
      {:input-component
       (if datetime-local-supported?
         [i/input-datetime-local
          (cond-> {:id        date-time-picker-id
                   :required? required?
                   :on-empty  (fn []
                                (reset! date-value nil)
                                (re-frame/dispatch [:haun-asetukset/unset-haun-asetus
                                                    haku-oid
                                                    haun-asetus-key]))
                   :on-change (fn [value]
                                (reset! date-value (some-> value (subs 0 10)))
                                (re-frame/dispatch [:haun-asetukset/set-haun-asetus
                                                    haku-oid
                                                    haun-asetus-key
                                                    value]))}
                  datetime-local-value
                  (assoc :value datetime-local-value))]
         (let [date-picker-id   (str id-prefix "-date-picker")
               time-picker-id   (str id-prefix "-time-picker")
               date-label-id    (str date-picker-id "-label")
               time-label-id    (str time-picker-id "-label")
               date-describedby @(re-frame/subscribe [:translation :haun-asetukset/input-date-describedby])
               time-describedby @(re-frame/subscribe [:translation :haun-asetukset/input-time-describedby])]
           [:div
            [l/label
             {:id     date-label-id
              :label  date-describedby
              :hidden true}]
            [i/input-date
             (cond-> {:id               date-picker-id
                      :on-change        (fn [value]
                                          (reset! date-value value)
                                          (set-datetime-local))
                      :aria-describedby date-label-id}
                     @date-value
                     (assoc :value @date-value))]
            [l/label
             {:id     time-label-id
              :label  time-describedby
              :hidden true}]
            [i/input-time
             (cond-> {:id               time-picker-id
                      :on-change        (fn [value]
                                          (reset! time-value value)
                                          (set-datetime-local))
                      :aria-describedby time-label-id}
                     @time-value
                     (assoc :value @time-value))]]))}]]))

(defn- hakijakohtainen-paikan-vastaanottoaika [{:keys [haku-oid]}]
  (let [id-prefix (get-id-prefix :haun-asetukset/hakijakohtainen-paikan-vastaanottoaika)
        label-id  (str id-prefix "-label")
        input-id  (str id-prefix "-input")
        label     @(re-frame/subscribe [:translation :haun-asetukset/hakijakohtainen-paikan-vastaanottoaika])
        disabled? @(re-frame/subscribe [:haun-asetukset/haun-asetukset-disabled? haku-oid])
        value     @(re-frame/subscribe [:haun-asetukset/haun-asetus haku-oid :haun-asetukset/hakijakohtainen-paikan-vastaanottoaika])]
    [:<>
     [haun-asetukset-label
      {:id    label-id
       :label label}]
     [haun-asetukset-input
      {:input-component [i/input-number
                         {:input-id  input-id
                          :value     value
                          :required? false
                          :on-empty  (fn []
                                       (re-frame/dispatch [:haun-asetukset/unset-haun-asetus
                                                           haku-oid
                                                           :haun-asetukset/hakijakohtainen-paikan-vastaanottoaika]))
                          :on-change (fn [value]
                                       (re-frame/dispatch [:haun-asetukset/set-haun-asetus
                                                           haku-oid
                                                           :haun-asetukset/hakijakohtainen-paikan-vastaanottoaika
                                                           value]))
                          :min       0
                          :disabled? disabled?}]}]]))

(defn- haun-asetukset-sijoittelu [{:keys [haku-oid]}]
  (let [sijoittelu? @(re-frame/subscribe [:haun-asetukset/haun-asetus haku-oid :haun-asetukset/sijoittelu])
        kk?         @(re-frame/subscribe [:haun-asetukset/kk? haku-oid])]
    (cond-> [:<>
             [haun-asetukset-checkbox
              {:haku-oid                haku-oid
               :haun-asetus-key         :haun-asetukset/sijoittelu
               :type                    :checkbox
               :bold-left-label-margin? sijoittelu?}]]
            sijoittelu?
            (into [[haun-asetukset-date-time
                    {:haku-oid                haku-oid
                     :haun-asetus-key         :haun-asetukset/valintatulokset-valmiina-viimeistaan
                     :required?               kk?
                     :bold-left-label-margin? true}]
                   [haun-asetukset-date-time
                    {:haku-oid                haku-oid
                     :haun-asetus-key         :haun-asetukset/varasijasaannot-astuvat-voimaan
                     :required?               kk?
                     :bold-left-label-margin? true}]
                   [haun-asetukset-date-time
                    {:haku-oid                haku-oid
                     :haun-asetus-key         :haun-asetukset/varasijataytto-paattyy
                     :required?               false
                     :bold-left-label-margin? true}]]))))

(defn- haun-asetukset []
  (let [haku-oid  @(re-frame/subscribe [:haun-asetukset/selected-haku-oid])
        haku      @(re-frame/subscribe [:haun-asetukset/haku haku-oid])
        lang      @(re-frame/subscribe [:lang])
        id-prefix (str "haun-asetukset-" haku-oid)
        header-id (str id-prefix "-header")
        haku-name (-> haku :nimi lang)]
    [:section
     [:header
      [h/heading
       {:cypressid header-id
        :level     :h3}
       (str haku-name)]]
     [:div
      (stylefy/use-style
        haun-asetukset-grid-styles
        {:role            "form"
         :aria-labelledby header-id})
      [hakukohteiden-maara-rajoitettu
       {:haku-oid haku-oid}]
      [haun-asetukset-checkbox
       {:haku-oid                haku-oid
        :haun-asetus-key         :haun-asetukset/jarjestetyt-hakutoiveet
        :type                    :slider
        :bold-left-label-margin? false}]
      [haun-asetukset-checkbox
       {:haku-oid                haku-oid
        :haun-asetus-key         :haun-asetukset/useita-hakemuksia
        :type                    :slider
        :bold-left-label-margin? false}]
      [hakijakohtainen-paikan-vastaanottoaika
       {:haku-oid haku-oid}]
      [haun-asetukset-date-time
       {:haku-oid                haku-oid
        :haun-asetus-key         :haun-asetukset/paikan-vastaanotto-paattyy
        :required?               false
        :bold-left-label-margin? false}]
      [haun-asetukset-date-time
       {:haku-oid                haku-oid
        :haun-asetus-key         :haun-asetukset/hakukierros-paattyy
        :required?               true
        :bold-left-label-margin? false}]
      [haun-asetukset-sijoittelu
       {:haku-oid haku-oid}]]
     [:div
      (stylefy/use-style
       haun-asetukset-required-legend-styles)
      @(re-frame/subscribe [:translation :required-legend])]]))

(defn haun-asetukset-panel []
  [p/panel
   {}
   @(re-frame/subscribe [:translation :haun-asetukset/title])
   [haun-asetukset]])

