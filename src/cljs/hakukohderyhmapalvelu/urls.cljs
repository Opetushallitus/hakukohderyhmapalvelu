(ns hakukohderyhmapalvelu.urls
  (:require [schema.core :as s]))

(s/defn get-url :- s/Str
  [url :- s/Keyword
   & args]
  (apply (partial
           (.-url js/window)
           (name url))
         args))
