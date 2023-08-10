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

(defn execute-json-request [^CasClient cas-client ^String method url body]
  (let [request (json-request method url body)
        response (-> (.executeAndRetryWithCleanSessionOnStatusCodesBlocking cas-client request retry-auth-codes)
                         (process-response))]
    (log/info method "Got response with status for" method "to" url ":" (:status response))
    response))

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
  (post [this {:keys [url body]}]
    (log/info "POST" url)
    (execute-json-request (:cas-client this) "POST" url body))

  (http-get [this url]
    (log/info "GET" url)
    (execute-json-request (:cas-client this) "GET" url nil))

  (http-put [this {:keys [url body]}]
    (log/info "PUT" url)
    (execute-json-request (:cas-client this) "PUT" url body))

  (delete [this url]
    (log/info "DELETE" url)
    (execute-json-request (:cas-client this) "DELETE" url nil)))
