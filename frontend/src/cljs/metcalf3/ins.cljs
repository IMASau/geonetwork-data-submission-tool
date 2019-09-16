(ns metcalf3.ins
  (:require [re-frame.core :as rf]))

(def log-effects
  (rf/->interceptor
    :id ::after
    :after (fn [ctx]
             (doseq [[k v] (rf/get-effect ctx)
                     :when (not= k :db)]
               (js/console.log "Effect: " k v))
             ctx)))

(def std-ins [rf/debug log-effects])
