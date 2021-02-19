(ns hakukohderyhmapalvelu.common-schemas
  (:require [schema.core :as s]))

(s/defschema Nimi
  {(s/optional-key :fi) s/Str
   (s/optional-key :sv) s/Str
   (s/optional-key :en) s/Str})
