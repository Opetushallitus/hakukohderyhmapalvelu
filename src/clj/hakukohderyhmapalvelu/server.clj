(ns hakukohderyhmapalvelu.server
  (:require [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.authentication.auth-routes :as auth-routes]
            [hakukohderyhmapalvelu.config :as c]
            [hakukohderyhmapalvelu.handler :as h]
            [hakukohderyhmapalvelu.health-check :as health]
            [hakukohderyhmapalvelu.organisaatio.organisaatio-protocol :as organisaatio-service-protocol]
            [hakukohderyhmapalvelu.schemas.class-pred :as p]
            [ring.adapter.jetty :as jetty]
            [schema.core :as s]))

(defrecord HttpServer [config
                       db
                       health-checker
                       organisaatio-service
                       mock-dispatcher
                       auth-routes-source]
  component/Lifecycle

  (start [this]
    (s/validate c/HakukohderyhmaConfig config)
    (s/validate (p/extends-class-pred organisaatio-service-protocol/OrganisaatioServiceProtocol) organisaatio-service)
    (s/validate (p/extends-class-pred health/HealthChecker) health-checker)
    (s/validate s/Any organisaatio-service)
    (s/validate (p/extends-class-pred auth-routes/AuthRoutesSource) auth-routes-source)
    (let [port   (-> config :server :http :port)
          server (jetty/run-jetty (h/make-handler
                                    (cond-> {:config               config
                                             :db                   db
                                             :health-checker       health-checker
                                             :organisaatio-service organisaatio-service
                                             :auth-routes-source   auth-routes-source}
                                            (some? mock-dispatcher)
                                            (assoc :mock-dispatcher mock-dispatcher)))
                                  {:port port :join? false})]
      (assoc this :server server)))

  (stop [this]
    (when-let [server (:server this)]
      (.stop server))
    (assoc this :server nil)))
