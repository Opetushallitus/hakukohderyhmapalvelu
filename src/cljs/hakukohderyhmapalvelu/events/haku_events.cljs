(ns hakukohderyhmapalvelu.events.haku-events
  (:require [hakukohderyhmapalvelu.macros.event-macros :as events]
            [hakukohderyhmapalvelu.api-schemas :as schemas]
            [day8.re-frame.tracing :refer-macros [fn-traced]]))


;; Polut
(def root-path [:hakukohderyhma])
(def haku-haut (conj root-path :haut))
(def haku-hakukohteet-filter (conj root-path :hakukohteet-filter))

;; Tapahtumat
(def get-haut :haku/get-haut)
(def handle-get-haut-response :haku/handle-get-haut-response)
(def select-haku :haku/select-haku)
(def clear-selected-haku :haku/clear-selected-haku)
(def get-haun-hakukohteet :haku/get-haun-hakukohteet)
(def handle-get-hakukohteet-response :haku/handle-get-hakukohteet-response)
(def toggle-hakukohde-selection :haku/toggle-hakukohde-selection)
(def set-hakukohteet-filter :haku/set-hakukohteet-filter)

;; Apufunktiot
(defn- deselect-all [items]
  (map #(assoc % :is-selected false
                 :hakukohteet []) items))

(defn- select-one [oid items]
  (map #(assoc % :is-selected (= (:oid %) oid)
                 :hakukohteet []) items))

(defn- toggle-item-select [oid items]
  (map (fn [item]
         (if (= (:oid item) oid)
           (update item :is-selected not)
           item)) items))

(defn- add-hakukohteet-for-haku [haku-oid hakukohteet haut]
  (map (fn [haku]
         (if (= (:oid haku) haku-oid)
           (assoc haku :hakukohteet hakukohteet)
           haku)) haut))

(defn- toggle-selection-of-hakukohde [hakukohde-oid haut]
  (map (fn [haku]
         (if (:is-selected haku)
           (update haku :hakukohteet (partial toggle-item-select hakukohde-oid))
           haku)) haut))

;; Käsittelijät
(events/reg-event-db-validating
  handle-get-haut-response
  (fn-traced [db [response]]
             (->> response
                  (map #(assoc % :is-selected false
                                 :hakukohteet []))
                  (assoc-in db haku-haut))))

(events/reg-event-fx-validating
  get-haut
  (fn-traced [{db :db} [is-fetch-all]]
             (let [http-request-id get-haut
                   is-fetch-all (boolean is-fetch-all)]
               {:db   (update db :requests (fnil conj #{}) http-request-id)
                :http {:method           :get
                       :http-request-id  http-request-id
                       :path             (str "/hakukohderyhmapalvelu/api/haku?all=" is-fetch-all)
                       :response-schema  schemas/HaunTiedotListResponse
                       :response-handler [handle-get-haut-response]}})))

(events/reg-event-db-validating
  handle-get-hakukohteet-response
  (fn-traced [db [haku-oid response]]
             (let [hakukohteet (map #(assoc % :is-selected false) response)]
               (update-in db haku-haut (partial add-hakukohteet-for-haku haku-oid hakukohteet)))))

(events/reg-event-db-validating
  clear-selected-haku
  (fn-traced [db _]
             (update-in db haku-haut deselect-all)))

(events/reg-event-fx-validating
  select-haku
  (fn-traced [{db :db} [haku-oid]]
             {:db       (update-in db haku-haut (partial select-one haku-oid))
              :dispatch [get-haun-hakukohteet haku-oid]}))

(events/reg-event-fx-validating
  get-haun-hakukohteet
  (fn-traced [{db :db} [haku-oid]]
             (let [http-request-id get-haun-hakukohteet]
               {:db   (update db :requests (fnil conj #{}) http-request-id)
                :http {:method           :get
                       :http-request-id  http-request-id
                       :path             (str "/hakukohderyhmapalvelu/api/haku/" haku-oid "/hakukohde")
                       :response-schema  schemas/HakukohdeListResponse
                       :response-handler [handle-get-hakukohteet-response haku-oid]}})))

(events/reg-event-db-validating
  toggle-hakukohde-selection
  (fn-traced [db [hakukohde-oid]]
             (update-in db haku-haut (partial toggle-selection-of-hakukohde hakukohde-oid))))

(events/reg-event-db-validating
  set-hakukohteet-filter
  (fn-traced [db [filter-text]]
             (assoc-in db haku-hakukohteet-filter filter-text)))
