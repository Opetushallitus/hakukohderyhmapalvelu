(ns hakukohderyhmapalvelu.events.core-events
  (:require
    [re-frame.core :as re-frame]
    [hakukohderyhmapalvelu.db :as db]
    [day8.re-frame.tracing :refer-macros [fn-traced]]))

(re-frame/reg-event-db
  :core/initialize-db
  (fn-traced [_ _]
    db/default-db))
