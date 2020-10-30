(ns hakukohderyhmapalvelu.validators.input-date-validator
  (:require [hakukohderyhmapalvelu.dates.date-parser :as d]))

(defn input-date-validator [{:keys [required?]}]
  (let [validate-value #(d/iso-date-str->date %)
        validate-type  #(= % "date")]
    (fn validate-input-date [value type]
      (or (and (not required?) (empty? value))
          (and (validate-type type)
               (validate-value value))))))
