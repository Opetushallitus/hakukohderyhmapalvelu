(ns hakukohderyhmapalvelu.views.hakukohderyhmien-hallinta-panel
  (:require [goog.string :as gstring]
            [hakukohderyhmapalvelu.components.common.button :as b]
            [hakukohderyhmapalvelu.components.common.dropdown :as dropdown]
            [hakukohderyhmapalvelu.components.common.input :as input]
            [hakukohderyhmapalvelu.components.common.panel :as p]
            [hakukohderyhmapalvelu.styles.layout-styles :as layout]
            [hakukohderyhmapalvelu.subs.hakukohderyhma-create-subs :refer [saved-hakukohderyhmas-as-options
                                                                           selected-hakukohderyhma
                                                                           selected-hakukohderyhma-as-option]]
            [hakukohderyhmapalvelu.events.hakukohderyhmien-hallinta-events :refer [add-new-hakukohderyhma-link-clicked
                                                                                   create-input-is-active
                                                                                   edit-hakukohderyhma-link-clicked
                                                                                   hakukohderyhma-persisted
                                                                                   hakukohderyhma-renamed
                                                                                   hakukohderyhma-selected
                                                                                   rename-input-is-active]]
            [hakukohderyhmapalvelu.views.haku-view :as haun-tiedot-panel]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [stylefy.core :as stylefy]
            [hakukohderyhmapalvelu.styles.styles-colors :as colors]))

(def ^:private hakukohderyhmapalvelu-grid-styles
  {:display  "grid"
   :grid     (str "[haku-row-start] \"haku-search hakukohderyhma-create\" [haku-row-end]"
                  "[hakukohderyhma-row-start] \"hakukohde-search hakukohderyhma-select\" 1fr [hakukohderyhma-row-end]"
                  "/ 1fr 1fr")
   :grid-gap "15px"})

(def ^:private grid-gap "10px")

(defn- format-grid-row [row n style-prefix]
  (->> [(repeat n style-prefix) "1fr" style-prefix]
       flatten
       (apply gstring/format row)))

(defn make-input-with-label-and-control-styles
  [style-prefix]
  (let [grid (str (format-grid-row "[%s-heading-row-start] \"%s-heading %s-control\" %s [%s-heading-row-end]" 3 style-prefix)
                  (format-grid-row "[%s-input-row-start] \"%s-input %s-input\" %s [%s-input-row-end]" 3 style-prefix)
                  "/ 1fr 1fr")]
    {:display             "grid"
     :grid-area           style-prefix
     :grid                grid
     :grid-gap            grid-gap
     ::stylefy/sub-styles {:heading
                           (merge layout/vertical-align-center-styles
                                  {:color     colors/black
                                   :grid-area (str style-prefix "-heading")})}}))

(defn input-with-label-and-control
  [{:keys [control-component
           cypressid
           input-component
           input-id
           style-prefix
           label]}]
  (let [input-styles (make-input-with-label-and-control-styles style-prefix)]
    [:div (stylefy/use-style input-styles)
     [:label (stylefy/use-sub-style
               input-styles
               :heading
               {:cypressid (str cypressid "-label")
                :for       input-id})
      label]
     input-component
     control-component]))

(defn- make-hakukohderyhmien-hallinta-input-styles
  [style-prefix]
  (merge
    layout/vertical-align-center-styles
    {:grid-area style-prefix
     :position  "relative"}))

(defn- hakukohderyhmien-hallinta-input
  [{:keys [style-prefix] :as props}]
  (let [hakukohderyhmien-hallinta-input-styles (make-hakukohderyhmien-hallinta-input-styles
                                                 style-prefix)
        props' (dissoc props :style-prefix)]
    [:div
     (stylefy/use-style hakukohderyhmien-hallinta-input-styles)
     [input/input-text props']]))

(defn on-save-button-click [input-value saved-operation-type] ;TODO saved-operation-type - add schema for possible vals, [:create :rename]
  (let [hakukohderyhma-name @input-value]
    (case saved-operation-type
      :create (re-frame/dispatch [hakukohderyhma-persisted hakukohderyhma-name])
      :rename (re-frame/dispatch [hakukohderyhma-renamed hakukohderyhma-name])))
  (reset! input-value ""))

(defn- make-input-without-top-row-styles [style-prefix]
  (let [grid (str (format-grid-row "[%s-top-row-start] \". .\" %s [%s-top-row-end]" 1 style-prefix)
                  (format-grid-row "[%s-input-row-start] \"%s-input %s-button\" %s [%s-input-row-end]" 3 style-prefix)
                  "/ minmax(auto, 100%) minmax(auto, 1fr)")]
    {:display  "grid"
     :grid     grid
     :grid-gap grid-gap}))

(defn input-and-button-without-top-row
  [{:keys [button-component
           input-component
           style-prefix]}]
  (let [input-styles (make-input-without-top-row-styles style-prefix)]
    [:div (stylefy/use-style input-styles)
     input-component
     button-component]))

(defn- hakukohderyhma-create-and-rename-input []
  (let [input-value (reagent/atom "")]
    (fn []
      (let [create-is-active @(re-frame/subscribe [:state-query create-input-is-active false])
            rename-is-active @(re-frame/subscribe [:state-query rename-input-is-active false])
            ongoing-request? @(re-frame/subscribe [:hakukohderyhmien-hallinta/ongoing-request?])
            selected-ryhma @(re-frame/subscribe [selected-hakukohderyhma])
            selected-ryhma-name (-> selected-ryhma :nimi :fi)
            text-input-label (if rename-is-active selected-ryhma-name "Uuden ryhmän nimi")
            is-visible (or create-is-active rename-is-active)
            button-disabled? (or ongoing-request?
                                 (-> @input-value seq nil?))
            operation-type (if rename-is-active "rename" "create")
            cypressid (str "hakukohderyhma-" operation-type)
            input-id (str "hakukohderyhma-" operation-type "-input")
            style-prefix (str "hakukohderyhma-" operation-type)]
        (when is-visible
          [input-and-button-without-top-row
           {:button-component [b/button
                               {:cypressid    (str cypressid "-button")
                                :disabled?    button-disabled?
                                :label        "Tallenna"
                                :on-click     #(on-save-button-click input-value (keyword operation-type))
                                :style-prefix (str style-prefix "-button")}]
            :input-component  [hakukohderyhmien-hallinta-input
                               {:cypressid    (str cypressid "-input")
                                :input-id     input-id
                                :on-change    (partial reset! input-value)
                                :placeholder  text-input-label
                                :aria-label   text-input-label
                                :style-prefix (str style-prefix "-input")}]
            :style-prefix     style-prefix}])))))

(def ^:private hakukohderyhma-selection-controls-styles
  (merge layout/vertical-align-center-styles
         {:grid-area    "hakukohderyhma-select-control"
          :justify-self "end"}))

(defn- hakukohderyhma-link [{:keys [cypressid
                                    style-prefix
                                    label
                                    on-click]}]
  [:span
   [b/text-button {:cypressid    cypressid
                   :disabled?    false
                   :style-prefix style-prefix
                   :label        label
                   :on-click     on-click}]])

(defn- add-new-hakukohderyhma-link [{:keys [cypressid]}]
  [hakukohderyhma-link {:cypressid    (str cypressid "--add-new-hakukohderyhma")
                        :style-prefix "new-hakukohderyhma-btn"
                        :label        "Luo uusi ryhmä"
                        :on-click     (fn [_]
                                        (re-frame/dispatch [add-new-hakukohderyhma-link-clicked]))}])

(defn- edit-hakukohderyhma-link [{:keys [cypressid]}]
  (let [selected-ryhma @(re-frame/subscribe [selected-hakukohderyhma])
        rename-is-active @(re-frame/subscribe [:state-query rename-input-is-active false])
        is-visible (and (some? selected-ryhma) (not rename-is-active))]
    (when is-visible
      [hakukohderyhma-link {:cypressid    (str cypressid "--rename-hakukohderyhma")
                            :style-prefix "rename-hakukohderyhma-btn"
                            :label        "Muokkaa ryhmää"
                            :on-click     (fn [_]
                                            (re-frame/dispatch [edit-hakukohderyhma-link-clicked]))}])))

(defn- hakukohdryhma-selection-controls [props]
  (let [separator [:span (stylefy/use-style {:margin "6px"})
                   " | "]]
    [:div (stylefy/use-style hakukohderyhma-selection-controls-styles)
     [edit-hakukohderyhma-link props]
     separator
     [add-new-hakukohderyhma-link props]]))

(defn- make-hakukohderyhmien-hallinta-input-dropdown-styles
  [style-prefix]
  (merge
    layout/vertical-align-center-styles
    {:grid-area style-prefix
     :width     "100%"}))

(defn- hakukohderyhmien-hallinta-dropdown
  [{:keys [style-prefix] :as props}]
  (let [hakukohderyhmien-hallinta-input-dropdown-styles (make-hakukohderyhmien-hallinta-input-dropdown-styles
                                                          style-prefix)
        props' (dissoc props :style-prefix)]
    [:div
     (stylefy/use-style
       hakukohderyhmien-hallinta-input-dropdown-styles)
     [dropdown/input-dropdown props']]))

(defn- hakukohderyhma-select []
  (let [cypressid "hakukohderyhma-select"
        input-id "hakukohderyhma-select-input"
        style-prefix "hakukohderyhma-select"]
    [input-with-label-and-control
     {:control-component [hakukohdryhma-selection-controls
                          {:cypressid (str cypressid "-control")}]
      :cypressid         cypressid
      :input-component   [hakukohderyhmien-hallinta-dropdown
                          {:cypressid              (str cypressid "-dropdown")
                           :style-prefix           (str style-prefix "-input")
                           :unselected-label       "Hakukohderyhmä"
                           :dropdown-items         (re-frame/subscribe [saved-hakukohderyhmas-as-options])
                           :selected-dropdown-item (re-frame/subscribe [selected-hakukohderyhma-as-option])
                           :selection-fn           #(re-frame/dispatch [hakukohderyhma-selected %])}]
      :input-id          input-id
      :style-prefix      style-prefix
      :label             "Hakukohderyhmät"}]))

(defn hakukohderyhmien-hallinta-panel []
  [p/panel
   {:cypressid "hakukohderyhmapalvelu-panel"}
   "Hakukohderyhmien hallinta"
   [:div (stylefy/use-style hakukohderyhmapalvelu-grid-styles)
    [haun-tiedot-panel/haku-search]
    [hakukohderyhma-create-and-rename-input]
    [haun-tiedot-panel/hakukohteet-container]
    [hakukohderyhma-select]]])
