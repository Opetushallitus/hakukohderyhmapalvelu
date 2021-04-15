(ns hakukohderyhmapalvelu.subs.alert-subs
  [:require [re-frame.core :as re-frame]
            [hakukohderyhmapalvelu.events.alert-events :as alert-events]])


;Tilaukset
(def alert-message :alert/alert-message)

(re-frame/reg-sub
  alert-message
  (fn [db _]
    (get-in db alert-events/alert-message-path)))
