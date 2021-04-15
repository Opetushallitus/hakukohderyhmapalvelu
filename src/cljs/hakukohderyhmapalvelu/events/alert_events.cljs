(ns hakukohderyhmapalvelu.events.alert-events
  (:require [hakukohderyhmapalvelu.macros.event-macros :as events]
            [day8.re-frame.tracing :refer-macros [fn-traced]]))

;Polut
(def root-path [:alert])
(def alert-message-path (conj root-path :message))

;Tapahtumat
(def alert-closed :alert/alert-window-closed)
(def new-alert :alert/new-alert)

(events/reg-event-db-validating
  new-alert
  (fn-traced [db [message]]
             (assoc-in db alert-message-path message)))

(events/reg-event-db-validating
  alert-closed
  (fn-traced [db [_]]
             (assoc-in db alert-message-path "")))
