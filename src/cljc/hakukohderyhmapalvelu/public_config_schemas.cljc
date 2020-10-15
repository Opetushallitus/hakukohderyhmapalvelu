(ns hakukohderyhmapalvelu.public-config-schemas
  (:require [schema.core :as s]))

(s/defschema PublicConfig
  {:environment (s/enum
                  :production
                  :development
                  :it)
   :default-panel (s/enum
                    :panel/hakukohderyhmien-hallinta
                    :panel/haun-asetukset)})
