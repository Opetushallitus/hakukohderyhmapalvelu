(ns hakukohderyhmapalvelu.siirtotiedosto.siirtotiedosto-service
  (:require [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.siirtotiedosto.siirtotiedosto-protocol :as siirtotiedosto-protocol]
            [schema.core :as s]
            [cheshire.core :as json]
            [clojure.java.io :refer [input-stream]]
            [taoensso.timbre :as log])
  (:import (fi.vm.sade.valinta.dokumenttipalvelu SiirtotiedostoPalvelu)))

(defrecord SiirtotiedostoService [config]
  component/Lifecycle

  (start [this]
    (let [region (get-in config [:siirtotiedosto :aws-region])
          bucket (get-in config [:siirtotiedosto :s3-bucket])
          role-arn (get-in config [:siirtotiedosto :s3-target-role-arn])]
    (s/validate s/Str region)
    (s/validate s/Str bucket)
    (s/validate s/Str role-arn)
    (assoc this :siirtotiedosto-client (new SiirtotiedostoPalvelu
                                        (str region)
                                        (str bucket)
                                        (str role-arn)))))
  (stop [this]
    this)

  siirtotiedosto-protocol/SiirtotiedostoProtocol
  (create-siirtotiedosto [this ryhmat]
    (let [json (json/generate-string ryhmat)
          stream (input-stream (.getBytes json))]
      (try (. (.saveSiirtotiedosto (:siirtotiedosto-client this) "hakukohderyhmapalvelu" "ryhma" "" stream 2) key)
           (catch Exception e
             (log/error (str "Transform file creation failed: " (.getMessage e)))
             ""
             )))))