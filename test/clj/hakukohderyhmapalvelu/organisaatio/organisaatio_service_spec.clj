(ns hakukohderyhmapalvelu.organisaatio.organisaatio-service-spec
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [hakukohderyhmapalvelu.organisaatio.organisaatio-protocol :as organisaatio-protocol]
            [hakukohderyhmapalvelu.organisaatio.fixtures :as organisaatio-test-fixtures]
            [hakukohderyhmapalvelu.test-fixtures :as test-fixtures :refer [dispatch-mock test-system]]))


(use-fixtures :once test-fixtures/with-mock-system)

(deftest organisaatio-service-test
  (testing "Organisaatioiden hakeminen oideilla, tyhjä hakuvektori"
    (is (empty? (organisaatio-protocol/find-by-oids (:organisaatio-service @test-system) []))))
  (testing "Organisaatioiden hakeminen oideilla"
    (let [expected [{:oid  "1.2.246.562.28.1"
                     :nimi {:fi "Organisaatio 1"}}
                    {:oid  "1.2.246.562.28.2"
                     :nimi {:fi "Organisaatio 2"}}]]
      (dispatch-mock {:method   :post
                      :path     "/organisaatio-service/rest/organisaatio/v4/findbyoids"
                      :service  :organisaatio-service
                      :request  ["1.2.246.562.28.1" "1.2.246.562.28.2"]
                      :response organisaatio-test-fixtures/organisaatiot-response})
      (is (= expected (organisaatio-protocol/find-by-oids (:organisaatio-service @test-system) ["1.2.246.562.28.1" "1.2.246.562.28.2"])))))
  (testing "Yhden organisaation hakeminen"
    (let [expected {:oid  "1.2.246.562.28.1"
                    :nimi {:fi "Organisaatio 1"}}]
      (dispatch-mock {:method   :get
                      :path     "/organisaatio-service/rest/organisaatio/v4/1.2.246.562.28.1"
                      :service  :organisaatio-service
                      :response organisaatio-test-fixtures/organisaatio-response})
      (is (= expected (organisaatio-protocol/get-organisaatio (:organisaatio-service @test-system) "1.2.246.562.28.1"))))))
