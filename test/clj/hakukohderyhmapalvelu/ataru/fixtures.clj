(ns hakukohderyhmapalvelu.ataru.fixtures)

(def forms-response
  {:forms [{:deleted      nil
            :key          "ae73b23d-7cf6-44e7-b225-7a1aa620349d"
            :content      []
            :name         {:fi "Testihakulomake"
                           :en "Testapplicationform"}
            :created-by   "user-1"
            :locked       nil
            :id           123
            :created-time "2020-01-01T01:01:01.007Z"
            :languages    ["fi" "en"]}
           {:deleted      nil
            :key          "b880cfbb-a4e9-4500-9eff-37d102bd0baf"
            :content      []
            :name         {:fi "Testihakulomake 2"}
            :created-by   "user-2"
            :locked       nil
            :id           124
            :created-time "2021-01-01T01:01:01.007Z"
            :languages    ["fi"]}]})


(def single-form-response
  {:forms [{:deleted      nil
            :key          "ae73b23d-7cf6-44e7-b225-7a1aa620349d"
            :content      []
            :name         {:fi "Testihakulomake"
                           :en "Testapplicationform"}
            :created-by   "user-1"
            :locked       nil
            :id           123
            :created-time "2020-01-01T01:01:01.007Z"
            :languages    ["fi" "en"]}]})

(def empty-form-response
  {:forms []})
