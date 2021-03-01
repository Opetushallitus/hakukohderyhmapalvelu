(ns hakukohderyhmapalvelu.schemas.organisaatio-service-schemas
  (:require [hakukohderyhmapalvelu.common-schemas :as c]
            [schema.core :as s]
            [schema-tools.core :as st]))

(s/defschema OrganisaatioTyypit
  [s/Str])

(s/defschema OrganisaatioRyhmatyypit
  [s/Str])

(s/defschema OrganisaatioKayttoryhmat
  [s/Str])

(s/defschema Organisaatio
  {:oid          s/Str
   :kayttoryhmat OrganisaatioKayttoryhmat
   :parentOid    s/Str
   :ryhmatyypit  OrganisaatioRyhmatyypit
   :tyypit       OrganisaatioTyypit
   :nimi         c/Nimi
   s/Any         s/Any})

(s/defschema FindByOidsRequest
  [s/Str])

(s/defschema FindByOidsResponse
  [Organisaatio])

(s/defschema PostNewOrganisaatioRequest
  (st/select-keys Organisaatio
                  [:kayttoryhmat
                   :parentOid
                   :ryhmatyypit
                   :tyypit
                   :nimi]))

(s/defschema PostNewOrganisaatioResponse
  {:organisaatio Organisaatio
   :status       s/Str})

(s/defschema GetOrganisaatioChildrenSchema
  (st/merge
    api-schemas/HakukohderyhmaGetResponse
    s/Any))

