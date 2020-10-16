(ns hakukohderyhmapalvelu.events.haun-asetukset-events
  (:require
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
              :response-handler [:haun-asetukset/handle-get-haku]
              :cas?             false
              :body             {}}})))
