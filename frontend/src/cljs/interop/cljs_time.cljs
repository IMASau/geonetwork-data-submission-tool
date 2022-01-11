(ns interop.cljs-time
  (:require [cljs-time.coerce :as c]
            [cljs-time.format :as f]
            [cljs-time.core :as cljs-time]
            [cljs.spec.alpha :as s]
            [clojure.string :as string]))


(def date-value-formatter (f/formatter "yyyy-MM-dd"))
(def date-string-formatter (f/formatter "dd-MM-yyyy"))


(defn date-to-value
  "Convert js date into a json string value.  Return nil if no date provided."
  [date]
  (s/assert (s/nilable inst?) date)
  (some->> (c/from-date date)
           cljs-time.coerce/to-date-time
           (f/unparse date-value-formatter)))

(comment (date-to-value nil)
         (date-to-value (js/Date.))
         (date-to-value "2012-12-12"))


(defn value-to-date
  "Parse json string value to js date.  Returns nil for blank strings."
  [str]
  (s/assert (s/nilable string?) str)
  (when-not (string/blank? str)
    (c/to-date (f/parse date-value-formatter str))))

(comment (value-to-date nil)
         (value-to-date "2012-12-12")
         (value-to-date 123))


(defn value-to-datetime
  "Parse json string value to js datetime.  Returns nil for blank strings."
  [str]
  (s/assert (s/nilable string?) str)
  (when-not (string/blank? str)
    (js/Date. str)))



(defn humanize-date
  "Return string for date in standard format for humans.  Returns nil if no date provided."
  [date]
  (s/assert (s/nilable inst?) date)
  (some->> (c/from-date date)
           cljs-time.coerce/to-date-time
           (f/unparse date-string-formatter)))

(comment (humanize-date nil)
         (humanize-date (value-to-date "1911-02-01"))
         (humanize-date 123))


(defn humanize-interval
  "Returns a short readable string describing the length of the interval.  Based on `moment.fromNow()`."
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
      :else "a few seconds")))


(comment (humanize-interval (let [now (cljs-time/now)
                                  period (cljs-time/days 1.6)]
                              (cljs-time/interval (cljs-time/minus now period) now))))

(defn from-now
  "Returns a short readable string describing how long ago `d` was.  Based on `moment.fromNow()`."
  [d]
  (let [start (c/from-date d)
        end (cljs-time/now)]
    (str (humanize-interval (cljs-time/interval start end)) " ago")))

(comment (from-now (js/Date. 2022 0 3))
         (from-now (js/Date. "2022-01-03T11:11:11")))
