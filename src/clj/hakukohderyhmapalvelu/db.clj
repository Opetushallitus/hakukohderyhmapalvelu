(ns hakukohderyhmapalvelu.db
  (:require [clojure.set :as cs]
            [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.config :as c]
            [hikari-cp.core :as hikari]
            [schema.core :as s]))

(defrecord DbPool [config]
  component/Lifecycle

  (start [this]
    (s/validate c/HakukohderyhmaConfig config)
    (let [datasource-options (merge
                               {:auto-commit        true
                                :read-only          false
                                :connection-timeout 30000
                                :validation-timeout 5000
                                :idle-timeout       600000
                                :max-lifetime       1800000
                                :minimum-idle       10
                                :maximum-pool-size  10
                                :pool-name          "db-pool"
                                :adapter            "postgresql"
                                :register-mbeans    false}
                               (-> config
                                   :db
                                   (select-keys [:username
                                                 :password
                                                 :database-name
                                                 :host
                                                 :port])
                                   (cs/rename-keys {:host :server-name
                                                    :port :port-number})))
          datasource         (hikari/make-datasource datasource-options)]
      (assoc this :datasource datasource)))

  (stop [this]
    (when-let [datasource (:datasource this)]
      (hikari/close-datasource datasource))
    (assoc this :datasource nil)))
