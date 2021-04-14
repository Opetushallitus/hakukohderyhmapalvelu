(ns hakukohderyhmapalvelu.events.hakukohderyhmien-hallinta-events
  (:require [hakukohderyhmapalvelu.macros.event-macros :as events]
            [hakukohderyhmapalvelu.api-schemas :as schemas]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [clojure.set :refer [union]]
            [schema-tools.core :as st]
            [hakukohderyhmapalvelu.i18n.utils :refer [sort-items-by-name]]))


(def root-path [:hakukohderyhma])
(def persisted-hakukohderyhmas (conj root-path :persisted))

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
(def hakukohderyhma-deleted :hakukohderyhmien-hallinta/hakukohderyhma-deleted)
(def hakukohderyhma-deletion-confirmed :hakukohderyhmien-hallinta/hakukohderyhma-rename-confirmed)

(defn- toggle-hakukohde [hakukohde-oid hakukohteet]
  (let [toggle-fn (fn [{oid :oid :as hakukohde}]
                    (if (= oid hakukohde-oid)
                      (update hakukohde :is-selected not)
                      hakukohde))]
    (map toggle-fn hakukohteet)))

(defn- conform-hakukohde-to-schema [hakukohde]
  (merge {:is-selected false} hakukohde))

(defn- conform-hakukohderyhma-to-schema [hakukohderyhma]
  (-> (merge {:is-selected false} hakukohderyhma)
      (update :hakukohteet #(map conform-hakukohde-to-schema %))))

(defn- selected-hakukohderyhma [db]
  (->> (get-in db persisted-hakukohderyhmas)
       (filter :is-selected)
       first))

(defn- update-hakukohderyhma [db hakukohderyhma]
  (->> (get-in db persisted-hakukohderyhmas)
       (map #(if (:is-selected %) hakukohderyhma %))
       (assoc-in db persisted-hakukohderyhmas)))

(events/reg-event-db-validating
  hakukohderyhma-selected
  (fn-traced [db [{selected-oid :value}]]
             (->> (get-in db persisted-hakukohderyhmas)
                  (map #(assoc % :is-selected (= (:oid %) selected-oid)))
                  (assoc-in db persisted-hakukohderyhmas))))

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

(events/reg-event-fx-validating
  hakukohderyhma-persisting-confirmed
  (fn-traced [{db :db} [hakukohderyhma _]]
             (let [hakukohderyhma' (conform-hakukohderyhma-to-schema hakukohderyhma)
                   db-ryhmat (get-in db persisted-hakukohderyhmas)
                   ryhmat-with-new-ryhma (->> hakukohderyhma'
                                              (conj db-ryhmat)
                                              (sort-items-by-name (:lang db)))]
               {:db (-> db
                        (assoc-in persisted-hakukohderyhmas ryhmat-with-new-ryhma)
                        (assoc-in create-input-is-active false))
                :dispatch [hakukohderyhma-selected {:value (:oid hakukohderyhma')}]})))

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
  (fn-traced [db [{:keys [oid] :as hakukohderyhma}]]
             (let [edited-fields [:version :nimi]
                   db-ryhmat (get-in db persisted-hakukohderyhmas)
                   merge-rename-data #(merge % (select-keys hakukohderyhma edited-fields))
                   ryhmat-with-rename (->> db-ryhmat
                                           (map #(cond-> % (= oid (:oid %)) merge-rename-data))
                                           (sort-items-by-name (:lang db)))]
               (-> db
                   (assoc-in persisted-hakukohderyhmas ryhmat-with-rename)
                   (assoc-in rename-input-is-active false)))))

(events/reg-event-fx-validating
  hakukohderyhma-renamed
  (fn-traced [{db :db} [hakukohderyhma-name]]
             (let [http-request-id hakukohderyhma-renamed
                   selected-ryhma (selected-hakukohderyhma db)
                   language (:lang db)
                   body (-> selected-ryhma
                            (st/select-schema schemas/HakukohderyhmaPutRequest)
                            (assoc-in [:nimi (keyword language)] hakukohderyhma-name))]
               {:db   (update db :requests (fnil conj #{}) http-request-id)
                :http {:method           :post
                       :http-request-id  http-request-id
                       :path             (str "/hakukohderyhmapalvelu/api/hakukohderyhma/" (:oid selected-ryhma) "/rename")
                       :request-schema   schemas/HakukohderyhmaPutRequest
                       :response-schema  schemas/HakukohderyhmaResponse
                       :response-handler [hakukohderyhma-renaming-confirmed]
                       :body             body}})))

(events/reg-event-db-validating
  hakukohderyhma-deletion-confirmed
  (fn-traced [db [deleted-oid {:keys [status]}]]
             (let [db-ryhmat (get-in db persisted-hakukohderyhmas)
                   with-deletion (filter #(not= deleted-oid (:oid %)) db-ryhmat)]
               (if (= "deleted" status)
                 (-> db
                     (assoc-in persisted-hakukohderyhmas with-deletion)
                     (assoc-in rename-input-is-active false))
                 (println "Hakukohderyhma deletion failed with status " status)))))

(events/reg-event-fx-validating
  hakukohderyhma-deleted
  (fn-traced [{db :db} [hakukohderyhma]]
             (let [http-request-id hakukohderyhma-deleted]
               {:db   (update db :requests (fnil conj #{}) http-request-id)
                :http {:method           :delete
                       :http-request-id  http-request-id
                       :path             (str "/hakukohderyhmapalvelu/api/hakukohderyhma/" (:oid hakukohderyhma))
                       :response-schema  schemas/HakukohderyhmaDeleteResponse
                       :response-handler [hakukohderyhma-deletion-confirmed (:oid hakukohderyhma)]}})))

(def get-hakukohderyhmat-for-hakukohteet :hakukohderyhmien-hallinta/get-all-hakukohderyhma)
(def handle-get-all-hakukohderyhma :hakukohderyhmien-hallinta/handle-get-all-hakukohderyhma)
(def added-hakukohteet-to-hakukohderyhma :hakukohderyhmien-hallinta/add-hakukohteet-to-hakukohderyhma)
(def removed-hakukohteet-from-hakukohderyhma :hakukohderyhmien-hallinta/remove-hakukohteet-from-hakukohderyhma)
(def toggle-hakukohde-selection :hakukohderyhmien-hallinta/toggle-hakukohde-selection)
(def save-hakukohderyhma-hakukohteet :hakukohderyhmien-hallinta/save-hakukohderyhma-hakukohteet)
(def handle-save-hakukohderyhma-hakukohteet :hakukohderyhmien-hallinta/handle-save-hakukohderyhma-hakukohteet)

(defn- create-hakukohderyhma-search-request [{:keys [http-request-id hakukohde-oids response-handler]}]
  {:method           :post
   :http-request-id  http-request-id
   :path             "/hakukohderyhmapalvelu/api/hakukohderyhma/search/find-by-hakukohde-oids"
   :response-schema  schemas/HakukohderyhmaListResponse
   :response-handler [response-handler]
   :body             {:oids hakukohde-oids :includeEmpty true}})


(events/reg-event-db-validating
  handle-get-all-hakukohderyhma
  (fn-traced [db [response]]
             (->> (map conform-hakukohderyhma-to-schema response)
                  (sort-items-by-name (:lang db))
                  (assoc-in db persisted-hakukohderyhmas))))

(events/reg-event-fx-validating
  get-hakukohderyhmat-for-hakukohteet
  (fn-traced [{db :db} [hakukohde-oids]]
             (let [http-request-id :hakukohderyhmien-hallinta/get-all-hakukohderyhma]
               {:db   (update db :requests (fnil conj #{}) http-request-id)
                :http (create-hakukohderyhma-search-request
                        {:http-request-id  http-request-id
                         :hakukohde-oids   hakukohde-oids
                         :response-handler handle-get-all-hakukohderyhma})})))

(events/reg-event-fx-validating
  added-hakukohteet-to-hakukohderyhma
  (fn-traced [{db :db} [hakukohteet]]
             (let [hakukohderyhma (selected-hakukohderyhma db)
                   current-hakukohteet (:hakukohteet hakukohderyhma)
                   unselected-hakukohteet (map #(assoc % :is-selected false) hakukohteet)
                   updated-unsorted-hakukohteet (vec (union (set current-hakukohteet) (set unselected-hakukohteet)))
                   updated-hakukohteet (sort-items-by-name (:lang db) updated-unsorted-hakukohteet)
                   hakukohderyhma' (assoc hakukohderyhma :hakukohteet updated-hakukohteet)]
               {:db       (update-hakukohderyhma db hakukohderyhma')
                :dispatch [save-hakukohderyhma-hakukohteet (:oid hakukohderyhma) updated-hakukohteet]})))

(events/reg-event-fx-validating
  removed-hakukohteet-from-hakukohderyhma
  (fn-traced [{db :db} [hakukohteet]]
             (let [hakukohderyhma (selected-hakukohderyhma db)
                   oids-to-remove (set (map :oid hakukohteet))
                   current-hakukohteet (:hakukohteet hakukohderyhma)
                   updated-hakukohteet (remove #(oids-to-remove (:oid %)) current-hakukohteet)
                   hakukohderyhma' (assoc hakukohderyhma :hakukohteet updated-hakukohteet)]
               {:db       (update-hakukohderyhma db hakukohderyhma')
                :dispatch [save-hakukohderyhma-hakukohteet (:oid hakukohderyhma) updated-hakukohteet]})))

(events/reg-event-db-validating
  toggle-hakukohde-selection
  (fn-traced [db [oid]]
             (let [hakukohderyhmas (->> (get-in db persisted-hakukohderyhmas)
                                        (map #(cond-> % (:is-selected %) (update :hakukohteet (partial toggle-hakukohde oid)))))]
               (assoc-in db persisted-hakukohderyhmas hakukohderyhmas))))

(events/reg-event-db-validating
  handle-save-hakukohderyhma-hakukohteet
  (fn-traced [db [{oid :oid :as hakukohderyhma}]]
             (let [updated-hakukohderyhma (-> hakukohderyhma
                                              (assoc :is-selected true)
                                              (select-keys [:is-selected :hakukohteet])
                                              (update :hakukohteet #(sort-items-by-name (:lang db) %))) ;TODO i18n and extract
                   update-fn (fn [hks] (map #(if (= (:oid %) oid)
                                               (merge % (conform-hakukohderyhma-to-schema updated-hakukohderyhma))
                                               %)
                                            hks))]
               (update-in db persisted-hakukohderyhmas update-fn))))

(events/reg-event-fx-validating
  save-hakukohderyhma-hakukohteet
  (fn-traced [_ [oid hakukohteet]]
             (let [body (st/select-schema hakukohteet [schemas/Hakukohde])]
               {:http {:method           :put
                       :http-request-id  save-hakukohderyhma-hakukohteet
                       :path             (str "/hakukohderyhmapalvelu/api/hakukohderyhma/" oid "/hakukohteet")
                       :request-schema   [schemas/Hakukohde]
                       :response-schema  schemas/Hakukohderyhma
                       :response-handler [handle-save-hakukohderyhma-hakukohteet]
                       :body             body}})))
