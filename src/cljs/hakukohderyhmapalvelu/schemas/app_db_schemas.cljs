(ns hakukohderyhmapalvelu.schemas.app-db-schemas
  (:require [schema.core :as s]
            [schema-tools.core :as st]))

(def panels [:panel-menu/haun-asetukset-panel
             :panel-menu/hakukohderyhmien-hallinta-panel])

(s/defschema ActivePanel
  {:active-panel (apply s/enum panels)})

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
