(ns hakukohderyhmapalvelu.dates.date-parser
  (:require [cljs-time.coerce :as c]
            [cljs-time.core :as t]
            [cljs-time.format :as f]
            [clojure.string :as string]))

(defonce date-hour-minute-formatter (f/formatters :date-hour-minute))

(defn iso-date-time-local-str->date [date-str]
  (when-not (string/blank? date-str)
    (try
      (f/parse-local date-hour-minute-formatter date-str)
      (catch js/Error _))))

(defn date->long [date]
  (try
    (c/to-long date)
    (catch js/Error _)))

(defn date->iso-date-time-local-str [date]
  (try
    (->> date
         t/to-default-time-zone
         (f/unparse date-hour-minute-formatter))
    (catch js/Error _)))

(defn long->date [long]
  (c/from-long long))
