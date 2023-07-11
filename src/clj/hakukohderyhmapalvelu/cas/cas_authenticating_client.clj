(ns hakukohderyhmapalvelu.cas.cas-authenticating-client
  (:require [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.caller-id :as caller-id]
            [hakukohderyhmapalvelu.cas.cas-authenticating-client-protocol :as cas-authenticating-protocol]
            [hakukohderyhmapalvelu.config :as c]
            [hakukohderyhmapalvelu.oph-url-properties :as url]
            [schema.core :as s]
            [taoensso.timbre :as log]
            [cheshire.core :as json])
  (:import [fi.vm.sade.javautils.nio.cas CasClientBuilder CasClient CasConfig$CasConfigBuilder]
           [org.asynchttpclient RequestBuilder]
           [org.asynchttpclient Response]))


(def retry-auth-codes #{302 401})

(defn- process-response [^Response response]
  {:status         (.getStatusCode response)
   :body           (.getResponseBody response)
   :headers        (.getHeaders response)})

(defn- json-request [^String method url data]
  (let [body            (when data (json/generate-string data))
        request-builder (RequestBuilder. method)]
    (doto request-builder
      (.addHeader "Content-Type" "application/json")
      (.setBody ^String body)
      (.setUrl url))
    (.build request-builder)))

(def csrf-token "hakukohderyhmapalvelu")

(defn create-cas-client [config service-url session-cookie-name]
  (let [{username :username
         password :password} config
        cas-url (-> config :cas)
        caller-id (-> config :oph-organisaatio-oid caller-id/make-caller-id)
        cas-config (doto (new CasConfig$CasConfigBuilder username password cas-url service-url csrf-token caller-id "")
                     (.setJsessionName session-cookie-name)
                     (.build))
        cas-client (CasClientBuilder/build cas-config)]
    cas-client))

(defn execute-request-and-validate [^CasClient cas-client method url body response-schema]
  (let [response (-> (.executeAndRetryWithCleanSessionOnStatusCodesBlocking cas-client (json-request method url body) retry-auth-codes)
                     (process-response))
        {response-body   :body
         response-status :status} response]
    (log/info "Got response with status" response-status "from" url ":" response-body)
    (cond
      (= 200 response-status)
      (let [response-json (json/parse-string response-body true)]
        (when response-schema
          (s/validate response-schema response-json))
        response-json)

      :else
      (log/error (str "Error when making " method " request to " url ": " response-status ", " response-body)))))

(defrecord CasAuthenticatingClient [config service ring-session?]
  component/Lifecycle
  (start [this]
    (s/validate c/HakukohderyhmaConfig config)
    (s/validate s/Keyword service)
    (let [{:keys [service-url-property
                  session-cookie-name]} (-> config :cas :services service)
          service-url (url/resolve-url service-url-property config)
          cas-client (create-cas-client config service-url session-cookie-name)]
      (assoc this
        :cas-client cas-client)))

  (stop [this]
    (assoc this
      :cas-client nil))

  cas-authenticating-protocol/CasAuthenticatingClientProtocol
  (post [this {:keys [url body]} {:keys [request-schema response-schema]}]
    (execute-request-and-validate (:cas-client this) "post" url body response-schema))

  (get [this url response-schema]
    (execute-request-and-validate (:cas-client this) "get" url nil response-schema))

  (http-put [this
             {:keys [url body]}
             {:keys [request-schema response-schema]}]
    (execute-request-and-validate (:cas-client this) "put" url body response-schema))

  (delete [this url response-schema]
    (execute-request-and-validate (:cas-client this) "delete" url nil response-schema))
  )
