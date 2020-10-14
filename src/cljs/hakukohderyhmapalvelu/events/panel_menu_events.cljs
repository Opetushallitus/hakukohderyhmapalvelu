(ns hakukohderyhmapalvelu.events.panel-menu-events
  (:require
    [re-frame.core :as re-frame]
    [day8.re-frame.tracing :refer-macros [fn-traced]]))

(re-frame/reg-event-db
  :panel-menu/set-active-panel
  (fn-traced [db [_ active-panel]]
    (assoc db :active-panel active-panel)))
