(ns hakukohderyhmapalvelu.validators.input-time-validator
  (:require [hakukohderyhmapalvelu.dates.date-parser :as d]))

(defn input-time-validator []
  (let [validate-value #(d/iso-time-str->date %)
        validate-type  #(= % "time")]
    (fn validate-input-time [value type]
      (and (validate-type type)
           (validate-value value)))))

