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
                    :settings     {:rajaava          false
                                   :max-hakukohteet  nil
                                   :priorisoiva false
                                   :prioriteettijarjestys []
                                   :jos-ylioppilastutkinto-ei-muita-pohjakoulutusliitepyyntoja false
                                   :yo-amm-autom-hakukelpoisuus false}
                    :hakukohteet  [{:oid                           "1.2.246.562.20.1"
                                    :nimi                          {:fi "Hakukohde 1"}
                                    :hakuOid                       "1.2.246.562.29.1"
                                    :tarjoaja                      test-fixtures/organisaatio-1
                                    :toinenAsteOnkoKaksoistutkinto false
                                    :oikeusHakukohteeseen          true
                                    :onkoHarkinnanvarainenKoulutus true
                                    :hasValintakoe                 false
                                    :tila                          "julkaistu"}
                                   {:oid                           "1.2.246.562.20.2"
                                    :nimi                          {:fi "Hakukohde 2"}
                                    :hakuOid                       "1.2.246.562.29.1"
                                    :tarjoaja                      test-fixtures/organisaatio-2
                                    :toinenAsteOnkoKaksoistutkinto false
                                    :oikeusHakukohteeseen          true
                                    :onkoHarkinnanvarainenKoulutus true
                                    :hasValintakoe                 false
                                    :tila                          "julkaistu"}]}]
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

(deftest hakukohderyhma-update-settings-service-test
  (testing "Update hakukohderyhma settings"
    (let [service (:hakukohderyhma-service @test-system)
          settings {:rajaava          true
                    :max-hakukohteet 3
                    :priorisoiva false
                    :prioriteettijarjestys []
                    :jos-ylioppilastutkinto-ei-muita-pohjakoulutusliitepyyntoja true
                    :yo-amm-autom-hakukelpoisuus false}
          expected {:rajaava          true
                    :max-hakukohteet 3
                    :priorisoiva false
                    :prioriteettijarjestys []
                    :jos-ylioppilastutkinto-ei-muita-pohjakoulutusliitepyyntoja true
                    :yo-amm-autom-hakukelpoisuus false}]
      (is (= (hakukohderyhma-protocol/insert-or-update-settings
               service hakukohderyhma-test-fixtures/fake-session "1.2.246.562.28.4" settings)
             expected)))))

(deftest hakukohderyhma-fetch-settings-service-test

  (testing "Fetches hakukohderyhma settings returning default"
    (let [service (:hakukohderyhma-service @test-system)
          expected {:rajaava          false
                    :max-hakukohteet  nil
                    :priorisoiva      false
                    :prioriteettijarjestys []
                    :jos-ylioppilastutkinto-ei-muita-pohjakoulutusliitepyyntoja false
                    :yo-amm-autom-hakukelpoisuus false}]
      (is (= (hakukohderyhma-protocol/get-settings
               service hakukohderyhma-test-fixtures/fake-session "1.2.246.562.28.3")
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
      (is (test-fixtures/has-row? db hakukohderyhma-oid))))

  (testing "Hakukohderyhymien ryhmittely hakukohteiden mukaan"
    (let [hk-1-oid "1.2.246.562.20.1"
          hk-2-oid "1.2.246.562.20.2"
          hkr-1-oid "1.2.246.562.28.001"
          hkr-2-oid "1.2.246.562.28.002"
          hkr-3-oid "1.2.246.562.28.003"
          service (:hakukohderyhma-service @test-system)
          session hakukohderyhma-test-fixtures/fake-session
          db (:db @test-system)
          expected #{{:oid hk-1-oid :hakukohderyhmat #{hkr-1-oid hkr-2-oid}}
                     {:oid hk-2-oid :hakukohderyhmat #{hkr-3-oid}}}]

      (test-fixtures/add-row! db hkr-1-oid hk-1-oid)
      (test-fixtures/add-row! db hkr-2-oid hk-1-oid)
      (test-fixtures/add-row! db hkr-3-oid hk-2-oid)

      (->> (hakukohderyhma-protocol/get-hakukohderyhmat-by-hakukohteet service session [hk-1-oid hk-2-oid])
           (map #(update % :hakukohderyhmat set))
           set
           (= expected)
           is)))

  (testing "Hakukohdeoidien haku hakukohderyhmällä"
    (let [hk-1-oid "1.2.246.562.20.1123"
          hk-2-oid "1.2.246.562.20.2123"
          hkr-oid "1.2.246.562.28.00001"
          service (:hakukohderyhma-service @test-system)
          db (:db @test-system)
          expected [hk-1-oid hk-2-oid]]

      (test-fixtures/add-row! db hkr-oid hk-1-oid)
      (test-fixtures/add-row! db hkr-oid hk-2-oid)

      (->> (hakukohderyhma-protocol/get-hakukohde-oids-for-hakukohderyhma-oid service hkr-oid)
           (sort)
           (= expected)
           is)))

  (testing "Hakukohderyhymien ryhmittely hakukohteiden mukaan, ei hakukohteita"
    (let [service (:hakukohderyhma-service @test-system)
          session hakukohderyhma-test-fixtures/fake-session]

      (-> (hakukohderyhma-protocol/get-hakukohderyhmat-by-hakukohteet service session [])
          empty?
          (is "Hakukohteita ei pitäisi palautua")))))
