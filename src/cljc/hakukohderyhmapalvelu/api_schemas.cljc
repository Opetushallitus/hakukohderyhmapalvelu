(ns hakukohderyhmapalvelu.api-schemas
  (:require [schema.core :as s]))

(s/defschema HakukohderyhmaNimiSchema
  {:fi s/Str})

(s/defschema HakukohderyhmaRequest
  {:nimi HakukohderyhmaNimiSchema})

(s/defschema HakukohderyhmaResponse
  {:oid  s/Str
   :nimi HakukohderyhmaNimiSchema})
