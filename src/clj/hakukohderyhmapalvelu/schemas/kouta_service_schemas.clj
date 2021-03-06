(ns hakukohderyhmapalvelu.schemas.kouta-service-schemas
  (:require [schema.core :as s]
            [hakukohderyhmapalvelu.common-schemas :as c])
  (:import (java.time LocalDateTime)))

(s/defschema Hakuaika
  {:alkaa                    LocalDateTime
   (s/optional-key :paattyy) LocalDateTime})

(s/defschema HaunTiedot
  {:oid      s/Str
   :nimi     c/Nimi
   :hakuajat [Hakuaika]
   s/Any     s/Any})

(s/defschema HaunTiedotListResponse
  [HaunTiedot])

(s/defschema Valintakoe
  {:id   s/Str
   s/Any s/Any})

(s/defschema Hakukohde
  {:oid                                            s/Str
   :nimi                                           c/Nimi
   :organisaatioOid                                s/Str
   :hakuOid                                        s/Str
   :toinenAsteOnkoKaksoistutkinto                  s/Bool
   :valintakokeet                                  [Valintakoe]
   (s/optional-key :onkoHarkinnanvarainenKoulutus) s/Bool
   (s/optional-key :oikeusHakukohteeseen)          s/Bool
   s/Any                                           s/Any})

(s/defschema HakukohdeListResponse
  [Hakukohde])
