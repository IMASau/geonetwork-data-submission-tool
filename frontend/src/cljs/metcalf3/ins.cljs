(ns metcalf3.ins
  (:require [re-frame.core :as rf]
            [cljs.spec.alpha :as s]
            [clojure.data :as data]))

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

(defn valid-db
  [a-spec]
  (rf/->interceptor
    :id ::valid-db
    :after
    (fn valid-db-after
      [context]
      (when-let [db (rf/get-effect context :db)]
        (when-not (s/valid? a-spec db)
          (let [event (rf/get-coeffect context :event)]
            (js/console.error "EVENT" (console-value {:kind :event :value event}))
            (js/console.error " SPEC" (s/explain-str a-spec db)))))
      context)))

(defn reg-global-singleton
  [{:keys [id] :as ins}]
  (rf/clear-global-interceptor id)
  (rf/reg-global-interceptor ins))

(def log-effects
  (rf/->interceptor
    :id ::log-effects
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
