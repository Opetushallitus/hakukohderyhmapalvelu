(ns hakukohderyhmapalvelu.schemas.app-db-schemas
  (:require [schema.core :as s]
            [schema-tools.core :as st]))

(s/defschema HakukohderyhmienHallintaPanel
  {:panel      (s/eq :panel/hakukohderyhmien-hallinta)
   :parameters {:query {}
                :path  {}}})

(s/defschema HaunAsetuksetPanel
  {:panel      (s/eq :panel/haun-asetukset)
   :parameters {:query {:hakuOid s/Str}
                :path  {}}})

(s/defschema ActivePanel
  {:active-panel
   (s/conditional
     #(-> % :panel (= :panel/hakukohderyhmien-hallinta))
     HakukohderyhmienHallintaPanel
     #(-> % :panel (= :panel/haun-asetukset))
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
