(ns hakukohderyhmapalvelu.db
  (:require [hakukohderyhmapalvelu.i18n.translations :as translations]
            [hakukohderyhmapalvelu.lisarajaimet :as lisarajaimet]
            [hakukohderyhmapalvelu.routes :as routes]))

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
                                                                  :filters       lisarajaimet/default-lisarajain-filters}}
   :haun-asetukset                    {:haut {}}
   :forms                             {}
   :ohjausparametrit                  {}
   :save-status                       {:changes-saved true
                                       :errors []}
   :ohjausparametrit/save-in-progress #{}})
