(ns hakukohderyhmapalvelu.components.common.svg)
;refers to svgs in html template

(defn icon [id style]
  [:svg {:style style} [:use {:xlinkHref (str "#icon-" id)}]])
