(ns hakukohderyhmapalvelu.http
  (:require [cheshire.core :as json]
            [clj-http.client :as http]
            [hakukohderyhmapalvelu.caller-id :as caller-id]
            [hakukohderyhmapalvelu.config :as c]
            [schema.core :as s]
            [schema-tools.core :as st]))

(s/defschema HttpMethod
  (s/enum :post
          :get))

(s/defschema HttpValidation
  {:request-schema  s/Any
   :response-schema s/Any})


(s/defn parse-and-validate
  [response :- (st/open-schema {:body s/Str})
   response-schema]
  (as-> response response'
        (:body response')
        (json/parse-string response' true)
        (s/validate response-schema response')))

(s/defn do-request
  [{:keys [body] :as opts} :- (st/open-schema
                                {:url    s/Str
                                 :method HttpMethod})
   {:keys [request-schema]} :- HttpValidation
   config :- c/HakukohderyhmaConfig]
  (when request-schema
    (s/validate request-schema body))
  (let [csrf-value "hakukohderyhmapalvelu"
        caller-id  (-> config :oph-organisaatio-oid caller-id/make-caller-id)
        opts       (-> opts
                       (assoc :redirect-strategy :none)
                       (assoc :throw-exceptions false)
                       (update :body json/generate-string)
                       (merge {:accept       :json
                               :content-type "application/json"})
                       (update :connection-timeout (fnil identity 60000))
                       (update :socket-timeout (fnil identity 60000))
                       (update :headers merge
                               {"Caller-Id" caller-id
                                "CSRF"      csrf-value})
                       (update :cookies merge {"CSRF" {:value csrf-value :path "/"}}))]
    (http/request opts)))

(defn do-json-request [opts schemas config]
  (let [response        (do-request opts schemas config)
        response-schema (:response-schema schemas)]
    (cond-> response
            (= (:status response) 200)
            (parse-and-validate response-schema))))
