(ns hakukohderyhmapalvelu.views.haun-asetukset-panel
  (:require [hakukohderyhmapalvelu.components.common.checkbox :as c]
            [hakukohderyhmapalvelu.components.common.headings :as h]
            [hakukohderyhmapalvelu.components.common.input :as i]
            [hakukohderyhmapalvelu.components.common.label :as l]
            [hakukohderyhmapalvelu.components.common.link :as a]
            [hakukohderyhmapalvelu.components.common.panel :as p]
            [hakukohderyhmapalvelu.dates.datetime-local :as dl]
            [hakukohderyhmapalvelu.dates.date-parser :as d]
            [hakukohderyhmapalvelu.styles.styles-colors :as colors]
            [hakukohderyhmapalvelu.styles.layout-styles :as layout]
            [hakukohderyhmapalvelu.urls :as urls]
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

(def ^:private haun-asetukset-haun-tiedot-styles
  {:border-left (str "1px solid " colors/gray-lighten-3)
   :padding     "20px"})

(def ^:private haun-asetukset-haun-tiedot-card-styles
  {:background-color      colors/gray-lighten-5
   :padding               "20px"
   :display               "grid"
   :grid-template-columns "[haun-tiedot-label] min-content 20px [haun-tiedot-data] 1fr [haun-tiedot-modify] min-content"})

(def ^:private haun-asetukset-haun-tiedot-label-styles
  {:grid-column-start "haun-tiedot-label"})

(def ^:private haun-asetukset-haun-tiedot-data-styles
  {:grid-column-start "haun-tiedot-data"})

(def ^:private haun-asetukset-haun-tiedot-hakuajat-styles
  {:list-style "none"
   :margin     "0"
   :padding    "0"})

(def ^:private haun-asetukset-haun-tiedot-modify-styles
  {:grid-column-start "haun-tiedot-modify"
   :grid-row-start    "1"
   :white-space       "nowrap"})

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
  {:color       colors/gray-lighten-1
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
                          :aria-labelledby label-id
                          :cypressid       checkbox-id}]}]]))

(defn- hakukohteiden-maara-rajoitettu [{:keys [haku-oid]}]
  (let [checkbox-haun-asetus-key :haun-asetukset/hakukohteiden-maara-rajoitettu
        id-prefix                (get-id-prefix checkbox-haun-asetus-key)
        enabled?                 @(re-frame/subscribe [:haun-asetukset/haun-asetus haku-oid checkbox-haun-asetus-key])
        text-input-id            (str id-prefix "-input")
        text-input-label-id      (str id-prefix "-input-label")
        text-input-label         @(re-frame/subscribe [:translation :haun-asetukset/hakukohteiden-enimmaismaara])
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
                            (merge {:input-id   text-input-id
                                    :required?  true
                                    :on-change  (fn [value]
                                                  (re-frame/dispatch [:haun-asetukset/set-haun-asetus
                                                                      haku-oid
                                                                      :haun-asetukset/hakukohteiden-maara-rajoitus
                                                                      value]))
                                    :aria-label text-input-label
                                    :min        1
                                    :disabled?  disabled?
                                    :cypressid  (str id-prefix "-input")}
                                   (when value
                                     {:value value}))]}]])]))

(defn- haun-asetukset-date-and-time [{:keys [id-prefix
                                             value]}]
  (let [get-date-value      (fn get-date-value [datetime-local-value]
                              (some-> datetime-local-value (subs 0 10)))
        get-time-value      (fn get-time-value [datetime-local-value]
                              (some-> datetime-local-value (subs 11)))
        date-picker-id      (str id-prefix "-date-picker")
        time-picker-id      (str id-prefix "-time-picker")
        date-describedby-id (str date-picker-id "-describedby")
        time-describedby-id (str time-picker-id "-describedby")
        date-describedby    @(re-frame/subscribe [:translation :haun-asetukset/syota-paivamaara])
        time-describedby    @(re-frame/subscribe [:translation :haun-asetukset/syota-kellonaika])
        prev-value-prop     (atom value)
        local-date-value    (reagent/atom (get-date-value value))
        local-time-value    (reagent/atom (get-time-value value))
        set-datetime-local  (fn set-datetime-local [on-change date-value time-value]
                              (cond (and (empty? date-value) (empty? time-value))
                                    (on-change "")
                                    (and (seq date-value) (seq time-value))
                                    (on-change (str date-value "T" time-value))))]
    (fn [{:keys [value on-change required? disabled? label-id]}]
      (when (not= value @prev-value-prop)
        (reset! prev-value-prop value)
        (reset! local-date-value (get-date-value value))
        (reset! local-time-value (get-time-value value)))
      [:div
       [l/label
        {:id     date-describedby-id
         :label  date-describedby
         :hidden true}]
       [i/input-date
        (cond-> {:id               date-picker-id
                 :required?        required?
                 :disabled?        disabled?
                 :on-change        (fn [value]
                                     (set-datetime-local
                                      on-change
                                      (reset! local-date-value value)
                                      @local-time-value))
                 :aria-describedby date-describedby-id
                 :aria-labelledby  label-id}
                @local-date-value
                (assoc :value @local-date-value))]
       [l/label
        {:id     time-describedby-id
         :label  time-describedby
         :hidden true}]
       [i/input-time
        (cond-> {:id               time-picker-id
                 :required?        required?
                 :disabled?        disabled?
                 :on-change        (fn [value]
                                     (set-datetime-local
                                      on-change
                                      @local-date-value
                                      (reset! local-time-value value)))
                 :aria-describedby time-describedby-id
                 :aria-labelledby  label-id}
                @local-time-value
                (assoc :value @local-time-value))]])))

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
        disabled?                 @(re-frame/subscribe [:haun-asetukset/haun-asetukset-disabled? haku-oid])]
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
                   :disabled? disabled?
                   :on-change (fn [value]
                                (re-frame/dispatch [:haun-asetukset/set-haun-asetus
                                                    haku-oid
                                                    haun-asetus-key
                                                    value]))}
                  datetime-local-value
                  (assoc :value datetime-local-value))]
         [haun-asetukset-date-and-time
          (cond-> {:on-change (fn [value]
                                (re-frame/dispatch [:haun-asetukset/set-haun-asetus
                                                    haku-oid
                                                    haun-asetus-key
                                                    value]))
                   :required? required?
                   :disabled? disabled?
                   :id-prefix id-prefix
                   :label-id  label-id}
                  datetime-local-value
                  (assoc :value datetime-local-value))])}]]))

(defn- liitteiden-muokkauksen-takaraja [{:keys [haku-oid]}]
  (let [id-prefix (get-id-prefix :haun-asetukset/liitteiden-muokkauksen-takaraja)
        label-id  (str id-prefix "-label")
        input-id  (str id-prefix "-input")
        label     @(re-frame/subscribe [:translation :haun-asetukset/liitteiden-muokkauksen-takaraja])
        disabled? @(re-frame/subscribe [:haun-asetukset/haun-asetukset-disabled? haku-oid])
        value     @(re-frame/subscribe [:haun-asetukset/haun-asetus haku-oid :haun-asetukset/liitteiden-muokkauksen-takaraja])]
    [:<>
     [haun-asetukset-label
      {:id    label-id
       :for   input-id
       :label label}]
     [haun-asetukset-input
      {:input-component [i/input-number
                         (merge {:input-id  input-id
                                 :required? false
                                 :on-change (fn [value]
                                              (re-frame/dispatch [:haun-asetukset/set-haun-asetus
                                                                  haku-oid
                                                                  :haun-asetukset/liitteiden-muokkauksen-takaraja
                                                                  value]))
                                 :min       0
                                 :disabled? disabled?
                                 :cypressid input-id}
                                (when value
                                  {:value value}))]}]]))

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
       :for   input-id
       :label label}]
     [haun-asetukset-input
      {:input-component [i/input-number
                         (merge {:input-id  input-id
                                 :required? false
                                 :on-change (fn [value]
                                              (re-frame/dispatch [:haun-asetukset/set-haun-asetus
                                                                  haku-oid
                                                                  :haun-asetukset/hakijakohtainen-paikan-vastaanottoaika
                                                                  value]))
                                 :min       0
                                 :disabled? disabled?
                                 :cypressid input-id}
                                (when value
                                  {:value value}))]}]]))

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

(defn- hakuajat [haku-oid hakuajat-label-id]
  (let [hakuajat @(re-frame/subscribe [:haun-asetukset/hakuajat haku-oid])]
    [:div
     (stylefy/use-style haun-asetukset-haun-tiedot-data-styles)
     (into [:ol
            (let [cypressid (str "hakuajat-" haku-oid)]
              (merge (stylefy/use-style haun-asetukset-haun-tiedot-hakuajat-styles)
                     {:aria-labelledby hakuajat-label-id
                      :cypressid       cypressid}))]
           (map (fn [hakuaika]
                  [:li
                   (:alkaa hakuaika) " - " (:paattyy hakuaika)])
                hakuajat))]))

(defn- haun-tiedot [haku-oid haku-name-id]
  (let [form               @(re-frame/subscribe [:haun-asetukset/form haku-oid])
        lang               @(re-frame/subscribe [:lang])
        hakuajat-label-id  (str "haun-asetukset-" haku-oid "-hakuajat-label")
        form-name-label-id (str "haun-asetukset-" haku-oid "-form-name-label")
        form-name-id       (str "haun-asetukset-" haku-oid "-form-name")]
    [:div
     (stylefy/use-style haun-asetukset-haun-tiedot-styles)
     [:div
      (stylefy/use-style haun-asetukset-haun-tiedot-card-styles)
      [:div
       (stylefy/use-style haun-asetukset-haun-tiedot-label-styles)
       [:span
        {:id hakuajat-label-id}
        @(re-frame/subscribe [:translation :yleiset/hakuajat])]]
      [hakuajat haku-oid hakuajat-label-id]
      [:div
       (stylefy/use-style haun-asetukset-haun-tiedot-label-styles)
       [:span
        {:id form-name-label-id}
        @(re-frame/subscribe [:translation :yleiset/hakulomake])]]
      [:div
       (stylefy/use-style haun-asetukset-haun-tiedot-data-styles)
       (when form
         [:<>
          [:span
           {:id              form-name-id
            :aria-labelledby form-name-label-id}
           (get-in form [:name lang])
           " "]
          [a/link
           {:href             (urls/get-url :lomake-editori.editor (:key form))
            :target           "_blank"
            :label            @(re-frame/subscribe [:translation :yleiset/muokkaa-lomaketta])
            :aria-describedby form-name-id}]])]
      [:div
       (stylefy/use-style haun-asetukset-haun-tiedot-modify-styles)
       [a/link
        {:href             (urls/get-url :kouta.haku haku-oid)
         :target           "_blank"
         :label            @(re-frame/subscribe [:translation :yleiset/muokkaa-hakua])
         :aria-describedby haku-name-id}]]]]))

(defn- haun-asetukset []
  (let [haku-oid  @(re-frame/subscribe [:haun-asetukset/selected-haku-oid])
        haku      @(re-frame/subscribe [:haun-asetukset/haku haku-oid])
        lang      @(re-frame/subscribe [:lang])
        toinen-aste? @(re-frame/subscribe [:haun-asetukset/toinen_aste? haku-oid])
        kk? @(re-frame/subscribe [:haun-asetukset/kk? haku-oid])
        id-prefix (str "haun-asetukset-" haku-oid)
        header-id (str id-prefix "-header")
        haku-name (-> haku :nimi lang)]
    [:section
     [:header
      [h/heading
       {:cypressid header-id
        :level     :h3
        :id        header-id}
       (str haku-name)]]
     [haun-tiedot haku-oid header-id]
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
       {:haku-oid haku-oid}]
      [liitteiden-muokkauksen-takaraja
       {:haku-oid haku-oid}]
      ;fixme placeholder for Valintatulosten julkaiseminen hakijoille (Aikaväli)
      (when kk?
        [haun-asetukset-date-time
         {:haku-oid                haku-oid
          :haun-asetus-key         :haun-asetukset/ilmoittautuminen-paattyy
          :required?               false
          :bold-left-label-margin? false}]
        [haun-asetukset-date-time
         {:haku-oid                haku-oid
          :haun-asetus-key         :haun-asetukset/automaattinen-hakukelpoisuus-paattyy
          :required?               false
          :bold-left-label-margin? false}]
        )
      (when toinen-aste?
        [haun-asetukset-date-time
         {:haku-oid                haku-oid
          :haun-asetus-key         :haun-asetukset/harkinnanvaraisen-valinnan-paatosten-tallennus-paattyy
          :required?               false ;fixme
          :bold-left-label-margin? false}]
        [haun-asetukset-date-time
         {:haku-oid                haku-oid
          :haun-asetus-key         :haun-asetukset/oppilaitosten-virkailijoiden-valintapalvelun-kaytto-estetty
          :required?               false ;fixme
          :bold-left-label-margin? false}]
        [haun-asetukset-date-time
         {:haku-oid                haku-oid
          :haun-asetus-key         :haun-asetukset/valintaesityksen-hyvaksyminen
          :required?               false ;fixme
          :bold-left-label-margin? false}]
        [haun-asetukset-date-time
         {:haku-oid                haku-oid
          :haun-asetus-key         :haun-asetukset/koetulosten-tallentaminen
          :required?               false ;fixme
          :bold-left-label-margin? false}])
      ]
     [:div
      (stylefy/use-style
        haun-asetukset-required-legend-styles)
      @(re-frame/subscribe [:translation :yleiset/pakolliset-kentat])]]))

(defn haun-asetukset-panel []
  [p/panel
   {}
   @(re-frame/subscribe [:translation :haun-asetukset/haun-asetukset])
   [haun-asetukset]])

