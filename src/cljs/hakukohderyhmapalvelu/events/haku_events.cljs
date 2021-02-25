(ns hakukohderyhmapalvelu.events.haku-events
  (:require [hakukohderyhmapalvelu.macros.event-macros :as events]
            [hakukohderyhmapalvelu.api-schemas :as schemas]
            [day8.re-frame.tracing :refer-macros [fn-traced]]))


;; Polut
(def haku-haut [:haku :haut])

;; Tapahtumat
(def get-haut :haku/get-haut)
(def handle-get-haut-response :haku/handle-get-haut-response)

;; Käsittelijät
(events/reg-event-db-validating
  handle-get-haut-response
  (fn-traced [db [response]]
             (assoc-in db haku-haut response)))

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
