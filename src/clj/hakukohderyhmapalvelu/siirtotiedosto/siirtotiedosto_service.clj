(ns hakukohderyhmapalvelu.siirtotiedosto.siirtotiedosto-service
  (:require [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.hakukohderyhma.db.hakukohderyhma-queries :as hakukohderyhma-queries]
            [hakukohderyhmapalvelu.siirtotiedosto.ajastus.siirtotiedosto-queries :as siirtotiedosto-queries]
            [hakukohderyhmapalvelu.siirtotiedosto.siirtotiedosto-protocol :as siirtotiedosto-protocol]
            [schema.core :as s]
            [cheshire.core :as json]
            [clojure.java.io :refer [input-stream]]
            [taoensso.timbre :as log])
  (:import (fi.vm.sade.valinta.dokumenttipalvelu SiirtotiedostoPalvelu)
           (java.util UUID)))

(defn- assoc-if-exists
  ([dest source key]
   (assoc-if-exists dest source key identity))
  ([dest source key modifier]
   (let [val (get source key)]
     (if (nil? val)
       dest
       (assoc dest key (modifier val))))))

(defn- get-hakukohderyhma-oids-by-timerange
  [db start-datetime end-datetime]
  (hakukohderyhma-queries/find-new-or-changed-hakukohderyhma-oids-by-timerange db start-datetime end-datetime))

(defn resolve-last-modified
  [raw]
  (let [datetimes-sorted (sort (filter #(not (nil? %)) [(:ryhma-created-at raw)
                                                        (:setting-created-at raw)
                                                        (:setting-updated-at raw)]))
        latest (last datetimes-sorted)]
    (if latest
      (str latest)
      "")))

(defn- list-hakukohteet-and-settings
  [db hakukohderyhma-oids]
  (let [hakukohderyhmat-raw (hakukohderyhma-queries/list-hakukohteet-and-settings db hakukohderyhma-oids)
        create-object (fn [raw] (-> {}
                                    (assoc :hakukohderyhma-oid (:hakukohderyhma-oid raw))
                                    (assoc :hakukohde-oids (vec (:hakukohde-oids raw)))
                                    (assoc :last-modified (resolve-last-modified raw))
                                    (assoc :settings (-> {}
                                                         (assoc-if-exists raw :rajaava)
                                                         (assoc-if-exists raw :max-hakukohteet)
                                                         (assoc-if-exists raw :yo-amm-autom-hakukelpoisuus)
                                                         (assoc-if-exists
                                                           raw
                                                           :jos-ylioppilastutkinto-ei-muita-pohjakoulutusliitepyyntoja)
                                                         (assoc-if-exists raw :priorisoiva)
                                                         (assoc-if-exists raw :prioriteettijarjestys vec)))))
        ryhma-objects (map create-object hakukohderyhmat-raw)]
    ryhma-objects))

(defn update-siirtotiedosto-data [base-data operation-results]
  (if (:success operation-results)
    (merge base-data {:success true :info (:info operation-results)})
    base-data))

(defn create-new-siirtotiedosto-data [last-siirtotiedosto-data execution-id]
  (log/info "Creating new data, last data" last-siirtotiedosto-data ", exec id" execution-id)
  (if (:success last-siirtotiedosto-data)
    {:window-start (:window-end last-siirtotiedosto-data)
     :execution-uuid execution-id
     :info {}
     :success nil
     :error-message nil}
    (throw (RuntimeException. "Edellistä onnistunutta operaatiota ei löytynyt."))))

(defrecord SiirtotiedostoService [config db]
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
  (create-siirtotiedosto [this execution-id execution-sub-id ryhmat]
    (log/info execution-id "Saving siirtotiedosto")
    (let [json (json/generate-string ryhmat)
          stream (input-stream (.getBytes json))]
      (try (. (.saveSiirtotiedosto
                (:siirtotiedosto-client this)
                "hakukohderyhmapalvelu"
                "ryhma"
                ""
                execution-id
                execution-sub-id
                stream
                2) key)
           (catch Exception e
             (log/error (str "Siirtotiedosto creation failed: " (.getMessage e)))
             (throw e)))))

  (create-siirtotiedostot-by-params [this _ params]
    (log/info "Create siirtotiedostot by params" params)
    (let [execution-id (str (UUID/randomUUID))
          max-kohderyhmacount-in-file (-> config
                                          :siirtotiedosto
                                          :max-kohderyhmacount-in-file)
          {:keys [window-start window-end]} params
          all-oids (get-hakukohderyhma-oids-by-timerange db window-start window-end)
          partitions (partition-all max-kohderyhmacount-in-file all-oids)
          create-siirtotiedosto-fn (fn [oid-chunk sub-exec-id] (->> oid-chunk
                                                                    (list-hakukohteet-and-settings db)
                                                                    (siirtotiedosto-protocol/create-siirtotiedosto
                                                                      this execution-id sub-exec-id)))
          partition-count (count partitions)
          id-range (if (> partition-count 1) (range 1 (+ 1 partition-count)) [1])]
      {:keys    (map #(create-siirtotiedosto-fn %1 %2) partitions id-range)
       :count   (count all-oids)
       :info    {:hakukohderyhmat (count all-oids)}
       :success true}))

  (create-next-siirtotiedostot [this]
    (let [
          execution-id            (str (UUID/randomUUID))
          previous-success        (siirtotiedosto-queries/get-latest-successful-data db)
          new-siirtotiedosto-data (siirtotiedosto-queries/insert-new-siirtotiedosto-operation db (create-new-siirtotiedosto-data previous-success execution-id))]
      (log/info execution-id "Launching siirtotiedosto operation. Previous data: " previous-success ", new data " new-siirtotiedosto-data)
      (try
        (let [siirtotiedosto-data-after-operation (->> (siirtotiedosto-protocol/create-siirtotiedostot-by-params this nil new-siirtotiedosto-data)
                                                       (update-siirtotiedosto-data new-siirtotiedosto-data))]
          (if (:success siirtotiedosto-data-after-operation)
            (log/info execution-id "Created siirtotiedostot" siirtotiedosto-data-after-operation)
            (log/error execution-id "Siirtotiedosto operation failed:" siirtotiedosto-data-after-operation))
          (siirtotiedosto-queries/update-siirtotiedosto-operation db siirtotiedosto-data-after-operation)
          siirtotiedosto-data-after-operation)
        (catch Exception e
          (let [failed-data (-> new-siirtotiedosto-data
                                (assoc :success false)
                                (assoc :error-message (.getMessage e)))]
            (log/error (str execution-id "Siirtotiedosto operation failed: ") e)
            (siirtotiedosto-queries/update-siirtotiedosto-operation db failed-data)
            failed-data)))))
  )