(ns hakukohderyhmapalvelu.core
  (:require
    [reagent.dom :as reagent]
    [re-frame.core :as re-frame]
    [schema.core :as s]
    [hakukohderyhmapalvelu.events.core-events]
    [hakukohderyhmapalvelu.events.alert-events]
    [hakukohderyhmapalvelu.events.hakukohderyhmien-hallinta-events]
    [hakukohderyhmapalvelu.events.haun-asetukset-events]
    [hakukohderyhmapalvelu.events.http-events]
    [hakukohderyhmapalvelu.events.panel-events]
    [hakukohderyhmapalvelu.fx.dispatch-debounced-fx]
    [hakukohderyhmapalvelu.fx.http-fx]
    [hakukohderyhmapalvelu.routes :as routes]
    [hakukohderyhmapalvelu.views :as views]
    [hakukohderyhmapalvelu.styles.styles-init :as styles]
    [hakukohderyhmapalvelu.subs.core-subs]
    [hakukohderyhmapalvelu.subs.alert-subs]
    [hakukohderyhmapalvelu.subs.hakukohderyhma-subs]
    [hakukohderyhmapalvelu.subs.haun-asetukset-subs]
    [hakukohderyhmapalvelu.subs.panel-subs]))

(defn- turn-on-schema-validation []
  (s/set-fn-validation! true))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn init []
  (turn-on-schema-validation)
  (styles/init-styles)
  (re-frame/dispatch-sync [:core/initialize-db])
  (routes/app-routes)
  (mount-root))
