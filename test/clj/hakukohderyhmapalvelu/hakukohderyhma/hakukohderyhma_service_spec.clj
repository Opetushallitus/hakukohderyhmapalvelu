(ns hakukohderyhmapalvelu.hakukohderyhma.hakukohderyhma-service-spec
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [hakukohderyhmapalvelu.hakukohderyhma.hakukohderyhma-service-protocol :as hakukohderyhma-protocol]
            [hakukohderyhmapalvelu.hakukohderyhma.fixtures :as hakukohderyhma-test-fixtures]
            [hakukohderyhmapalvelu.organisaatio.fixtures :as organisaatio-test-fixtures]
            [hakukohderyhmapalvelu.kouta.fixtures :as kouta-test-fixtures]
            [hakukohderyhmapalvelu.test-fixtures :as test-fixtures :refer [dispatch-mock test-system]]))


(use-fixtures :once test-fixtures/with-mock-system)
(use-fixtures :each test-fixtures/with-empty-database)

(deftest hakukohderyhma-update-service-test
  (testing "Update hakukohderyhma hakukohteet"
    (let [service (:hakukohderyhma-service @test-system)
          hakukohteet [{:oid          "1.2.246.562.20.1"
                        :nimi         {:fi "Hakukohde 1"}
                        :hakuOid      "1.2.246.562.29.1"
                        :organisaatio {:oid  "1.2.246.562.28.1"
                                       :nimi {:fi "Organisaatio 1"}}}
                       {:oid          "1.2.246.562.20.2"
                        :nimi         {:fi "Hakukohde 2"}
                        :hakuOid      "1.2.246.562.29.1"
                        :organisaatio {:oid  "1.2.246.562.28.2"
                                       :nimi {:fi "Organisaatio 2"}}}]
          expected {:oid         "1.2.246.562.28.4"
                    :nimi        {:fi "Hakukohderyhmä 1"}
                    :hakukohteet [{:oid          "1.2.246.562.20.1"
                                   :nimi         {:fi "Hakukohde 1"}
                                   :hakuOid      "1.2.246.562.29.1"
                                   :organisaatio {:oid  "1.2.246.562.28.1"
                                                  :nimi {:fi "Organisaatio 1"}}}
                                  {:oid          "1.2.246.562.20.2"
                                   :nimi         {:fi "Hakukohde 2"}
                                   :hakuOid      "1.2.246.562.29.1"
                                   :organisaatio {:oid  "1.2.246.562.28.2"
                                                  :nimi {:fi "Organisaatio 2"}}}]}]
      (dispatch-mock {:method   :get
                      :path     "/organisaatio-service/rest/organisaatio/v4/1.2.246.562.28.4"
                      :service  :organisaatio-service
                      :response organisaatio-test-fixtures/hakukohderyhma-response})
      (dispatch-mock {:method   :post
                      :path     "/kouta-internal/hakukohde/findbyoids"
                      :service  :kouta-service
                      :request  []
                      :response []})
      (dispatch-mock {:method   :post
                      :path     "/kouta-internal/hakukohde/findbyoids"
                      :service  :kouta-service
                      :request  ["1.2.246.562.20.1" "1.2.246.562.20.2"]
                      :response kouta-test-fixtures/kouta-hakukohteet-response})
      (dispatch-mock {:method   :post
                      :path     "/organisaatio-service/rest/organisaatio/v4/findbyoids"
                      :service  :organisaatio-service
                      :request  ["1.2.246.562.28.1" "1.2.246.562.28.2"]
                      :response organisaatio-test-fixtures/organisaatiot-response})
      (is (= (hakukohderyhma-protocol/update-hakukohderyhma-hakukohteet
               service hakukohderyhma-test-fixtures/fake-session "1.2.246.562.28.4" hakukohteet)
             expected)))))

(deftest hakukohderyhma-update-service-test-multiple-haku
  (testing "Update hakukohderyhma hakukohteet. Should fail due to hakukohteet are not from same haku."
    (let [service (:hakukohderyhma-service @test-system)
          hakukohteet [{:oid          "1.2.246.562.20.1"
                        :nimi         {:fi "Hakukohde 1"}
                        :hakuOid      "1.2.246.562.29.1"
                        :organisaatio {:oid  "1.2.246.562.28.1"
                                       :nimi {:fi "Organisaatio 1"}}}
                       {:oid          "1.2.246.562.20.3"
                        :nimi         {:fi "Hakukohde 3"}
                        :hakuOid      "1.2.246.562.29.2"
                        :organisaatio {:oid  "1.2.246.562.28.2"
                                       :nimi {:fi "Organisaatio 2"}}}]]
      (dispatch-mock {:method   :get
                      :path     "/organisaatio-service/rest/organisaatio/v4/1.2.246.562.28.4"
                      :service  :organisaatio-service
                      :response organisaatio-test-fixtures/hakukohderyhma-response})
      (dispatch-mock {:method   :post
                      :path     "/kouta-internal/hakukohde/findbyoids"
                      :service  :kouta-service
                      :request  []
                      :response []})
      (dispatch-mock {:method   :post
                      :path     "/kouta-internal/hakukohde/findbyoids"
                      :service  :kouta-service
                      :request  ["1.2.246.562.20.1" "1.2.246.562.20.3"]
                      :response kouta-test-fixtures/kouta-hakukohteet-response-1-3})
      (dispatch-mock {:method   :post
                      :path     "/organisaatio-service/rest/organisaatio/v4/findbyoids"
                      :service  :organisaatio-service
                      :request  ["1.2.246.562.28.1" "1.2.246.562.28.2"]
                      :response organisaatio-test-fixtures/organisaatiot-response})
      (is (thrown? Exception
                     (hakukohderyhma-protocol/update-hakukohderyhma-hakukohteet
                       service hakukohderyhma-test-fixtures/fake-session "1.2.246.562.28.4" hakukohteet))))))