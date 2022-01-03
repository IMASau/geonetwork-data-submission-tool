(ns interop.date
  (:require [cljs-time.coerce :as c]
            [cljs-time.format :as f]
            [cljs-time.core :as cljs-time]
            [cljs.spec.alpha :as s]
            [clojure.string :as string]))


(def value-formatter (f/formatter "yyyy-MM-dd"))
(def string-formatter (f/formatter "dd-MM-yyyy"))


(defn to-value
  "Convert date into a json string value.  Return nil if no date provided."
  [date]
  (s/assert (s/nilable inst?) date)
  (some->> (c/from-date date)
           cljs-time.coerce/to-date-time
           (f/unparse value-formatter)))

(comment (to-value nil)
         (to-value (js/Date.))
         (to-value "2012-12-12"))


(defn from-value
  "Parse json string value to date.  Returns nil for blank strings."
  [str]
  (s/assert (s/nilable string?) str)
  (when-not (string/blank? str)
    (c/to-date (f/parse value-formatter str))))

(comment (from-value nil)
         (from-value "2012-12-12")
         (from-value 123))



(defn to-string
  "Print date in standard format for humans.  Returns nil if no date provided."
  [date]
  (s/assert (s/nilable inst?) date)
  (some->> (c/from-date date)
           cljs-time.coerce/to-date-time
           (f/unparse string-formatter)))

(comment (to-string nil)
         (to-string (from-value "1911-02-01"))
         (to-string 123))


(defn humanize-interval
  [i]
  (let [total-days (cljs-time/in-days i)
        total-hours (cljs-time/in-hours i)
        total-minutes (cljs-time/in-minutes i)
        total-seconds (cljs-time/in-seconds i)
        {:keys [years months days hours minutes seconds millis]} (cljs-time/->period i)
        ceil-years (cond-> years (some pos? [months days hours minutes seconds millis]) inc)
        ceil-months (cond-> months (some pos? [days hours minutes seconds millis]) inc)
        ceil-days (cond-> days (some pos? [hours minutes seconds millis]) inc)
        ceil-hours (cond-> hours (some pos? [minutes seconds millis]) inc)
        ceil-minutes (cond-> minutes (some pos? [seconds millis]) inc)]
    (cond
      (>= total-days 548) (str ceil-years " years")         ;2 years ... 20 years
      (>= total-days 320) "a year"
      (>= total-days 45) (str ceil-months " months")        ;2 months ... 10 months
      (>= total-days 26) "a month"
      (>= total-hours 36) (str ceil-days " days")           ;2 days ... 25 days
      (>= total-hours 22) "a day"
      (>= total-minutes 90) (str ceil-hours " hours")       ;2 hours ... 21 hours
      (>= total-minutes 45) "an hour"
      (>= total-seconds 90) (str ceil-minutes " minutes")   ;2 minutes ... 44 minutes
      (>= total-seconds 44) "a minute"
      :default "a few seconds")))


(comment (humanize-interval (let [now (cljs-time/now)
                                  period (cljs-time/days 1.6)]
                              (cljs-time/interval (cljs-time/minus now period) now))))

(defn from-now
  [dt]
  (str (humanize-interval (cljs-time/interval dt (cljs-time/now))) " ago"))

(comment (from-now (c/from-date (js/Date. 2022 0 3)))
         (from-now (c/from-date (js/Date. "2022-01-03T11:11:11"))))