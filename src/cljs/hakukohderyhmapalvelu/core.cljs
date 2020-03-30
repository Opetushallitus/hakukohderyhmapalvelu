(ns hakukohderyhmapalvelu.core
  (:require
    [reagent.dom :as reagent]
    [re-frame.core :as re-frame]
    [schema.core :as s]
    [hakukohderyhmapalvelu.events.core-events]
    [hakukohderyhmapalvelu.routes :as routes]
    [hakukohderyhmapalvelu.views :as views]
    [hakukohderyhmapalvelu.config :as config]
    [hakukohderyhmapalvelu.styles.styles-init :as styles]
    [hakukohderyhmapalvelu.subs.core-subs]
    [hakukohderyhmapalvelu.subs.hakukohderyhma-create-subs]))


(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn- turn-on-schema-validation []
  (s/set-fn-validation! true))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn init []
  (turn-on-schema-validation)
  (styles/init-styles)
  (routes/app-routes)
  (re-frame/dispatch-sync [:core/initialize-db])
  (dev-setup)
  (mount-root))
