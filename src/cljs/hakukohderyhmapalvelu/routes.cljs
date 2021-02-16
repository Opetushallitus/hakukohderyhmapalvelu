(ns hakukohderyhmapalvelu.routes
  (:require [camel-snake-kebab.core :as csk]
            [camel-snake-kebab.extras :as cske]
            [hakukohderyhmapalvelu.config :as c]
            [reitit.coercion.spec :as rss]
            [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]
            [re-frame.core :as re-frame]))

(def default-panel (:default-panel c/config))

(def routes
  [["/"
    {:redirect default-panel}]
   ["/hakukohderyhmapalvelu"
    {:redirect default-panel}]
   ["/hakukohderyhmapalvelu/"
    {:redirect default-panel}]
   ["/hakukohderyhmapalvelu/hakukohderyhmien-hallinta"
    {:name default-panel}]
   ["/hakukohderyhmapalvelu/haun-asetukset"
    {:name       :panel/haun-asetukset
     :parameters {:query {:hakuOid string?}}}]])

(defn app-routes []
  (rfe/start!
    (rf/router
      routes
      {:data {:coercion rss/coercion}})
    (fn on-navigate [m]
      (let [{{:keys [name redirect]}    :data
             {:keys [path query]
              :or   {path {} query {}}} :parameters}
            m]
        (cond redirect
              (rfe/replace-state redirect)

              name
              (re-frame/dispatch [:panel/set-active-panel
                                  {:panel      name
                                   :parameters {:path  (cske/transform-keys csk/->kebab-case-keyword path)
                                                :query (cske/transform-keys csk/->kebab-case-keyword query)}}]))))
    {:use-fragment false}))
