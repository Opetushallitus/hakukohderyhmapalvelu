(ns hakukohderyhmapalvelu.schemas.app-db-schemas
  (:require [schema.core :as s]
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
  {:fi                  s/Str
   (s/optional-key :sv) s/Str
   (s/optional-key :en) s/Str})

(s/defschema Haku
  {:nimi LocalizedString})

(s/defschema Haut
  {:haut {s/Str Haku}})

(s/defschema HaunOhjausparametrit
  {(s/optional-key :PH_OPVP)                      (s/named
                                                    {:date s/Int}
                                                    "Opiskelijan paikan vastaanotto päättyy")
   (s/optional-key :PH_HPVOA)                     (s/named
                                                    {:value s/Int}
                                                    "Hakijakohtainen paikan vastaanottoaika")
   (s/optional-key :PH_HKP)                       (s/named
                                                    {:date s/Int}
                                                    "Hakukierros päättyy")
   (s/optional-key :jarjestetytHakutoiveet)       s/Bool
   (s/optional-key :hakutoiveidenMaaraRajoitettu) s/Bool
   (s/optional-key :vainYksiHakemusRajoitus)      s/Bool
   (s/optional-key :sijoittelu)                   s/Bool
   :__modified__                                  s/Int
   :__modifiedBy__                                s/Str})

(s/defschema HakujenOhjausparametrit
  {:ohjausparametrit                  {s/Str HaunOhjausparametrit}
   :ohjausparametrit/save-in-progress #{s/Str}})

(s/defschema AppDb
  (st/merge ActivePanel
            Lang
            Requests
            CreateHakukohderyhmapalvelu
            Haut
            HakujenOhjausparametrit))
