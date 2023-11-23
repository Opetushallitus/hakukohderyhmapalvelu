(ns hakukohderyhmapalvelu.events.haun-asetukset-events
  (:require [hakukohderyhmapalvelu.ohjausparametrit.haun-asetukset-ohjausparametrit-mapping :as m]
            [hakukohderyhmapalvelu.macros.event-macros :as events]
            [hakukohderyhmapalvelu.urls :as urls]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [hakukohderyhmapalvelu.i18n.utils :as i18n-utils]
            [clojure.string :as str]))

(events/reg-event-fx-validating
 :haun-asetukset/get-user-rights
 (fn-traced [_ _]
            (let [url (urls/get-url :kayttooikeus-service.me)]
              {:http {:http-request-id  :haun-asetukset/get-user-rights
                      :method           :get
                      :path             url
                      :response-handler [:haun-asetukset/handle-get-user-rights]
                      :body             {}}})))

(events/reg-event-db-validating
 :haun-asetukset/handle-get-user-rights
 (fn-traced [db [response]]
            (->> (:groups response)
                 (filter (fn [right] (str/starts-with? right "APP_HAKUKOHDERYHMAPALVELU")))
                 (assoc db :user-groups))))

(events/reg-event-fx-validating
  :haun-asetukset/get-forms
  (fn-traced [_ _]
    (let [url (urls/get-url :lomake-editori.forms)]
      {:http {:http-request-id  :haun-asetukset/get-forms
              :method           :get
              :path             url
              :response-handler [:haun-asetukset/handle-get-forms]
              :cas              :lomake-editori.login
              :body             {}}})))

(events/reg-event-db-validating
  :haun-asetukset/handle-get-forms
  (fn-traced [db [response]]
    (->> (:forms response)
         (map (fn [f] [(:key f)
                       {:key  (:key f)
                        :name (:name f)}]))
         (into {})
         (assoc db :forms))))

(events/reg-event-fx-validating
  :haun-asetukset/get-haku
  (fn-traced [_ [haku-oid]]
    (let [url (urls/get-url :kouta-internal.haku haku-oid)]
      {:http {:http-request-id  :haun-asetukset/get-haku
              :method           :get
              :path             url
              :response-handler [:haun-asetukset/handle-get-haku haku-oid]
              :cas              :kouta-internal.login
              :body             {}}})))

(events/reg-event-db-validating
  :haun-asetukset/handle-get-haku
  (fn-traced [db [haku-oid response]]
    (let [haku (select-keys
                 response
                 [:nimi
                  :hakulomakeAtaruId
                  :hakulomaketyyppi
                  :kohdejoukkoKoodiUri
                  :hakuajat])]
      (assoc-in db
                [:haun-asetukset :haut haku-oid]
                haku))))

(events/reg-event-fx-validating
  :haun-asetukset/get-ohjausparametrit
  (fn-traced [{db :db} [haku-oid]]
    (let [url (urls/get-url :ohjausparametrit-service.parametri haku-oid)]
      (cond-> {:http {:http-request-id  :haun-asetukset/get-ohjausparametrit
                      :method           :get
                      :path             url
                      :response-handler [:haun-asetukset/handle-get-ohjausparametrit haku-oid]
                      :error-handler    [:haun-asetukset/handle-get-ohjausparametrit-error haku-oid]
                      :body             {}}}
              (some-> db :ohjausparametrit/save-in-progress (get haku-oid))
              (assoc :db (update
                           db
                           :ohjausparametrit/save-in-progress
                           (fnil disj #{})
                           haku-oid))))))

(events/reg-event-db-validating
  :haun-asetukset/handle-get-ohjausparametrit
  (fn-traced [db [haku-oid ohjausparametrit]]
    (let [ohjausparametrit' (if-not (map? ohjausparametrit)
                              {}
                              ohjausparametrit)]
      (-> db
          (assoc-in [:ohjausparametrit haku-oid] ohjausparametrit')
          (assoc :save-status {:changes-saved true
                                :errors []})))))

(events/reg-event-db-validating
  :haun-asetukset/handle-get-ohjausparametrit-error
  (fn-traced [db [haku-oid ohjausparametrit response-code]]
             (let [error-message (cond
                                   (= response-code 403) (i18n-utils/get-translation (:lang db) (:translations db) :yleiset/http-403)
                                   :else (when response-code (str "http " response-code)))]
               (js/console.log "Virhe haettaessa ohjausparametreja: " + ohjausparametrit)
               (-> db
                   (assoc-in [:ohjausparametrit haku-oid] {})
                   (update :save-status (fn [status] (-> status
                                                         (assoc :changes-saved true)
                                                         (update :errors (fn [errors] (conj errors {:message
                                                                                                    (str "Ohjausparametrien hakeminen haulle " haku-oid " epäonnistui"
                                                                                                         (when error-message (str "(" error-message ")")))}))))))))))

(events/reg-event-db-validating
  :haun-asetukset/handle-save-ohjausparametrit-error
  (fn-traced [db [haku-oid body response-code]]
             (let [error-message (cond
                                   (= response-code 403) (i18n-utils/get-translation (:lang db) (:translations db) :yleiset/http-403)
                                   :else (when response-code (str "http " response-code)))]
               (js/console.log "Virhe tallennettaessa ohjausparametreja: " + body)
               (-> db
                   (update :ohjausparametrit/save-in-progress
                           (fnil disj #{}) haku-oid)
                   (update :save-status (fn [status] (-> status
                                                         (assoc :changes-saved false)
                                                         (update :errors (fn [errors] (conj errors {:message
                                                                                                    (str "Ohjausparametrien tallentaminen haulle " haku-oid " epäonnistui "
                                                                                                         (when error-message (str "(" error-message ")")))}))))))))))

(events/reg-event-fx-validating
  :haun-asetukset/set-haun-asetus
  (fn-traced [{db :db} [haku-oid haun-asetus-key haun-asetus-value]]
    (let [ohjausparametri-key   (m/haun-asetus-key->ohjausparametri haun-asetus-key)
          ohjausparametri-value (m/haun-asetus-value->ohjausparametri-value
                                  haun-asetus-value
                                  haun-asetus-key)]
      {:db                 (as-> db db'
                                 (update db' :save-status (fn [status]
                                                            (assoc status :changes-saved false)))
                                 (if (nil? ohjausparametri-value)
                                   (update-in db' [:ohjausparametrit haku-oid]
                                              dissoc ohjausparametri-key)
                                   (assoc-in db'
                                             [:ohjausparametrit haku-oid ohjausparametri-key]
                                             ohjausparametri-value))
                                 (cond-> db'
                                         (not ohjausparametri-value)
                                         (update-in
                                           [:ohjausparametrit haku-oid]
                                           (fn [ohjausparametrit]
                                             (apply (partial dissoc ohjausparametrit)
                                                    (->> haun-asetus-key
                                                         m/clear-keys-on-empty-value
                                                         (map m/haun-asetus-key->ohjausparametri)))))))
       })))

(events/reg-event-fx-validating
  :haun-asetukset/save-ohjausparametrit
  (fn-traced [{db :db} [haku-oid]]
    (let [url  (urls/get-url :ohjausparametrit-service.parametri haku-oid)
          body (-> db :ohjausparametrit (get haku-oid))]
      {:db   (update db
                     :ohjausparametrit/save-in-progress
                     (fnil conj #{})
                     haku-oid)
       :http {:http-request-id  :haun-asetukset/save-ohjausparametrit
              :method           :post
              :path             url
              :response-handler [:haun-asetukset/get-ohjausparametrit haku-oid]
              :error-handler    [:haun-asetukset/handle-save-ohjausparametrit-error haku-oid]
              :cas              :ohjausparametrit-service.login
              :body             body}})))
