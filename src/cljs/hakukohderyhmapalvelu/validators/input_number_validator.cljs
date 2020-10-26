(ns hakukohderyhmapalvelu.validators.input-number-validator)

(defn input-number-validator
  [{:keys [min max required?]}]
  (let [validate-min (if min
                       #(>= % min)
                       (constantly true))
        validate-max (if max
                       #(<= % max)
                       (constantly true))]
    (fn validate-input-number [value]
      (or (and (not required?) (empty? value))
          (and (validate-min value)
               (validate-max value))))))
