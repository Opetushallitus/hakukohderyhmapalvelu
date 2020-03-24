(ns hakukohderyhmapalvelu.db
  (:require [com.stuartsierra.component :as component]
            [hikari-cp.core :as hikari]))

(defrecord DbPool [config]
  component/Lifecycle

  (start [this]
    (let [datasource-options {:auto-commit        true
                              :read-only          false
                              :connection-timeout 30000
                              :validation-timeout 5000
                              :idle-timeout       600000
                              :max-lifetime       1800000
                              :minimum-idle       10
                              :maximum-pool-size  10
                              :pool-name          "db-pool"
                              :adapter            "postgresql"
                              :username           (-> config :config :db :username)
                              :password           (-> config :config :db :password)
                              :database-name      (-> config :config :db :database-name)
                              :server-name        (-> config :config :db :host)
                              :port-number        (-> config :config :db :port)
                              :register-mbeans    false}
          datasource         (hikari/make-datasource datasource-options)]
      (assoc this :datasource datasource)))

  (stop [this]
    (when-let [datasource (:datasource this)]
      (hikari/close-datasource datasource))
    (assoc this :datasource nil)))
