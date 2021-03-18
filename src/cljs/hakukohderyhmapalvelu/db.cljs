(ns hakukohderyhmapalvelu.db
  (:require [hakukohderyhmapalvelu.routes :as routes]))

(def default-db
  {:active-panel                      {:panel      routes/default-panel
                                       :parameters {:path  {}
                                                    :query {}}}
   :requests                          #{}
   :lang                              :fi
   :hakukohderyhma                    {:persisted               #{}
                                       :selected-hakukohderyhma nil
                                       :input-visibility        {:create-active? false
                                                                 :rename-active? false}
                                       :haut                    []
                                       :hakukohteet-filter      ""}
   :haun-asetukset                    {:haut {}}
   :forms                             {}
   :ohjausparametrit                  {}
   :ohjausparametrit/save-in-progress #{}})
