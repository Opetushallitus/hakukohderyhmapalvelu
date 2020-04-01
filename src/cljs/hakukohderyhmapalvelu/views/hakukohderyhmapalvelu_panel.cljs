(ns hakukohderyhmapalvelu.views.hakukohderyhmapalvelu-panel
  (:require [hakukohderyhmapalvelu.components.common.button :as b]
            [hakukohderyhmapalvelu.components.common.checkbox :as checkbox]
            [hakukohderyhmapalvelu.components.common.grid :as grid]
            [hakukohderyhmapalvelu.components.common.input :as input]
            [hakukohderyhmapalvelu.components.common.link :as l]
            [hakukohderyhmapalvelu.components.common.panel :as p]
            [hakukohderyhmapalvelu.styles.layout-styles :as layout]
            [re-frame.core :as re-frame]
            [stylefy.core :as stylefy]))

(def ^:private hakukohderyhmapalvelu-grid-styles
  {:display  "grid"
   :grid     (str "[haku-row-start] \"haku-search hakukohderyhma-create\" 1fr [haku-row-end]"
                  "[hakukohderyhma-row-start] \". hakukohderyhma-select\" 1fr [hakukohderyhma-row-end]"
                  "/ 1fr 1fr")
   :grid-gap "15px"})

(defn- haku-search []
  (let [input-id     "haku-search-input"
        style-prefix "haku-search"]
    [grid/input-with-label-and-control
     {:control-component [checkbox/checkbox-with-label {:checkbox-id  "haku-search-checkbox"
                                                        :cypressid    "haku-search-checkbox"
                                                        :label        "Näytä myös päättyneet"
                                                        :style-prefix (str style-prefix "-control")}]
      :cypressid         "haku-search"
      :input-component   [input/input-text {:cypressid    "haku-search-input"
                                            :input-id     input-id
                                            :on-change    (fn [])
                                            :placeholder  "Haun nimi"
                                            :style-prefix (str style-prefix "-input")}]
      :input-id          input-id
      :style-prefix      style-prefix
      :label             "Haku"}]))

(defn on-save-button-click []
  (re-frame/dispatch [:hakukohderyhma-create/save-hakukohderyhma]))

(defn- on-hakukohderyhma-name-changed [hakukohderyhma-name]
  (re-frame/dispatch [:hakukohderyhma-create/set-hakukohderyhma-name hakukohderyhma-name]))

(defn- hakukohderyhma-create []
  (let [cypressid        "hakukohderyhma-create"
        input-id         "hakukohderyhma-create-input"
        style-prefix     "hakukohderyhma-create"
        visible?         @(re-frame/subscribe [:hakukohderyhma-create/create-grid-visible?])
        button-disabled? (not @(re-frame/subscribe [:hakukohderyhma-create/save-button-enabled?]))]
    (when visible?
      [grid/input-and-button-without-top-row
       {:button-component [b/button
                           {:cypressid    (str cypressid "-button")
                            :disabled?    button-disabled?
                            :label        "Tallenna"
                            :on-click     on-save-button-click
                            :style-prefix (str style-prefix "-button")}]
        :input-component  [input/input-text
                           {:cypressid    (str cypressid "-input")
                            :input-id     input-id
                            :on-change    on-hakukohderyhma-name-changed
                            :placeholder  "Ryhmän nimi"
                            :style-prefix (str style-prefix "-input")}]
        :style-prefix     style-prefix}])))


(def ^:private add-new-hakukohderyhma-link-styles
  (merge layout/vertical-align-center-styles
         {:grid-area    "hakukohderyhma-select-control"
          :justify-self "end"}))

(defn- add-new-hakukohderyhma-link [{:keys [cypressid]}]
  [:div (stylefy/use-style add-new-hakukohderyhma-link-styles)
   [l/link-with-left-separator {:cypressid cypressid
                                :href      ""
                                :label     "Luo uusi ryhmä"
                                :on-click  (fn [_]
                                             (re-frame/dispatch [:hakukohderyhma-create/toggle-grid-visibility]))}]])

(defn- hakukohderyhma-select []
  (let [cypressid    "hakukohderyhma-select"
        input-id     "hakukohderyhma-select-input"
        style-prefix "hakukohderyhma-select"]
    [grid/input-with-label-and-control
     {:control-component [add-new-hakukohderyhma-link
                          {:cypressid (str cypressid "-add-new-hakukohderyhma")}]
      :cypressid         cypressid
      :input-component   [input/input-dropdown
                          {:cypressid        (str cypressid "-dropdown")
                           :style-prefix     (str style-prefix "-input")
                           :unselected-label "Hakukohderyhmä"}]
      :input-id          input-id
      :style-prefix      style-prefix
      :label             "Hakukohderyhmät"}]))

(defn hakukohderyhmapalvelu-panel []
  [p/panel
   {:cypressid "hakukohderyhmapalvelu-panel"}
   "Hakukohderyhmien hallinta"
   [:div (stylefy/use-style hakukohderyhmapalvelu-grid-styles)
    [haku-search]
    [hakukohderyhma-create]
    [hakukohderyhma-select]]])
