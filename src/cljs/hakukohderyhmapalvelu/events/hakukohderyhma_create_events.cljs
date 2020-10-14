(ns hakukohderyhmapalvelu.events.hakukohderyhma-create-events
  (:require [hakukohderyhmapalvelu.macros.event-macros :as events]
            [hakukohderyhmapalvelu.api-schemas :as schemas]
            [day8.re-frame.tracing :refer-macros [fn-traced]]))

(events/reg-event-db-validating
  :hakukohderyhma-create/toggle-grid-visibility
  (fn-traced [db]
    (update-in db [:create-hakukohderyhma :visible?] not)))

(events/reg-event-db-validating
  :hakukohderyhma-create/handle-save-hakukohderyhma
  (fn-traced [db [hakukohderyhma response]]
    (println (str "DEBUG :hakukohderyhma-create/handle-save-hakukohderyhma: " {:hakukohderyhma hakukohderyhma :response response}))
    db))

(events/reg-event-fx-validating
  :hakukohderyhma-create/save-hakukohderyhma
  (fn-traced [{db :db} [hakukohderyhma-name]]
    (let [http-request-id :hakukohderyhma-create/save-hakukohderyhma
          body            {:nimi {:fi hakukohderyhma-name}}]
      {:db   (update db :requests (fnil conj #{}) http-request-id)
       :http {:method           :post
              :http-request-id  http-request-id
              :path             "/hakukohderyhmapalvelu/api/hakukohderyhma"
              :request-schema   schemas/HakukohderyhmaRequest
              :response-schema  schemas/HakukohderyhmaResponse
              :response-handler [:hakukohderyhma-create/handle-save-hakukohderyhma {:nimi hakukohderyhma-name}]
              :body             body}})))
