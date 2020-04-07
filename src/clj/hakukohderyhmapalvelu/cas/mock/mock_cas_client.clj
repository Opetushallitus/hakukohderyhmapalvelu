(ns hakukohderyhmapalvelu.cas.mock.mock-cas-client
  (:require [clojure.core.async :as async]
            [clojure.string :as string]
            [hakukohderyhmapalvelu.cas.cas-protocol :as cas-protocol]))

(defn- validate-method [expected-method actual-method]
  (when-not (= actual-method expected-method)
    (format "HTTP-kutsun metodi oli väärä (oli: %s, vaadittiin: %s)" actual-method expected-method)))

(defn- validate-path [expected-path actual-url]
  (when-not (string/ends-with? actual-url expected-path)
    (format "HTTP-kutsun polku oli väärä (koko osoite oli: %s, vaadittiin päättyvän: %s" actual-url expected-path)))

(defn- validate-body [expected-body actual-body]
  (when-not (= expected-body actual-body)
    (format "HTTP-kutsun sanoma oli väärä\n\n\tvaadittiin:\n\n%s\n\n\toli:\n\n%s" expected-body actual-body)))

(defrecord MockedCasClient [chan]
  cas-protocol/CasClientProtocol

  (post [this
         {actual-url  :url
          actual-body :body}
         _]
    (if-let [{expected-method :method
              expected-path   :path
              expected-body   :request
              mock-response   :response} (async/poll! chan)]
      (let [errors (filter
                     some?
                     (conj []
                           (validate-method expected-method :post)
                           (validate-path expected-path actual-url)
                           (validate-body expected-body actual-body)))]
        (if (seq errors)
          (throw (Exception. (format "Hakukohderyhmäpalvelun taustajärjestelmä yritti tehdä määritysten vastaisen HTTP-kutsun:\n\n%s" (clojure.string/join "\n" errors))))
          mock-response))
      (throw (Exception. (format "Hakukohderyhmäpalvelun taustajärjestelmä yritti lähettää määrittämättömän HTTP-kutsun osoitteeseen %s datalla %s" actual-url actual-body))))))
