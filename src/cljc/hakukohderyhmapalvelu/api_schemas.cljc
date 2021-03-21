(ns hakukohderyhmapalvelu.api-schemas
  (:require [schema.core :as s]
            [hakukohderyhmapalvelu.common-schemas :as c]))

(s/defschema CommonOrganisaatioEntityPayload
  {:oid  s/Str
   :nimi c/Nimi})

(s/defschema HakukohderyhmaPostRequest
  {:nimi c/Nimi})

(s/defschema HakukohderyhmaPutRequest
  CommonOrganisaatioEntityPayload)

(s/defschema HakukohderyhmaResponse
  CommonOrganisaatioEntityPayload)

(s/defschema HakukohderyhmaSearchRequest
  {:oids [s/Str]})

(s/defschema HakukohderyhmaListResponse
  [HakukohderyhmaResponse])

(s/defschema HaunTiedot
  CommonOrganisaatioEntityPayload)

(s/defschema HaunTiedotListResponse
  [HaunTiedot])

(s/defschema Organisaatio
  CommonOrganisaatioEntityPayload)

(s/defschema Hakukohde
  (merge
    CommonOrganisaatioEntityPayload
    {:organisaatio Organisaatio}))

(s/defschema HakukohdeListResponse
  [Hakukohde])
