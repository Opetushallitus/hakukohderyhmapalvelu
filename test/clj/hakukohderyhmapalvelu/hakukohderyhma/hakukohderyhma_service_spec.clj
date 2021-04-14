(ns hakukohderyhmapalvelu.hakukohderyhma.hakukohderyhma-service-spec
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [hakukohderyhmapalvelu.hakukohderyhma.hakukohderyhma-service-protocol :as hakukohderyhma-protocol]
            [hakukohderyhmapalvelu.hakukohderyhma.fixtures :as hakukohderyhma-test-fixtures]
            [hakukohderyhmapalvelu.organisaatio.fixtures :as organisaatio-test-fixtures]
            [hakukohderyhmapalvelu.kouta.fixtures :as kouta-test-fixtures]
            [hakukohderyhmapalvelu.test-fixtures :as test-fixtures :refer [dispatch-mock
                                                                           test-system
                                                                           add-row!
                                                                           has-row?]]
            [hakukohderyhmapalvelu.api-schemas :as api-schemas]
            [hakukohderyhmapalvelu.ataru.fixtures :as ataru-test-fixtures]))


(use-fixtures :once test-fixtures/with-mock-system)
(use-fixtures :each test-fixtures/with-empty-database)

(deftest hakukohderyhma-update-service-test
  (testing "Update hakukohderyhma hakukohteet"
    (let [service (:hakukohderyhma-service @test-system)
          hakukohteet [{:oid          "1.2.246.562.20.1"
                        :nimi         {:fi "Hakukohde 1"}
                        :hakuOid      "1.2.246.562.29.1"
                        :organisaatio test-fixtures/organisaatio-1}
                       {:oid          "1.2.246.562.20.2"
                        :nimi         {:fi "Hakukohde 2"}
                        :hakuOid      "1.2.246.562.29.1"
                        :organisaatio test-fixtures/organisaatio-2}]
          expected {:oid          "1.2.246.562.28.4"
                    :nimi         {:fi "Hakukohderyhmä 1"}
                    :version      0
                    :kayttoryhmat []
                    :parentOid    "1.2.246.562.28.01"
                    :ryhmatyypit  []
                    :tyypit       []
                    :hakukohteet  [{:oid                           "1.2.246.562.20.1"
                                    :nimi                          {:fi "Hakukohde 1"}
                                    :hakuOid                       "1.2.246.562.29.1"
                                    :organisaatio                  test-fixtures/organisaatio-1
                                    :toinenAsteOnkoKaksoistutkinto false
                                    :oikeusHakukohteeseen          true
                                    :onkoHarkinnanvarainenKoulutus true
                                    :hasValintakoe                 false}
                                   {:oid                           "1.2.246.562.20.2"
                                    :nimi                          {:fi "Hakukohde 2"}
                                    :hakuOid                       "1.2.246.562.29.1"
                                    :organisaatio                  test-fixtures/organisaatio-2
                                    :toinenAsteOnkoKaksoistutkinto false
                                    :oikeusHakukohteeseen          true
                                    :onkoHarkinnanvarainenKoulutus true
                                    :hasValintakoe                 false}]}]
      (dispatch-mock {:method   :get
                      :path     "/organisaatio-service/rest/organisaatio/v4/1.2.246.562.28.4"
                      :service  :organisaatio-service
                      :response organisaatio-test-fixtures/hakukohderyhma-response})
      (dispatch-mock {:method   :post
                      :path     "/kouta-internal/hakukohde/findbyoids?tarjoaja=1.2.246.562.10.00000000001"
                      :service  :kouta-service
                      :request  []
                      :response []})
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
                      :path     "/kouta-internal/hakukohde/findbyoids?tarjoaja=1.2.246.562.10.00000000001"
                      :service  :kouta-service
                      :request  []
                      :response []})
      (dispatch-mock {:method   :post
                      :path     "/kouta-internal/hakukohde/findbyoids?tarjoaja=1.2.246.562.10.00000000001"
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

(deftest hakukohderyhma-delete-service-test
  (testing "Hakukohderyhmän poistaminen onnistuu, kun hakukohderyhmää ei käytetä atarussa"
    (let [service (:hakukohderyhma-service @test-system)
          session hakukohderyhma-test-fixtures/fake-session
          db (:db @test-system)
          hakukohderyhma-oid "1.2.246.562.28.4"
          expected api-schemas/StatusDeleted]
      (dispatch-mock {:method   :post
                      :path     "/kouta-internal/hakukohde/findbyoids?tarjoaja=1.2.246.562.10.00000000001"
                      :service  :kouta-service
                      :request  ["1.2.3.4.5.6.7.8.9.10"]
                      :response kouta-test-fixtures/kouta-hakukohteet-response-for-delete})
      (dispatch-mock {:method   :post
                      :path     "/organisaatio-service/rest/organisaatio/v4/findbyoids"
                      :service  :organisaatio-service
                      :request  ["1.2.246.562.28.1"]
                      :response [test-fixtures/organisaatio-1]})
      (dispatch-mock {:method   :delete
                      :path     "/organisaatio-service/rest/organisaatio/v4/1.2.246.562.28.4"
                      :service  :organisaatio-service
                      :response organisaatio-test-fixtures/organisaatio-delete-response})
      (dispatch-mock {:method   :get
                      :path     "/lomake-editori/api/forms?hakukohderyhma-oid=1.2.246.562.28.4"
                      :service  :ataru-service
                      :request  nil
                      :response ataru-test-fixtures/empty-form-response})
      (test-fixtures/add-row! db hakukohderyhma-oid "1.2.3.4.5.6.7.8.9.10")

      (is (= (hakukohderyhma-protocol/delete service session hakukohderyhma-oid) expected))
      (is (not (test-fixtures/has-row? db hakukohderyhma-oid)))))

  (testing "Hakukohderyhmän poistaminen epäonnistuu, kun hakukohderyhmää on käytössä atarussa"
    (let [service (:hakukohderyhma-service @test-system)
          session hakukohderyhma-test-fixtures/fake-session
          db (:db @test-system)
          hakukohderyhma-oid "1.2.246.562.28.4"
          expected api-schemas/StatusInUse]
      (dispatch-mock {:method :delete
                      :path   "/organisaatio-service/rest/organisaatio/v4/1.2.246.562.28.4"
                      :service :organisaatio-service
                      :response organisaatio-test-fixtures/organisaatio-delete-response})
      (dispatch-mock {:method   :get
                      :path     "/lomake-editori/api/forms?hakukohderyhma-oid=1.2.246.562.28.4"
                      :service  :ataru-service
                      :request  nil
                      :response ataru-test-fixtures/single-form-response})
      (test-fixtures/add-row! db hakukohderyhma-oid "1.2.3.4.5.6.7.8.9.10")

      (is (= (hakukohderyhma-protocol/delete service session hakukohderyhma-oid) expected))
      (is (test-fixtures/has-row? db hakukohderyhma-oid)))))
