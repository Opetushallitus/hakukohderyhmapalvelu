(ns hakukohderyhmapalvelu.cas.cas-utils
  (:require
    [hakukohderyhmapalvelu.caller-id :as caller-id])
  (:import [fi.vm.sade.javautils.nio.cas CasClientBuilder CasConfig$CasConfigBuilder]))

(def csrf-token "hakukohderyhmapalvelu")

(defn create-cas-client [config service-url session-cookie-name]
  (let [{username :username
         password :password} config
        cas-url (-> config :cas :url)
        caller-id (-> config
                       :oph-organisaatio-oid
                       (caller-id/make-caller-id))
        cas-config (-> (new CasConfig$CasConfigBuilder username password cas-url service-url csrf-token caller-id "")
                       (.setJsessionName session-cookie-name)
                       (.build))
        cas-client (CasClientBuilder/build cas-config)]
    cas-client))