(ns hakukohderyhmapalvelu.components.common.select-all
  (:require [hakukohderyhmapalvelu.components.common.button :as button]
            [stylefy.core :as stylefy]
            [hakukohderyhmapalvelu.styles.styles-colors :as colors]))


(def ^:private select-all-button-row-style
  {:display "flex"
   :justify-content "left"
   :align-items "center"
   :grid-area "multi-selection-buttons"})

(defn select-all-buttons [{:keys [cypressid
                                    select-all-is-disabled
                                    deselect-all-is-disabled
                                    on-select-all
                                    on-deselect-all
                                    hakukohde-count
                                    select-all-label
                                    deselect-all-label]}]
    [:div (stylefy/use-style select-all-button-row-style)
     [button/text-button {:cypressid    cypressid
                          :disabled?    select-all-is-disabled
                          :label        (str select-all-label " (" hakukohde-count ")")
                          :on-click     on-select-all
                          :style-prefix "select-all-btn"}]
     [:span (stylefy/use-style {:margin "6px"
                                :color  (if (and select-all-is-disabled deselect-all-is-disabled)
                                          colors/gray-lighten-3 colors/black)})
      " | "]
     [button/text-button {:cypressid    (str "de" cypressid)
                          :disabled?    deselect-all-is-disabled
                          :label        deselect-all-label
                          :on-click     on-deselect-all
                          :style-prefix "deselect-all-btn"}]])
