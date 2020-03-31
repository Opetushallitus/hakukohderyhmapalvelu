(ns hakukohderyhmapalvelu.api-schemas
  (:require [schema.core :as s]))

(s/defschema Hakukohderyhma
  "Hakukohderyhmä"
  {:nimi s/Str})
