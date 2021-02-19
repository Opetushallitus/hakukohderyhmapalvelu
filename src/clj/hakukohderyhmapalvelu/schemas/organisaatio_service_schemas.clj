(ns hakukohderyhmapalvelu.schemas.organisaatio-service-schemas
  (:require [hakukohderyhmapalvelu.api-schemas :as api-schemas]
            [hakukohderyhmapalvelu.common-schemas :as c]
            [schema.core :as s]
            [schema-tools.core :as st]))

(s/defschema OrganisaatioTyypit
  [s/Str])

(s/defschema OrganisaatioRyhmatyypit
  [s/Str])

(s/defschema OrganisaatioKayttoryhmat
  [s/Str])

(s/defschema PostNewOrganisaatioRequest
  {:kayttoryhmat OrganisaatioKayttoryhmat
   :parentOid    s/Str
   :ryhmatyypit  OrganisaatioRyhmatyypit
   :tyypit       OrganisaatioTyypit
   :nimi         c/Nimi})

(s/defschema PostNewOrganisaatioResponse
  {:organisaatio (st/merge
                   api-schemas/HakukohderyhmaResponse
                   PostNewOrganisaatioRequest
                   {:kayntiosoite             {}
                    :kieletUris               []
                    :kuvaus2                  {}
                    :lisatiedot               []
                    :muutKotipaikatUris       []
                    :muutOppilaitosTyyppiUris []
                    :nimet                    []
                    :oid                      s/Str
                    :parentOidPath            s/Str
                    :piilotettu               s/Bool
                    :postiosoite              {}
                    :status                   s/Str
                    :toimipistekoodi          s/Str
                    :version                  s/Int
                    :vuosiluokat              []
                    :yhteystiedot             []
                    :yhteystietoArvos         []})
   :status       s/Str})
