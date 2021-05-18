(ns hakukohderyhmapalvelu.events.translation-events
  (:require [clojure.string :as str]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [hakukohderyhmapalvelu.macros.event-macros :as events]))

(def get-remote-translations :translations/get-remote-translations)
(def handle-get-remote-translations :translations/handle-get-remote-translations)

(events/reg-event-db-validating
  handle-get-remote-translations
  (fn-traced [db [locale response]]
             (let [current-translations (:translations db)
                   sync-translation (fn [translations trans-res]
                                      (let [[namespace-key name-key] (-> trans-res
                                                                         (get "key")
                                                                         (str/split #"\."))
                                            value (get trans-res "value")]
                                        (assoc-in translations
                                                  (map keyword [namespace-key name-key locale])
                                                  value)))
                   synced-translations (reduce
                                         sync-translation
                                         current-translations
                                         response)]
               (assoc db :translations synced-translations))))

(events/reg-event-fx-validating
  get-remote-translations
  (fn-traced [_ [locale]]
             {:http {:method           :get
                     :http-request-id  (keyword (str get-remote-translations "-" (name locale)))
                     :path             (str "/lokalisointi/cxf/rest/v1/localisation")
                     :search-params    [[:category "hakukohderyhmapalvelu"]
                                        [:locale (name locale)]]
                     ;TODO response schema?
                     :response-handler [handle-get-remote-translations locale]}}))
