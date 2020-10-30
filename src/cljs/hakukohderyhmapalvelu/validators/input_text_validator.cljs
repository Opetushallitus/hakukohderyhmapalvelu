(ns hakukohderyhmapalvelu.validators.input-text-validator
  (:require [clojure.string :as string]))

(defn input-text-validator []
  (fn validate-input-text [value]
    (-> value
        string/blank?
        not)))
