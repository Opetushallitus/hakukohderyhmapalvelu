(ns hakukohderyhmapalvelu.api-schemas
  (:require [schema.core :as s]
            [hakukohderyhmapalvelu.common-schemas :as c]))

(s/defschema CommonOrganisaatioEntityPayload
  {:oid  s/Str
   :nimi c/Nimi})

(s/defschema HakukohderyhmaPayload
  (merge
    CommonOrganisaatioEntityPayload
    {:version s/Int}))

(s/defschema HakukohderyhmaPostRequest
  {:nimi c/Nimi})

(s/defschema HakukohderyhmaPutRequest
  HakukohderyhmaPayload)

(s/defschema HakukohderyhmaResponse
  HakukohderyhmaPayload)

(s/defschema HakukohderyhmaSearchRequest
  {:oids [s/Str]})

(s/defschema HaunTiedot
  CommonOrganisaatioEntityPayload)

(s/defschema HaunTiedotListResponse
  [HaunTiedot])

(s/defschema Organisaatio
  CommonOrganisaatioEntityPayload)

(s/defschema Hakukohde
  (merge
    CommonOrganisaatioEntityPayload
    {:organisaatio Organisaatio
     (s/optional-key :hakuOid) s/Str}))

(s/defschema HakukohdeListResponse
  [Hakukohde])

(s/defschema Hakukohderyhma
  {:oid  s/Str
   :nimi c/Nimi
   :version s/Int
   :hakukohteet [Hakukohde]})

(s/defschema HakukohderyhmaListResponse
  [Hakukohderyhma])
