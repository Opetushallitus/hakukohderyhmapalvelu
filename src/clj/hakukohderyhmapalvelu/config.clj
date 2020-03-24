(ns hakukohderyhmapalvelu.config
  (:require [clojure.edn :as edn]
            [config.core :as c]
            [schema.core :as s]
            [com.stuartsierra.component :as component]))

(s/defschema HakukohderyhmaConfig
  {:environment (s/enum :production :development)
   :server      {:http {:port s/Int}}
   :log         {:base-path s/Str}
   :db          {:username      s/Str
                 :password      s/Str
                 :database-name s/Str
                 :host          s/Str
                 :port          s/Int}})

(s/defn ^:always-validate make-config :- HakukohderyhmaConfig []
  (-> (:config c/env)
      (slurp)
      (edn/read-string)))

(defrecord Config []
  component/Lifecycle

  (start [this]
    (assoc this :config (make-config)))

  (stop [this]
    (assoc this :config nil)))
