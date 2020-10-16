(ns hakukohderyhmapalvelu.subs.haun-asetukset-subs
  (:require [hakukohderyhmapalvelu.ohjausparametrit.haun-asetukset-ohjausparametrit-mapping :as m]
            [re-frame.core :as re-frame]))

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
    [(re-frame/subscribe [:state-query [:haut haku-oid]])])
  (fn [[haku]]
    haku))

(re-frame/reg-sub
  :haun-asetukset/haun-asetus
  (fn [[_ haku-oid haun-asetus-key]]
    [(re-frame/subscribe [:state-query
                          [:ohjausparametrit haku-oid (m/haun-asetus-key->ohjausparametri haun-asetus-key)]])])
  (fn [[haun-asetus]]
    haun-asetus))

(re-frame/reg-sub
  :haun-asetukset/haun-asetus-disabled?
  (fn [[_ haku-oid]]
    [(re-frame/subscribe [:state-query
                          [:ohjausparametrit/save-in-progress haku-oid]])])
  (fn [[haku-oid]]
    (some? haku-oid)))
