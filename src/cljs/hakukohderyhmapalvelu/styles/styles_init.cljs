(ns hakukohderyhmapalvelu.styles.styles-init
  (:require [stylefy.core :as stylefy]
            [hakukohderyhmapalvelu.styles.styles-colors :as colors]
            [hakukohderyhmapalvelu.styles.styles-fonts :as vars]))

(def ^:private body-styles
  {:background-color colors/gray-lighten-5
   :color            colors/gray
   :font-family      vars/font-family
   :font-size        "16px"
   :font-weight      vars/font-weight-regular
   :line-height      "24px"})

(defn- add-font-styles []
  (doseq [format ["woff" "woff2"]]
    (doseq [weight [vars/font-weight-regular
                    vars/font-weight-medium
                    vars/font-weight-bold]]
      (stylefy/font-face {:font-family "Roboto"
                          :src         (str "url('/hakukohderyhmapalvelu/fonts/roboto-" weight "." format "') format('" format "')")
                          :font-weight weight
                          :font-style  "normal"}))))

(defn init-styles []
  (stylefy/init)
  (stylefy/tag "body" body-styles)
  (add-font-styles))
