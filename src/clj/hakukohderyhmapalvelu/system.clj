(ns hakukohderyhmapalvelu.system
  (:require [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.config :as config]
            [hakukohderyhmapalvelu.server :as http]))

(def hakukohderyhmapalvelu-system
  (component/system-map
    :config (config/map->Config {})

    :http-server (component/using
                   (http/map->HttpServer {})
                   [:config])))
