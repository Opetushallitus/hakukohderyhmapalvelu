(ns hakukohderyhmapalvelu.core
  (:require [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.system :as system]
            [hakukohderyhmapalvelu.timbre-config :as timbre-config]
            [schema.core :as s])
  (:gen-class))

(defn- turn-on-schema-validation []
  (s/set-fn-validation! true))

(defn -main [& _args]
  (timbre-config/configure-logging!)
  (turn-on-schema-validation)
  (component/start-system (system/hakukohderyhmapalvelu-system)))
