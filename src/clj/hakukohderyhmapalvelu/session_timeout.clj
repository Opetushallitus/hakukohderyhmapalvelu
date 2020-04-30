(ns hakukohderyhmapalvelu.session-timeout
  (:require [cheshire.core :as json]
            [clojure.string :refer [starts-with?]]
            [hakukohderyhmapalvelu.config :as c]
            [hakukohderyhmapalvelu.oph-url-properties :refer [resolve-url]]
            [ring.middleware.session-timeout :as session-timeout]
            [ring.util.http-response :as response]
            [schema.core :as s]))

(s/defn ^:private create-timeout-handler
  [config :- c/HakukohderyhmaConfig]
  (fn [{:keys [uri]}]
    (let [auth-url (resolve-url :cas.login config)]
      (if (starts-with? uri "/hakukohderyhmapalvelu/api")
        (response/unauthorized (json/generate-string {:redirect auth-url}))
        (response/found auth-url)))))


(defn- timeout-options [config]
  {:timeout         28800
   :timeout-handler (create-timeout-handler config)})

(s/defn create-wrap-idle-session-timeout
  [config :- c/HakukohderyhmaConfig]
  (fn [handler]
    (let [options (timeout-options config)]
      (session-timeout/wrap-idle-session-timeout handler options))))

