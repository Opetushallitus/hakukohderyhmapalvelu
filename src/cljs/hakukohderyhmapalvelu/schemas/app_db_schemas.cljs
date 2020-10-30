(ns hakukohderyhmapalvelu.schemas.app-db-schemas
  (:require [clojure.string]
            [schema.core :as s]
            [schema-tools.core :as st]))

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

(s/defschema CreateHakukohderyhmapalvelu
  {:create-hakukohderyhma
   {:visible? s/Bool}})

(s/defschema Requests
  {:requests #{s/Keyword}})

(s/defschema LocalizedString
  {(s/optional-key :fi) s/Str
   (s/optional-key :sv) s/Str
   (s/optional-key :en) s/Str})

(s/defschema KoodiUri
  (s/constrained s/Str #(clojure.string/includes? % "#")))

(s/defschema Hakuaika
  {:alkaa                    s/Str
   (s/optional-key :paattyy) s/Str})

(s/defschema Haku
  {:nimi                               LocalizedString
   (s/optional-key :hakulomakeAtaruId) s/Str
   :kohdejoukkoKoodiUri                KoodiUri
   :hakuajat                           [Hakuaika]})

(s/defschema Haut
  {:haut {s/Str Haku}})

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
            CreateHakukohderyhmapalvelu
            Haut
            Forms
            HakujenOhjausparametrit))
