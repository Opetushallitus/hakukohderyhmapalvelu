(ns hakukohderyhmapalvelu.events.hakukohderyhma-create-events
  (:require [hakukohderyhmapalvelu.macros.event-macros :as events]))

(events/reg-event-db-validating
  :hakukohderyhma-create/toggle-grid-visibility
  (fn [db]
    (update-in db [:ui :create-hakukohderyhma-grid :visible?] not)))
