(ns hakukohderyhmapalvelu.siirtotiedosto.ajastus.siirtotiedosto-queries
  (:require [clojure.set :as set]
            [hugsql.core :as hugsql]
            [taoensso.timbre :as log]))


(hugsql/def-db-fns "hakukohderyhmapalvelu/siirtotiedosto/ajastus/siirtotiedosto_queries.sql")

(declare insert-new-siirtotiedosto-operation!<)
(declare upsert-siirtotiedosto-data!)
(declare latest-siirtotiedosto-data)

(defn get-latest-successful-data [db]
  (log/info "Fetching latest siirtotiedosto data")
  (let [db-result (latest-siirtotiedosto-data db)
        processed-result (set/rename-keys db-result {:execution_uuid :execution-uuid
                                                     :window_start   :window-start
                                                     :window_end     :window-end
                                                     :run_start      :run-start
                                                     :run_end        :run-end
                                                     :error_message  :error-message})]
    (log/info "Fetched siirtotiedosto data:" processed-result)
    processed-result))

(defn insert-new-siirtotiedosto-operation [db data]
  (log/info "Persisting new siirtotiedosto operation" data)
  (let [db-result (insert-new-siirtotiedosto-operation!< db {:execution_uuid (:execution-uuid data)
                                                             :window_start   (:window-start data)})
        processed-result (set/rename-keys db-result {:execution_uuid :execution-uuid
                                                     :window_start   :window-start
                                                     :window_end     :window-end
                                                     :run_start      :run-start
                                                     :run_end        :run-end
                                                     :error_message  :error-message})]
    (log/info "Persisted siirtotiedosto data, result" processed-result)
    processed-result)

  )

(defn update-siirtotiedosto-operation [db data]
  (log/info "Persisting siirtotiedosto data" data)
  (upsert-siirtotiedosto-data! db (set/rename-keys data {:run-start      :run_start
                                                         :run-end        :run_end
                                                         :error-message  :error_message})))