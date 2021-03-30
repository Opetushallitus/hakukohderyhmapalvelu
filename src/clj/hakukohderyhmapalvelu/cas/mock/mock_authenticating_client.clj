(ns hakukohderyhmapalvelu.cas.mock.mock-authenticating-client
  (:require [cheshire.core :as json]
            [clojure.string :as string]
            [hakukohderyhmapalvelu.cas.cas-authenticating-client-protocol :as cas-protocol]))

(defn- validate-method [expected-method actual-method]
  (when-not (= actual-method expected-method)
    (format "HTTP-kutsun metodi oli väärä (oli: %s, vaadittiin: %s)" actual-method expected-method)))

(defn- validate-path [expected-path actual-path]
  (when-not (string/ends-with? actual-path expected-path)
    (format "HTTP-kutsun polku oli väärä (koko osoite oli: %s, vaadittiin päättyvän: %s" actual-path expected-path)))

(defn- validate-body [expected-body actual-body]
  (when-not (= expected-body actual-body)
    (format "HTTP-kutsun sanoma oli väärä\n\n\tvaadittiin:\n\n%s\n\n\toli:\n\n%s" expected-body actual-body)))

(defn- validate ([response url method]
                 (validate response url method nil))
  ([response url method body]
   (if-let [{expected-method :method
             expected-path   :path
             expected-body   :request
             mock-response   :response} response]
     (let [method-errors (validate-method expected-method method)
           path-errors (validate-path expected-path url)
           body-errors (when body (validate-body expected-body body))
           errors (remove nil? [method-errors path-errors body-errors])]
       (if (empty? errors)
         {:status 200 :body (json/generate-string mock-response)}
         (throw (Exception. (format "Hakukohderyhmäpalvelun taustajärjestelmä yritti tehdä määritysten vastaisen HTTP-kutsun:\n\n%s" (clojure.string/join "\n" errors))))))
     (throw (Exception. (format "Hakukohderyhmäpalvelun taustajärjestelmä yritti lähettää määrittämättömän HTTP-kutsun osoitteeseen %s datalla %s" url body))))))


(defrecord MockedCasClient [request-map]
  cas-protocol/CasAuthenticatingClientProtocol

  (get [_ url _]
    (-> (get-in @request-map [:get url])
        (validate url :get)))

  (post [_ {:keys [url body]} _]
    (-> (get-in @request-map [:post url (hash body)])
        (validate url :post body)))

  (http-put [_ {:keys [url body]} _]
    (-> (get-in @request-map [:put url (hash body)])
        (validate url :put body))))
