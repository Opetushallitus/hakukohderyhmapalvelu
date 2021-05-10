(ns hakukohderyhmapalvelu.db
  (:require [hakukohderyhmapalvelu.routes :as routes]))

(def default-lisarajain-filters
  [{:id      "sora-filter"
    :label   :haku/lisarajain-sora-hakukohde
    :path    [:sora :tila]
    :type    :boolean
    :pred-fn #(= "aktiivinen" %)
    :value   false}
   {:id      "kaksoistutkinto-filter"
    :label   :haku/lisarajain-kaksoistutkinto
    :path    [:toinenAsteOnkoKaksoistutkinto]
    :type    :boolean
    :pred-fn true?
    :value   false}])

(def default-db
  {:active-panel                      {:panel      routes/default-panel
                                       :parameters {:path  {}
                                                    :query {}}}
   :alert                             {:message ""}
   :requests                          #{}
   :lang                              :fi
   :hakukohderyhma                    {:persisted               []
                                       :selected-hakukohderyhma nil
                                       :input-visibility        {:create-active? false
                                                                 :rename-active? false
                                                                 :deletion-confirmation-active? false}
                                       :haut                    []
                                       :hakukohteet-filter      ""
                                       :lisarajaimet            {:popup-visible false
                                                                 :filters       default-lisarajain-filters}}
   :haun-asetukset                    {:haut {}}
   :forms                             {}
   :ohjausparametrit                  {}
   :ohjausparametrit/save-in-progress #{}})
