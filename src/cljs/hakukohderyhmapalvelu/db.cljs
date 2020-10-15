(ns hakukohderyhmapalvelu.db)

(def default-db
  {:active-panel          {:panel      :panel-menu/hakukohderyhmien-hallinta-panel
                           :parameters {:path  {}
                                        :query {}}}
   :requests              #{}
   :lang                  :fi
   :create-hakukohderyhma {:visible? false}})
