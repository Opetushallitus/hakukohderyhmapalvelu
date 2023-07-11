(ns hakukohderyhmapalvelu.cas.cas-authenticating-client
  (:require [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.cas.cas-authenticating-client-protocol :as cas-authenticating-protocol]
            [hakukohderyhmapalvelu.config :as c]
            [hakukohderyhmapalvelu.oph-url-properties :as url]
            [schema.core :as s]
            [taoensso.timbre :as log]
            [cheshire.core :as json]
            [hakukohderyhmapalvelu.cas.cas-utils :as cas-utils])
  (:import [fi.vm.sade.javautils.nio.cas CasClient]
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

(defrecord CasAuthenticatingClient [config service]
  component/Lifecycle
  (start [this]
    (s/validate c/HakukohderyhmaConfig config)
    (s/validate s/Keyword service)
    (let [{:keys [service-url-property
                  session-cookie-name]} (-> config :cas :services service)
          service-url (url/resolve-url service-url-property config)
          cas-client (cas-utils/create-cas-client config service-url session-cookie-name)]
      (assoc this
        :cas-client cas-client)))

  (stop [this]
    (assoc this
      :cas-client nil))

  cas-authenticating-protocol/CasAuthenticatingClientProtocol
  (post [this {:keys [url body]} {:keys [request-schema response-schema]}]
    (execute-request-and-validate (:cas-client this) "post" url body response-schema))

  (get [this url {:keys [request-schema response-schema]}]
    (execute-request-and-validate (:cas-client this) "get" url nil response-schema))

  (http-put [this
             {:keys [url body]}
             {:keys [request-schema response-schema]}]
    (execute-request-and-validate (:cas-client this) "put" url body response-schema))

  (delete [this url {:keys [request-schema response-schema]}]
    (execute-request-and-validate (:cas-client this) "delete" url nil response-schema)))
