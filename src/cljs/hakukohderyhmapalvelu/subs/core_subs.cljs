(ns hakukohderyhmapalvelu.subs.core-subs
  (:require [hakukohderyhmapalvelu.i18n.utils :as i18n]
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
  :translations
  (fn [db]
    (:translations db)))

(re-frame/reg-sub
  :translation
  (fn []
    [(re-frame/subscribe [:lang])
     (re-frame/subscribe [:translations])])
  (fn [[lang translations] [_ tx-key]]
    (i18n/get-translation lang translations tx-key)))
