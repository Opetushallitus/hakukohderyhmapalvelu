(ns hakukohderyhmapalvelu.fx.http-fx
  (:require [cljs.core.async :as async]
            [cljs.core.async.interop]
            [hakukohderyhmapalvelu.config :as c]
            [hakukohderyhmapalvelu.urls :as urls]
            [re-frame.core :as re-frame]
            [schema.core :as s])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [cljs.core.async.interop :refer [<p!]])
  (:import [goog.net.XhrIo]))

(defn- fetch [{:keys [url
                      method
                      redirect?
                      body]}]
  (let [method'   (case method
                    :get "GET"
                    :post "POST")
        caller-id (:caller-id c/config)
        redirect  (if redirect?
                    "follow"
                    "error")]
    (go
      (let [response        (<p! (js/fetch
                                   url
                                   (clj->js (cond-> {:method   method'
                                                     :headers  {"caller-id"    caller-id
                                                                "content-type" "application/json"}
                                                     :redirect redirect}
                                                    (seq body)
                                                    (assoc
                                                      :body
                                                      (->> body clj->js (.stringify js/JSON)))))))
            status          (.-status response)
            redirected?     (.-redirected response)
            response-common {:status      status
                             :redirected? redirected?}]
        (try
          (let [body (<p! (.json response))]
            (assoc response-common
                   :body
                   (js->clj body :keywordize-keys true)))
          (catch js/Error _
            response-common))))))



(s/defschema HttpSpec
  {:http-request-id                  s/Keyword
   :method                           (s/enum :get :post)
   :path                             s/Str
   (s/optional-key :request-schema)  s/Any
   (s/optional-key :response-schema) s/Any
   :response-handler                 [(s/one s/Keyword "handler ID") s/Any]
   (s/optional-key :cas)             s/Keyword
   (s/optional-key :body)            {s/Any s/Any}})

(re-frame/reg-fx
  :http
  (s/fn http-fx [{:keys [http-request-id
                         method
                         path
                         request-schema
                         response-schema
                         response-handler
                         cas
                         body]} :- HttpSpec]
    (when request-schema
      (s/validate request-schema body))
    (go
      (let [do-request            (fn do-request []
                                    (fetch (cond-> {:url       path
                                                    :method    method
                                                    :redirect? true}
                                                   (seq body)
                                                   (assoc :body body))))
            do-cas-authentication (fn do-cas-authentication []
                                    (let [url (urls/get-url cas)]
                                      (fetch {:url       url
                                              :method    :get
                                              :redirect? true})))
            {body :body} (let [response' (async/<! (do-request))]
                           (cond (and (:redirected? response')
                                      (not= method :get))
                                 (async/<! (do-request))

                                 (and cas
                                      (some #{(:status response')} #{401}))
                                 (do
                                   (async/<! (do-cas-authentication))
                                   (async/<! (do-request)))

                                 :else
                                 response'))]
        (when response-schema
          (s/validate response-schema body))
        (re-frame/dispatch [:http/remove-http-request-id http-request-id])
        (re-frame/dispatch (conj response-handler body))))))
