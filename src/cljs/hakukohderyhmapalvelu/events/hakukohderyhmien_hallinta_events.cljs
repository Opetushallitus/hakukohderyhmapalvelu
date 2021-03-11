(ns hakukohderyhmapalvelu.events.hakukohderyhmien-hallinta-events
  (:require [hakukohderyhmapalvelu.macros.event-macros :as events]
            [hakukohderyhmapalvelu.api-schemas :as schemas]
            [day8.re-frame.tracing :refer-macros [fn-traced]]))

(def root-path [:hakukohderyhma])
(def persisted-hakukohderyhmas (conj root-path :persisted))
(def selected-hakukohderyhma (conj root-path :selected-hakukohderyhma))
(def create-hakukohderyhma-is-visible (conj root-path :create-hakukohderyhma-visible?))

(events/reg-event-db-validating
  :hakukohderyhmien-hallinta/select-hakukohderyhma
  (fn-traced [db [hakukohderyhma]]
             (let [persisted-hakukohderyhmas (get-in db persisted-hakukohderyhmas)
                   selected-oid (:value hakukohderyhma)
                   hakukohderyhma-to-be-selected (->> persisted-hakukohderyhmas
                                                      (filter #(= selected-oid (:oid %)))
                                                      first)]
               (assoc-in db selected-hakukohderyhma hakukohderyhma-to-be-selected))))

(events/reg-event-db-validating
  :hakukohderyhmien-hallinta/toggle-grid-visibility
  (fn-traced [db]
             (update-in db create-hakukohderyhma-is-visible not)))

(events/reg-event-db-validating
  :hakukohderyhmien-hallinta/handle-save-hakukohderyhma
  (fn-traced [db [hakukohderyhma _]]
             (-> db
                 (update-in persisted-hakukohderyhmas #(conj % hakukohderyhma))
                 (assoc-in selected-hakukohderyhma hakukohderyhma)
                 (assoc-in create-hakukohderyhma-is-visible false))))

(events/reg-event-fx-validating
  :hakukohderyhmien-hallinta/save-hakukohderyhma
  (fn-traced [{db :db} [hakukohderyhma-name]]
    (let [http-request-id :hakukohderyhmien-hallinta/save-hakukohderyhma
          body            {:nimi {:fi hakukohderyhma-name}}]
      {:db   (update db :requests (fnil conj #{}) http-request-id)
       :http {:method           :post
              :http-request-id  http-request-id
              :path             "/hakukohderyhmapalvelu/api/hakukohderyhma"
              :request-schema   schemas/HakukohderyhmaRequest
              :response-schema  schemas/HakukohderyhmaResponse
              :response-handler [:hakukohderyhmien-hallinta/handle-save-hakukohderyhma]
              :body             body}})))

(def get-all-hakukohderyhma :hakukohderyhmien-hallinta/get-all-hakukohderyhma)
(def handle-get-all-hakukohderyhma :hakukohderyhmien-hallinta/handle-get-all-hakukohderyhma)

(events/reg-event-db-validating
  :hakukohderyhmien-hallinta/handle-get-all-hakukohderyhma
  (fn-traced [db [response]]
             (println (str "DEBUG :hakukohderyhmien-hallinta/handle-get-all-hakukohderyhma: " {:response response}))
             (assoc-in db persisted-hakukohderyhmas (set response))))

(events/reg-event-fx-validating
  :hakukohderyhmien-hallinta/get-all-hakukohderyhma
  (fn-traced [{db :db} [_]]
    (let [http-request-id :hakukohderyhmien-hallinta/get-all-hakukohderyhma]
      {:db   (update db :requests (fnil conj #{}) http-request-id)
       :http {:method           :get
              :http-request-id  http-request-id
              :path             "/hakukohderyhmapalvelu/api/hakukohderyhma-all"
              :response-handler [handle-get-all-hakukohderyhma]
              :body             {}}})))
