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

(def form-ticker
  (rf/->interceptor
    :id ::form-ticker
    :after (fn [ctx]
             (let [orig-db (rf/get-coeffect ctx :db)
                   new-db (rf/get-effect ctx :db ::not-found)
                   form1 (get-in orig-db [:form])
                   form2 (get-in new-db [:form])]
               (if (and (not= new-db ::not-found)
                        (not= form1 form2))
                 (rf/assoc-effect ctx :db (update new-db :form/tick inc))
                 ctx)))))

(def std-ins [rf/debug log-effects form-ticker])
