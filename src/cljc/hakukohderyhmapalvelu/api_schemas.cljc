(ns hakukohderyhmapalvelu.api-schemas
  (:require [schema.core :as s]))

(s/defschema HakukohderyhmaRequest
  {:nimi s/Str})

(s/defschema HakukohderyhmaResponse
  {})
