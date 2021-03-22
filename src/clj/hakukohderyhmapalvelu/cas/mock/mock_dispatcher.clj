(ns hakukohderyhmapalvelu.cas.mock.mock-dispatcher
  (:require [clojure.core.async :as async]
            [hakukohderyhmapalvelu.cas.mock.mock-authenticating-client-schemas :as schema]
            [hakukohderyhmapalvelu.cas.mock.mock-dispatcher-protocol :as mock-dispatcher-protocol]
            [schema.core :as s]))

(defrecord MockDispatcher [organisaatio-service-request-map kouta-service-request-map config]
  mock-dispatcher-protocol/MockDispatcherProtocol

  (dispatch-mock [this {:keys [service method path request] :as spec}]
    (s/validate schema/MockCasAuthenticatingClientRequest spec)
    (let [request-map (case service
                        :organisaatio-service organisaatio-service-request-map
                        :kouta-service kouta-service-request-map)
          virkailija-baseurl (get-in config [:urls :virkailija-baseurl])
          full-path (str virkailija-baseurl path)
          assoc-path (case method
                       :post [method full-path (hash request)]
                       :get [method full-path])]
      (->> (assoc-in @request-map assoc-path spec)
           (reset! request-map))))

  (reset-mocks [this]
    (reset! organisaatio-service-request-map {})
    (reset! kouta-service-request-map {})))
