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
                          :services {:organisaatio-service {:service-url-property s/Keyword
                                                            :session-cookie-name  s/Str}}}
   :urls                 {:virkailija-baseurl s/Str}
   :oph-organisaatio-oid s/Str
   :public-config        public/PublicConfig})

(s/defn make-config :- HakukohderyhmaConfig []
  (-> (:config c/env)
      (#(do (println (str "Luetaan konfiguraatio tiedostosta '" % "'")) %))
      (slurp)
      (edn/read-string)))
