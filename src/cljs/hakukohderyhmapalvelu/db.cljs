(ns hakukohderyhmapalvelu.db
  (:require [hakukohderyhmapalvelu.routes :as routes]))

(def default-db
  {:active-panel                      {:panel      routes/default-panel
                                       :parameters {:path  {}
                                                    :query {}}}
   :requests                          #{}
   :lang                              :fi
   :create-hakukohderyhma             {:visible? false}
   :haut                              {}
   :forms                             {}
   :ohjausparametrit                  {}
   :ohjausparametrit/save-in-progress #{}})
