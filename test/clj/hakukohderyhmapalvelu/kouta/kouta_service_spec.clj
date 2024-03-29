(ns hakukohderyhmapalvelu.kouta.kouta-service-spec
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [hakukohderyhmapalvelu.kouta.kouta-protocol :as kouta-service-protocol]
            [hakukohderyhmapalvelu.kouta.fixtures :as kouta-test-fixtures]
            [hakukohderyhmapalvelu.organisaatio.fixtures :as organisaatio-test-fixtures]
            [hakukohderyhmapalvelu.test-fixtures :as test-fixtures :refer [dispatch-mock test-system]]))


(use-fixtures :once test-fixtures/with-mock-system)

(def tarjoajat ["1.2.246.562.10.00000000001"])

(deftest kouta-service-test
  (testing "List haut, no results"
    (dispatch-mock {:method   :get
                    :path     "/kouta-internal/haku/search?tarjoaja=1.2.246.562.10.00000000001"
                    :service  :kouta-service
                    :request  nil
                    :response []})
    (is (empty? (kouta-service-protocol/list-haun-tiedot (:kouta-service @test-system) false tarjoajat false)) "Kutsun ei pitäisi palauttaa hakuja"))

  (testing "List haut, active"
    (let [expected [{:oid  "1.2.246.562.29.2"
                     :nimi {:fi "Tulevaisuuden haku"}}
                    {:oid  "1.2.246.562.29.3"
                     :nimi {:fi "Nykyhetkellä voimassa"}}
                    {:oid  "1.2.246.562.29.4"
                     :nimi {:fi "Nykyhetkellä voimassa oleva jatkuva"}}]]
      (dispatch-mock {:method   :get
                      :path     "/kouta-internal/haku/search?tarjoaja=1.2.246.562.10.00000000001"
                      :service  :kouta-service
                      :response kouta-test-fixtures/kouta-haun-tiedot-response})
      (is (= expected (kouta-service-protocol/list-haun-tiedot (:kouta-service @test-system) false tarjoajat false)) "Kutsu ei vastaa oletettua")))

  {:oid      "1.2.246.562.29.555"
   :nimi     {:fi "Toisen asteen yhteishaku 2038"}
   :hakuajat [{:alkaa "2038-01-03T00:00:00"}]
   :kohdejoukkoKoodiUri "haunkohdejoukko_11#1"}
  (testing "List haut, active, superuser"
    (let [expected [{:oid  "1.2.246.562.29.2"
                     :nimi {:fi "Tulevaisuuden haku"}}
                    {:oid  "1.2.246.562.29.3"
                     :nimi {:fi "Nykyhetkellä voimassa"}}
                    {:oid  "1.2.246.562.29.4"
                     :nimi {:fi "Nykyhetkellä voimassa oleva jatkuva"}}
                    {:oid "1.2.246.562.29.555"
                     :nimi {:fi "Toisen asteen yhteishaku 2038"}}]]
      (dispatch-mock {:method   :get
                      :path     "/kouta-internal/haku/search?tarjoaja=1.2.246.562.10.00000000001"
                      :service  :kouta-service
                      :response kouta-test-fixtures/kouta-haun-tiedot-response})
      (is (= expected (kouta-service-protocol/list-haun-tiedot (:kouta-service @test-system) false tarjoajat true)) "Kutsu ei vastaa oletettua")))

  (testing "List haut, all"
    (let [expected [{:oid  "1.2.246.562.29.1"
                     :nimi {:fi "Päättynyt haku"}}
                    {:oid  "1.2.246.562.29.2"
                     :nimi {:fi "Tulevaisuuden haku"}}
                    {:oid  "1.2.246.562.29.3"
                     :nimi {:fi "Nykyhetkellä voimassa"}}
                    {:oid  "1.2.246.562.29.4"
                     :nimi {:fi "Nykyhetkellä voimassa oleva jatkuva"}}]]
      (dispatch-mock {:method   :get
                      :path     "/kouta-internal/haku/search?tarjoaja=1.2.246.562.10.00000000001"
                      :service  :kouta-service
                      :response kouta-test-fixtures/kouta-haun-tiedot-response})
      (is (= expected (kouta-service-protocol/list-haun-tiedot (:kouta-service @test-system) true tarjoajat false)) "Kutsu ei vastaa oletettua")))

  (testing "List hakukohteet for haku, no results"
    (dispatch-mock {:method   :get
                    :path     "/kouta-internal/hakukohde/search?haku=1.2.246.562.29.1&tarjoaja=1.2.246.562.10.00000000001&all=true"
                    :service  :kouta-service
                    :response []})
    (is (empty? (kouta-service-protocol/list-haun-hakukohteet (:kouta-service @test-system) "1.2.246.562.29.1" tarjoajat))))
  (testing "List hakukohteet for haku"
    (let [expected [{:oid                           "1.2.246.562.20.1"
                     :nimi                          {:fi "Hakukohde 1"}
                     :hakuOid                       "1.2.246.562.29.1"
                     :tarjoaja                      test-fixtures/organisaatio-1
                     :toinenAsteOnkoKaksoistutkinto false
                     :oikeusHakukohteeseen          true
                     :salliikoHakukohdeHarkinnanvaraisuudenKysymisen true
                     :hasPaasyJaSoveltuvuuskoeOma   false
                     :tila                          "julkaistu"}
                    {:oid                           "1.2.246.562.20.2"
                     :nimi                          {:fi "Hakukohde 2"}
                     :hakuOid                       "1.2.246.562.29.1"
                     :tarjoaja                      test-fixtures/organisaatio-2
                     :toinenAsteOnkoKaksoistutkinto false
                     :oikeusHakukohteeseen          true
                     :salliikoHakukohdeHarkinnanvaraisuudenKysymisen true
                     :hasPaasyJaSoveltuvuuskoeOma   false
                     :tila                          "julkaistu"}]]
      (dispatch-mock {:method   :get
                      :path     "/kouta-internal/hakukohde/search?haku=1.2.246.562.29.1&tarjoaja=1.2.246.562.10.00000000001&all=true"
                      :service  :kouta-service
                      :response kouta-test-fixtures/kouta-hakukohteet-response})
      (dispatch-mock {:method   :post
                      :path     "/organisaatio-service/rest/organisaatio/v4/findbyoids"
                      :service  :organisaatio-service
                      :request  ["1.2.246.562.28.1" "1.2.246.562.28.2"]
                      :response organisaatio-test-fixtures/organisaatiot-response})
      (is (= expected (kouta-service-protocol/list-haun-hakukohteet (:kouta-service @test-system) "1.2.246.562.29.1" tarjoajat)))))

  (testing "Find hakukohteet by oids"
    (let [expected [{:oid                           "1.2.246.562.20.1"
                     :nimi                          {:fi "Hakukohde 1"}
                     :hakuOid                       "1.2.246.562.29.1"
                     :tarjoaja                      test-fixtures/organisaatio-1
                     :toinenAsteOnkoKaksoistutkinto false
                     :oikeusHakukohteeseen          true
                     :salliikoHakukohdeHarkinnanvaraisuudenKysymisen true
                     :hasPaasyJaSoveltuvuuskoeOma   false
                     :tila                          "julkaistu"}
                    {:oid                           "1.2.246.562.20.2"
                     :nimi                          {:fi "Hakukohde 2"}
                     :hakuOid                       "1.2.246.562.29.1"
                     :tarjoaja                      test-fixtures/organisaatio-2
                     :toinenAsteOnkoKaksoistutkinto false
                     :oikeusHakukohteeseen          true
                     :salliikoHakukohdeHarkinnanvaraisuudenKysymisen true
                     :hasPaasyJaSoveltuvuuskoeOma   false
                     :tila                          "julkaistu"}]]
      (dispatch-mock {:method   :post
                      :path     "/kouta-internal/hakukohde/findbyoids?tarjoaja=1.2.246.562.10.00000000001"
                      :service  :kouta-service
                      :request  ["1.2.246.562.20.1" "1.2.246.562.20.2"]
                      :response kouta-test-fixtures/kouta-hakukohteet-response})
      (dispatch-mock {:method   :post
                      :path     "/organisaatio-service/rest/organisaatio/v4/findbyoids"
                      :service  :organisaatio-service
                      :request  ["1.2.246.562.28.1" "1.2.246.562.28.2"]
                      :response organisaatio-test-fixtures/organisaatiot-response})
      (is (= expected (kouta-service-protocol/find-hakukohteet-by-oids
                        (:kouta-service @test-system)
                        ["1.2.246.562.20.1" "1.2.246.562.20.2"]
                        tarjoajat)))))

  (testing "Find hakukohteet with hasPaasyJaSoveltuvuuskoeOma"
    (dispatch-mock {:method   :post
                    :path     "/kouta-internal/hakukohde/findbyoids?tarjoaja=1.2.246.562.10.00000000001"
                    :service  :kouta-service
                    :request  ["1.2.246.562.20.1" "1.2.246.562.20.2" "1.2.246.562.20.3"]
                    :response kouta-test-fixtures/kouta-hakukohteet-with-kokeet-response})
    (dispatch-mock {:method   :post
                    :path     "/organisaatio-service/rest/organisaatio/v4/findbyoids"
                    :service  :organisaatio-service
                    :request  ["1.2.246.562.28.1" "1.2.246.562.28.2"]
                    :response organisaatio-test-fixtures/organisaatiot-response})
    (let [expected ["1.2.246.562.20.2"]
          results (->> (kouta-service-protocol/find-hakukohteet-by-oids
                         (:kouta-service @test-system)
                         ["1.2.246.562.20.1" "1.2.246.562.20.2" "1.2.246.562.20.3"]
                         tarjoajat)
                       (filter :hasPaasyJaSoveltuvuuskoeOma)
                       (map :oid)
                       vec)]
      (is (= expected results))))

  (testing "Find hakukohteet by empty oids"
    (let [expected []]
      (is (= expected (kouta-service-protocol/find-hakukohteet-by-oids (:kouta-service @test-system) [] tarjoajat))))))
