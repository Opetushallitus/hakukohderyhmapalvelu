(ns hakukohderyhmapalvelu.system
  (:require [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.config :as config]
            [hakukohderyhmapalvelu.db :as db]
            [hakukohderyhmapalvelu.server :as http]))

(def hakukohderyhmapalvelu-system
  (component/system-map
    :config (config/map->Config {})

    :db (component/using
          (db/map->DbPool {})
          [:config])

    :http-server (component/using
                   (http/map->HttpServer {})
                   [:config :db])))
