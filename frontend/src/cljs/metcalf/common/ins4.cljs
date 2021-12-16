(ns metcalf.common.ins4
  (:require [cljs.spec.alpha :as s]
            [clojure.data :as data]
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
              (js/console.log "EVENT" (console-value {:kind :event :value (rf/get-coeffect context :event)}))
              context)
    :after (fn [context]
             (let [effects (rf/get-effect context)
                   fx0 (map vec (remove (comp #{:db :fx} key) effects))
                   fx1 (:fx effects)]
               (doseq [fx [fx1 fx0] effect fx]
                 (js/console.log "   FX" (console-value {:kind :effect :value (vec effect)}))))
             context)))

(def db-diff
  (rf/->interceptor
    :id ::db-diff
    :after (fn [context]
             (let [orig-db (rf/get-coeffect context :db)
                   new-db (rf/get-effect context :db ::not-found)]
               (when-not (= new-db ::not-found)
                 (let [start (system-time)
                       [only-before only-after] (data/diff orig-db new-db)]
                   (when (some? only-before)
                     (js/console.log "  -DB" (console-value {:kind :diff :value only-before})))
                   (when (some? only-after)
                     (js/console.log "  +DB" (console-value {:kind :diff :value only-after})))
                   (let [ms (- (system-time) start)]
                     (when (> ms 100)
                       (js/console.warn (str "SLOW_DIFF  " (.toFixed ms 6) " msecs")))))))
             context)))

(defn slow-handler [ms]
  (rf/->interceptor
    :id ::slow-handler
    :before (fn [context]
              (rf/assoc-coeffect context ::start (system-time)))
    :after (fn [context]
             (let [event (rf/get-coeffect context :event)
                   start (rf/get-coeffect context ::start)
                   interval (- (system-time) start)]
               (when (> interval ms)
                 (js/console.log "SLOW_HANDLER EVENT" (console-value {:kind :event :value event}))
                 (js/console.log (str "SLOW_HANDLER TIME  " (.toFixed interval 6) " msecs"))))
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

(defn check-and-throw
  [a-spec]
  (rf/->interceptor
    :id ::check-and-throw
    :after (fn [context]
             (let [new-db (rf/get-effect context :db ::not-found)]
               (when-not (= new-db ::not-found)
                 (when-not (s/valid? a-spec new-db)
                   (js/console.log ::check-and-throw--new-db new-db)
                   (throw (ex-info (str "spec check failed: " (s/explain-str a-spec new-db)) {})))))
             context)))
