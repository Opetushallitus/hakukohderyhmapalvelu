(ns hakukohderyhmapalvelu.views.haun-asetukset-panel
  (:require [hakukohderyhmapalvelu.components.common.panel :as p]
            [re-frame.core :as re-frame]))

(defn haun-asetukset-panel []
  [p/panel
   {}
   @(re-frame/subscribe [:translation :panel-menu/haun-asetukset-panel])
   [:div (str @(re-frame/subscribe [:panel-menu/active-panel]))]])

