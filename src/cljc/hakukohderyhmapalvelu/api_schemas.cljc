(ns hakukohderyhmapalvelu.api-schemas
  (:require [schema.core :as s]))

(s/defschema HakukohderyhmaNimi
  {:fi s/Str})

(s/defschema HakukohderyhmaRequest
  {:nimi HakukohderyhmaNimi})

(s/defschema HakukohderyhmaResponse
  {:oid  s/Str
   :nimi HakukohderyhmaNimi})
