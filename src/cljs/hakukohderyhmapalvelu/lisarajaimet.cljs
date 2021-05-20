(ns hakukohderyhmapalvelu.lisarajaimet)

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
   {:id      "kaksoistutkinto-filter"
    :label   :hakukohderyhma/lisarajain-kaksoistutkinto
    :path    [:toinenAsteOnkoKaksoistutkinto]
    :type    :boolean
    :pred-fn true?
    :value   false}])
