(ns hakukohderyhmapalvelu.schemas.kouta-service-schemas
  (:require [schema.core :as s]
            [hakukohderyhmapalvelu.common-schemas :as c])
  (:import (java.time LocalDateTime)))

(s/defschema Hakuaika
  {:alkaa                    LocalDateTime
   (s/optional-key :paattyy) LocalDateTime})

(s/defschema HaunTiedot
  {:oid                                  s/Str
   :nimi                                 c/Nimi
   :hakuajat                             [Hakuaika]
   (s/optional-key :kohdejoukkoKoodiUri) s/Str
   s/Any     s/Any})

(s/defschema HaunTiedotListResponse
  [HaunTiedot])

(s/defschema Valintakoe
  {:id   s/Str
   s/Any s/Any})

(s/defschema Hakukohde
  {:oid                                               s/Str
   :nimi                                              c/Nimi
   :organisaatioOid                                   s/Str
   :tarjoaja                                          s/Str
   :hakuOid                                           s/Str
   :toinenAsteOnkoKaksoistutkinto                     s/Bool
   :valintakokeet                                     [Valintakoe]
   :tila                                              (s/enum "tallennettu" "julkaistu" "arkistoitu")
   (s/optional-key :salliikoHakukohdeHarkinnanvaraisuudenKysymisen)    s/Bool
   (s/optional-key :oikeusHakukohteeseen)             s/Bool
   (s/optional-key :jarjestaaUrheilijanAmmKoulutusta) s/Bool
   (s/optional-key :koulutustyyppikoodi)              (s/maybe s/Str)
   s/Any                                              s/Any})

(s/defschema HakukohdeListResponse
  [Hakukohde])
