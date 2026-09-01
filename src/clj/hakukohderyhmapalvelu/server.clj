(ns hakukohderyhmapalvelu.server
  (:require [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.authentication.auth-routes :as auth-routes]
            [hakukohderyhmapalvelu.config :as c]
            [hakukohderyhmapalvelu.handler :as h]
            [hakukohderyhmapalvelu.health-check :as health]
            [hakukohderyhmapalvelu.schemas.class-pred :as p]
            [ring.adapter.jetty :as jetty]
            [schema.core :as s])
  (:import [org.eclipse.jetty.ee9.nested ContextHandler ErrorHandler]))

;; ring 1.15 ajaa Jetty 12:n ee9-yhteensopivuuskerroksella. ee9.nested.ErrorHandler
;; säilyttää handleErrorPage-metodin (poistettu Jetty 12:n core-ErrorHandlerista).
(defonce jetty-error-handler
  (doto (proxy [ErrorHandler] []
          (handleErrorPage [_ writer _ _]
            (.write writer "Internal server error\n")))
    (.setShowStacks false)))

(defn- attach-error-handler! [^org.eclipse.jetty.server.Server server]
  ;; ee9-kontekstia ei saa suoraan configuratorin Server-oliosta -> haetaan beaneista
  (doseq [ctx (.getContainedBeans server ContextHandler)]
    (.setErrorHandler ^ContextHandler ctx jetty-error-handler)))

(defrecord HttpServer [config
                       db
                       health-checker
                       hakukohderyhma-service
                       siirtotiedosto-service
                       mock-dispatcher
                       auth-routes-source]
  component/Lifecycle

  (start [this]
    (s/validate c/HakukohderyhmaConfig config)
    (s/validate (p/extends-class-pred health/HealthChecker) health-checker)
    (s/validate (p/extends-class-pred auth-routes/AuthRoutesSource) auth-routes-source)
    (let [port   (-> config :server :http :port)
          server (jetty/run-jetty (h/make-handler
                                    (cond-> {:config                 config
                                             :db                     db
                                             :health-checker         health-checker
                                             :hakukohderyhma-service hakukohderyhma-service
                                             :siirtotiedosto-service siirtotiedosto-service
                                             :auth-routes-source     auth-routes-source}
                                            (some? mock-dispatcher)
                                            (assoc :mock-dispatcher mock-dispatcher)))
                                  {:port         port
                                   :join?        false
                                   :configurator attach-error-handler!})]
      (assoc this :server server)))

  (stop [this]
    (when-let [server (:server this)]
      (.stop server))
    (assoc this :server nil)))
