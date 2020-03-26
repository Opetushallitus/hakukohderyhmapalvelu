(ns hakukohderyhmapalvelu.views.hakukohderyhmapalvelu-panel
  (:require [hakukohderyhmapalvelu.components.common.checkbox :as checkbox]
            [hakukohderyhmapalvelu.components.common.grid :as grid]
            [hakukohderyhmapalvelu.components.common.input :as input]
            [hakukohderyhmapalvelu.components.common.panel :as p]
            [stylefy.core :as stylefy]))

(def ^:private hakukohderyhmapalvelu-grid-styles
  {:display "grid"
   :grid    (str "[haku-row-start] \"haku-search .\" 1fr [haku-row-end]"
                 "/ 50% 50%")})

(defn hakukohderyhmapalvelu-panel []
  [p/panel
   {:cypressid "hakukohderyhmapalvelu-panel"}
   "Hakukohderyhmien hallinta"
   [:div (stylefy/use-style hakukohderyhmapalvelu-grid-styles)
    (let [input-id     "haku-search-input"
          style-prefix "haku-search"]
      [grid/input-with-label-and-control
       {:control-component [checkbox/checkbox-with-label {:checkbox-id  "haku-search-checkbox"
                                                          :cypressid    "haku-search-checkbox"
                                                          :label        "Näytä myös päättyneet"
                                                          :style-prefix (str style-prefix "-control")}]
        :cypressid         "haku-search"
        :input-component   [input/input-text {:cypressid    "haku-search-input"
                                              :input-id     input-id
                                              :placeholder  "Haun nimi"
                                              :style-prefix (str style-prefix "-input")}]
        :input-id          input-id
        :style-prefix      style-prefix
        :label             "Haku"}])]])

