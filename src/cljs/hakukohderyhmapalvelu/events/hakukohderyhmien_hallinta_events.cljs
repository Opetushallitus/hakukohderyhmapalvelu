(ns hakukohderyhmapalvelu.events.hakukohderyhmien-hallinta-events
  (:require [hakukohderyhmapalvelu.macros.event-macros :as events]
            [hakukohderyhmapalvelu.api-schemas :as schemas]
            [day8.re-frame.tracing :refer-macros [fn-traced]]))

(def root-path [:hakukohderyhma])
(def persisted-hakukohderyhmas (conj root-path :persisted))
(def selected-hakukohderyhma (conj root-path :selected-hakukohderyhma))

(def ^:private input-visibility (conj root-path :input-visibility))
(def create-input-is-active (conj input-visibility :create-active?))
(def rename-input-is-active (conj input-visibility :rename-active?))

(def add-new-hakukohderyhma-link-clicked :hakukohderyhmien-hallinta/add-new-hakukohderyhma-link-clicked)
(def edit-hakukohderyhma-link-clicked :hakukohderyhmien-hallinta/rename-hakukohderyhma-link-clicked)

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
  add-new-hakukohderyhma-link-clicked
  (fn-traced [db]
             (-> db
                 (assoc-in rename-input-is-active false)
                 (update-in create-input-is-active not))))

(events/reg-event-db-validating
  edit-hakukohderyhma-link-clicked
  (fn-traced [db]
             (-> db
                 (assoc-in create-input-is-active false)
                 (update-in rename-input-is-active not))))

(events/reg-event-db-validating
  :hakukohderyhmien-hallinta/handle-save-hakukohderyhma
  (fn-traced [db [hakukohderyhma _]]
             (-> db
                 (update-in persisted-hakukohderyhmas #(conj % hakukohderyhma))
                 (assoc-in selected-hakukohderyhma hakukohderyhma)
                 (assoc-in create-input-is-active false))))

(events/reg-event-fx-validating
  :hakukohderyhmien-hallinta/save-hakukohderyhma
  (fn-traced [{db :db} [hakukohderyhma-name]]
             (let [http-request-id :hakukohderyhmien-hallinta/save-hakukohderyhma
                   body {:nimi {:fi hakukohderyhma-name}}]
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

(defn- create-hakukohderyhma-search-request [{:keys [http-request-id hakukohde-oids response-handler]}]
  {:method           :post
   :http-request-id  http-request-id
   :path             "/hakukohderyhmapalvelu/api/hakukohderyhma/find-by-hakukohde-oids"
   :response-schema  schemas/HakukohderyhmaListResponse
   :response-handler [response-handler]
   :body             {:oids hakukohde-oids}})

(events/reg-event-db-validating
  handle-get-all-hakukohderyhma
  (fn-traced [db [response]]
             (assoc-in db persisted-hakukohderyhmas (set response))))

(events/reg-event-fx-validating
  get-all-hakukohderyhma
  (fn-traced [{db :db} [_]]
             (let [http-request-id :hakukohderyhmien-hallinta/get-all-hakukohderyhma]
               {:db   (update db :requests (fnil conj #{}) http-request-id)
                :http (create-hakukohderyhma-search-request
                        {:http-request-id  http-request-id
                         :hakukohde-oids   []
                         :response-handler handle-get-all-hakukohderyhma})})))
