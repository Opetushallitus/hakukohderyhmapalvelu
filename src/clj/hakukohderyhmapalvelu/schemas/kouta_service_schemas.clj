(ns hakukohderyhmapalvelu.schemas.kouta-service-schemas
  (:require [schema.core :as s]
            [hakukohderyhmapalvelu.common-schemas :as c])
  (:import (java.time LocalDateTime)))

(s/defschema Hakuaika
  {:alkaa                    LocalDateTime
   (s/optional-key :paattyy) LocalDateTime})

(s/defschema HaunTiedot
  {:oid      s/Str
   :nimi     c/Nimi
   :hakuajat [Hakuaika]
   s/Any     s/Any})

(s/defschema HaunTiedotListResponse
  [HaunTiedot])

(s/defschema Hakukohde
  {:oid  s/Str
   :nimi c/Nimi
   s/Any s/Any})

(s/defschema HakukohdeListResponse
  [Hakukohde])
