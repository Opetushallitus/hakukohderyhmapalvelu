(ns hakukohderyhmapalvelu.cas.mock.mock-dispatcher
  (:require [clojure.core.async :as async]
            [hakukohderyhmapalvelu.cas.mock.mock-authenticating-client-schemas :as schema]
            [hakukohderyhmapalvelu.cas.mock.mock-dispatcher-protocol :as mock-dispatcher-protocol]
            [schema.core :as s]))

(defrecord MockDispatcher [organisaatio-service-chan]
  mock-dispatcher-protocol/MockDispatcherProtocol

  (dispatch-mock [this {:keys [service] :as spec}]
    (s/validate schema/MockCasAuthenticatingClientRequest spec)
    (let [chan (case service
                 :organisaatio-service organisaatio-service-chan)]
      (async/put! chan spec)))

  (reset-mocks [this]
    (take-while some? (repeatedly #(async/poll! organisaatio-service-chan)))))
