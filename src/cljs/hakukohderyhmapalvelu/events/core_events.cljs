(ns hakukohderyhmapalvelu.events.core-events
  (:require
    [hakukohderyhmapalvelu.db :as db]
    [hakukohderyhmapalvelu.macros.event-macros :as events]
    [day8.re-frame.tracing :refer-macros [fn-traced]]))

(events/reg-event-db-validating
  :core/initialize-db
  (fn-traced [_ _]
    db/default-db))
