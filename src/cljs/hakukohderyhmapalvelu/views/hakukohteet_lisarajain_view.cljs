(ns hakukohderyhmapalvelu.views.hakukohteet-lisarajain-view
  (:require [re-frame.core :refer [dispatch subscribe]]
            [hakukohderyhmapalvelu.components.common.popup :as popup]
            [hakukohderyhmapalvelu.components.common.checkbox :as checkbox]
            [hakukohderyhmapalvelu.events.haku-events :as haku-events]
            [hakukohderyhmapalvelu.subs.haku-subs :as haku-subs]))


(defmulti extra-filter :type)

(defmethod extra-filter :boolean [{:keys [id label value]}]
  [checkbox/checkbox-with-label {:checked?  value
                                 :cypressid (str id "-extra-filter")
                                 :disabled? false
                                 :id        id
                                 :label     @(subscribe [:translation label])
                                 :on-change #(dispatch [haku-events/set-haku-lisarajaimet-filter id not])}])

(def ^:private style
  {:width "25rem"
   :left  "calc(50% - 25rem)"})

(defn extra-filters []
  (let [filters @(subscribe [haku-subs/haku-lisarajaimet-filters])]
    [popup/popup {:style     style
                  :on-close  #(dispatch [haku-events/close-haku-lisarajaimet])
                  :cypressid "extra-filters-popup"}
     (for [filter-opts filters]
       ^{:key (:id filter-opts)} [extra-filter filter-opts])]))
