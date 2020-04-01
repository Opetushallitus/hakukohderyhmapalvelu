(ns hakukohderyhmapalvelu.schemas.app-db-schemas
  (:require [schema.core :as s]
            [schema-tools.core :as st]))

(s/defschema ActivePanel
  {:active-panel (s/enum :hakukohderyhmapalvelu-panel)})

(s/defschema CreateHakukohderyhmapalvelu
  {:create-hakukohderyhma
   {:visible? s/Bool}})

(s/defschema Requests
  {:requests #{s/Keyword}})

(s/defschema AppDb
  (st/merge ActivePanel
            Requests
            CreateHakukohderyhmapalvelu))
