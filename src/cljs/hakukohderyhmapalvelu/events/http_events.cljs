(ns hakukohderyhmapalvelu.events.http-events
  (:require [hakukohderyhmapalvelu.macros.event-macros :as events]))

(events/reg-event-db-validating
  :http/remove-http-request-id
  (fn [db [http-request-id]]
    (update db :requests (fnil disj #{}) http-request-id)))
