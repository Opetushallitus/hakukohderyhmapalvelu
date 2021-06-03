(ns hakukohderyhmapalvelu.events.haun-asetukset-events
  (:require [hakukohderyhmapalvelu.ohjausparametrit.haun-asetukset-ohjausparametrit-mapping :as m]
            [hakukohderyhmapalvelu.macros.event-macros :as events]
            [hakukohderyhmapalvelu.urls :as urls]
            [day8.re-frame.tracing :refer-macros [fn-traced]]))

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
      (assoc-in db
                [:ohjausparametrit haku-oid]
                (or ohjausparametrit' {})))))

(events/reg-event-fx-validating
  :haun-asetukset/set-haun-asetus
  (fn-traced [{db :db} [haku-oid haun-asetus-key haun-asetus-value]]
    (let [ohjausparametri-key   (m/haun-asetus-key->ohjausparametri haun-asetus-key)
          ohjausparametri-value (m/haun-asetus-value->ohjausparametri-value
                                  haun-asetus-value
                                  haun-asetus-key)]
      {:db                 (as-> db db'
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
       :dispatch-debounced {:id       :haun-asetukset/save-ohjausparametrit
                            :timeout  1000
                            :dispatch [:haun-asetukset/save-ohjausparametrit haku-oid]}})))

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
              :cas              :ohjausparametrit-service.login
              :body             body}})))


(events/reg-event-fx-validating
  :haun-asetukset/handle-get-user-rights-response
  (fn-traced [{db :db} [haku-oid user-rights? _]]
             {:db (update-in db haku-haut (partial add-user-rights-for-haku haku-oid user-rights?))}))

(events/reg-event-fx-validating
  :haun-asetukset/get-user-rights
  (fn-traced [{db :db} [haku-oid]]
             (let [http-request-id get-user-rights]
               {:db   (update db :requests (fnil conj #{}) http-request-id)
                :http {:method           :get
                       :http-request-id  http-request-id
                       :path             (str "/ohjausparametrit-service/api/v1/rest/parametri/authorize/" haku-oid)
                       :response-handler [:haun-asetukset/handle-get-user-rights-response haku-oid true]
                       :error-handler    [:haun-asetukset/handle-get-user-rights-response haku-oid false]}})))
