(ns hakukohderyhmapalvelu.public-config-schemas
  (:require [schema.core :as s]))

(s/defschema PublicConfig
  {:environment (s/enum :production :development :it)})