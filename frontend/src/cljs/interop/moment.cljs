(ns interop.moment
  (:require [cljsjs.moment]))

(defn moment
  ([x] (js/moment x))
  ([x y] (js/moment x y)))

(defn from-now [x] (.fromNow (moment x)))
(defn format [x s] (.format (js/moment x) s))
(defn to-date [x] (.toDate (js/moment x)))