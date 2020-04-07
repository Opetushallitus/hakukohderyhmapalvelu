(ns hakukohderyhmapalvelu.cas.mock.mock-cas-client-schemas
  (:require [schema.core :as s]))

(s/defschema MockCasClientRequest
  {:method   (s/enum :post)
   :path     s/Str
   :service  (s/enum :organisaatio-service)
   :request  s/Any
   :response s/Any})
