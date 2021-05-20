(ns hakukohderyhmapalvelu.events.translation-events
  (:require [camel-snake-kebab.core :as csk]
            [clojure.string :as str]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [hakukohderyhmapalvelu.macros.event-macros :as events]
            [hakukohderyhmapalvelu.urls :as urls]
            [hakukohderyhmapalvelu.api-schemas :as api-schemas]))

(def get-remote-translations :translations/get-remote-translations)
(def handle-get-remote-translations :translations/handle-get-remote-translations)

(events/reg-event-db-validating
  handle-get-remote-translations
  (fn-traced [db [locale response]]
             (let [current-translations (:translations db)
                   sync-translation (fn [translations trans-res]
                                      (let [[namespace-key name-key] (-> trans-res
                                                                         :key
                                                                         (str/split #"\."))
                                            value (:value trans-res)]
                                        (assoc-in translations
                                                  (map csk/->kebab-case-keyword [namespace-key name-key locale])
                                                  value)))
                   synced-translations (reduce
                                         sync-translation
                                         current-translations
                                         response)]
               (assoc db :translations synced-translations))))

(events/reg-event-fx-validating
  get-remote-translations
  (fn-traced [_ [locale]]
             (let [request-id (keyword (str get-remote-translations "-" (name locale)))
                   url (str (urls/get-url :lokalisointi-service.baseurl)
                            "/lokalisointi/cxf/rest/v1/localisation")]
               {:http {:method           :get
                       :http-request-id  request-id
                       :path             url
                       :search-params    [[:category "hakukohderyhmapalvelu"]
                                          [:locale (name locale)]]
                       :response-schema  [api-schemas/LocalizationEntity]
                       :response-handler [handle-get-remote-translations locale]}})))
