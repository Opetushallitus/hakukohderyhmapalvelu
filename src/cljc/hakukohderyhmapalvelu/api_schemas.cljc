(ns hakukohderyhmapalvelu.api-schemas
  (:require [schema.core :as s]
            [hakukohderyhmapalvelu.common-schemas :as c]))

(s/defschema HakukohderyhmaRequest
  {:nimi c/Nimi})

(s/defschema HakukohderyhmaSearchRequest
  {:oids [s/Str]})

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
   (s/optional-key :hakuOid) s/Str
   :organisaatio Organisaatio})

(s/defschema HakukohdeListResponse
  [Hakukohde])

(s/defschema Hakukohderyhma
  {:oid  s/Str
   :nimi c/Nimi
   :hakukohteet [Hakukohde]})

(s/defschema HakukohderyhmaListResponse
  [Hakukohderyhma])
