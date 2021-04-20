(ns hakukohderyhmapalvelu.components.common.svg
  (:require [stylefy.core :as stylefy]))

(def base-url "/hakukohderyhmapalvelu/images/")

(defn icons [svg-style]
  {:alert [:svg (merge {:xmlns "http://www.w3.org/2000/svg"
                        "xmlns:xlink" "http://www.w3.org/1999/xlink"
                        :version "1.1"}
                       svg-style)
           [:path {:d "M11,15H13V17H11V15M11,7H13V13H11V7M12,2C6.47,2 2,6.5 2,12A10,10 0 0,0 12,22A10,10 0 0,0 22,12A10,10 0 0,0 12,2M12,20A8,8 0 0,1 4,12A8,8 0 0,1 12,4A8,8 0 0,1 20,12A8,8 0 0,1 12,20Z"}]]

   :cross [:svg (merge {:xmlns "http://www.w3.org/2000/svg"}
                       svg-style)
           [:path {:d "M18 1.81286L16.1871 0L9 7.18714L1.81286 0L0 1.81286L7.18714 9L0 16.1871L1.81286 18L9 10.8129L16.1871 18L18 16.1871L10.8129 9L18 1.81286Z"}]]})

(defn icon [name style svg-style]
  [:div (stylefy/use-style style)
   (get (icons svg-style) name)])

(defn img-icon [name style]
  [:img
   {:src (str base-url name ".svg")
    :style style}])
