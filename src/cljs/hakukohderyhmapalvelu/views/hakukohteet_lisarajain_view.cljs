(ns hakukohderyhmapalvelu.views.hakukohteet-lisarajain-view
  (:require [re-frame.core :refer [dispatch subscribe]]
            [hakukohderyhmapalvelu.components.common.popup :as popup]
            [hakukohderyhmapalvelu.components.common.checkbox :as checkbox]
            [hakukohderyhmapalvelu.components.common.react-select :as react-select]
            [hakukohderyhmapalvelu.events.haku-events :as haku-events]
            [hakukohderyhmapalvelu.subs.haku-subs :as haku-subs]
            [stylefy.core :as stylefy]))


(defmulti extra-filter :type)

(defmethod extra-filter :boolean [{:keys [id label value]}]
  [checkbox/checkbox-with-label {:checked?  value
                                 :cypressid (str id "-extra-filter")
                                 :disabled? false
                                 :id        id
                                 :label     @(subscribe [:translation label])
                                 :on-change #(dispatch [haku-events/set-haku-lisarajaimet-filter id not])}])

(defmethod extra-filter :select [{:keys [id label value options]}]
  [:div {:cypressid (str id "-extra-filter")}
   [react-select/select {:placeholder  @(subscribe [:translation label])
                         :options      options
                         :is-disabled  (empty? options)
                         :on-select-fn #(dispatch [haku-events/set-haku-lisarajaimet-filter id (constantly %)])
                         :on-clear-fn  #(dispatch [haku-events/set-haku-lisarajaimet-filter id (constantly nil)])
                         :value        value}]])

(def ^:private vertical-padding
  {:padding-top "1rem"})

(def ^:private style
  {:width "25rem"
   :left  "calc(50% - 25rem)"})

(defn extra-filters []
  (let [filters @(subscribe [haku-subs/haku-lisarajaimet-filters])]
    [popup/popup {:style     style
                  :on-close  #(dispatch [haku-events/close-haku-lisarajaimet])
                  :cypressid "extra-filters-popup"}
     (for [filter-opts filters]
       ^{:key (:id filter-opts)} [:div (stylefy/use-style vertical-padding)
                                  [extra-filter filter-opts]])]))
