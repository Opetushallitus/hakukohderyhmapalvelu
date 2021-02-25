(ns hakukohderyhmapalvelu.subs.haku-subs
  (:require [re-frame.core :as re-frame]
            [hakukohderyhmapalvelu.events.haku-events :as haku-events]))

;; Oletusjärjestys, jolla haetaan kielistettyä arvoa eri kielillä
(def fi-order [:fi :sv :en])
(def sv-order [:sv :fi :en])
(def en-order [:en :fi :sv])

(defn- get-with-fallback [m order]
  (->> (map #(get m %) order)
       (remove nil?)
       first))

(defn- haku->option [order haku]
  (let [nimi (:nimi haku)
        oid (:oid haku)]
    {:label (get-with-fallback nimi order)
     :value oid}))

;; Tilaukset
(def haku-haut :haku/haut)
(def haku-haut-as-options :haku/haut-as-options)
(def haku-is-loading :haku/is-loading)

(re-frame/reg-sub
  haku-haut
  (fn [db _]
    (get-in db haku-events/haku-haut)))

(re-frame/reg-sub
  haku-haut-as-options
  (fn []
    [(re-frame/subscribe [:lang])
     (re-frame/subscribe [haku-haut])])
  (fn [[lang haut] _]
    (let [order (case lang
                  :fi fi-order
                  :sv sv-order
                  :en en-order
                  :default fi-order)]
      (map (partial haku->option order) haut))))

(re-frame/reg-sub
  haku-is-loading
  (fn [db _]
    (let [requests (:requests db)]
      (contains? requests haku-events/get-haut))))
