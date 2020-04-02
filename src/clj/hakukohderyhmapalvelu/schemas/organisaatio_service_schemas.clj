(ns hakukohderyhmapalvelu.schemas.organisaatio-service-schemas
  (:require [hakukohderyhmapalvelu.api-schemas :as api-schemas]
            [schema.core :as s]
            [schema-tools.core :as st]))

(s/defschema OrganisaatioTyypitSchema
  [s/Str])

(s/defschema OrganisaatioRyhmatyypitSchema
  [s/Str])

(s/defschema OrganisaatioKayttoryhmatSchema
  [s/Str])

(s/defschema PostNewOrganisaatioRequestSchema
  {:kayttoryhmat OrganisaatioKayttoryhmatSchema
   :parentOid    s/Str
   :ryhmatyypit  OrganisaatioRyhmatyypitSchema
   :tyypit       OrganisaatioTyypitSchema
   :nimi         api-schemas/HakukohderyhmaNimiSchema})

(s/defschema PostNewOrganisaatioResponseSchema
  {:organisaatio (st/merge
                   api-schemas/HakukohderyhmaResponse
                   PostNewOrganisaatioRequestSchema
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


