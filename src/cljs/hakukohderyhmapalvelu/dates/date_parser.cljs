(ns hakukohderyhmapalvelu.dates.date-parser
  (:require [cljs-time.coerce :as c]
            [cljs-time.core :as t]
            [cljs-time.format :as f]
            [clojure.string :as string]))

(defonce date-hour-minute-formatter (f/formatters :date-hour-minute))

(defonce date-formatter (f/formatters :date))

(defonce time-formatter (f/formatters :hour-minute))

(defn iso-date-time-local-str->date [date-str]
  (when-not (string/blank? date-str)
    (try
      (f/parse-local date-hour-minute-formatter date-str)
      (catch js/Error _))))

(defn iso-date-str->date [date-str]
  (when-not (string/blank? date-str)
    (try
      (f/parse-local date-formatter date-str)
      (catch js/Error _))))

(defn iso-time-str->date [time-str]
  (when-not (string/blank? time-str)
    (try
      (f/parse-local time-formatter time-str)
      (catch js/Error e
        (println (str "ERROR: " e))))))

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
