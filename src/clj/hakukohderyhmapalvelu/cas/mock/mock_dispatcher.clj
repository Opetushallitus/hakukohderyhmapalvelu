(ns hakukohderyhmapalvelu.cas.mock.mock-dispatcher
  (:require [clojure.core.async :as async]
            [hakukohderyhmapalvelu.cas.mock.mock-cas-client-schemas :as schema]
            [schema.core :as s]))

(defprotocol MockDispatcherProtocol
  (dispatch-mock [this spec])

  (reset-mocks [this]))

(defrecord MockDispatcher [organisaatio-service-chan]
  MockDispatcherProtocol

  (dispatch-mock [this {:keys [service] :as spec}]
    (s/validate schema/MockCasClientRequest spec)
    (let [chan (case service
                 :organisaatio-service organisaatio-service-chan)]
      (async/put! chan spec)))

  (reset-mocks [this]
    (take-while some? (repeatedly #(async/poll! organisaatio-service-chan)))))
