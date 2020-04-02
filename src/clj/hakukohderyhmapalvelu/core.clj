(ns hakukohderyhmapalvelu.core
  (:require [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.system :as system]
            [hakukohderyhmapalvelu.timbre-config :as timbre-config])
  (:gen-class))

(defn -main [& _args]
  (timbre-config/configure-logging!)
  (component/start-system (system/hakukohderyhmapalvelu-system)))
