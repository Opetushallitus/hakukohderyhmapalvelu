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
    (let [expected [{:oid  "1.4.1.1"
                     :nimi {:fi "Organisaatio 1"}}
                    {:oid  "1.4.1.2"
                     :nimi {:fi "Organisaatio 2"}}]]
      (dispatch-mock {:method   :post
                      :path     "http://localhost/organisaatio-service/rest/organisaatio/v4/findbyoids"
                      :service  :organisaatio-service
                      :request  ["1.4.1.1" "1.4.1.2"]
                      :response organisaatio-test-fixtures/organisaatiot-response})
      (is (= expected (organisaatio-protocol/find-by-oids (:organisaatio-service @test-system) ["1.4.1.1" "1.4.1.2"]))))))
