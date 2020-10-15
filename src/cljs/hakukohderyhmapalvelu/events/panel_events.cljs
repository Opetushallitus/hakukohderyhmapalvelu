(ns hakukohderyhmapalvelu.events.panel-events
  (:require
    [hakukohderyhmapalvelu.macros.event-macros :as events]
    [day8.re-frame.tracing :refer-macros [fn-traced]]))

(events/reg-event-db-validating
  :panel/set-active-panel
  (fn-traced [db [active-panel]]
    (assoc db :active-panel active-panel)))
