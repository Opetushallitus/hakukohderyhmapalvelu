(ns hakukohderyhmapalvelu.views.panel-menu
  (:require [hakukohderyhmapalvelu.components.common.link :as l]
            [hakukohderyhmapalvelu.styles.styles-colors :as colors]
            [hakukohderyhmapalvelu.styles.layout-styles :as layout]
            [hakukohderyhmapalvelu.schemas.app-db-schemas :as schema]
            [re-frame.core :as re-frame]
            [stylefy.core :as stylefy]))

(def ^:private panel-menu-styles
  (merge {:align-items      "center"
          :background-color colors/white
          :flex-flow        "row nowrap"
          :margin           0
          ::stylefy/manual  [[":not(:first-child)"
                              {:margin-left "20px"}]]}
         layout/horizontal-center-styles))

(def ^:private panel-menu-item-styles
  (merge {:list-style  "none"
          :line-height "50px"}
         layout/vertical-align-center-styles))

(def ^:private panel-menu-selected-item-styles
  (merge panel-menu-item-styles
         {:border-bottom (str "1px solid " colors/blue-lighten-2)}))

(defn- panel-menu-link
  [{:keys [panel]}]
  (let [active-panel  @(re-frame/subscribe [:panel-menu/active-panel])
        label         @(re-frame/subscribe [:translation panel])
        active-panel? (= panel active-panel)]
    [:li
     (stylefy/use-style
       (if active-panel? panel-menu-selected-item-styles panel-menu-item-styles)
       {:role "none"})
     [l/link
      {:href     "#"
       :label    label
       :on-click (fn []
                   (re-frame/dispatch [:panel-menu/set-active-panel panel]))
       :role     "menuitem"
       :tabindex (if active-panel?
                   0
                   -1)}]]))

(defn panel-menu []
  (let [panel-menu-label @(re-frame/subscribe [:translation :panel-menu/tarjonnan-asetukset])]
    [:nav
     {:aria-label panel-menu-label}
     [:ul
      (stylefy/use-style
        panel-menu-styles
        {:id         "panel-menu"
         :role       "menubar"
         :aria-label panel-menu-label})
      (map (fn [panel]
             ^{:key (str "panel-menu-item-" (name panel))}
             [panel-menu-link
              {:panel panel}])
           schema/panels)]]))
