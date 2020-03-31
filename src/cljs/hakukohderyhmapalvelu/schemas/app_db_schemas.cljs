(ns hakukohderyhmapalvelu.schemas.app-db-schemas
  (:require [schema.core :as s]
            [schema-tools.core :as st]))

(s/defschema ActivePanelSchema
  {:active-panel (s/enum :hakukohderyhmapalvelu-panel)})

(s/defschema CreateHakukohderyhmapalveluGridSchema
  {:create-hakukohderyhma-grid
   {:visible? s/Bool}})

(s/defschema UISchema
  {:ui CreateHakukohderyhmapalveluGridSchema})

(s/defschema AppDbSchema
  (st/merge ActivePanelSchema
            UISchema))

