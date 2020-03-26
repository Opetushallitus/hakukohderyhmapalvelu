(ns hakukohderyhmapalvelu.views.hakukohderyhmapalvelu-panel
  (:require [hakukohderyhmapalvelu.components.panel :as p]))

(defn hakukohderyhmapalvelu-panel []
  [p/panel
   {:id "hakukohderyhmapalvelu-panel"}
   "Hakukohderyhmien hallinta"
   [:div]])
