(ns hakukohderyhmapalvelu.lisarajaimet)

(defn- harkinnanvarainen-hakukohde? [hakukohde]
  (let [ammatillinen-koulustyyppi? "koulutustyyppi_26"]
    (and
      (= ammatillinen-koulustyyppi? (:koulutustyyppikoodi hakukohde))
      (or
        (boolean (:salliikoHakukohdeHarkinnanvaraisuudenKysymisen hakukohde))
        (not (boolean (:hasPaasyJaSoveltuvuuskoeOma hakukohde)))))))

(def default-lisarajain-filters
  [{:id      "koulutustyypit-filter"
    :label   :hakukohderyhma/lisarajain-koulutustyypit
    :path    [:koulutustyyppikoodi]
    :type    :select
    :pred-fn (fn [option value] (= value option))
    :value   nil
    :options []}
   {:id      "sora-filter"
    :label   :hakukohderyhma/sora-hakukohteet
    :path    [:sora :tila]
    :type    :boolean
    :pred-fn #(= "julkaistu" %)
    :value   false}
   {:id      "urheilu-filter"
    :label   :hakukohderyhma/lisarajain-urheilu
    :path    [:jarjestaaUrheilijanAmmKoulutusta]
    :type    :boolean
    :pred-fn true?
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
