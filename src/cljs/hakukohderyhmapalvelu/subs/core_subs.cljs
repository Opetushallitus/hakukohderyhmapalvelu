(ns hakukohderyhmapalvelu.subs.core-subs
  (:require [hakukohderyhmapalvelu.i18n.translations :as t]
            [re-frame.core :as re-frame]))

(re-frame/reg-sub
  :state-query
  (fn [db [_ path default]]
    (get-in db path default)))

(re-frame/reg-sub
  :lang
  (fn [db]
    (:lang db)))

(re-frame/reg-sub
  :translation
  (fn []
    [(re-frame/subscribe [:lang])])
  (fn [[lang] [_ tx-key]]
    (or (-> t/translations tx-key lang)
        tx-key)))
