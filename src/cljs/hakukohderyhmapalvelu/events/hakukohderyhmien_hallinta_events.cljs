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
(def hakukohderyhma-selected :hakukohderyhmien-hallinta/hakukohderyhma-selected)
(def hakukohderyhma-persisted :hakukohderyhmien-hallinta/hakukohderyhma-persisted)
(def hakukohderyhma-persisting-confirmed :hakukohderyhmien-hallinta/hakukohderyhma-persist-confirmed)
(def hakukohderyhma-renamed :hakukohderyhmien-hallinta/hakukohderyhma-renamed)
(def hakukohderyhma-renaming-confirmed :hakukohderyhmien-hallinta/hakukohderyhma-rename-confirmed)

(events/reg-event-db-validating
  hakukohderyhma-selected
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
  hakukohderyhma-persisting-confirmed
  (fn-traced [db [hakukohderyhma _]]
             (-> db
                 (update-in persisted-hakukohderyhmas #(conj % hakukohderyhma))
                 (assoc-in selected-hakukohderyhma hakukohderyhma)
                 (assoc-in create-input-is-active false))))

(events/reg-event-fx-validating
  hakukohderyhma-persisted
  (fn-traced [{db :db} [hakukohderyhma-name]]
             (let [http-request-id hakukohderyhma-persisted
                   body {:nimi {:fi hakukohderyhma-name}}]
               {:db   (update db :requests (fnil conj #{}) http-request-id)
                :http {:method           :post
                       :http-request-id  http-request-id
                       :path             "/hakukohderyhmapalvelu/api/hakukohderyhma"
                       :request-schema   schemas/HakukohderyhmaPostRequest
                       :response-schema  schemas/Hakukohderyhma
                       :response-handler [hakukohderyhma-persisting-confirmed]
                       :body             body}})))

(events/reg-event-db-validating
  hakukohderyhma-renaming-confirmed
  (fn-traced [db [{:keys [oid nimi] :as hakukohderyhma} _]]
             (let [db-ryhmat (get-in db persisted-hakukohderyhmas)
                   ryhmat-with-rename (as-> db-ryhmat persisted-hakukohderyhmas'
                                            (filter #(not= oid (:oid %)) persisted-hakukohderyhmas')
                                            (conj persisted-hakukohderyhmas' hakukohderyhma))]
               (-> db
                   (assoc-in persisted-hakukohderyhmas (set ryhmat-with-rename))
                   (assoc-in selected-hakukohderyhma hakukohderyhma)
                   (assoc-in rename-input-is-active false)))))

(events/reg-event-fx-validating
  hakukohderyhma-renamed
  (fn-traced [{db :db} [hakukohderyhma-name]]
             (let [http-request-id hakukohderyhma-renamed
                   selected-ryhma (get-in db selected-hakukohderyhma)
                   body (merge
                          selected-ryhma
                          {:nimi {:fi hakukohderyhma-name}})]
               {:db   (update db :requests (fnil conj #{}) http-request-id)
                :http {:method           :post              ;TODO, should bet PUT-request?
                       :http-request-id  http-request-id
                       :path             "/hakukohderyhmapalvelu/api/hakukohderyhma/rename"
                       :request-schema   schemas/HakukohderyhmaPutRequest
                       :response-schema  schemas/HakukohderyhmaResponse
                       :response-handler [hakukohderyhma-renaming-confirmed]
                       :body             body}})))

(def get-hakukohderyhmat-for-hakukohteet :hakukohderyhmien-hallinta/get-all-hakukohderyhma)
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
  get-hakukohderyhmat-for-hakukohteet
  (fn-traced [{db :db} [hakukohde-oids]]
             (let [http-request-id :hakukohderyhmien-hallinta/get-all-hakukohderyhma]
               {:db   (update db :requests (fnil conj #{}) http-request-id)
                :http (create-hakukohderyhma-search-request
                        {:http-request-id  http-request-id
                         :hakukohde-oids   hakukohde-oids
                         :response-handler handle-get-all-hakukohderyhma})})))
