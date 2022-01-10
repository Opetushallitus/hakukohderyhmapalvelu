(ns hakukohderyhmapalvelu.subs.haun-asetukset-subs
  (:require [hakukohderyhmapalvelu.ohjausparametrit.haun-asetukset-ohjausparametrit-mapping :as m]
            [cljs-time.format :as f]
            [clojure.string]
            [re-frame.core :as re-frame]))

(defn- iso->finnish
  [s]
  (let [in-fmt  (f/formatter "yyyy-MM-dd'T'HH:mm:ss")
        out-fmt (f/formatter "dd.MM.yyyy 'klo' HH.mm.ss")]
    (f/unparse-local out-fmt (f/parse-local in-fmt s))))

(re-frame/reg-sub
  :haun-asetukset/selected-haku-oid
  (fn []
    [(re-frame/subscribe [:panel/active-panel])])
  (fn [[active-panel]]
    (-> active-panel
        :parameters
        :query
        :haku-oid)))

(re-frame/reg-sub
  :haun-asetukset/haku
  (fn [[_ haku-oid]]
    [(re-frame/subscribe [:state-query [:haun-asetukset :haut haku-oid]])])
  (fn [[haku]]
    haku))

(re-frame/reg-sub
  :haun-asetukset/haun-asetus
  (fn [[_ haku-oid haun-asetus-key]]
    [(re-frame/subscribe [:state-query
                          [:ohjausparametrit haku-oid (m/haun-asetus-key->ohjausparametri haun-asetus-key)]])])
  (fn [[ohjausparametri-value] [_ _ haun-asetus-key]]
    (m/ohjausparametri-value->haun-asetus-value
      ohjausparametri-value
      haun-asetus-key)))

(re-frame/reg-sub
  :haun-asetukset/haun-asetukset-disabled?
  (fn [[_ haku-oid]]
    [(re-frame/subscribe [:state-query
                          [:ohjausparametrit/save-in-progress haku-oid]])])
  (fn [[haku-oid]]
    (some? haku-oid)))

(re-frame/reg-sub
  :haun-asetukset/save-status
  (fn [[_ _]]
    [(re-frame/subscribe [:state-query
                          [:save-status]])])
  (fn [[state]]
    (js/console.log (str "SAVE-STATUS " state))
    state))

(re-frame/reg-sub
  :haun-asetukset/form
  (fn [[_ haku-oid]]
    [(re-frame/subscribe [:haun-asetukset/haku haku-oid])
     (re-frame/subscribe [:state-query [:forms]])])
  (fn [[haku forms]]
    (get forms (:hakulomakeAtaruId haku))))

(re-frame/reg-sub
  :haun-asetukset/kk?
  (fn [[_ haku-oid]]
    [(re-frame/subscribe [:haun-asetukset/haku haku-oid])])
  (fn [[haku]]
    (and (string? (:kohdejoukkoKoodiUri haku))
         (clojure.string/starts-with?
          (:kohdejoukkoKoodiUri haku)
          "haunkohdejoukko_12#"))))

(re-frame/reg-sub
  :haun-asetukset/toinen_aste?
  (fn [[_ haku-oid]]
    [(re-frame/subscribe [:haun-asetukset/haku haku-oid])])
  (fn [[haku]]
    (and (string? (:kohdejoukkoKoodiUri haku))
         (clojure.string/starts-with?
           (:kohdejoukkoKoodiUri haku)
           "haunkohdejoukko_11#")))) ;fixme tarkista

(re-frame/reg-sub
  :haun-asetukset/hakuajat
  (fn [[_ haku-oid]]
    [(re-frame/subscribe [:haun-asetukset/haku haku-oid])])
  (fn [[haku]]
    (mapv (fn [hakuaika]
            (merge {:alkaa (iso->finnish (:alkaa hakuaika))}
                   (when (:paattyy hakuaika)
                     {:paattyy (iso->finnish (:paattyy hakuaika))})))
          (:hakuajat haku))))
