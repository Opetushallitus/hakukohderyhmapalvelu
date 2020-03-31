(ns hakukohderyhmapalvelu.events.hakukohderyhma-create-events
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-event-db
  :hakukohderyhma-create/toggle-grid-visibility
  [re-frame/trim-v]
  (fn [db]
    (update-in db [:ui :create-grid :visible?] not)))

