(ns hakukohderyhmapalvelu.schemas.app-db-schemas
  (:require [schema.core :as s]
            [schema-tools.core :as st]))

(s/defschema ActivePanelSchema
  {:active-panel (s/enum :hakukohderyhmapalvelu-panel)})

(s/defschema CreateHakukohderyhmapalveluGridSchema
  {:create-hakukohderyhma-grid
   {(s/optional-key :hakukohderyhma-name) s/Str
    :visible?                             s/Bool}})

(s/defschema UISchema
  {:ui CreateHakukohderyhmapalveluGridSchema})

(s/defschema RequestSchema
  {:requests #{s/Keyword}})

(s/defschema AppDbSchema
  (st/merge ActivePanelSchema
            RequestSchema
            UISchema))

