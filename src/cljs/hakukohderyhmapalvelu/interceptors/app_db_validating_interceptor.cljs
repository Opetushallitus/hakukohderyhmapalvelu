(ns hakukohderyhmapalvelu.interceptors.app-db-validating-interceptor
  (:require [hakukohderyhmapalvelu.schemas.app-db-schemas :as schema]
            [re-frame.std-interceptors :as interceptors]
            [schema.core :as s]))

(defn- validate-app-db [db]
  (s/validate schema/AppDb db)
  db)

(def validate-interceptor (interceptors/after validate-app-db))
