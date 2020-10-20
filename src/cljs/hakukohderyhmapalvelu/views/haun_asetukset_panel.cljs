(ns hakukohderyhmapalvelu.views.haun-asetukset-panel
  (:require [hakukohderyhmapalvelu.components.common.checkbox :as c]
            [hakukohderyhmapalvelu.components.common.label :as l]
            [hakukohderyhmapalvelu.components.common.panel :as p]
            [re-frame.core :as re-frame]
            [stylefy.core :as stylefy]))

(def ^:private haun-asetukset-grid-styles
  {:display               "grid"
   :grid-template-columns "[haun-asetukset-label] 2fr [haun-asetukset-input] 4fr [end]"
   :grid-auto-rows        "30px"})

(defn- get-id-prefix [haun-asetus-key]
  (str (namespace haun-asetus-key)
       "-"
       (name haun-asetus-key)))

(def ^:private haun-asetus-checkbox-label-styles
  {:grid-column-start "haun-asetukset-label"})

(defn- haun-asetukset-label [{:keys [label
                                     for]}]
  [:div
   (stylefy/use-style haun-asetus-checkbox-label-styles)
   [l/label
    {:label label
     :for   for}]])

(defn- haun-asetukset-checkbox [{:keys [haku-oid
                                        haun-asetus-key]}]
  (let [id-prefix   (get-id-prefix haun-asetus-key)
        checkbox-id (str id-prefix "-checkbox")
        checked?    (true? @(re-frame/subscribe [:haun-asetukset/haun-asetus haku-oid haun-asetus-key]))
        disabled?   @(re-frame/subscribe [:haun-asetukset/haun-asetus-disabled? haku-oid])
        label       @(re-frame/subscribe [:translation haun-asetus-key])]
    [:<>
     [haun-asetukset-label
      {:label label
       :for   checkbox-id}]
     [c/checkbox
      {:id        checkbox-id
       :checked?  checked?
       :disabled? disabled?
       :on-change (fn []
                    (re-frame/dispatch [:haun-asetukset/set-haun-asetus
                                        haku-oid
                                        haun-asetus-key
                                        (not checked?)]))}]]))

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
      [haun-asetukset-checkbox
       {:haku-oid        haku-oid
        :haun-asetus-key :haun-asetukset/hakukohteiden-maara-rajoitus}]
      [haun-asetukset-checkbox
       {:haku-oid        haku-oid
        :haun-asetus-key :haun-asetukset/hakukohteiden-priorisointi}]
      [haun-asetukset-checkbox
       {:haku-oid        haku-oid
        :haun-asetus-key :haun-asetukset/vain-yksi-hakemus-rajoitus}]]]))

(defn haun-asetukset-panel []
  [p/panel
   {}
   @(re-frame/subscribe [:translation :haun-asetukset/title])
   [haun-asetukset]])

