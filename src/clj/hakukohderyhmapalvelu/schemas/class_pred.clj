(ns hakukohderyhmapalvelu.schemas.class-pred
  (:require [schema.core :as s]))

(defn extends-class-pred [c]
  (s/pred (fn extends-class? [x]
            (->> x class (extends? c)))))
