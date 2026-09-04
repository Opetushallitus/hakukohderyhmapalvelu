(ns hakukohderyhmapalvelu.server-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [clj-http.client :as http]
            ;; health-check importaa hakukohderyhmapalvelu.db.DbPool-recordin mutta ei requireä db:tä
            ;; -> ladataan se ensin, muuten server-vaatimusketju kaatuu ilman AOT:ta
            [hakukohderyhmapalvelu.db]
            [hakukohderyhmapalvelu.server :as server]
            [ring.adapter.jetty :as jetty])
  (:import [java.net InetSocketAddress Socket]))

(defn- test-handler [{:keys [uri]}]
  (if (= uri "/boom")
    (throw (ex-info "kaboom - salainen sisainen viesti" {}))
    {:status 200 :headers {"Content-Type" "text/plain"} :body "ok"}))

(defn- start-server []
  ;; sama configurator + :send-server-version? kuin server/HttpServer.start
  (jetty/run-jetty test-handler
                   {:port                 0
                    :join?                false
                    :send-server-version? false
                    :configurator         #'server/attach-error-handler!}))

(defn- local-port [^org.eclipse.jetty.server.Server srv]
  (.getLocalPort ^org.eclipse.jetty.server.ServerConnector (first (.getConnectors srv))))

(defn- raw-request
  "Kirjoittaa raakatavut soketille ja palauttaa vastauksen merkkijonona (myos
   epakelvot pyynnot, joita http-clientit eivat suostu lahettamaan)."
  [port ^String request]
  (with-open [sock (doto (Socket.)
                     (.connect (InetSocketAddress. "127.0.0.1" (int port)) 2000))]
    (.setSoTimeout sock 2000)
    (doto (.getOutputStream sock)
      (.write (.getBytes request "US-ASCII"))
      (.flush))
    (let [in  (.getInputStream sock)
          sb  (StringBuilder.)
          buf (byte-array 4096)]
      (try
        (loop []
          (let [n (.read in buf)]
            (when (pos? n)
              (.append sb (String. buf 0 n "ISO-8859-1"))
              (recur))))
        (catch java.net.SocketTimeoutException _ nil))
      (.toString sb))))

(deftest error-handler-ei-vuoda-jetty-versiota
  (let [srv  (start-server)
        port (local-port srv)]
    (try
      (testing "sovellus-500 vastaa siistin virhesivun, ei stacktracea eika Jetty-versiota"
        (let [{:keys [status body]} (http/get (str "http://127.0.0.1:" port "/boom")
                                              {:throw-exceptions false})]
          (is (= 500 status))
          (is (= "Internal server error\n" body))
          (is (not (re-find #"(?i)kaboom|salainen|Exception|\.clj:|jetty" body)))))

      (testing "normaali vastaus ei sisalla Server: Jetty -headeria"
        (let [{:keys [headers]} (http/get (str "http://127.0.0.1:" port "/")
                                          {:throw-exceptions false})]
          (is (not (some-> (get headers "Server") (str/includes? "Jetty"))))))

      (testing "connection-level-virhe (kelvoton HTTP-versio) ei vuoda Jetty-versiota vastaukseen"
        (let [resp (raw-request port "GET / HTTP/9.9\r\nHost: localhost\r\n\r\n")]
          (is (seq resp) "palvelin vastasi jotain")
          (is (not (re-find #"(?i)jetty" resp))
              (str "vastaus vuosi Jetty-merkkijonon:\n" resp))
          (is (not (re-find #"12\.1\." resp))
              (str "vastaus vuosi Jetty-version:\n" resp))))
      (finally
        (.stop srv)))))
