(ns hakukohderyhmapalvelu.cas.cas-authenticating-client
  (:require [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.caller-id :as caller-id]
            [hakukohderyhmapalvelu.cas.cas-authenticating-client-protocol :as cas-authenticating-protocol]
            [hakukohderyhmapalvelu.config :as c]
            [hakukohderyhmapalvelu.http :as http]
            [hakukohderyhmapalvelu.oph-url-properties :as url]
            [schema.core :as s]
            [taoensso.timbre :as log])
  (:import [fi.vm.sade.javautils.cas CasSession ApplicationSession SessionToken]
           [java.net.http HttpClient]
           [java.time Duration]
           [java.net CookieManager URI]))

(def auth-fail-status #{302 401})
(def error-status #{500 400})

(defn- invalidate-cas-session [^ApplicationSession application-session
                               ^SessionToken session-token]
  (when session-token
    (.invalidateSession application-session session-token)))

(defn- init-cas-session [^ApplicationSession application-session]
  (-> application-session
      .getSessionToken
      .get))

(s/defschema PostOrPutOpts
  {:url  s/Str
   :body s/Any})

(defn retry-with-session-refresh [application-session session-token request-fn]
  (invalidate-cas-session application-session session-token)
  (let [new-session-token (init-cas-session application-session)]
    (request-fn new-session-token)))

(s/defn do-authenticated-json-request
  [{:keys [method
           body
           session-token
           url]} :- {:method                http/HttpMethod
                     :session-token         SessionToken
                     :url                   s/Str
                     (s/optional-key :body) s/Any}
   schemas :- http/HttpValidation
   config :- c/HakukohderyhmaConfig]
  (let [cookie (.cookie session-token)]
    (http/do-request {:method  method
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
                      :method                http/HttpMethod
                      (s/optional-key :body) s/Any}
   schemas :- http/HttpValidation
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
                             (do-authenticated-json-request
                               schemas
                               config)))
        response       (request-fn session-token)
        status (:status response)]
    (when (error-status status)
      (log/error (str "CAS-authenticated request failed with status " status " on url " url)))
    (cond
      (auth-fail-status status) (retry-with-session-refresh application-session session-token request-fn)
      :else response)))

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
      (s/validate PostOrPutOpts opts)
      (do-cas-authenticated-request {:application-session (:application-session this)
                                     :method              :post
                                     :url                 url
                                     :body                body}
                                    schemas
                                    config))

  (http-put [this
        {:keys [url body] :as opts}
        schemas]
    (s/validate PostOrPutOpts opts)
    (do-cas-authenticated-request {:application-session (:application-session this)
                                   :method              :put
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
                                  config))

  (delete [this url response-schema]
    (do-cas-authenticated-request {:application-session (:application-session this)
                                   :method              :delete
                                   :url                 url}
                                  {:request-schema  nil
                                   :response-schema response-schema}
                                  config)))
