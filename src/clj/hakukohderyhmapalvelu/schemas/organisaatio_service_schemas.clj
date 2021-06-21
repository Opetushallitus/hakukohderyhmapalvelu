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
   :version      s/Int
   s/Any         s/Any})

(s/defschema FindByOidsRequest
  [s/Str])

(s/defschema FindByOidsResponse
  [Organisaatio])

(s/defschema PostOrganisaatioHakukohderyhmaParameter
  (st/select-keys Organisaatio
                  [:kayttoryhmat
                   :ryhmatyypit
                   :tyypit
                   :nimi]))

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

(s/defschema PutNewOrganisaatioResponse
  {:organisaatio Organisaatio
   :status       s/Str})

(s/defschema DeleteOrganisaatioResponse
  {:message (s/eq "deleted")})

(s/defschema Hakukohderyhma
  {:oid  s/Str
   :nimi c/Nimi
   s/Any s/Any})

(s/defschema GetRyhmatResponse
  [Hakukohderyhma])

(s/defschema Form
  {(s/optional-key :id) s/Int
   :name                c/Nimi
   s/Any                s/Any})

(s/defschema GetFormsResponse
  {:forms [Form]})
