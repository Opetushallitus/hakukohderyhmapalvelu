(ns hakukohderyhmapalvelu.views.haku-view
  (:require
    [reagent.core :as reagent]
    [re-frame.core :refer [subscribe dispatch]]
    [stylefy.core :as stylefy]
    [hakukohderyhmapalvelu.subs.haku-subs :as haku-subs]
    [hakukohderyhmapalvelu.subs.hakukohderyhma-subs :as hakukohderyhma-subs]
    [hakukohderyhmapalvelu.events.haku-events :as haku-events]
    [hakukohderyhmapalvelu.events.hakukohderyhmien-hallinta-events :as hakukohderyhma-events]
    [hakukohderyhmapalvelu.components.common.react-select :as react-select]
    [hakukohderyhmapalvelu.components.common.checkbox :as checkbox]
    [hakukohderyhmapalvelu.components.common.multi-select :as multi-select]
    [hakukohderyhmapalvelu.components.common.input :as input]
    [hakukohderyhmapalvelu.components.common.button :as button]))


(defn haku-search []
  (let [options (subscribe [haku-subs/haku-haut-as-options])
        selected-option (subscribe [haku-subs/haku-selected-haku-as-option])
        is-selected (reagent/atom false)
        is-loading (subscribe [haku-subs/haku-is-loading])
        title-text (subscribe [:translation :haku/haku])
        label-text (subscribe [:translation :haku/show-all-haut])
        placeholder-text (subscribe [:translation :haku/haku-search-placeholder])
        on-change (fn [_]
                    (swap! is-selected not)
                    (dispatch [haku-events/get-haut @is-selected]))
        on-select (fn [{oid :value}] (dispatch [haku-events/select-haku oid]))
        on-clear (fn [] (dispatch [haku-events/clear-selected-haku]))]
    (fn []
      [:div (stylefy/use-style {:grid-area "haku-search"} {:cypressid "haku-search-cypress"})
       [:div (stylefy/use-style {:margin-bottom "10px"})
        [:span @title-text]
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
   :grid "\"select-all none add-to-group-btn\" 40px"
   :grid-row 4
   :grid-column 3})

(defn hakukohteet-container []
  (let [hakukohteet (subscribe [haku-subs/haku-hakukohteet-as-options])
        hakukohteet-is-empty (subscribe [haku-subs/haku-hakukohteet-is-empty])
        hakukohteet-label (subscribe [:translation :haku/hakukohteet])
        hakukohteet-search-placeholder (subscribe [:translation :haku/hakukohteet-search-placeholder])
        selected-hakukohteet (subscribe [haku-subs/haku-selected-hakukohteet])
        selected-hakukohderyhma (subscribe [hakukohderyhma-subs/selected-hakukohderyhma])
        add-to-group-btn-text (subscribe [:translation :hakukohderyhma/liita-ryhmaan])]
    (fn []
      [:div (stylefy/use-style hakukohteet-container-style)
       [:span (stylefy/use-style {:grid-row 1 :grid-column "1 / 3"}) @hakukohteet-label]
       [:div (stylefy/use-style {:grid-row 2 :grid-column "1 / 4"})
        [input/input-text {:cypressid   "hakukohteet-text-filter"
                           :input-id    "hakukohteet-text-filter"
                           :on-change   #(dispatch [haku-events/set-hakukohteet-filter %])
                           :placeholder @hakukohteet-search-placeholder
                           :aria-label  @hakukohteet-search-placeholder
                           :is-disabled @hakukohteet-is-empty}]]
       [:div (stylefy/use-style {:grid-row 3 :grid-column "1 / 4"})
        [multi-select/multi-select {:options   @hakukohteet
                                    :select-fn #(dispatch [haku-events/toggle-hakukohde-selection %])
                                    :cypressid "hakukohteet-container"}]]
       [:div (stylefy/use-style button-row-style)
        [button/button {:cypressid "add-to-group-btn"
                        :disabled? (or (empty? @selected-hakukohteet) (nil? @selected-hakukohderyhma))
                        :label @add-to-group-btn-text
                        :on-click #(dispatch [hakukohderyhma-events/add-hakukohteet-to-hakukohderyhma
                                              @selected-hakukohteet])
                        :style-prefix "add-to-group-btn"}]]])))
