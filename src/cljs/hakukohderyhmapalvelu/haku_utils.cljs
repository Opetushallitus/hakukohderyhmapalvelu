(ns hakukohderyhmapalvelu.haku-utils
  (:require [hakukohderyhmapalvelu.i18n.utils :as i18n-utils]
            [clojure.string :as str]))

(defn- includes-string? [m string lang]
  (-> (i18n-utils/get-with-fallback m lang)
      str/lower-case
      (str/includes? string)))

(defn hakukohde-includes-string? [hakukohde string lang]
  (let [search-paths [[:tarjoaja :nimi] [:nimi]]
        lower-str (str/lower-case string)]
    (some #(includes-string? (get-in hakukohde %) lower-str lang) search-paths)))

(defn select-item-by-oid [oid item]
  (assoc item :is-selected (= (:oid item) oid)))

(defn deselect-item [item]
  (assoc item :is-selected false))

(defn- change-filtered-item [filtered? item change-fn]
  (if (filtered? item)
    (change-fn item)
    item))

(defn select-filtered-item [filtered? item]
  (change-filtered-item filtered? item #(assoc % :is-selected true)))

(defn deselect-unfiltered-item [unfiltered? item]
  (change-filtered-item unfiltered? item #(assoc % :is-selected false)))

(defn toggle-filtered-item-selection [filtered? item]
  (change-filtered-item filtered? item #(update % :is-selected not)))

(defn create-hakukohde-matches-all-lisarajaimet [lisarajaimet]
  (fn [hakukohde]
    (every? #(apply % [hakukohde]) lisarajaimet)))

(defn lisarajain->fn [{:keys [type value path pred-fn]}]
  (case type
    :boolean (when value (fn [hk] (pred-fn (get-in hk path))))
    :select (when value (fn [hk] (pred-fn (:value value) (get-in hk path))))))
