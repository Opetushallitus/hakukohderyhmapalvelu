(ns hakukohderyhmapalvelu.views.haun-asetukset-panel
  (:require [hakukohderyhmapalvelu.components.common.panel :as p]
            [re-frame.core :as re-frame]))

(defn haun-asetukset-panel []
  [p/panel
   {}
   @(re-frame/subscribe [:translation :haun-asetukset/title])
   [:div (str @(re-frame/subscribe [:panel/active-panel]))]])

