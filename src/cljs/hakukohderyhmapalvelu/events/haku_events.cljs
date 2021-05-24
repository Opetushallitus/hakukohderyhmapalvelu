(ns hakukohderyhmapalvelu.events.haku-events
  (:require [hakukohderyhmapalvelu.macros.event-macros :as events]
            [hakukohderyhmapalvelu.api-schemas :as schemas]
            [hakukohderyhmapalvelu.events.hakukohderyhmien-hallinta-events :as hakukohderyhma-events]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [hakukohderyhmapalvelu.haku-utils :as u]
            [hakukohderyhmapalvelu.i18n.utils :refer [sort-items-by-name koodisto->option]]
            [hakukohderyhmapalvelu.urls :as urls]
            [hakukohderyhmapalvelu.events.alert-events :as alert-events]))

;; Polut
(def root-path [:hakukohderyhma])
(def haku-haut (conj root-path :haut))
(def haku-hakukohteet-filter (conj root-path :hakukohteet-filter))
(def haku-lisarajaimet-path (conj root-path :lisarajaimet))
(def haku-lisarajaimet-visible-path (conj haku-lisarajaimet-path :popup-visible))
(def haku-lisarajaimet-filters-path (conj haku-lisarajaimet-path :filters))

;; Tapahtumat
(def get-haut :haku/get-haut)
(def handle-get-haut-response :haku/handle-get-haut-response)
(def select-haku :haku/select-haku)
(def clear-selected-haku :haku/clear-selected-haku)
(def get-haun-hakukohteet :haku/get-haun-hakukohteet)
(def handle-get-hakukohteet-response :haku/handle-get-hakukohteet-response)
(def toggle-hakukohde-selection :haku/toggle-hakukohde-selection)
(def all-hakukohde-in-view-selected :haku/select-all-hakukohde-in-view)
(def all-hakukohde-deselected :haku/deselect-all-hakukohde)
(def set-hakukohteet-filter :haku/set-hakukohteet-filter)
(def open-haku-lisarajaimet :haku/open-haku-lisarajaimet)
(def close-haku-lisarajaimet :haku/close-haku-lisarajaimet)
(def set-haku-lisarajaimet-filter :haku/set-haku-lisarajaimet-filter)
(def set-haku-lisarajaimet-options :haku/set-haku-lisarajaimet-options)
(def get-koulutustyypit :haku/get-koulutustyypit)
(def handle-get-koulutustyypit-response :haku/handle-get-koulutustyypit-response)

;; Apufunktiot
(defn- update-hakus-hakukohteet [items should-update? update-fn]
  (map
    #(if (should-update? %) (update-fn %) %)
    items))

(defn- add-hakukohteet-for-haku [haku-oid hakukohteet haut]
  (update-hakus-hakukohteet haut
                            #(= (:oid %) haku-oid)
                            #(assoc % :hakukohteet hakukohteet)))

(defn- edit-selected-hakus-hakukohteet [haut hakukohde-handler]
  (update-hakus-hakukohteet haut
                            :is-selected
                            (fn [haku] (update haku :hakukohteet #(map hakukohde-handler %)))))

(defn- deselect-all-hakukohde [haut]
  (edit-selected-hakus-hakukohteet haut u/deselect-item))

(defn- select-all-hakukohde-in-view [in-view? haut]
  (edit-selected-hakus-hakukohteet haut (partial u/select-filtered-item in-view?)))

(defn- toggle-selection-of-hakukohde [hakukohde-oid haut]
  (edit-selected-hakus-hakukohteet haut (partial u/toggle-filtered-item-selection #(= (:oid %) hakukohde-oid))))

(defn- update-haku-lisarajaimet-filter [id key value-fn filters]
  (map #(cond-> % (= id (:id %)) (update key value-fn)) filters))

;; Käsittelijät
(events/reg-event-db-validating
  handle-get-haut-response
  (fn-traced [db [response]]
             (->> response
                  (map #(assoc % :is-selected false
                                 :hakukohteet []))
                  (sort-items-by-name (:lang db))
                  (assoc-in db haku-haut))))

(events/reg-event-fx-validating
  get-haut
  (fn-traced [{db :db} [is-fetch-all]]
             (let [http-request-id get-haut
                   is-fetch-all (boolean is-fetch-all)]
               {:db   (update db :requests (fnil conj #{}) http-request-id)
                :http {:method           :get
                       :http-request-id  http-request-id
                       :path             "/hakukohderyhmapalvelu/api/haku"
                       :search-params    [[:all (str is-fetch-all)]]
                       :response-schema  schemas/HaunTiedotListResponse
                       :response-handler [handle-get-haut-response]
                       :error-handler    [alert-events/http-request-failed]}})))

(events/reg-event-fx-validating
  get-koulutustyypit
  (fn-traced [{db :db}]
             (let [http-request-id get-koulutustyypit
                   url (str (urls/get-url :koodisto-service.baseurl) "/koodisto-service/rest/json/searchKoodis")]
               {:db   (update db :requests (fnil conj #{}) http-request-id)
                :http {:method           :get
                       :http-request-id  http-request-id
                       :path             url
                       :search-params    [[:koodiUris "koulutustyyppi_1"]
                                          [:koodiUris "koulutustyyppi_2"]
                                          [:koodiUris "koulutustyyppi_4"]
                                          [:koodiUris "koulutustyyppi_10"]
                                          [:koodiUris "koulutustyyppi_40"]
                                          [:koodiTilas "HYVAKSYTTY"]
                                          [:koodiVersioSelection "LATEST"]]
                       :response-schema  schemas/KoodistoResponse
                       :response-handler [handle-get-koulutustyypit-response]
                       :error-handler    [alert-events/http-request-failed]}})))

(events/reg-event-fx-validating
  handle-get-koulutustyypit-response
  (fn-traced [{db :db} [response]]
             (let [lang (get db :lang)
                   options (->> response
                                (map (partial koodisto->option lang))
                                (sort-by :label))]
               {:dispatch [set-haku-lisarajaimet-options "koulutustyypit-filter" options]})))

(events/reg-event-fx-validating
  handle-get-hakukohteet-response
  (fn-traced [{db :db} [haku-oid response]]
             (let [hakukohteet (->> response
                                    (map #(assoc % :is-selected false))
                                    (sort-items-by-name (:lang db)))
                   hakukohde-oids (map :oid hakukohteet)]
               {:db (update-in db haku-haut (partial add-hakukohteet-for-haku haku-oid hakukohteet))
                :dispatch [hakukohderyhma-events/get-hakukohderyhmat-for-hakukohteet hakukohde-oids]})))

(events/reg-event-fx-validating
  clear-selected-haku
  (fn-traced [{db :db} _]
             {:db       (update-in db haku-haut #(map u/deselect-item %))
              :dispatch [hakukohderyhma-events/handle-get-all-hakukohderyhma []]}))

(events/reg-event-fx-validating
  select-haku
  (fn-traced [{db :db} [haku-oid]]
             {:db       (update-in db haku-haut #(map (partial u/select-item-by-oid haku-oid) %))
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
                       :response-handler [handle-get-hakukohteet-response haku-oid]
                       :error-handler    [alert-events/http-request-failed]}})))

(events/reg-event-db-validating
  all-hakukohde-deselected
  (fn-traced [db [_]]
             (update-in db haku-haut deselect-all-hakukohde)))

(events/reg-event-db-validating
  all-hakukohde-in-view-selected
  (fn-traced [db [_]]
             (let [lang (get db :lang)
                   lisarajaimet (->> (get-in db haku-lisarajaimet-filters-path)
                                     (keep u/lisarajain->fn))
                   filter-str (get-in db haku-hakukohteet-filter)
                   in-view? #(and
                               (u/hakukohde-includes-string? % filter-str lang)
                               (apply (u/create-hakukohde-matches-all-lisarajaimet lisarajaimet) [%])
                               (:oikeusHakukohteeseen %))]
               (update-in db haku-haut (partial select-all-hakukohde-in-view in-view?)))))

(events/reg-event-db-validating
  toggle-hakukohde-selection
  (fn-traced [db [hakukohde-oid]]
             (update-in db haku-haut (partial toggle-selection-of-hakukohde hakukohde-oid))))

(events/reg-event-db-validating
  set-hakukohteet-filter
  (fn-traced [db [filter-text]]
             (assoc-in db haku-hakukohteet-filter filter-text)))

(events/reg-event-db-validating
  open-haku-lisarajaimet
  (fn-traced [db]
             (assoc-in db haku-lisarajaimet-visible-path true)))

(events/reg-event-db-validating
  close-haku-lisarajaimet
  (fn-traced [db]
             (assoc-in db haku-lisarajaimet-visible-path false)))

(events/reg-event-db-validating
  set-haku-lisarajaimet-filter
  (fn-traced [db [id value-fn]]
             (->> (partial update-haku-lisarajaimet-filter id :value value-fn)
                  (update-in db haku-lisarajaimet-filters-path))))

(events/reg-event-db-validating
  set-haku-lisarajaimet-options
  (fn-traced [db [id options]]
             (->> (partial update-haku-lisarajaimet-filter id :options (constantly options))
                  (update-in db haku-lisarajaimet-filters-path))))
