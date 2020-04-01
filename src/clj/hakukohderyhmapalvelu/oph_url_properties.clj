(ns hakukohderyhmapalvelu.oph-url-properties
  (:import (fi.vm.sade.properties OphProperties)))

(def ^OphProperties url-properties (atom nil))

(defn- load-config
  []
  (reset! url-properties
          (OphProperties. (into-array String ["/hakukohderyhmapalvelu-oph.properties"]))))

(defn front-json
  []
  (when (nil? @url-properties)
    (load-config))
  (.frontPropertiesToJson @url-properties))
