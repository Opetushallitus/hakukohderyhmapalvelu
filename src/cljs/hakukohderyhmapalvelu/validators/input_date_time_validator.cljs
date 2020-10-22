(ns hakukohderyhmapalvelu.validators.input-date-time-validator
  (:require [hakukohderyhmapalvelu.dates.date-parser :as d]))

(defn input-date-time-validator []
  (let [validate-value #(d/iso-date-time-local-str->date %)
        validate-type  #(= % "datetime-local")]
    (fn validate-input-date-time [value type]
      (and (validate-type type)
           (validate-value value)))))
