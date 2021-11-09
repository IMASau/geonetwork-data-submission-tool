(ns interop.moment
  (:require ["moment" :as js-moment]))

(assert js-moment)

(defn from-now [x] (.fromNow (js-moment x)))