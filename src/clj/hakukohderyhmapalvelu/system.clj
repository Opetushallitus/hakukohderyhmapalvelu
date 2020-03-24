(ns hakukohderyhmapalvelu.system
  (:require [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.server :as http]))

(def hakukohderyhmapalvelu-system
  (component/system-map
    :http-server (http/new-http-server)))
