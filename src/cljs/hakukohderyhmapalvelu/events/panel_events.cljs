(ns hakukohderyhmapalvelu.events.panel-events
  (:require
    [hakukohderyhmapalvelu.macros.event-macros :as events]
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [hakukohderyhmapalvelu.events.haku-events :as haku-events]
    [hakukohderyhmapalvelu.events.hakukohderyhmien-hallinta-events :as hakukohderyhma-events]))

(defn- make-haun-asetukset-dispatches [{:keys [query]}]
  (let [haku-oid (:haku-oid query)]
    [[:haun-asetukset/get-forms]
     [:haun-asetukset/get-haku haku-oid]
     [:haun-asetukset/get-ohjausparametrit haku-oid]]))

(defn- make-hakukohderyhmien-hallinta-dispatches [_]
  [[hakukohderyhma-events/get-all-hakukohderyhma]
   [haku-events/get-haut]])

(defn- make-dispatches [{:keys [panel parameters]}]
  (when-let [make-fn (case panel
                       :panel/haun-asetukset make-haun-asetukset-dispatches
                       :panel/hakukohderyhmien-hallinta make-hakukohderyhmien-hallinta-dispatches
                       :default nil)]
    (make-fn parameters)))

(events/reg-event-fx-validating
  :panel/set-active-panel
  (fn-traced [{db :db} [active-panel]]
             (let [dispatches (make-dispatches active-panel)]
               (cond-> {:db (assoc db :active-panel active-panel)}
                       (seq dispatches)
                       (assoc :dispatch-n dispatches)))))
