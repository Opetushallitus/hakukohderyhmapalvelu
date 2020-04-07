(ns hakukohderyhmapalvelu.server
  (:require [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.handler :as h]
            [ring.adapter.jetty :as jetty]
            [schema.core :as s]
            [hakukohderyhmapalvelu.config :as c]
            [hakukohderyhmapalvelu.organisaatio.organisaatio-protocol :as organisaatio-service-protocol]
            [hakukohderyhmapalvelu.schemas.class-pred :as p]
            [hakukohderyhmapalvelu.health-check :as health]))

(defrecord HttpServer [config
                       health-checker
                       organisaatio-service
                       mock-dispatcher]
  component/Lifecycle

  (start [this]
    (s/validate c/HakukohderyhmaConfig config)
    (s/validate (p/extends-class-pred organisaatio-service-protocol/OrganisaatioServiceProtocol) organisaatio-service)
    (s/validate (p/extends-class-pred health/HealthChecker) health-checker)
    (s/validate s/Any organisaatio-service)
    (let [port   (-> config :server :http :port)
          server (jetty/run-jetty (h/make-handler
                                    (cond-> {:config               config
                                             :health-checker       health-checker
                                             :organisaatio-service organisaatio-service}
                                            (some? mock-dispatcher)
                                            (assoc :mock-dispatcher mock-dispatcher)))
                                  {:port port :join? false})]
      (assoc this :server server)))

  (stop [this]
    (when-let [server (:server this)]
      (.stop server))
    (assoc this :server nil)))
