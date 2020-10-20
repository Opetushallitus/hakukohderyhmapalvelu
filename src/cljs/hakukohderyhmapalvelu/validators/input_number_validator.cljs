(ns hakukohderyhmapalvelu.validators.input-number-validator)

(defn input-number-validator
  ([min]
   (input-number-validator min nil))
  ([min max]
   (let [validate-min (if min
                        #(>= % min)
                        (constantly true))
         validate-max (if max
                        #(<= % max)
                        (constantly true))]
     (fn validate-input-number [value]
       (and (validate-min value)
            (validate-max value))))))
