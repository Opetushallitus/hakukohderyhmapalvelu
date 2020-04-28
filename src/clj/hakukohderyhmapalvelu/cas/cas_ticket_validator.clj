(ns hakukohderyhmapalvelu.cas.cas-ticket-validator
  (:require [clojure.xml :as xml]
            [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.cas.cas-client :as cas-client]
            [hakukohderyhmapalvelu.oph-url-properties :as url]
            [taoensso.timbre :as log])
  (:import [java.io ByteArrayInputStream]))


(defprotocol CasTicketClientProtocol
  (validate-service-ticket [this ticket]))

(defn- check-result [parse-result]
  (if (and (some? parse-result) (> (.length parse-result) 0))
    parse-result
    (throw (IllegalArgumentException. (str "Ei pystytty jäsentämään käyttäjätunnusta CASin vastauksesta")))))

(defn- parse-username [^String xml-response]
  (try
    (-> xml-response
        (.getBytes)
        (ByteArrayInputStream.)
        (xml/parse)
        (:content)
        (first)
        (:content)
        (first)
        (:content)
        (first)
        (check-result))
    (catch Exception e
      (log/error e (str "Ongelma käsiteltäessä CASin ticket-validoinnin vastausta '" xml-response "'"))
      (throw e))))

(defn- assert-ok-response [response]
  (when (not= 200 (:status response))
    (throw (RuntimeException. (str "Saatiin ei-OK-vastaus CASilta: " response))))
  response)

(defrecord CasTicketClient [config]
  component/Lifecycle
  (start [this]
    (let [service-parameter (url/resolve-url :hakukohderyhmapalvelu.login-success config)]
      (assoc this :service-parameter service-parameter)))

  (stop [this]
    (assoc this :service-parameter nil))

  CasTicketClientProtocol
  (validate-service-ticket [this ticket]
    (-> (cas-client/do-request {:method :get
                                :url    (url/resolve-url :cas.validate-service-ticket config {"ticket"  ticket
                                                                                              "service" (:service-parameter this)})
                                :body   {}}
                               {:request-schema  {}
                                :response-schema {}}
                               config)
        (assert-ok-response)
        (:body)
        (parse-username))))
