(ns hakukohderyhmapalvelu.views.hakukohderyhmien-hallinta-panel
  (:require [hakukohderyhmapalvelu.components.common.button :as b]
            [hakukohderyhmapalvelu.components.common.input :as input]
            [hakukohderyhmapalvelu.components.common.multi-select :as multi-select]
            [hakukohderyhmapalvelu.components.common.react-select :as react-select]
            [hakukohderyhmapalvelu.components.common.panel :as p]
            [hakukohderyhmapalvelu.components.common.select-all :as select-all-btns]
            [hakukohderyhmapalvelu.styles.layout-styles :as layout]
            [hakukohderyhmapalvelu.subs.haku-subs :as haku-subs]
            [hakukohderyhmapalvelu.subs.hakukohderyhma-subs :as hakukohderyhma-subs]
            [hakukohderyhmapalvelu.events.hakukohderyhmien-hallinta-events :as hakukohderyhma-events]
            [hakukohderyhmapalvelu.views.haku-view :as haun-tiedot-panel]
            [re-frame.core :refer [dispatch subscribe]]
            [reagent.core :as reagent]
            [stylefy.core :as stylefy]
            [hakukohderyhmapalvelu.views.hakukohderyhma-settings-view :as settings-view]
            [hakukohderyhmapalvelu.styles.styles-colors :as colors]
            [hakukohderyhmapalvelu.components.common.svg :as svg]))

(def ^:private hakukohderyhmapalvelu-grid-styles
  {:display  "grid"
   :align-items "start"
   :grid     (str "[haku-row-start] \"haku-search hakukohderyhma-create\" [haku-row-end]"
                  "[hakukohderyhma-row-start] \"hakukohde-search hakukohderyhma-container\" 1fr [hakukohderyhma-row-end]"
                  "/ 1fr 1fr")
   :grid-gap "15px"})

(def ^:private grid-gap "10px")

(defn make-input-with-label-and-control-styles
  [style-prefix]
  {:display               "grid"
   :grid-area             style-prefix
   :grid-template-columns "repeat(3, 1fr)"
   :grid-auto-rows        "minmax(auto, auto)"
   :grid-gap              grid-gap
   ::stylefy/sub-styles   {:heading
                           (-> layout/vertical-align-center-styles
                               (assoc :color colors/black
                                      :grid-area (str style-prefix "-heading"))
                               (dissoc :align-items))}})

(defn input-with-label-and-control
  [{:keys [control-component
           cypressid
           input-component
           input-id
           style-prefix
           label]}]
  (let [input-styles (make-input-with-label-and-control-styles style-prefix)]
    [:div (stylefy/use-style input-styles)
     [:label (stylefy/use-style
               (-> layout/vertical-align-center-styles
                   (assoc :color colors/black
                          :grid-row "1"
                          :grid-column "1/2")
                   (dissoc :align-items))
               {:cypressid (str cypressid "-label")
                :for       input-id})
      label]
     input-component
     control-component]))

(defn- hakukohderyhmien-hallinta-input
  [{:keys [style-prefix value] :as props}]
  (let [props' (-> props
                   (dissoc :style-prefix)
                   (assoc :value (reagent/atom (or value ""))))]
    [:div
     (stylefy/use-style {:grid-area style-prefix})
     [input/input-text props']]))

(defn on-save-button-click [input-value saved-operation-type] ;TODO saved-operation-type - add schema for possible vals, [:create :rename]
  (case saved-operation-type
    :create (dispatch [hakukohderyhma-events/hakukohderyhma-persisted input-value])
    :rename (dispatch [hakukohderyhma-events/hakukohderyhma-renamed input-value])))

(defn on-delete-button-click [deleted-hakukohderyhma]
  (dispatch [hakukohderyhma-events/hakukohderyhma-deleted deleted-hakukohderyhma]))

(def trash-can-icon (svg/img-icon "trash-can" {:height "20px"
                                               :width  "16px"
                                               :margin "6px 5px 0px 5px"}))

(defn- hakukohderyhma-create-and-rename-input []
  (fn []
    (let [create-is-active @(subscribe [:state-query hakukohderyhma-events/create-input-is-active false])
          rename-is-active @(subscribe [:state-query hakukohderyhma-events/rename-input-is-active false])
          is-confirming-delete @(subscribe [:state-query hakukohderyhma-events/deletion-confirmation-is-active false])
          ongoing-request? @(subscribe [:hakukohderyhmien-hallinta/ongoing-request?])
          selected-haku @(subscribe [haku-subs/haku-selected-haku])
          selected-ryhma @(subscribe [hakukohderyhma-subs/selected-hakukohderyhma])
          selected-ryhma-name (:label @(subscribe [hakukohderyhma-subs/selected-hakukohderyhma-as-option]))
          text-input-label @(subscribe [:translation :hakukohderyhma/hakukohderyhma-nimi])
          name-text @(subscribe [:state-query hakukohderyhma-events/hakukohderyhma-name-text])
          is-visible (and (some? selected-haku) (or create-is-active rename-is-active))
          button-disabled? (or ongoing-request?
                               (= selected-ryhma-name name-text)
                               (-> name-text seq nil?))
          operation-type (if rename-is-active "rename" "create")
          cypressid (str "hakukohderyhma-" operation-type)
          input-id (str "hakukohderyhma-" operation-type "-input")
          style-prefix (str "hakukohderyhma-" operation-type)
          all-hakukohteet-are-authorized (every? :oikeusHakukohteeseen (:hakukohteet selected-ryhma))]
      (when is-visible
        [:div (stylefy/use-style {:grid-area "hakukohderyhma-create"
                                  :display   "grid"
                                  :grid-gap  grid-gap
                                  :grid      (str
                                               (str "[empty-row-start]\". .\" 24px [empty-row-end]")
                                               (str "\"" style-prefix "-input " style-prefix "-button\" 40px")
                                               (str "/ minmax(auto, 95%) 1fr"))})
         [hakukohderyhmien-hallinta-input
          {:cypressid    (str cypressid "-input")
           :input-id     input-id
           :on-change    #(dispatch [hakukohderyhma-events/set-hakukohderyhma-name-text %])
           :placeholder  text-input-label
           :aria-label   text-input-label
           :style-prefix (str style-prefix "-input")
           :value        name-text}]
         [:span
          {:style {:grid-area      (str style-prefix "-button")
                   :display        "flex"
                   :justify-self   "end"
                   :flex-direction "row"}}
          (if (and rename-is-active is-confirming-delete)
            [:<>
             [b/button
              {:cypressid    "hakukohderyhma-delete-confirm-button"
               :disabled?    false
               :label        @(subscribe [:translation :yleiset/vahvista-poisto])
               :on-click     #(on-delete-button-click selected-ryhma)
               :style-prefix (str style-prefix "-button")
               :custom-style {:is-danger    true
                              :margin-right "4px"
                              :font-size    "12px"}}]
             [b/button
              {:cypressid    "hakukohderyhma-delete-cancel-button"
               :label        @(subscribe [:translation :yleiset/peruuta])
               :on-click     #(dispatch [hakukohderyhma-events/set-deletion-confirmation-dialogue-visibility false])
               :style-prefix (str style-prefix "-button")}]]
            [:<>
             (when (and
                     rename-is-active
                     all-hakukohteet-are-authorized)
               [b/button
                {:cypressid    "hakukohderyhma-delete-button"
                 :disabled?    false
                 :label        trash-can-icon
                 :on-click     #(dispatch [hakukohderyhma-events/set-deletion-confirmation-dialogue-visibility true])
                 :style-prefix (str style-prefix "-button")
                 :custom-style {:is-danger    true
                                :margin-right "4px"}}])
             [b/button
              {:cypressid    (str cypressid "-button")
               :disabled?    button-disabled?
               :label        @(subscribe [:translation :yleiset/tallenna])
               :on-click     #(on-save-button-click name-text (keyword operation-type))
               :style-prefix (str style-prefix "-button")}]])]]))))

(def ^:private hakukohderyhma-selection-controls-styles
  (-> layout/vertical-align-center-styles
      (assoc :grid-row "1"
             :grid-column "2/4"
             :justify-self "end")
      (dissoc :align-items)))

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
                            :on-click     #(dispatch [hakukohderyhma-events/edit-hakukohderyhma-link-clicked selected-ryhma])}])))

(defn- hakukohderyhma-selection-controls [{disabled? :disabled? :as props}]
  (let [separator [:span (stylefy/use-style {:margin "0 6px"
                                             :color  (if disabled? colors/gray-lighten-3 colors/black)})
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
      :input-component   [:div (stylefy/use-style {:grid-row       "3"
                                                   :grid-column    "1/4"
                                                   :padding-bottom "2px"}
                                                  {:cypressid (str cypressid "-input")})
                          [react-select/select {:options      @hakukohderyhmas
                                                :on-select-fn #(dispatch [hakukohderyhma-events/hakukohderyhma-selected %])
                                                :on-clear-fn  #(dispatch [hakukohderyhma-events/hakukohderyhma-selected nil])
                                                :is-disabled  (empty? @hakukohderyhmas)
                                                :is-loading   @is-loading
                                                :placeholder  "Hakukohderyhmä"
                                                :value        @selected}]]
      :input-id          input-id
      :style-prefix      style-prefix
      :label             "Hakukohderyhmät"}]))

(def ^:private button-row-style
  {:display "grid"
   :grid "\"multi-selection-buttons remove-from-group-btn\" 40px"
   :grid-gap "10px"
   :grid-row 5
   :grid-column "1/4"
   :grid-area "bottom-row-buttons"})

(defn- hakukohderyhma-container []
  (let [hakukohteet (subscribe [hakukohderyhma-subs/hakukohderyhman-hakukohteet-as-options])
        selected-hakukohteet (subscribe [hakukohderyhma-subs/selected-hakukohderyhmas-hakukohteet])
        remove-from-goup-btn-text (subscribe [:translation :hakukohderyhma/poista-ryhmasta])]
    (fn []
      (let [enabled-hakukohde-count (count (remove :is-disabled @hakukohteet))
            select-all-is-disabled (= enabled-hakukohde-count (count @selected-hakukohteet))
            deselect-all-is-disabled (empty? @selected-hakukohteet)]
        [:div (stylefy/use-style {:grid-area "hakukohderyhma-container"
                                  :display   "grid"
                                  :grid-gap  "10px"
                                  :margin-top "2rem"
                                  :grid      (str "\"hakukohderyhma-select hakukohderyhma-select hakukohderyhma-select hakukohderyhma-select\" 1fr "
                                                  "\"hakukohderyhma-select hakukohderyhma-select hakukohderyhma-select hakukohderyhma-select\" 1fr "
                                                  "\"hakukohderyhma-hakukohteet hakukohderyhma-hakukohteet hakukohderyhma-hakukohteet hakukohderyhma-hakukohteet\" 20rem"
                                                  "\"bottom-row-buttons bottom-row-buttons bottom-row-buttons bottom-row-buttons\" 40px"
                                                  "\"hakukohderyhma-settings-view hakukohderyhma-settings-view hakukohderyhma-settings-view . \" 40px"
                                                  "\"hakukohderyhma-settings-view hakukohderyhma-settings-view hakukohderyhma-settings-view . \" 40px"
                                                  "\"hakukohderyhma-settings-view hakukohderyhma-settings-view hakukohderyhma-settings-view . \" 40px")}
                                 {:cypressid "hakukohderyhma-container"})

         [hakukohderyhma-select]
         [:div (stylefy/use-style {:grid-area  "hakukohderyhma-hakukohteet"
                                   :margin-top "-2px"})
          [multi-select/multi-select-priorisoiva {:options   @hakukohteet
                                                  :cypressid "hakukohderyhma-hakukohteet"
                                                  :select-fn #(dispatch [hakukohderyhma-events/toggle-hakukohde-selection %])}]]
         [:div (stylefy/use-style button-row-style)
          [select-all-btns/select-all-buttons
           {:cypressid                "select-all-btn-2"
            :select-all-is-disabled   select-all-is-disabled
            :deselect-all-is-disabled deselect-all-is-disabled
            :on-select-all            #(dispatch [hakukohderyhma-events/all-hakukohde-in-selected-hakukohderyhma-selected])
            :on-deselect-all          #(dispatch [hakukohderyhma-events/all-hakukohde-in-selected-hakukohderyhma-deselected])
            :hakukohde-count          enabled-hakukohde-count
            :select-all-label         @(subscribe [:translation :hakukohderyhma/valitse-kaikki])
            :deselect-all-label       @(subscribe [:translation :hakukohderyhma/poista-valinnat])}]
          [b/button {:cypressid    "remove-from-group-btn"
                     :disabled?    (empty? @selected-hakukohteet)
                     :label        @remove-from-goup-btn-text
                     :on-click     #(dispatch [hakukohderyhma-events/removed-hakukohteet-from-hakukohderyhma
                                               @selected-hakukohteet])
                     :style-prefix "remove-from-group-btn"}]]
         [settings-view/hakukohderyhma-settings-view]]))))

(defn hakukohderyhmien-hallinta-panel []
  [p/panel
   {:cypressid "hakukohderyhmapalvelu-panel"}
   "Hakukohderyhmien hallinta"
   [:div (stylefy/use-style hakukohderyhmapalvelu-grid-styles)
    [haun-tiedot-panel/haku-search]
    [hakukohderyhma-create-and-rename-input]
    [haun-tiedot-panel/hakukohteet-container]
    [hakukohderyhma-container]]])
