(ns hakukohderyhmapalvelu.i18n.utils)

;; Oletusjärjestys, jolla haetaan kielistettyä arvoa eri kielillä
(def ^:private fi-order [:fi :sv :en])
(def ^:private sv-order [:sv :fi :en])
(def ^:private en-order [:en :fi :sv])

(defn- order-for-lang [lang]
  (case lang
    :fi fi-order
    :sv sv-order
    :en en-order
    :default fi-order))

(defn get-with-fallback [m lang]
  (->> (order-for-lang lang)
       (map #(get m %))
       (remove nil?)
       first))

(defn- item->option [lang label-field value-field item]
  (when (some? item)
    (let [localized (get item label-field)
          value (get item value-field)
          is-selected (get item :is-selected)]
      {:label       (get-with-fallback localized lang)
       :value       value
       :is-selected is-selected})))

(defn create-item->option-transformer [lang label-field value-field]
  (partial
    item->option
    lang
    label-field
    value-field))
