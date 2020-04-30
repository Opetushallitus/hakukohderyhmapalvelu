(ns hakukohderyhmapalvelu.cas.mock.mock-authenticating-client-schemas
  (:require [schema.core :as s]))

(s/defschema MockCasAuthenticatingClientRequest
  {:method   (s/enum :post)
   :path     s/Str
   :service  (s/enum :organisaatio-service)
   :request  s/Any
   :response s/Any})
