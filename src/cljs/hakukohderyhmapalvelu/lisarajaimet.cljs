(ns hakukohderyhmapalvelu.lisarajaimet
  (:require [re-frame.core :as re-frame]
            [hakukohderyhmapalvelu.subs.haku-subs :refer [haku-lisarajaimet-ei-harkinnanvaraiset-koulutuskoodit]]))

(defn- harkinnanvarainen-hakukohde? [hakukohde]
  (let [ammatillinen-koulustyyppi? #{"koulutustyyppi_1" "koulutustyyppi_4"}
        koulutuskoodi-uri (-> hakukohde :koulutuksetKoodiUri first)
        ei-harkinnanvaraiset-koulutuskoodit @(re-frame/subscribe [haku-lisarajaimet-ei-harkinnanvaraiset-koulutuskoodit])
        is-ei-harkinnanvarainen (some
                                  #(str/includes? koulutuskoodi-uri %)
                                  ei-harkinnanvaraiset-koulutuskoodit)]
    (and
      (some ammatillinen-koulustyyppi? (:koulutustyypit hakukohde))
      (or
        (not is-ei-harkinnanvarainen)
        (not (:hasValintakoe hakukohde))))))

(def default-lisarajain-filters
  [{:id      "koulutustyypit-filter"
    :label   :hakukohderyhma/lisarajain-koulutustyypit
    :path    [:koulutustyypit]
    :type    :select
    :pred-fn (fn [val coll] (some #(= val %) coll))
    :value   nil
    :options []}
   {:id      "sora-filter"
    :label   :hakukohderyhma/sora-hakukohteet
    :path    [:sora :tila]
    :type    :boolean
    :pred-fn #(= "aktiivinen" %)
    :value   false}
   {:id      "harkinnanvaraiset-filter"
    :label   :hakukohderyhma/lisarajain-harkinnanvaraiset
    :path    []
    :type    :boolean
    :pred-fn harkinnanvarainen-hakukohde?
    :value   false}
   {:id      "kaksoistutkinto-filter"
    :label   :hakukohderyhma/lisarajain-kaksoistutkinto
    :path    [:toinenAsteOnkoKaksoistutkinto]
    :type    :boolean
    :pred-fn true?
    :value   false}])

(defn lisarajain->fn [{:keys [type value path pred-fn]}]
  (case type
    :boolean (when value (fn [hk] (pred-fn (get-in hk path))))
    :select (when value (fn [hk] (pred-fn (:value value) (get-in hk path))))))
