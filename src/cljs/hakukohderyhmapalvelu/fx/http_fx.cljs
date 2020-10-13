(ns hakukohderyhmapalvelu.fx.http-fx
  (:require [cljs.core.async :as async]
            [cljs-http.client :as http]
            [re-frame.core :as re-frame]
            [schema.core :as s])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(s/defschema HttpSpec
  {:http-request-id  s/Keyword
   :method           (s/enum :post)
   :path             s/Str
   :request-schema   s/Any
   :response-schema  s/Any
   :response-handler [(s/one s/Keyword "handler ID") s/Any]
   :body             {s/Any s/Any}})

(re-frame/reg-fx
  :http
  (s/fn [{:keys [http-request-id
                 method
                 path
                 request-schema
                 response-schema
                 response-handler
                 body]} :- HttpSpec]
    (s/validate request-schema body)
    (go
      (let [do-request (case method
                         :post http/post)
            response   (async/<!
                         (do-request path {:json-params body}))]
        (->> response :body (s/validate response-schema))
        (re-frame/dispatch [:http/remove-http-request-id http-request-id])
        (re-frame/dispatch (conj response-handler response))))))
