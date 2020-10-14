(ns hakukohderyhmapalvelu.db)

(def default-db
  {:active-panel          :panel-menu/hakukohderyhmien-hallinta-panel
   :requests              #{}
   :lang                  :fi
   :create-hakukohderyhma {:visible? false}})
