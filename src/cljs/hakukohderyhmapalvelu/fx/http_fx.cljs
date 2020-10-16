(ns hakukohderyhmapalvelu.fx.http-fx
  (:require [cljs.core.async :as async]
            [cljs-http.client :as http]
            [hakukohderyhmapalvelu.urls :as urls]
            [re-frame.core :as re-frame]
            [schema.core :as s])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(s/defschema HttpSpec
  {:http-request-id                  s/Keyword
   :method                           (s/enum :get :post)
   :path                             s/Str
   (s/optional-key :request-schema)  s/Any
   (s/optional-key :response-schema) s/Any
   :response-handler                 [(s/one s/Keyword "handler ID") s/Any]
   :cas?                             s/Bool
   :body                             {s/Any s/Any}})

(re-frame/reg-fx
  :http
  (s/fn http-fx [{:keys [http-request-id
                         method
                         path
                         request-schema
                         response-schema
                         response-handler
                         body]} :- HttpSpec]
    (when request-schema
      (s/validate request-schema body))
    (go
      (let [do-request            (fn do-request []
                                    (let [req-fn (case method
                                                   :get http/get
                                                   :post http/post)]
                                      (req-fn path {:json-params body})))
            do-cas-authentication (fn do-cas-authentication []
                                    (let [url (urls/get-url :cas.service.kouta-internal)]
                                      (http/get url)))
            {body :body
             :as  response} (as-> (async/<! (do-request)) response'
                                  (when (some #{(:status response')} #{302 401})
                                    (async/<! (do-cas-authentication)))
                                  (async/<! (do-request)))]
        (when response-schema
          (s/validate response-schema body))
        (re-frame/dispatch [:http/remove-http-request-id http-request-id])
        (re-frame/dispatch (conj response-handler response))))))
