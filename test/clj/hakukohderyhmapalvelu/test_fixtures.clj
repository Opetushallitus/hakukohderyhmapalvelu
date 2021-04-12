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

(defn add-row! [db hakukohderyhma-oid hakukohde-oid]
  (jdbc/insert! db :hakukohderyhma {:hakukohderyhma_oid hakukohderyhma-oid
                                    :hakukohde_oid      hakukohde-oid}))

(defn has-row? [db hakukohderyhma-oid]
  (-> (jdbc/query db ["SELECT * FROM hakukohderyhma WHERE hakukohderyhma_oid = ?" hakukohderyhma-oid])
      empty?
      not))

(defn- truncate-database [db]
  (jdbc/execute! db ["TRUNCATE hakukohderyhma"]))

(defn with-empty-database [f]
  (truncate-database (:db @test-system))
  (f)
  (truncate-database (:db @test-system)))

(def organisaatio
  {:oid          ""
   :nimi         {:fi ""}
   :parentOid    ""
   :version      0
   :kayttoryhmat []
   :ryhmatyypit  []
   :tyypit       []})

(def organisaatio-1
  (merge organisaatio
        {:oid          "1.2.246.562.28.1"
         :nimi         {:fi "Organisaatio 1"}
         :parentOid    "1.2.246.562.28.01"}))

(def organisaatio-2
  (merge organisaatio
         {:oid          "1.2.246.562.28.2"
          :nimi         {:fi "Organisaatio 2"}
          :parentOid    "1.2.246.562.28.02"}))
