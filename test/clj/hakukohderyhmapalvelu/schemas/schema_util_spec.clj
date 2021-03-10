(ns hakukohderyhmapalvelu.schemas.schema-util-spec
  (:require [clojure.test :refer [deftest testing is]]
            [hakukohderyhmapalvelu.schemas.schema-util :as schema-util])
  (:import [java.time LocalDateTime]))


(deftest schema-util-test
  (testing "Should parse into LocalDateTime"
    (is (= (LocalDateTime/of 2020 8 10 5 12 32)
           (schema-util/parse-local-date-time "2020-08-10T05:12:32"))))
  (testing "Should not parse"
    (is (= "this-should-not-parse"
           (schema-util/parse-local-date-time "this-should-not-parse")))))
