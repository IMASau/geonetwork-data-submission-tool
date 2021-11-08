(ns metcalf4.ins4
  (:require [cljs.pprint :as pprint]
            [clojure.data :as data]
            [metcalf4.blocks4 :as blocks]
            [metcalf4.rules4 :as rules]
            [re-frame.core :as rf]))

(defmulti console-config :kind)
(defmethod console-config :default [_] {})

(defn console-value
  [{:keys [value] :as data}]
  (if goog/DEBUG
    value
    (let [{:keys [level length]} (console-config data)]
      (binding [*print-level* (or level 3)
                *print-length* (or length 5)]
        (pr-str value)))))

(def breadcrumbs
  (rf/->interceptor
    :id ::breadcrumbs
    :before (fn [context]
              (let [event (rf/get-coeffect context :event)]
                (js/console.log "EVENT" (console-value {:kind :event :value event}))
                context))
    :after (fn [context]
             (let [orig-db (rf/get-coeffect context :db)
                   new-db (rf/get-effect context :db ::not-found)]
               (when-not (= new-db ::not-found)
                 (let [[only-before only-after] (data/diff orig-db new-db)]
                   (when (some? only-before)
                     (js/console.log "  -DB" (console-value {:kind :diff :value only-before})))
                   (when (some? only-after)
                     (js/console.log "  +DB" (console-value {:kind :diff :value only-after}))))))
             (let [effects (rf/get-effect context)
                   fx0 (map vec (remove (comp #{:db :fx} key) effects))
                   fx1 (:fx effects)]
               (doseq [fx [fx1 fx0] effect fx]
                 (js/console.log "   FX" (console-value {:kind :effect :value (vec effect)}))))
             context)))

(defn reg-global-singleton
  [{:keys [id] :as ins}]
  (rf/clear-global-interceptor id)
  (rf/reg-global-interceptor ins))

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

(def what-changed
  (rf/->interceptor
    :id ::what-changed
    :after (fn [ctx]
             (let [db0 (rf/get-coeffect ctx :db)
                   db1 (rf/get-effect ctx :db ::not-found)]
               (when (and (not= db1 ::not-found)
                          (not= db0 db1))
                 (let [data0 (get-in db0 [:form :data])
                       state1 (get-in db1 [:form :state])
                       data1 (blocks/as-data (blocks/postwalk rules/apply-rules state1))
                       [only0 only1] (clojure.data/diff data0 data1)]
                   (pprint/pprint {::what-changed.only0 only0
                                   ::what-changed.only1 only1})))
               ctx))))

