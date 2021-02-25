(ns hakukohderyhmapalvelu.views.haku-view
  (:require
    [reagent.core :as reagent]
    [re-frame.core :refer [subscribe dispatch]]
    [stylefy.core :as stylefy]
    [hakukohderyhmapalvelu.subs.haku-subs :as haku-subs]
    [hakukohderyhmapalvelu.events.haku-events :as haku-events]
    [hakukohderyhmapalvelu.components.common.react-select :as react-select]
    [hakukohderyhmapalvelu.components.common.checkbox :as checkbox]))


(defn haku-search []
  (let [options (subscribe [haku-subs/haku-haut-as-options])
        is-selected (reagent/atom false)
        is-loading (subscribe [haku-subs/haku-is-loading])
        title-text (subscribe [:translation :haku/haku])
        label-text (subscribe [:translation :haku/show-all-haut])
        placeholder-text (subscribe [:translation :haku/haku-search-placeholder])
        on-change (fn [_]
                    (swap! is-selected not)
                    (dispatch [haku-events/get-haut @is-selected]))]
    (fn []
      [:div {:cypressid "haku-search-cypress"}
       [:div (stylefy/use-style {:margin-bottom "10px"})
        [:span @title-text]
        [:div (stylefy/use-style {:float "right"})
         [checkbox/checkbox-with-label {:id        "haku-search-checkbox"
                                        :checked?  @is-selected
                                        :cypressid "haku-search-checkbox-cypress"
                                        :disabled? @is-loading
                                        :label     @label-text
                                        :on-change on-change}]]]
       [react-select/select {:options     @options
                             :is-loading  @is-loading
                             :is-disabled @is-loading
                             :placeholder @placeholder-text}]])))
