(ns hakukohderyhmapalvelu.events.haku-events
  (:require [hakukohderyhmapalvelu.macros.event-macros :as events]
            [hakukohderyhmapalvelu.api-schemas :as schemas]
            [hakukohderyhmapalvelu.events.hakukohderyhmien-hallinta-events :as hakukohderyhma-events]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [hakukohderyhmapalvelu.i18n.utils :as i18n-utils]
            [clojure.string :as str]))

(defn- includes-string? [m string lang]
  (-> (i18n-utils/get-with-fallback m lang)
      str/lower-case
      (str/includes? string)))

(defn- hakukohde-includes-string? [hakukohde string lang]
  (let [search-paths [[:organisaatio :nimi] [:nimi]]
        lower-str (str/lower-case string)]
    (some #(includes-string? (get-in hakukohde %) lower-str lang) search-paths)))

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
(def all-hakukohde-selected :haku/select-all-hakukohde)
(def all-hakukohde-deselected :haku/deselect-all-hakukohde)
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

(defn- deselect-all-items [items]
  (map
    #(assoc % :is-selected false)
    items))

(defn- deselect-all-hakukohde [haut]
  (map (fn [haku]
         (if (:is-selected haku)
           (update haku :hakukohteet deselect-all-items)
           haku))
       haut))

(defn- select-all-hakukohde-in-view [filter-str haut]
  (map (fn [haku]
         (if (:is-selected haku)
           (update haku :hakukohteet #(map
                                        (fn [hakukohde]
                                          (if (hakukohde-includes-string? hakukohde filter-str :fi);TODO pass current lang
                                            (assoc hakukohde :is-selected true)
                                            hakukohde))
                                        %))
           haku))
       haut))

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

(events/reg-event-fx-validating
  handle-get-hakukohteet-response
  (fn-traced [{db :db} [haku-oid response]]
             (let [hakukohteet (map #(assoc % :is-selected false) response)
                   hakukohde-oids (map :oid hakukohteet)]
               {:db (update-in db haku-haut (partial add-hakukohteet-for-haku haku-oid hakukohteet))
                :dispatch [hakukohderyhma-events/get-hakukohderyhmat-for-hakukohteet hakukohde-oids]})))

(events/reg-event-fx-validating
  clear-selected-haku
  (fn-traced [{db :db} _]
             {:db       (update-in db haku-haut deselect-all)
              :dispatch [hakukohderyhma-events/handle-get-all-hakukohderyhma []]}))

(events/reg-event-fx-validating
  select-haku
  (fn-traced [{db :db} [haku-oid]]
             {:db       (update-in db haku-haut (partial select-one haku-oid))
              :dispatch-n [[hakukohderyhma-events/handle-get-all-hakukohderyhma []]
                           [get-haun-hakukohteet haku-oid]]}))

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
  all-hakukohde-deselected
  (fn-traced [db [_]]
             (update-in db haku-haut deselect-all-hakukohde)))

(events/reg-event-db-validating
  all-hakukohde-selected
  (fn-traced [db [_]]
             (let [filter-str (get-in db haku-hakukohteet-filter)]
               (update-in db haku-haut (partial select-all-hakukohde-in-view filter-str)))))

(events/reg-event-db-validating
  toggle-hakukohde-selection
  (fn-traced [db [hakukohde-oid]]
             (update-in db haku-haut (partial toggle-selection-of-hakukohde hakukohde-oid))))

(events/reg-event-db-validating
  set-hakukohteet-filter
  (fn-traced [db [filter-text]]
             (assoc-in db haku-hakukohteet-filter filter-text)))
