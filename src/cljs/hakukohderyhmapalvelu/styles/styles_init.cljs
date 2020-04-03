(ns hakukohderyhmapalvelu.styles.styles-init
  (:require [goog.string :as gstring]
            [stylefy.core :as stylefy]
            [hakukohderyhmapalvelu.styles.styles-colors :as colors]
            [hakukohderyhmapalvelu.config :as c]
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
                          :src         (gstring/format "url('/hakukohderyhmapalvelu/fonts/roboto-%d.%s') format('%s')" weight format format)
                          :font-weight weight
                          :font-style  "normal"})
      (stylefy/font-face {:font-family "Material Icons"
                          :src         (gstring/format "url('/hakukohderyhmapalvelu/fonts/MaterialIcons-Regular.%s') format('%s')" format format)
                          :font-weight weight
                          :font-style  "normal"}))))

(defn init-styles []
  (stylefy/init {:use-caching? (-> c/config :environment (= :production))})
  (stylefy/tag "body" body-styles)
  (add-font-styles))
