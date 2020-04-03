(ns hakukohderyhmapalvelu.core
  (:require
    [reagent.dom :as reagent]
    [re-frame.core :as re-frame]
    [schema.core :as s]
    [hakukohderyhmapalvelu.events.core-events]
    [hakukohderyhmapalvelu.events.hakukohderyhma-create-events]
    [hakukohderyhmapalvelu.events.http-events]
    [hakukohderyhmapalvelu.fx.http-fx]
    [hakukohderyhmapalvelu.routes :as routes]
    [hakukohderyhmapalvelu.views :as views]
    [hakukohderyhmapalvelu.styles.styles-init :as styles]
    [hakukohderyhmapalvelu.subs.core-subs]
    [hakukohderyhmapalvelu.subs.hakukohderyhma-create-subs]))

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
  (mount-root))
