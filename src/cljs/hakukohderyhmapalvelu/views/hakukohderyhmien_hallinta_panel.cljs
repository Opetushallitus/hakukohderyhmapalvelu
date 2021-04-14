(ns hakukohderyhmapalvelu.views.hakukohderyhmien-hallinta-panel
  (:require [goog.string :as gstring]
            [hakukohderyhmapalvelu.components.common.button :as b]
            [hakukohderyhmapalvelu.components.common.input :as input]
            [hakukohderyhmapalvelu.components.common.multi-select :as multi-select]
            [hakukohderyhmapalvelu.components.common.react-select :as react-select]
            [hakukohderyhmapalvelu.components.common.panel :as p]
            [hakukohderyhmapalvelu.styles.layout-styles :as layout]
            [hakukohderyhmapalvelu.subs.haku-subs :as haku-subs]
            [hakukohderyhmapalvelu.subs.hakukohderyhma-subs :as hakukohderyhma-subs]
            [hakukohderyhmapalvelu.events.hakukohderyhmien-hallinta-events :as hakukohderyhma-events]
            [hakukohderyhmapalvelu.views.haku-view :as haun-tiedot-panel]
            [re-frame.core :refer [dispatch subscribe]]
            [reagent.core :as reagent]
            [stylefy.core :as stylefy]
            [hakukohderyhmapalvelu.styles.styles-colors :as colors]
            [hakukohderyhmapalvelu.components.common.svg :as svg]))

(def ^:private hakukohderyhmapalvelu-grid-styles
  {:display  "grid"
   :grid     (str "[haku-row-start] \"haku-search hakukohderyhma-create\" [haku-row-end]"
                  "[hakukohderyhma-row-start] \"hakukohde-search hakukohderyhma-container\" 1fr [hakukohderyhma-row-end]"
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
  {:grid-area style-prefix})

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
      :create (dispatch [hakukohderyhma-events/hakukohderyhma-persisted hakukohderyhma-name])
      :rename (dispatch [hakukohderyhma-events/hakukohderyhma-renamed hakukohderyhma-name])))
  (reset! input-value ""))

(defn on-delete-button-click [deleted-hakukohderyhma]
  (dispatch [hakukohderyhma-events/hakukohderyhma-deleted deleted-hakukohderyhma]))

(defn- make-input-without-top-row-styles [style-prefix]
  (let [grid (str (format-grid-row "[%s-top-row-start] \". .\" %s [%s-top-row-end]" 1 style-prefix)
                  (format-grid-row "[%s-input-row-start] \"%s-input %s-button\" %s [%s-input-row-end]" 3 style-prefix)
                  "/ minmax(auto, 100%) minmax(auto, 1fr)")]
    {:display  "grid"
     :grid     grid
     :grid-gap grid-gap}))

(def trash-can-icon (svg/icon "trash-can" {:height "20px"
                                           :width "16px"
                                           :margin "6px 5px 0px 5px"} ))

(defn- hakukohderyhma-create-and-rename-input []
  (let [input-value (reagent/atom "")
        is-confirming-delete (reagent/atom false)]
    (fn []
      (let [create-is-active @(subscribe [:state-query hakukohderyhma-events/create-input-is-active false])
            rename-is-active @(subscribe [:state-query hakukohderyhma-events/rename-input-is-active false])
            ongoing-request? @(subscribe [:hakukohderyhmien-hallinta/ongoing-request?])
            selected-haku @(subscribe [haku-subs/haku-selected-haku])
            selected-ryhma @(subscribe [hakukohderyhma-subs/selected-hakukohderyhma])
            selected-ryhma-name (-> selected-ryhma :nimi :fi)
            text-input-label (if rename-is-active selected-ryhma-name "Uuden ryhmän nimi")
            is-visible (and (some? selected-haku) (or create-is-active rename-is-active))
            button-disabled? (or ongoing-request?
                                 (-> @input-value seq nil?))
            operation-type (if rename-is-active "rename" "create")
            cypressid (str "hakukohderyhma-" operation-type)
            input-id (str "hakukohderyhma-" operation-type "-input")
            style-prefix (str "hakukohderyhma-" operation-type)]
        (when is-visible
          (let [input-styles (make-input-without-top-row-styles style-prefix)]
            [:div (stylefy/use-style input-styles)
             [hakukohderyhmien-hallinta-input
              {:cypressid    (str cypressid "-input")
               :input-id     input-id
               :on-change    (partial reset! input-value)
               :placeholder  text-input-label
               :aria-label   text-input-label
               :style-prefix (str style-prefix "-input")}]
             [:span
              {:style {:grid-area (str style-prefix "-button")
                       :display "flex"
                       :flex-direction "row"}}
              (if @is-confirming-delete
                [:<>
                 [b/button
                  {:cypressid    (str cypressid "-button")
                   :disabled?    false
                   :label        "Vahvista poisto"
                   :on-click     #(on-delete-button-click selected-ryhma)
                   :style-prefix (str style-prefix "-button")
                   :custom-style {:is-danger true
                                  :margin-right "4px"
                                  :font-size "12px"}}]
                 [b/button
                  {:cypressid    (str cypressid "-button")
                   :label        "Peruuta"
                   :on-click     #(reset! is-confirming-delete false)
                   :style-prefix (str style-prefix "-button")}]]
                [:<>
                 (when rename-is-active
                   [b/button
                    {:cypressid    (str cypressid "-button")
                     :disabled?    false
                     :label        trash-can-icon
                     :on-click     #(reset! is-confirming-delete true)
                     :style-prefix (str style-prefix "-button")
                     :custom-style {:is-danger true
                                    :margin-right "4px"}}])
                 [b/button
                  {:cypressid    (str cypressid "-button")
                   :disabled?    button-disabled?
                   :label        "Tallenna"
                   :on-click     #(on-save-button-click input-value (keyword operation-type))
                   :style-prefix (str style-prefix "-button")}]])]]))))))

(def ^:private hakukohderyhma-selection-controls-styles
  (merge layout/vertical-align-center-styles
         {:grid-area    "hakukohderyhma-select-control"
          :justify-self "end"}))

(defn- hakukohderyhma-link [{:keys [cypressid
                                    style-prefix
                                    label
                                    on-click
                                    disabled?]}]
  [:span
   [b/text-button {:cypressid    cypressid
                   :disabled?    disabled?
                   :style-prefix style-prefix
                   :label        label
                   :on-click     on-click}]])

(defn- add-new-hakukohderyhma-link [{:keys [cypressid disabled?]}]
  [hakukohderyhma-link {:cypressid    (str cypressid "--add-new-hakukohderyhma")
                        :style-prefix "new-hakukohderyhma-btn"
                        :label        @(subscribe [:translation :hakukohderyhma/luo-uusi-ryhma])
                        :disabled?    disabled?
                        :on-click     #(dispatch [hakukohderyhma-events/add-new-hakukohderyhma-link-clicked])}])

(defn- edit-hakukohderyhma-link [{:keys [cypressid]}]
  (let [selected-ryhma @(subscribe [hakukohderyhma-subs/selected-hakukohderyhma])
        rename-is-active @(subscribe [:state-query hakukohderyhma-events/rename-input-is-active false])
        label @(subscribe [:translation :hakukohderyhma/muokkaa-ryhmaa])
        is-visible (and (some? selected-ryhma) (not rename-is-active))]
    (when is-visible
      [hakukohderyhma-link {:cypressid    (str cypressid "--rename-hakukohderyhma")
                            :style-prefix "rename-hakukohderyhma-btn"
                            :label        label
                            :disabled?    false
                            :on-click     #(dispatch [hakukohderyhma-events/edit-hakukohderyhma-link-clicked])}])))

(defn- hakukohderyhma-selection-controls [{disabled? :disabled? :as props}]
  (let [separator [:span (stylefy/use-style {:margin "6px"
                                             :color (if disabled? colors/gray-lighten-3 colors/black)})
                   " | "]]
    [:div (stylefy/use-style hakukohderyhma-selection-controls-styles)
     [edit-hakukohderyhma-link props]
     separator
     [add-new-hakukohderyhma-link props]]))

(defn- hakukohderyhma-select []
  (let [cypressid "hakukohderyhma-select"
        input-id "hakukohderyhma-select-input"
        style-prefix "hakukohderyhma-select"
        hakukohderyhmas (subscribe [hakukohderyhma-subs/saved-hakukohderyhmas-as-options])
        is-loading (subscribe [hakukohderyhma-subs/is-loading-hakukohderyhmas])
        selected (subscribe [hakukohderyhma-subs/selected-hakukohderyhma-as-option])
        selected-haku (subscribe [haku-subs/haku-selected-haku])]
    [input-with-label-and-control
     {:control-component [hakukohderyhma-selection-controls
                          {:cypressid (str cypressid "-control")
                           :disabled? (nil? @selected-haku)}]
      :cypressid         cypressid
      :input-component   [:div (stylefy/use-style {:grid-area input-id
                                                   :margin-top "8px"}
                                                  {:cypressid (str cypressid "-input")})
                          [react-select/select {:options      @hakukohderyhmas
                                                :on-select-fn #(dispatch [hakukohderyhma-events/hakukohderyhma-selected %])
                                                :is-disabled  (empty? @hakukohderyhmas)
                                                :is-loading   @is-loading
                                                :placeholder  "Hakukohderyhmä"
                                                :value        @selected}]]
      :input-id          input-id
      :style-prefix      style-prefix
      :label             "Hakukohderyhmät"}]))

(defn- hakukohderyhma-container []
  (let [hakukohteet (subscribe [hakukohderyhma-subs/hakukohderyhman-hakukohteet-as-options])
        selected-hakukohteet (subscribe [hakukohderyhma-subs/selected-hakukohderyhmas-hakukohteet])
        remove-from-goup-btn-text (subscribe [:translation :hakukohderyhma/poista-ryhmasta])]
    (fn []
      [:div (stylefy/use-style {:grid-area "hakukohderyhma-container"
                                :display   "grid"
                                :grid-gap  "10px"
                                :grid      (str "\"hakukohderyhma-select hakukohderyhma-select hakukohderyhma-select hakukohderyhma-select\" 1fr "
                                                "\"hakukohderyhma-select hakukohderyhma-select hakukohderyhma-select hakukohderyhma-select\" 1fr "
                                                "\"hakukohderyhma-hakukohteet hakukohderyhma-hakukohteet hakukohderyhma-hakukohteet hakukohderyhma-hakukohteet\" 20rem"
                                                "\"hakukohderyhma-buttons hakukohderyhma-buttons hakukohderyhma-buttons remove-from-group-btn\" 40px")}
                               {:cypressid "hakukohderyhma-container"})

       [hakukohderyhma-select]
       [:div (stylefy/use-style {:grid-area "hakukohderyhma-hakukohteet"})
        [multi-select/multi-select {:options   @hakukohteet
                                    :cypressid "hakukohderyhma-hakukohteet"
                                    :select-fn #(dispatch [hakukohderyhma-events/toggle-hakukohde-selection %])}]]
       [b/button {:cypressid    "remove-from-group-btn"
                  :disabled?    (empty? @selected-hakukohteet)
                  :label        @remove-from-goup-btn-text
                  :on-click     #(dispatch [hakukohderyhma-events/removed-hakukohteet-from-hakukohderyhma
                                            @selected-hakukohteet])
                  :style-prefix "remove-from-group-btn"}]])))

(defn hakukohderyhmien-hallinta-panel []
  [p/panel
   {:cypressid "hakukohderyhmapalvelu-panel"}
   "Hakukohderyhmien hallinta"
   [:div (stylefy/use-style hakukohderyhmapalvelu-grid-styles)
    [haun-tiedot-panel/haku-search]
    [hakukohderyhma-create-and-rename-input]
    [haun-tiedot-panel/hakukohteet-container]
    [hakukohderyhma-container]]])
