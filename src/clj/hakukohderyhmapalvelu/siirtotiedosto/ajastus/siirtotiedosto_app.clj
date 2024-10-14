(ns hakukohderyhmapalvelu.siirtotiedosto.ajastus.siirtotiedosto-app
  (:require [hakukohderyhmapalvelu.siirtotiedosto.siirtotiedosto-protocol :as siirtotiedosto]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.config :as c]
            [hakukohderyhmapalvelu.system :as system]
            [taoensso.timbre :as timbre])
  (:gen-class))


(defn -main [& _]
  (let [config (c/make-config)]
    (timbre/set-level! :info)
    (log/info "Ovara-hakukohderyhmapalvelu up, creating client and service")
      (try
        (let [system (component/start-system (system/ovara-hakukohderyhmapalvelu-system config))
              s-service (:siirtotiedosto-service system)
              result (siirtotiedosto/create-next-siirtotiedostot s-service)]
          (log/info "Ready!" result)
          (System/exit 0))
        (catch Throwable t
          (log/error "Siirtotiedosto operation failed unexpectedly:" t)
          (System/exit 1)))))