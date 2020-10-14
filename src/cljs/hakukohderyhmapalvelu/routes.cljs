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
    {:name :panel-menu/hakukohderyhmien-hallinta-panel}]])

(defn app-routes []
  (rfe/start!
    (rf/router
      routes
      {:data {:coercion rss/coercion}})
    (fn [m]
      (let [{:keys [name redirect]} (-> m :data)]
        (cond redirect
              (rfe/replace-state redirect)

              name
              (re-frame/dispatch [:panel-menu/set-active-panel name]))))
    {:use-fragment false}))
