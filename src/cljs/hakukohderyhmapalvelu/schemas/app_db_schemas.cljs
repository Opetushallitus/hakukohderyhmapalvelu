(ns hakukohderyhmapalvelu.schemas.app-db-schemas
  (:require [schema.core :as s]
            [schema-tools.core :as st]))

(s/defschema ActivePanel
  {:active-panel (s/enum :hakukohderyhmapalvelu-panel)})

(s/defschema CreateHakukohderyhmapalveluGrid
  {:create-hakukohderyhma-grid
   {(s/optional-key :hakukohderyhma-name) s/Str
    :visible?                             s/Bool}})

(s/defschema UI
  {:ui CreateHakukohderyhmapalveluGrid})

(s/defschema Requests
  {:requests #{s/Keyword}})

(s/defschema AppDb
  (st/merge ActivePanel
            Requests
            UI))
