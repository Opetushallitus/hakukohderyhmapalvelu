(ns hakukohderyhmapalvelu.test-fixtures
  (:require [hakukohderyhmapalvelu.cas.mock.mock-dispatcher-protocol :as mock-dispatcher-protocol]
            [hakukohderyhmapalvelu.config :as c]
            [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.system :as system]
            [clojure.java.jdbc :as jdbc]))


(def test-system (atom nil))

(defn- start-system [& _args]
  (let [config (c/make-config)
        sys (-> (system/hakukohderyhmapalvelu-system config)
                (dissoc :auth-routes-source
                        :health-checker
                        :http-server
                        :migrations))]
    (reset! test-system (component/start-system sys))))

(defn- stop-system []
  (when @test-system
    (component/stop-system @test-system)
    (reset! test-system nil)))

(defn dispatch-mock [spec]
  (let [mock-dispatcher (:mock-dispatcher @test-system)]
    (mock-dispatcher-protocol/dispatch-mock mock-dispatcher spec)))

(defn with-mock-system [f]
  (start-system)
  (f)
  (stop-system))

(defn- truncate-database [db]
  (jdbc/execute! db ["TRUNCATE hakukohderyhma"]))

(defn with-empty-database [f]
  (truncate-database (:db @test-system))
  (f)
  (truncate-database (:db @test-system)))
