(ns hakukohderyhmapalvelu.api-schemas
  (:require [schema.core :as s]
            [hakukohderyhmapalvelu.common-schemas :as c]))

(s/defschema HakukohderyhmaRequest
  {:nimi c/Nimi})

(s/defschema HakukohderyhmaResponse
  {:oid  s/Str
   :nimi c/Nimi})

(s/defschema HaunTiedot
  {:oid  s/Str
   :nimi c/Nimi})

(s/defschema HaunTiedotListResponse
  [HaunTiedot])

(s/defschema Organisaatio
  {:oid  s/Str
   :nimi c/Nimi})

(s/defschema Hakukohde
  {:oid  s/Str
   :nimi c/Nimi
   :organisaatio Organisaatio})

(s/defschema HakukohdeListResponse
  [Hakukohde])
