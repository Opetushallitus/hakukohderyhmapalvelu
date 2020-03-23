(ns hakukohderyhmapalvelu.config
  (:require [clojure.edn :as edn]
            [config.core :as c]
            [schema.core :as s]))

(s/defschema HakukohderyhmaConfig
  {:environment (s/enum :production :development)
   :server      {:http                                    {:port s/Int}
                 (s/optional-key :shadow-cljs-server-url) s/Str}})

(s/defn ^:always-validate make-config :- HakukohderyhmaConfig []
  (-> (:config c/env)
      (slurp)
      (edn/read-string)))

(defn- make-config-memoized []
  (make-config))

(defn config [& path]
  (let [config (make-config-memoized)]
    (get-in config path)))
