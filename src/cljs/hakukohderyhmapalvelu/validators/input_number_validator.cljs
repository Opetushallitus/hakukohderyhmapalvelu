(ns hakukohderyhmapalvelu.validators.input-number-validator)

(defn- parse-int [value]
  (let [i (.parseInt js/Number value 10)]
    (when-not (.isNaN js/Number i)
      i)))

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
          (when-let [i (parse-int value)]
            (and (validate-min i)
                 (validate-max i)))))))
