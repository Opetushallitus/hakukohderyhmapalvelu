(ns hakukohderyhmapalvelu.cas.mock.mock-authenticating-client-schemas
  (:require [schema.core :as s]))

(s/defschema MockCasAuthenticatingClientRequest
  {:method                   (s/enum :post :get :put)
   :path                     s/Str
   :service                  (s/enum :organisaatio-service :kouta-service)
   (s/optional-key :request) s/Any
   :response                 s/Any})
