(ns hakukohderyhmapalvelu.core
  (:require [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.config :as c]
            [hakukohderyhmapalvelu.system :as system]
            [hakukohderyhmapalvelu.timbre-config :as timbre-config]
            [schema.core :as s])
  (:gen-class))

(defn- turn-on-schema-validation []
  (s/set-fn-validation! true))

(defn -main [& _args]
  (let [config (c/make-config)]
    (timbre-config/configure-logging! config)
    (turn-on-schema-validation)
    (component/start-system (system/hakukohderyhmapalvelu-system config))))
