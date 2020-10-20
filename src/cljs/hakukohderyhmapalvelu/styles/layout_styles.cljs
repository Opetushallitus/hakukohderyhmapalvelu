(ns hakukohderyhmapalvelu.styles.layout-styles)

(def vertical-align-center-styles
  {:display     "flex"
   :align-items "center"})

(def horizontal-center-styles
  {:display         "flex"
   :justify-content "center"})

(def horizontal-space-between-styles
  {:display         "flex"
   :justify-content "space-between"})

(defn flex-row-styles [align-items justify-content]
  {:display         "flex"
   :flex-flow       "column nowrap"
   :align-items     align-items
   :justify-content justify-content})
