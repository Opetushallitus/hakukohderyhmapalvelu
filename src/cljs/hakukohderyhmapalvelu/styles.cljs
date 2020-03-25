(ns hakukohderyhmapalvelu.styles
  (:require [stylefy.core :as stylefy]))

(defn init-styles []
  (stylefy/init)
  (doseq [format ["woff" "woff2"]]
    (doseq [weight [400 500 700]]
      (stylefy/font-face {:font-family "Roboto"
                          :src         (str "url('/hakukohderyhmapalvelu/fonts/roboto-" weight "." format "') format('" format "')")
                          :font-weight weight
                          :font-style  "normal"}))))




