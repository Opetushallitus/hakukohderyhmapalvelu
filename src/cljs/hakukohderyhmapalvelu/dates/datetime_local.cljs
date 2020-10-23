(ns hakukohderyhmapalvelu.dates.datetime-local)

(defn datetime-local-supported? []
  (let [element (doto (.createElement js/document "input")
                  (.setAttribute "type" "datetime-local"))]
    (= (.-type element) "datetime-local")))
