(ns hakukohderyhmapalvelu.db
  (:require [hakukohderyhmapalvelu.routes :as routes]))

(def default-db
  {:active-panel                      {:panel      routes/default-panel
                                       :parameters {:path  {}
                                                    :query {}}}
   :requests                          #{}
   :lang                              :fi
   :hakukohderyhma                    {:persisted #{}
                                       :selected-hakukohderyhma nil
                                       :create-hakukohderyhma-visible? false}
   :haku                              {:haut         []}
   :haut                              {}
   :forms                             {}
   :ohjausparametrit                  {}
   :ohjausparametrit/save-in-progress #{}})
