(ns hakukohderyhmapalvelu.views.haku-view
  (:require
    [reagent.core :as reagent]
    [re-frame.core :refer [subscribe dispatch]]
    [stylefy.core :as stylefy]
    [hakukohderyhmapalvelu.subs.haku-subs :as haku-subs]
    [hakukohderyhmapalvelu.subs.hakukohderyhma-subs :as hakukohderyhma-subs]
    [hakukohderyhmapalvelu.events.haku-events :as haku-events]
    [hakukohderyhmapalvelu.events.hakukohderyhmien-hallinta-events :as hakukohderyhma-events]
    [hakukohderyhmapalvelu.views.hakukohteet-lisarajain-view :as filter-view]
    [hakukohderyhmapalvelu.components.common.react-select :as react-select]
    [hakukohderyhmapalvelu.components.common.checkbox :as checkbox]
    [hakukohderyhmapalvelu.components.common.multi-select :as multi-select]
    [hakukohderyhmapalvelu.components.common.input :as input]
    [hakukohderyhmapalvelu.components.common.button :as button]
    [hakukohderyhmapalvelu.styles.styles-colors :as colors]))


(defn haku-search []
  (let [options (subscribe [haku-subs/haku-haut-as-options])
        selected-option (subscribe [haku-subs/haku-selected-haku-as-option])
        is-selected (reagent/atom false)
        is-loading (subscribe [haku-subs/haku-is-loading])
        title-text (subscribe [:translation :hakukohderyhma/haku])
        label-text (subscribe [:translation :hakukohderyhma/nayta-myos-paattyneet])
        placeholder-text (subscribe [:translation :hakukohderyhma/haun-nimi])
        on-change (fn [_]
                    (swap! is-selected not)
                    (dispatch [haku-events/get-haut @is-selected]))
        on-select (fn [{oid :value}] (dispatch [haku-events/select-haku oid]))
        on-clear (fn [] (dispatch [haku-events/clear-selected-haku]))]
    (fn []
      [:div (stylefy/use-style {:grid-area "haku-search" :margin-bottom "2px"} {:cypressid "haku-search-cypress"})
       [:div (stylefy/use-style {:margin-bottom "10px"})
        [:span {:cypressid "haku-search-title"} @title-text]
        [:div (stylefy/use-style {:float "right"})
         [checkbox/checkbox-with-label {:id        "haku-search-checkbox"
                                        :checked?  @is-selected
                                        :cypressid "haku-search-checkbox-cypress"
                                        :disabled? @is-loading
                                        :label     @label-text
                                        :on-change on-change}]]]
       [react-select/select {:options      @options
                             :on-select-fn on-select
                             :on-clear-fn  on-clear
                             :is-loading   @is-loading
                             :is-disabled  @is-loading
                             :placeholder  @placeholder-text
                             :value        @selected-option}]])))

(def ^:private hakukohteet-container-style
  {:grid-area "hakukohde-search"
   :margin-top "2rem"
   :display "grid"
   :grid-template-columns "repeat(3, 1fr)"
   :grid-auto-rows "minmax(auto, auto)"
   :grid-gap "10px"})

(def ^:private button-row-style
  {:display "grid"
   :grid "\"multi-selection-buttons add-to-group-btn\" 40px"
   :grid-gap "10px"
   :grid-row 5
   :grid-column "1/4"})

(def ^:private multi-selection-button-row-style
  {:display "flex"
   :justify-content "left"
   :align-items "center"
   :grid-area "multi-selection-buttons"})

(defn multi-select-buttons [{:keys [cypressid
                                    select-all-is-disabled
                                    deselect-all-is-disabled
                                    on-select-all
                                    on-deselect-all
                                    hakukohde-count]}]
  (let [select-all-btn-text (subscribe [:translation :hakukohderyhma/valitse-kaikki])
        deselect-all-btn-text (subscribe [:translation :hakukohderyhma/poista-valinnat])]
    [:div (stylefy/use-style multi-selection-button-row-style)
     [button/text-button {:cypressid    cypressid
                          :disabled?    select-all-is-disabled
                          :label        (str @select-all-btn-text " (" hakukohde-count ")")
                          :on-click     on-select-all
                          :style-prefix "select-all-btn"}]
     [:span (stylefy/use-style {:margin "6px"
                                :color (if (and select-all-is-disabled deselect-all-is-disabled)
                                         colors/gray-lighten-3 colors/black)})
      " | "]
     [button/text-button {:cypressid    (str "de" cypressid)
                          :disabled?    deselect-all-is-disabled
                          :label        @deselect-all-btn-text
                          :on-click     on-deselect-all
                          :style-prefix "deselect-all-btn"}]]))

(defn hakukohteet-container []
  (let [hakukohteet (subscribe [haku-subs/haku-hakukohteet-as-options])
        hakukohteet-is-empty (subscribe [haku-subs/haku-hakukohteet-is-empty])
        hakukohteet-label (subscribe [:translation :hakukohderyhma/hakukohteet])
        hakukohteet-search-placeholder (subscribe [:translation :hakukohderyhma/nimi-tai-organisaatio])
        selected-hakukohteet (subscribe [haku-subs/haku-selected-hakukohteet])
        selected-hakukohderyhma (subscribe [hakukohderyhma-subs/selected-hakukohderyhma])
        haku-lisarajaimet-visible (subscribe [haku-subs/haku-lisarajaimet-visible])
        add-to-group-btn-text (subscribe [:translation :hakukohderyhma/liita-ryhmaan])
        lisarajain-text (subscribe [haku-subs/haku-lisarajaimet-text])]
    (fn []
      (let [enabled-hakukohde-count (count (remove :is-disabled @hakukohteet))
            select-all-is-disabled (= enabled-hakukohde-count (count @selected-hakukohteet))
            deselect-all-is-disabled (empty? @selected-hakukohteet)]
        [:div (stylefy/use-style hakukohteet-container-style)
         [:span (stylefy/use-style {:grid-row 1 :grid-column "1 / 3"}) @hakukohteet-label]
         [:div (stylefy/use-style {:grid-row 1 :grid-column "3 / 3" :text-align "end"})
          [button/text-button {:cypressid    "extra-filters-btn"
                               :disabled?    @hakukohteet-is-empty
                               :label        @lisarajain-text
                               :on-click     #(dispatch [haku-events/open-haku-lisarajaimet])
                               :style-prefix "extra-filters-btn"}]]
         [:div (stylefy/use-style {:grid-row 2 :grid-auto-columns "auto"})
          (when @haku-lisarajaimet-visible
            [filter-view/extra-filters])]
         [:div (stylefy/use-style {:grid-row 3 :grid-column "1 / 4"})
          [input/input-text {:cypressid   "hakukohteet-text-filter"
                             :input-id    "hakukohteet-text-filter"
                             :on-change   #(dispatch [haku-events/set-hakukohteet-filter %])
                             :placeholder @hakukohteet-search-placeholder
                             :aria-label  @hakukohteet-search-placeholder
                             :is-disabled @hakukohteet-is-empty}]]
         [:div (stylefy/use-style {:grid-row 4 :grid-column "1 / 4"})
          [multi-select/multi-select {:options   @hakukohteet
                                      :select-fn #(dispatch [haku-events/toggle-hakukohde-selection %])
                                      :cypressid "hakukohteet-container"}]]
         [:div (stylefy/use-style button-row-style)
          [multi-select-buttons
           {:cypressid "select-all-btn"
            :select-all-is-disabled select-all-is-disabled
            :deselect-all-is-disabled deselect-all-is-disabled
            :on-select-all #(dispatch [haku-events/all-hakukohde-in-view-selected])
            :on-deselect-all #(dispatch [haku-events/all-hakukohde-deselected])
            :hakukohde-count enabled-hakukohde-count}]
          [button/button {:cypressid    "add-to-group-btn"
                          :disabled?    (or (empty? @selected-hakukohteet) (nil? @selected-hakukohderyhma))
                          :label        @add-to-group-btn-text
                          :on-click     #(dispatch [hakukohderyhma-events/added-hakukohteet-to-hakukohderyhma
                                                    @selected-hakukohteet])
                          :style-prefix "add-to-group-btn"}]]]))))
