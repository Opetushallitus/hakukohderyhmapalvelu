(ns hakukohderyhmapalvelu.i18n.utils
  (:require [clojure.string :as str]
            [camel-snake-kebab.core :as csk]))

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
       (keep #(get m %))
       first))

(defn- item->option [lang label-field value-field disabled? item]
  (when (some? item)
    (let [localized (get item label-field)
          value (get item value-field)
          is-selected (:is-selected item)
          is-disabled (disabled? item)]
      (cond-> {:label       (get-with-fallback localized lang)
               :value       value
               :is-selected is-selected}
              (boolean? is-disabled) (assoc :is-disabled is-disabled)))))

(defn create-item->option-transformer
  ([lang label value disabled?]
   (partial
     item->option
     lang
     label
     value
     disabled?))
  ([lang label value]
   (create-item->option-transformer lang label value
     (constantly nil))))

(defn sort-items-by-name [lang organizations]
  (sort-by
    #(get-with-fallback (:nimi %) lang)
    organizations))

(defn koodisto->option [lang {koodi-uri :koodiUri metadata :metadata}]
  (let [grouped-metadata (group-by #(keyword (str/lower-case (:kieli %))) metadata)
        label (-> (get-with-fallback grouped-metadata lang)
                  first
                  :nimi)]
    {:value koodi-uri
     :label label}))

(defn get-translation [lang translations tx-key]
  (let [[namespace-key name-key] (->> ((juxt namespace name) tx-key)
                                      (map #(-> % csk/->kebab-case keyword)))]
    (-> translations namespace-key name-key lang)))
