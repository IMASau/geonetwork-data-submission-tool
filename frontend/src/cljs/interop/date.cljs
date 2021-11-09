(ns interop.date
  (:require [cljs-time.coerce :as c]
            [cljs-time.format :as f]
            [cljs.spec.alpha :as s]
            [clojure.string :as string]))


(def value-formatter (f/formatter "yyyy-MM-dd"))
(def string-formatter (f/formatter "dd-MM-yyyy"))


(defn to-value
  "Convert date into a json string value"
  [date]
  (s/assert (s/nilable inst?) date)
  (some->> (c/from-date date)
           cljs-time.coerce/to-date-time
           (f/unparse value-formatter)))

(comment (to-value nil)
         (to-value (js/Date.))
         (to-value "2012-12-12"))


(defn from-value
  "Parse json string value to date"
  [str]
  (s/assert (s/nilable string?) str)
  (when-not (string/blank? str)
    (c/to-date (f/parse value-formatter str))))

(comment (from-value nil)
         (from-value "2012-12-12")
         (from-value 123))



(defn to-string
  "Print date in standard format for humans"
  [date]
  (s/assert (s/nilable inst?) date)
  (some->> (c/from-date date)
           cljs-time.coerce/to-date-time
           (f/unparse string-formatter)))

(comment (to-string nil)
         (to-string (from-value "1911-02-01"))
         (to-string 123))

