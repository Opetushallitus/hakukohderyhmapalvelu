(ns hakukohderyhmapalvelu.schemas.app-db-schemas
  (:require [schema.core :as s]
            [schema-tools.core :as st]))

(s/defschema HakukohderyhmienHallintaPanel
  {:panel      (s/eq :panel-menu/hakukohderyhmien-hallinta-panel)
   :parameters {:query {}
                :path  {}}})

(s/defschema HaunAsetuksetPanel
  {:panel      (s/eq :panel-menu/haun-asetukset-panel)
   :parameters {:query {:hakuOid s/Str}
                :path  {}}})

(s/defschema ActivePanel
  {:active-panel
   (s/conditional
     #(-> % :panel (= :panel-menu/hakukohderyhmien-hallinta-panel))
     HakukohderyhmienHallintaPanel
     #(-> % :panel (= :panel-menu/haun-asetukset-panel))
     HaunAsetuksetPanel)})

(s/defschema Lang
  {:lang (s/enum :fi)})

(s/defschema CreateHakukohderyhmapalvelu
  {:create-hakukohderyhma
   {:visible? s/Bool}})

(s/defschema Requests
  {:requests #{s/Keyword}})

(s/defschema AppDb
  (st/merge ActivePanel
            Lang
            Requests
            CreateHakukohderyhmapalvelu))
