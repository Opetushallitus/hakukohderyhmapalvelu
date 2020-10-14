(ns hakukohderyhmapalvelu.events.panel-menu-events
  (:require
    [hakukohderyhmapalvelu.macros.event-macros :as events]
    [day8.re-frame.tracing :refer-macros [fn-traced]]))

(events/reg-event-db-validating
  :panel-menu/set-active-panel
  (fn-traced [db [active-panel]]
    (assoc db :active-panel active-panel)))
