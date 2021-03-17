(ns hakukohderyhmapalvelu.kouta.fixtures)

(def kouta-haun-tiedot-response
  [{:oid      "1.2.1.1"
    :nimi     {:fi "Päättynyt haku"}
    :hakuajat [{:alkaa   "2018-01-02T00:00:00"
                :paattyy "2019-12-01T00:00:00"}]}
   {:oid      "1.2.1.2"
    :nimi     {:fi "Tulevaisuuden haku"}
    :hakuajat [{:alkaa   "2010-03-03T00:00:00"
                :paattyy "2011-06-06T00:00:00"}
               {:alkaa   "2050-01-02T00:00:00"
                :paattyy "2100-12-01T00:00:00"}]}
   {:oid      "1.2.1.3"
    :nimi     {:fi "Nykyhetkellä voimassa"}
    :hakuajat [{:alkaa   "2020-01-02T00:00:00"
                :paattyy "2100-12-01T00:00:00"}]}])

(def kouta-hakukohteet-response
  [{:oid             "1.3.1.1"
    :nimi            {:fi "Hakukohde 1"}
    :organisaatioOid "1.4.1.1"}
   {:oid             "1.3.1.2"
    :nimi            {:fi "Hakukohde 2"}
    :organisaatioOid "1.4.1.2"}])