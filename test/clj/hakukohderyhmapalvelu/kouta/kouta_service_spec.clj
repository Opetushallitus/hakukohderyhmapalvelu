(ns hakukohderyhmapalvelu.kouta.kouta-service-spec
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [hakukohderyhmapalvelu.kouta.kouta-protocol :as kouta-service-protocol]
            [hakukohderyhmapalvelu.kouta.fixtures :as kouta-test-fixtures]
            [hakukohderyhmapalvelu.organisaatio.fixtures :as organisaatio-test-fixtures]
            [hakukohderyhmapalvelu.test-fixtures :as test-fixtures :refer [dispatch-mock test-system]]))


(use-fixtures :once test-fixtures/with-mock-system)

(deftest kouta-service-test
  (testing "List haut, no results"
    (dispatch-mock {:method   :get
                    :path     "http://localhost/kouta-internal/haku/search?tarjoaja=1.2.246.562.10.00000000001"
                    :service  :kouta-service
                    :request  nil
                    :response []})
    (is (empty? (kouta-service-protocol/list-haun-tiedot (:kouta-service @test-system) false)) "Kutsun ei pitäisi palauttaa hakuja"))
  (testing "List haut, active"
    (let [expected [{:oid  "1.2.1.2"
                     :nimi {:fi "Tulevaisuuden haku"}}
                    {:oid  "1.2.1.3"
                     :nimi {:fi "Nykyhetkellä voimassa"}}]]
      (dispatch-mock {:method   :get
                      :path     "http://localhost/kouta-internal/haku/search?tarjoaja=1.2.246.562.10.00000000001"
                      :service  :kouta-service
                      :response kouta-test-fixtures/kouta-haun-tiedot-response})
      (is (= expected (kouta-service-protocol/list-haun-tiedot (:kouta-service @test-system) false)) "Kutsu ei vastaa oletettua")))
  (testing "List haut, all"
    (let [expected [{:oid  "1.2.1.1"
                     :nimi {:fi "Päättynyt haku"}}
                    {:oid  "1.2.1.2"
                     :nimi {:fi "Tulevaisuuden haku"}}
                    {:oid  "1.2.1.3"
                     :nimi {:fi "Nykyhetkellä voimassa"}}]]
      (dispatch-mock {:method   :get
                      :path     "http://localhost/kouta-internal/haku/search?tarjoaja=1.2.246.562.10.00000000001"
                      :service  :kouta-service
                      :response kouta-test-fixtures/kouta-haun-tiedot-response})
      (is (= expected (kouta-service-protocol/list-haun-tiedot (:kouta-service @test-system) true)) "Kutsu ei vastaa oletettua")))
  (testing "List hakukohteet for haku, no results"
    (dispatch-mock {:method   :get
                    :path     "http://localhost/kouta-internal/hakukohde/search?haku=1.2.4.1.1.1"
                    :service  :kouta-service
                    :response []})
    (is (empty? (kouta-service-protocol/list-haun-hakukohteet (:kouta-service @test-system) "1.2.4.1.1.1"))))
  (testing "List hakukohteet for haku"
    (let [expected [{:oid          "1.3.1.1"
                     :nimi         {:fi "Hakukohde 1"}
                     :organisaatio {:oid  "1.4.1.1"
                                    :nimi {:fi "Organisaatio 1"}}}
                    {:oid          "1.3.1.2"
                     :nimi         {:fi "Hakukohde 2"}
                     :organisaatio {:oid  "1.4.1.2"
                                    :nimi {:fi "Organisaatio 2"}}}]]
      (dispatch-mock {:method   :get
                      :path     "http://localhost/kouta-internal/hakukohde/search?haku=1.2.4.1.1.1"
                      :service  :kouta-service
                      :response kouta-test-fixtures/kouta-hakukohteet-response})
      (dispatch-mock {:method   :post
                      :path     "http://localhost/organisaatio-service/rest/organisaatio/v4/findbyoids"
                      :service  :organisaatio-service
                      :request  ["1.4.1.1" "1.4.1.2"]
                      :response organisaatio-test-fixtures/organisaatiot-response})
      (is (= expected (kouta-service-protocol/list-haun-hakukohteet (:kouta-service @test-system) "1.2.4.1.1.1"))))))