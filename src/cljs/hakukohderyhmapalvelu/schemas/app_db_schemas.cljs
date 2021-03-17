(ns hakukohderyhmapalvelu.schemas.app-db-schemas
  (:require [clojure.string]
            [schema.core :as s]
            [schema-tools.core :as st]
            [hakukohderyhmapalvelu.api-schemas :as api-schemas]))

(s/defschema HakukohderyhmienHallintaPanel
  {:panel      (s/eq :panel/hakukohderyhmien-hallinta)
   :parameters {:query {}
                :path  {}}})

(s/defschema HaunAsetuksetPanel
  {:panel      (s/eq :panel/haun-asetukset)
   :parameters {:query {:haku-oid s/Str}
                :path  {}}})

(s/defschema ActivePanel
  {:active-panel
   (s/conditional
     #(-> % :panel (= :panel/hakukohderyhmien-hallinta))
     HakukohderyhmienHallintaPanel
     #(-> % :panel (= :panel/haun-asetukset))
     HaunAsetuksetPanel)})

(s/defschema Lang
  {:lang (s/enum :fi)})

(s/defschema LocalizedString
  {(s/optional-key :fi) s/Str
   (s/optional-key :sv) s/Str
   (s/optional-key :en) s/Str})

(s/defschema Hakukohde
  {:oid          s/Str
   :nimi         LocalizedString
   (s/optional-key :hakuOid) s/Str
   :organisaatio api-schemas/Organisaatio
   :is-selected  s/Bool})

(s/defschema HaunTiedot
  {:oid         s/Str
   :nimi        LocalizedString
   :is-selected s/Bool
   :hakukohteet [Hakukohde]})


(s/defschema HakukohderyhmaPalvelu
  {:hakukohderyhma
   {:persisted                      #{api-schemas/Hakukohderyhma}
    :selected-hakukohderyhma        (s/maybe api-schemas/Hakukohderyhma)
    :create-hakukohderyhma-visible? s/Bool
    :haut                           [HaunTiedot]
    :hakukohteet-filter             s/Str}})

(s/defschema Requests
  {:requests #{s/Keyword}})

(s/defschema KoodiUri
  (s/constrained s/Str #(clojure.string/includes? % "#")))

(s/defschema Hakuaika
  {:alkaa                    s/Str
   (s/optional-key :paattyy) s/Str})

(s/defschema HaunAsetukset
  {:nimi                               LocalizedString
   (s/optional-key :hakulomakeAtaruId) s/Str
   :kohdejoukkoKoodiUri                KoodiUri
   :hakuajat                           [Hakuaika]})

(s/defschema Haut
  {:haun-asetukset {:haut {s/Str HaunAsetukset}}})

(s/defschema Form
  {:key  s/Str
   :name LocalizedString})

(s/defschema Forms
  {:forms {s/Str Form}})

(s/defschema OhjausparametritInt
  {:value s/Int})

(s/defschema OhjausparametritDate
  {:date s/Int})

(s/defschema HaunOhjausparametrit
  {(s/optional-key :PH_OPVP)                      (s/named
                                                    OhjausparametritDate
                                                    "Opiskelijan paikan vastaanotto päättyy")
   (s/optional-key :PH_HPVOA)                     (s/named
                                                    OhjausparametritInt
                                                    "Hakijakohtainen paikan vastaanottoaika")
   (s/optional-key :PH_HKP)                       (s/named
                                                    OhjausparametritDate
                                                    "Hakukierros päättyy")
   (s/optional-key :PH_VTSSV)                     (s/named
                                                    OhjausparametritDate
                                                    "Valintatulokset valmiina viimeistään")
   (s/optional-key :PH_VSSAV)                     (s/named
                                                    OhjausparametritDate
                                                    "Varasijasäännöt astuvat voimaan")
   (s/optional-key :PH_VSTP)                      (s/named
                                                    OhjausparametritDate
                                                    "Varasijatäyttö päättyy")
   (s/optional-key :jarjestetytHakutoiveet)       s/Bool
   (s/optional-key :hakutoiveidenMaaraRajoitettu) s/Bool
   (s/optional-key :hakutoiveidenEnimmaismaara)   s/Int
   (s/optional-key :useitaHakemuksia)             s/Bool
   (s/optional-key :sijoittelu)                   s/Bool
   (s/optional-key :__modified__)                 s/Int
   (s/optional-key :__modifiedBy__)               s/Str})

(s/defschema HakujenOhjausparametrit
  {:ohjausparametrit                  {s/Str HaunOhjausparametrit}
   :ohjausparametrit/save-in-progress #{s/Str}})

(s/defschema AppDb
  (st/merge ActivePanel
            Lang
            Requests
            HakukohderyhmaPalvelu
            Haut
            Forms
            HakujenOhjausparametrit))
