(ns hakukohderyhmapalvelu.routes
  (:require [reitit.coercion.spec :as rss]
            [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]
            [re-frame.core :as re-frame]))

(def routes
  [["/"
    {:redirect :panel-menu/hakukohderyhmien-hallinta-panel}]
   ["/hakukohderyhmapalvelu"
    {:redirect :panel-menu/hakukohderyhmien-hallinta-panel}]
   ["/hakukohderyhmapalvelu/hakukohderyhmien-hallinta"
    {:name :panel-menu/hakukohderyhmien-hallinta-panel}]
   ["/hakukohderyhmapalvelu/haun-asetukset"
    {:name       :panel-menu/haun-asetukset-panel
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
              (re-frame/dispatch [:panel-menu/set-active-panel
                                  {:panel      name
                                   :parameters {:path  path
                                                :query query}}]))))
    {:use-fragment false}))
