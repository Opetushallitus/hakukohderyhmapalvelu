(ns hakukohderyhmapalvelu.config
  (:require [clojure.edn :as edn]
            [config.core :as c]
            [hakukohderyhmapalvelu.public-config-schemas :as public]
            [schema.core :as s]))

(s/defschema HakukohderyhmaConfig
  {:server               {:http {:port s/Int}}
   :log                  {:base-path s/Str}
   :db                   {:username      s/Str
                          :password      s/Str
                          :database-name s/Str
                          :host          s/Str
                          :port          s/Int}
   :cas                  {:username s/Str
                          :password s/Str
                          :url s/Str
                          :services {:organisaatio-service  {:service-url-property s/Keyword
                                                             :session-cookie-name  s/Str}
                                     :oppijanumerorekisteri {:service-url-property s/Keyword
                                                             :session-cookie-name  s/Str}
                                     :kouta-internal        {:service-url-property s/Keyword
                                                             :session-cookie-name  s/Str}
                                     :ataru                 {:service-url-property s/Keyword
                                                             :session-cookie-name  s/Str}}}
   :urls                 {:virkailija-baseurl        s/Str
                          :hakukohderyhmapalvelu-url s/Str}
   :oph-organisaatio-oid s/Str
   :public-config        public/PublicConfig
   :siirtotiedosto       {:aws-region s/Str
                          :s3-bucket s/Str
                          :s3-target-role-arn s/Str
                          :max-kohderyhmacount-in-file s/Int}})

(defn- report-error [^Throwable e message]
  (.println System/err message)
  (.printStackTrace e)
  (throw e))

(defn- parse-edn [source-string]
  (try
    (edn/read-string source-string)
    (catch Exception e
      (report-error e "Ei saatu jäsennettyä EDN:ää syötteestä."))))

(defn- validate-config [config-edn]
  (let [validation-result (s/check HakukohderyhmaConfig config-edn)]
    (if validation-result
      (let [message (str "Rikkinäinen konfiguraatio: " validation-result)]
        (report-error (IllegalArgumentException.) message))
      config-edn)))

(s/defn make-config :- HakukohderyhmaConfig []
  (-> (:config c/env)
      (#(do (println (str "Luetaan konfiguraatio tiedostosta '" % "'")) %))
      (slurp)
      (parse-edn)
      (validate-config)))

(s/defn production-environment? [config :- HakukohderyhmaConfig]
  (= :production (get-in config [:public-config :environment])))

(s/defn integration-environment? [config :- HakukohderyhmaConfig]
  (= :it (get-in config [:public-config :environment])))
