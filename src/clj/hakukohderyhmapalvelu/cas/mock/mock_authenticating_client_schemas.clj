(ns hakukohderyhmapalvelu.cas.mock.mock-authenticating-client-schemas
  (:require [schema.core :as s]))

(s/defschema MockCasAuthenticatingClientRequest
  {:method                   (s/enum :post :get :put :delete)
   :path                     s/Str
   :service                  (s/enum :organisaatio-service :kouta-service :ataru-service)
   (s/optional-key :request) s/Any
   :response                 s/Any})
