(ns hakukohderyhmapalvelu.api-schemas
  (:require [schema.core :as s]
            [schema-tools.core :as st]
            [hakukohderyhmapalvelu.common-schemas :as c]))

(defonce StatusDeleted "deleted")
(defonce StatusInUse "in-use")

(s/defschema CommonOrganisaatioEntityPayload
  {:oid  s/Str
   :nimi c/Nimi})

(s/defschema Organisaatio
  (st/merge CommonOrganisaatioEntityPayload
            {:version      s/Int
             :parentOid    s/Str
             :tyypit       [s/Str]
             :ryhmatyypit  [s/Str]
             :kayttoryhmat [s/Str]}))

(s/defschema SoraTieto
  {:tila s/Str})

(s/defschema Hakukohde
  (st/merge
    CommonOrganisaatioEntityPayload
    {:organisaatio                                   Organisaatio
     :toinenAsteOnkoKaksoistutkinto                  s/Bool
     (s/optional-key :hasValintakoe)                 s/Bool
     (s/optional-key :sora)                          SoraTieto
     (s/optional-key :oikeusHakukohteeseen)          s/Bool
     (s/optional-key :onkoHarkinnanvarainenKoulutus) s/Bool
     (s/optional-key :hakuOid)                       s/Str
     (s/optional-key :koulutustyypit)                [s/Str]}))

(s/defschema LocalizationEntity
  {:id       s/Int
   :category s/Str
   :key      s/Str
   :locale   (s/enum "fi" "sv" "en")
   :value    s/Str
   s/Any     s/Any})

(s/defschema Hakukohderyhma
  (st/merge Organisaatio
            {:hakukohteet [Hakukohde]}))

(s/defschema HakukohderyhmaPayload
  (st/merge
    CommonOrganisaatioEntityPayload
    {:version s/Int}))

(s/defschema HakukohderyhmaPostRequest
  {:nimi c/Nimi})

(s/defschema HakukohderyhmaPutRequest
  Organisaatio)

(s/defschema HakukohderyhmaResponse
  Organisaatio)

(s/defschema HakukohderyhmaDeleteResponse
  {:status (s/enum StatusDeleted StatusInUse)})

(s/defschema HakukohderyhmaSearchRequest
  {:oids         [s/Str]
   :includeEmpty s/Bool})

(s/defschema HaunTiedot
  CommonOrganisaatioEntityPayload)

(s/defschema HaunTiedotListResponse
  [HaunTiedot])

(s/defschema HakukohdeListResponse
  [Hakukohde])

(s/defschema HakukohderyhmaListResponse
  [Hakukohderyhma])

(s/defschema KoodistoResponse
  [{:koodiUri s/Str
    :metadata [{:nimi  s/Str
                :kieli s/Str
                s/Any  s/Any}]
    s/Any     s/Any}])
