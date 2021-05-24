(ns hakukohderyhmapalvelu.db
  (:require [hakukohderyhmapalvelu.routes :as routes]
            [hakukohderyhmapalvelu.i18n.translations :as translations]))

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

(def default-db
  {:active-panel                      {:panel      routes/default-panel
                                       :parameters {:path  {}
                                                    :query {}}}
   :alert                             {:message ""
                                       :id      nil}
   :requests                          #{}
   :lang                              :fi
   :translations                      translations/local-translations
   :hakukohderyhma                    {:persisted                []
                                       :input-visibility         {:create-active?                false
                                                                  :rename-active?                false
                                                                  :deletion-confirmation-active? false}
                                       :haut                     []
                                       :hakukohteet-filter       ""
                                       :hakukohderyhma-name-text ""
                                       :lisarajaimet             {:popup-visible false
                                                                  :filters       default-lisarajain-filters}}
   :haun-asetukset                    {:haut {}}
   :forms                             {}
   :ohjausparametrit                  {}
   :ohjausparametrit/save-in-progress #{}})
