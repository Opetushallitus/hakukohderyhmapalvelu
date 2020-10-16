(ns hakukohderyhmapalvelu.events.haun-asetukset-events
  (:require [hakukohderyhmapalvelu.ohjausparametrit.haun-asetukset-ohjausparametrit-mapping :as m]
            [hakukohderyhmapalvelu.macros.event-macros :as events]
            [hakukohderyhmapalvelu.urls :as urls]
            [day8.re-frame.tracing :refer-macros [fn-traced]]))

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
                 [:nimi])]
      (assoc-in db
                [:haut haku-oid]
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
  (fn-traced [{db :db} [haku-oid haun-asetus-key value]]
    (let [ohjausparametrit-key (m/haun-asetus-key->ohjausparametri haun-asetus-key)]
      {:db                 (assoc-in db
                                     [:ohjausparametrit haku-oid ohjausparametrit-key]
                                     value)
       :dispatch-debounced {:id       :haun-asetukset/set-haun-asetus
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
