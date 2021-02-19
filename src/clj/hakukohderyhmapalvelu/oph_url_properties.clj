(ns hakukohderyhmapalvelu.oph-url-properties
  (:require [clojure.string :as string]
            [hakukohderyhmapalvelu.config :as c]
            [schema.core :as s]
            [clojure.walk :as walk])
  (:import [fi.vm.sade.properties OphProperties]))

(def ^OphProperties url-properties (atom nil))

(s/defn load-config
  [config :- c/HakukohderyhmaConfig]
  (let [{:keys [virkailija-baseurl
                hakukohderyhmapalvelu-url]} (-> config :urls)
        [virkailija-protocol
         virkailija-host] (string/split virkailija-baseurl #":\/\/")
        oph-properties (doto (OphProperties. (into-array String ["/hakukohderyhmapalvelu-oph.properties"]))
                         (.addDefault "virkailija.protocol" virkailija-protocol)
                         (.addDefault "host.virkailija" virkailija-host)
                         (.addDefault "url-hakukohderyhmapalvelu" hakukohderyhmapalvelu-url))]
    (reset! url-properties oph-properties)))

(s/defn front-json
  [config :- c/HakukohderyhmaConfig]
  (when (nil? @url-properties)
    (load-config config))
  (.frontPropertiesToJson @url-properties))

(s/defn resolve-url
  [key :- s/Keyword
   config :- c/HakukohderyhmaConfig
   & params]
  (when (nil? @url-properties)
    (load-config config))
  (let [params (walk/stringify-keys (or params {}))]
    (.url @url-properties (name key) (to-array params))))
