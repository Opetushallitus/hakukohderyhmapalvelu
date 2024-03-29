(ns hakukohderyhmapalvelu.kouta.fixtures)

(def kouta-haun-tiedot-response
  [{:oid      "1.2.246.562.29.1"
    :nimi     {:fi "Päättynyt haku"}
    :hakuajat [{:alkaa   "2018-01-02T00:00:00"
                :paattyy "2019-12-01T00:00:00"}]
    :kohdejoukkoKoodiUri "haunkohdejoukko_5#1"}
   {:oid      "1.2.246.562.29.2"
    :nimi     {:fi "Tulevaisuuden haku"}
    :hakuajat [{:alkaa   "2010-03-03T00:00:00"
                :paattyy "2011-06-06T00:00:00"}
               {:alkaa   "2050-01-02T00:00:00"
                :paattyy "2100-12-01T00:00:00"}]
    :kohdejoukkoKoodiUri "haunkohdejoukko_5#1"}
   {:oid      "1.2.246.562.29.3"
    :nimi     {:fi "Nykyhetkellä voimassa"}
    :hakuajat [{:alkaa   "2020-01-02T00:00:00"
                :paattyy "2100-12-01T00:00:00"}]
    :kohdejoukkoKoodiUri "haunkohdejoukko_5#1"}
   {:oid      "1.2.246.562.29.4"
    :nimi     {:fi "Nykyhetkellä voimassa oleva jatkuva"}
    :hakuajat [{:alkaa "2020-01-03T00:00:00"}]
    :kohdejoukkoKoodiUri "haunkohdejoukko_5#1"}
   {:oid      "1.2.246.562.29.555"
    :nimi     {:fi "Toisen asteen yhteishaku 2038"}
    :hakuajat [{:alkaa "2038-01-03T00:00:00"}]
    :kohdejoukkoKoodiUri "haunkohdejoukko_11#1"}])

(def kouta-hakukohteet-response
  [{:oid                           "1.2.246.562.20.1"
    :nimi                          {:fi "Hakukohde 1"}
    :organisaatioOid               "1.2.246.562.28.1"
    :tarjoaja                      "1.2.246.562.28.1"
    :hakuOid                       "1.2.246.562.29.1"
    :tila                          "julkaistu"
    :toinenAsteOnkoKaksoistutkinto false
    :oikeusHakukohteeseen          true
    :valintakokeet []
    :salliikoHakukohdeHarkinnanvaraisuudenKysymisen true}
   {:oid                           "1.2.246.562.20.2"
    :nimi                          {:fi "Hakukohde 2"}
    :organisaatioOid               "1.2.246.562.28.2"
    :tarjoaja                      "1.2.246.562.28.2"
    :hakuOid                       "1.2.246.562.29.1"
    :tila                          "julkaistu"
    :toinenAsteOnkoKaksoistutkinto false
    :oikeusHakukohteeseen          true
    :valintakokeet []
    :salliikoHakukohdeHarkinnanvaraisuudenKysymisen true}])

(def kouta-hakukohteet-response-1-3
  [{:oid                           "1.2.246.562.20.1"
    :nimi                          {:fi "Hakukohde 1"}
    :organisaatioOid               "1.2.246.562.28.1"
    :tarjoaja                      "1.2.246.562.28.1"
    :hakuOid                       "1.2.246.562.29.1"
    :tila                          "julkaistu"
    :toinenAsteOnkoKaksoistutkinto false
    :oikeusHakukohteeseen          true
    :valintakokeet []
    :salliikoHakukohdeHarkinnanvaraisuudenKysymisen true}
   {:oid                           "1.2.246.562.20.3"
    :nimi                          {:fi "Hakukohde 3"}
    :organisaatioOid               "1.2.246.562.28.2"
    :tarjoaja                      "1.2.246.562.28.2"
    :hakuOid                       "1.2.246.562.29.2"
    :tila                          "arkistoitu"
    :toinenAsteOnkoKaksoistutkinto false
    :oikeusHakukohteeseen          true
    :valintakokeet []
    :salliikoHakukohdeHarkinnanvaraisuudenKysymisen true}])


(def kouta-hakukohteet-response-for-delete
  [{:oid                           "1.2.3.4.5.6.7.8.9.10"
    :nimi                          {:fi "Hakukohde 1"}
    :organisaatioOid               "1.2.246.562.28.1"
    :tarjoaja                      "1.2.246.562.28.1"
    :hakuOid                       "1.2.246.562.29.1"
    :tila                          "arkistoitu"
    :toinenAsteOnkoKaksoistutkinto false
    :oikeusHakukohteeseen          true
    :valintakokeet []
    :salliikoHakukohdeHarkinnanvaraisuudenKysymisen true}])

(def kouta-hakukohteet-with-kokeet-response
  [{:oid                           "1.2.246.562.20.1"
    :nimi                          {:fi "Hakukohde 1"}
    :organisaatioOid               "1.2.246.562.28.1"
    :tarjoaja                      "1.2.246.562.28.1"
    :hakuOid                       "1.2.246.562.29.1"
    :tila                          "julkaistu"
    :toinenAsteOnkoKaksoistutkinto false
    :oikeusHakukohteeseen          true
    :valintakokeet [{:tyyppi "valintakokeentyyppi_1#3" :id "1.2.342.2.1"}]
    :salliikoHakukohdeHarkinnanvaraisuudenKysymisen true}
   {:oid                           "1.2.246.562.20.2"
    :nimi                          {:fi "Hakukohde 2"}
    :organisaatioOid               "1.2.246.562.28.2"
    :tarjoaja                      "1.2.246.562.28.2"
    :hakuOid                       "1.2.246.562.29.1"
    :tila                          "julkaistu"
    :toinenAsteOnkoKaksoistutkinto false
    :oikeusHakukohteeseen          true
    :valintakokeet []
    :valintaperusteValintakokeet [{:tyyppi "valintakokeentyyppi_10" :id "1.2.342.2.2"}]
    :salliikoHakukohdeHarkinnanvaraisuudenKysymisen true}
   {:oid                           "1.2.246.562.20.3"
    :nimi                          {:fi "Hakukohde 3"}
    :organisaatioOid               "1.2.246.562.28.2"
    :tarjoaja                      "1.2.246.562.28.2"
    :hakuOid                       "1.2.246.562.29.1"
    :tila                          "julkaistu"
    :toinenAsteOnkoKaksoistutkinto false
    :oikeusHakukohteeseen          true
    :valintakokeet []
    :valintaperusteValintakokeet [{:tyyppi "valintakokeentyyppi_2" :id "1.2.342.2.3"}]
    :salliikoHakukohdeHarkinnanvaraisuudenKysymisen true}])
