(ns hakukohderyhmapalvelu.core
  (:require
    [reagent.dom :as reagent]
    [re-frame.core :as re-frame]
    [hakukohderyhmapalvelu.events.core-events]
    [hakukohderyhmapalvelu.routes :as routes]
    [hakukohderyhmapalvelu.views :as views]
    [hakukohderyhmapalvelu.config :as config]))


(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn init []
  (routes/app-routes)
  (re-frame/dispatch-sync [:core/initialize-db])
  (dev-setup)
  (mount-root))
