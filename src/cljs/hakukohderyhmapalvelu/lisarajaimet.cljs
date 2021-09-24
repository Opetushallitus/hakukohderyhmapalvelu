(ns hakukohderyhmapalvelu.lisarajaimet)

(defn- harkinnanvarainen-hakukohde? [hakukohde]
  (let [ammatillinen-koulustyyppi? #{"koulutustyyppi_1"}]
    (and
      (= ammatillinen-koulustyyppi? (:koulutustyyppikoodi hakukohde))
      (:onkoHarkinnanvarainenKoulutus hakukohde)
      (not (:hasValintakoe hakukohde)))))

(def default-lisarajain-filters
  [{:id      "koulutustyypit-filter"
    :label   :hakukohderyhma/lisarajain-koulutustyypit
    :path    [:koulutustyyppikoodi]
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
