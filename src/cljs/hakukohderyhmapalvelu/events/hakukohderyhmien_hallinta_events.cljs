(ns hakukohderyhmapalvelu.events.hakukohderyhmien-hallinta-events
  (:require [hakukohderyhmapalvelu.macros.event-macros :as events]
            [hakukohderyhmapalvelu.api-schemas :as schemas]
            [day8.re-frame.tracing :refer-macros [fn-traced]]))

(events/reg-event-db-validating
  :hakukohderyhmien-hallinta/toggle-grid-visibility
  (fn-traced [db]
    (update-in db [:create-hakukohderyhma :visible?] not)))

(events/reg-event-db-validating
  :hakukohderyhmien-hallinta/handle-save-hakukohderyhma
  (fn-traced [db [hakukohderyhma response]]
    (println (str "DEBUG :hakukohderyhmien-hallinta/handle-save-hakukohderyhma: " {:hakukohderyhma hakukohderyhma :response response}))
    db))

(events/reg-event-fx-validating
  :hakukohderyhmien-hallinta/save-hakukohderyhma
  (fn-traced [{db :db} [hakukohderyhma-name]]
    (let [http-request-id :hakukohderyhmien-hallinta/save-hakukohderyhma
          body            {:nimi {:fi hakukohderyhma-name}}]
      {:db               (update db :requests (fnil conj #{}) http-request-id)
       :http             {:method          :post
                          :http-request-id http-request-id
                          :path            "/hakukohderyhmapalvelu/api/hakukohderyhma"
                          :request-schema  schemas/HakukohderyhmaRequest
                          :response-schema schemas/HakukohderyhmaResponse}
       :response-handler [:hakukohderyhmien-hallinta/handle-save-hakukohderyhma {:nimi hakukohderyhma-name}]
       :body             body})))
