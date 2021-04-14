(ns hakukohderyhmapalvelu.ataru.ataru-service-spec
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [hakukohderyhmapalvelu.ataru.ataru-protocol :as ataru-protocol]
            [hakukohderyhmapalvelu.ataru.fixtures :as ataru-test-fixtures]
            [hakukohderyhmapalvelu.test-fixtures :refer [with-mock-system dispatch-mock test-system]]))

(use-fixtures :once with-mock-system)

(deftest ataru-service-test
  (testing "Lomakkeiden hakeminen onnistuu ilman hakukohderyhmää"
    (let [service (:ataru-service @test-system)
          expected (:forms ataru-test-fixtures/forms-response)]
      (dispatch-mock {:method   :get
                      :path     "/lomake-editori/api/forms"
                      :service  :ataru-service
                      :request  nil
                      :response ataru-test-fixtures/forms-response})
      (is (= expected (ataru-protocol/get-forms service nil)))))
  (testing "Lomakkeiden hakeminen onnistuu hakukohderyhmällä"
    (let [service (:ataru-service @test-system)
          expected (:forms ataru-test-fixtures/single-form-response)]
      (dispatch-mock {:method   :get
                      :path     "/lomake-editori/api/forms?hakukohderyhma-oid=1.2.3.4.5.6"
                      :service  :ataru-service
                      :request  nil
                      :response ataru-test-fixtures/single-form-response})
      (is (= expected (ataru-protocol/get-forms service "1.2.3.4.5.6")))))
  (testing "Lomakkeiden hakeminen epäonnistuu"
    (let [service (:ataru-service @test-system)]
      (dispatch-mock {:method   :get
                      :path     "/lomake-editori/api/forms?hakukohderyhma-oid=1.2.3.4.5.7"
                      :service  :ataru-service
                      :request  nil
                      :response nil})
      (is (thrown? Exception (ataru-protocol/get-forms service "1.2.3.4.5.7"))))))
