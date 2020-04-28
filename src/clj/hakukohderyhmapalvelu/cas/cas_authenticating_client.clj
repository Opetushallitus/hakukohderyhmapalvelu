(ns hakukohderyhmapalvelu.cas.cas-authenticating-client
  (:require [cheshire.core :as json]
            [clj-http.client :as http]
            [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.caller-id :as caller-id]
            [hakukohderyhmapalvelu.cas.cas-authenticating-client-protocol :as cas-authenticating-protocol]
            [hakukohderyhmapalvelu.config :as c]
            [hakukohderyhmapalvelu.oph-url-properties :as url]
            [schema.core :as s]
            [schema-tools.core :as st])
  (:import [fi.vm.sade.javautils.cas CasSession ApplicationSession SessionToken]
           [java.net.http HttpClient]
           [java.time Duration]
           [java.net CookieManager URI]))

(defn- invalidate-cas-session [^ApplicationSession application-session
                               ^SessionToken session-token]
  (when session-token
    (.invalidateSession application-session session-token)))

(defn- init-cas-session [^ApplicationSession application-session]
  (-> application-session
      .getSessionToken
      .get))

(s/defschema HttpMethod
  (s/enum :post
          :get))

(s/defschema HttpValidation
  {:request-schema  s/Any
   :response-schema s/Any})

(s/defschema PostOpts
  {:url  s/Str
   :body s/Any})

(s/defn parse-and-validate
  [response :- (st/open-schema {:body s/Str})
   response-schema]
  (as-> response response'
        (:body response')
        (json/parse-string response' true)
        (s/validate response-schema response')))

; TODO: Siirrä http-namespaceen
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

(s/defn do-request-and-validate
  [{:keys [method
           body
           session-token
           url]} :- {:method                HttpMethod
                     :session-token         SessionToken
                     :url                   s/Str
                     (s/optional-key :body) s/Any}
   schemas :- HttpValidation
   config :- c/HakukohderyhmaConfig]
  (let [cookie (.cookie session-token)]
    (do-json-request {:method  method
                      :url     url
                      :body    body
                      :cookies {(.getName cookie) {:path  (.getPath cookie)
                                                   :value (.getValue cookie)}}}
                     schemas
                     config)))

(s/defn do-cas-authenticated-request
  [{:keys [application-session
           method
           url
           body]} :- {:application-session   ApplicationSession
                      :url                   s/Str
                      :method                HttpMethod
                      (s/optional-key :body) s/Any}
   schemas :- HttpValidation
   config :- c/HakukohderyhmaConfig]
  (let [session-token  (some-> application-session
                               .getSessionToken
                               .get)
        request-params (cond-> {:method method
                                :url    url}
                               (some? body)
                               (assoc :body body))
        request-fn     (fn [session-token']
                         (-> request-params
                             (assoc :session-token session-token')
                             (do-request-and-validate
                               schemas
                               config)))
        response       (request-fn session-token)]
    (if (some #{(:status response)} [302 401])
      (do
        (invalidate-cas-session application-session session-token)
        (let [new-session-token (init-cas-session application-session)]
          (request-fn new-session-token)))
      response)))

(defn- create-uri [url-key config]
  (s/validate c/HakukohderyhmaConfig config)
  (-> (url/resolve-url url-key config)
      (URI/create)))

(defrecord CasAuthenticatingClient [config service]
  component/Lifecycle
  (start [this]
    (s/validate c/HakukohderyhmaConfig config)
    (s/validate s/Keyword service)
    (let [{:keys [service-url-property
                  session-cookie-name]} (-> config :cas :services service)
          caller-id           (-> config :oph-organisaatio-oid caller-id/make-caller-id)
          cookie-manager      (CookieManager.)
          http-client         (-> (HttpClient/newBuilder)
                                  (.cookieHandler cookie-manager)
                                  (.connectTimeout (Duration/ofSeconds 10))
                                  (.build))
          cas-tickets-url     (create-uri :cas.tickets config)
          {:keys [username
                  password]} (-> config :cas)
          application-session (ApplicationSession. http-client
                                                   cookie-manager
                                                   caller-id
                                                   (Duration/ofSeconds 10)
                                                   (CasSession. http-client
                                                                (Duration/ofSeconds 10)
                                                                caller-id
                                                                cas-tickets-url
                                                                username
                                                                password)
                                                   (url/resolve-url service-url-property
                                                                    config)
                                                   session-cookie-name)]
      (assoc this
        :application-session application-session)))

  (stop [this]
    (assoc this
      :application-session nil))

  cas-authenticating-protocol/CasAuthenticatingClientProtocol

  (post [this
           {:keys [url body] :as opts}
           schemas]
      (s/validate PostOpts opts)
      (do-cas-authenticated-request {:application-session (:application-session this)
                                     :method              :post
                                     :url                 url
                                     :body                body}
                                    schemas
                                    config))

  (get [this url response-schema]
    (do-cas-authenticated-request {:application-session (:application-session this)
                                   :method              :get
                                   :url                 url}
                                  {:request-schema  nil
                                   :response-schema response-schema}
                                  config)))