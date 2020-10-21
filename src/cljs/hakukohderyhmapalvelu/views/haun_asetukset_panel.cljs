(ns hakukohderyhmapalvelu.views.haun-asetukset-panel
  (:require [hakukohderyhmapalvelu.components.common.checkbox :as c]
            [hakukohderyhmapalvelu.components.common.input :as i]
            [hakukohderyhmapalvelu.components.common.label :as l]
            [hakukohderyhmapalvelu.components.common.panel :as p]
            [hakukohderyhmapalvelu.styles.layout-styles :as layout]
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
    {:padding "5px 0"}))

(def ^:private haun-asetukset-label-styles
  (merge
    haun-asetukset-grid-item-layout-styles
    {:grid-column-start "haun-asetukset-label"}))

(def ^:private haun-asetukset-input-styles
  (merge
    haun-asetukset-grid-item-layout-styles
    {:grid-column-start "haun-asetukset-input"}))

(defn- haun-asetukset-label-container [{:keys [component]}]
  [:div
   (stylefy/use-style haun-asetukset-label-styles)
   component])

(defn- haun-asetukset-label [{:keys [id
                                     label]}]
  [haun-asetukset-label-container
   {:component [l/label
                {:id    id
                 :label label}]}])

(defn- haun-asetukset-input [{:keys [input-component]}]
  [:div
   (stylefy/use-style haun-asetukset-input-styles)
   input-component])

(defn- haun-asetukset-checkbox-slider [{:keys [haku-oid
                                               haun-asetus-key]}]
  (let [id-prefix   (get-id-prefix haun-asetus-key)
        checkbox-id (str id-prefix "-checkbox")
        label-id    (str id-prefix "-label")
        checked?    @(re-frame/subscribe [:haun-asetukset/haun-asetus haku-oid haun-asetus-key])
        disabled?   @(re-frame/subscribe [:haun-asetukset/haun-asetukset-disabled? haku-oid])
        label       @(re-frame/subscribe [:translation haun-asetus-key])]
    [:<>
     [haun-asetukset-label
      {:id    label-id
       :label label}]
     [haun-asetukset-input
      {:input-component [c/checkbox-slider
                         {:id              checkbox-id
                          :aria-labelledby label-id
                          :checked?        checked?
                          :disabled?       disabled?
                          :on-change       (fn []
                                             (re-frame/dispatch [:haun-asetukset/set-haun-asetus
                                                                 haku-oid
                                                                 haun-asetus-key
                                                                 (not checked?)]))}]}]]))

(defn- hakukohteiden-maara-rajoitettu [{:keys [haku-oid]}]
  (let [checkbox-haun-asetus-key :haun-asetukset/hakukohteiden-maara-rajoitettu
        id-prefix                (get-id-prefix checkbox-haun-asetus-key)
        enabled?                 @(re-frame/subscribe [:haun-asetukset/haun-asetus haku-oid checkbox-haun-asetus-key])
        text-input-id            (str id-prefix "-input")
        text-input-label         @(re-frame/subscribe [:translation :haun-asetukset/hakukohteiden-maara])
        disabled?                @(re-frame/subscribe [:haun-asetukset/haun-asetukset-disabled? haku-oid])]
    [:<>
     [haun-asetukset-checkbox-slider
      {:haku-oid        haku-oid
       :haun-asetus-key checkbox-haun-asetus-key}]
     (when enabled?
       [haun-asetukset-label-container
        {:component [i/input-number
                     {:input-id    text-input-id
                      :on-change   (fn [value]
                                     (re-frame/dispatch [:haun-asetukset/set-haun-asetus
                                                         haku-oid
                                                         :haun-asetukset/hakukohteiden-maara-rajoitus
                                                         value]))
                      :placeholder text-input-label
                      :aria-label  text-input-label
                      :min         1
                      :disabled?   disabled?}]}])]))

(defn- haun-asetukset []
  (let [haku-oid  @(re-frame/subscribe [:haun-asetukset/selected-haku-oid])
        haku      @(re-frame/subscribe [:haun-asetukset/haku haku-oid])
        lang      @(re-frame/subscribe [:lang])
        id-prefix (str "haun-asetukset-" haku-oid)
        header-id (str id-prefix "-header")
        haku-name (-> haku :nimi lang)]
    [:section
     [:header
      [:h3
       {:id header-id}
       (str haku-name)]]
     [:div
      (stylefy/use-style
        haun-asetukset-grid-styles
        {:role            "form"
         :aria-labelledby header-id})
      [hakukohteiden-maara-rajoitettu
       {:haku-oid haku-oid}]
      [haun-asetukset-checkbox-slider
       {:haku-oid        haku-oid
        :haun-asetus-key :haun-asetukset/jarjestetyt-hakutoiveet}]
      [haun-asetukset-checkbox-slider
       {:haku-oid        haku-oid
        :haun-asetus-key :haun-asetukset/useita-hakemuksia}]]]))

(defn haun-asetukset-panel []
  [p/panel
   {}
   @(re-frame/subscribe [:translation :haun-asetukset/title])
   [haun-asetukset]])

