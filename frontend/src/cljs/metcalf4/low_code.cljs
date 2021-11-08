(ns metcalf4.low-code
  (:require [cljs.spec.alpha :as s]
            [clojure.walk :as walk]
            [metcalf4.schema :as schema]
            [metcalf4.utils :as utils4]
            [re-frame.core :as rf]
            [clojure.string :as string]))

(defonce ^:dynamic component-registry {})
(defonce ^:dynamic template-registry {})
(defonce ^:dynamic not-found-hiccup '[:div "not found"])

; Private use template cache
(def *build-template-cache (atom {}))

(defn init!
  "Merge templates into template-registry."
  [{:keys [low-code/templates]}]
  (when (map? templates)
    (reset! *build-template-cache ({}))
    (set! template-registry (merge template-registry templates))))

(defn variable?
  "A variable is a simple symbol which starts with ?"
  [x]
  (and (simple-symbol? x) (string/starts-with? (name x) "?")))

(defn report-unregistered-syms
  [x]
  (when (and (symbol? x) (not (variable? x)))
    (utils4/console-error (str "Symbol not registered: " (pr-str x)) {:sym x}))
  x)

(defn report-unregistered-var
  [x]
  (when (variable? x)
    (utils4/console-error (str "Variable not bound: " (pr-str x)) {:var x}))
  x)

(defn check-missing-keys
  [{:keys [config settings] :as ctx}]
  (when-let [req-ks (get-in settings [::req-ks])]
    (let [missing-ks (remove (set (keys config)) req-ks)]
      (doseq [k missing-ks]
        (utils4/console-error (str "Missing required key (" (pr-str k) ") in config") {:k k :ctx ctx})))))

(defn check-compatible-schema
  [{:keys [settings schema config]}]
  (when-let [schema2 (get-in settings [::schema])]
    (schema/assert-compatible-schema {:schema1 schema :schema2 schema2 :path (schema/schema-path (:data-path config))})))

(defn check-compatible-paths
  [{:keys [settings schema] :as ctx}]
  (doseq [path (remove nil? (get-in settings [::schema-paths]))]
    (if (= "array" (get-in schema [:type]))
      (when-not (schema/contains-path? {:schema (:items schema) :path path})
        (utils4/console-error (str "Path not present in schema: " (pr-str path)) {:ctx ctx :path path}))
      (when-not (schema/contains-path? {:schema schema :path path})
        (utils4/console-error (str "Path not present in schema: " (pr-str path)) {:ctx ctx :path path})))))

(defn build-component
  [sym reg-data]
  (s/assert map? reg-data)
  (let [{:keys [init view]} reg-data]
    (fn [raw-config & args]
      (let [config (utils4/if-contains-update raw-config :data-path utils4/massage-data-path)
            settings (when init (init config))
            schema @(rf/subscribe [::get-data-schema config])
            ctx {:sym sym :config config :settings settings :schema schema}]
        (check-missing-keys ctx)
        (check-compatible-schema ctx)
        (check-compatible-paths ctx)
        (into [view config] args)))))

(defn eval-or-val [ctx]
  (fn [x] (if-let [f (::eval x)] (f ctx) x)))

(defn compile-form
  [env x]
  (cond (vector? x)
        (let [fx (mapv (partial compile-form env) x)]
          (if (not-any? ::eval fx)
            fx
            {::eval (fn [ctx]
                      (mapv (eval-or-val ctx) fx))}))

        (map? x)
        (let [ks (map (partial compile-form env) (keys x))
              vs (map (partial compile-form env) (vals x))
              data-ks (when (not-any? ::eval ks) ks)
              data-vs (when (not-any? ::eval vs) vs)]
          (if (and data-ks data-vs)
            (zipmap ks vs)
            {::eval (fn [ctx]
                      (zipmap (or data-ks (map (eval-or-val ctx) ks))
                              (or data-vs (map (eval-or-val ctx) vs))))}))

        (variable? x)
        {::eval (fn [ctx]
                  (if (contains? (:variables ctx) x)
                    (get-in ctx [:variables x])
                    (report-unregistered-var x)))}

        (symbol? x)
        (if-let [reg-data (get-in env [:component-registry x])]
          (build-component x reg-data)
          (report-unregistered-syms x))

        :default
        x))

(defn prepare-template
  [{:keys [template-id template-registry component-registry]}]
  (let [hiccup (get template-registry template-id not-found-hiccup)
        template (compile-form {:component-registry component-registry} hiccup)]
    template))

(def prepare-template-once (utils4/memoize-to-atom prepare-template *build-template-cache))

(defn render-template
  [{:keys [template-id variables]}]
  (let [template (prepare-template-once
                   {:template-id        template-id
                    :template-registry  template-registry
                    :component-registry component-registry})]
    ((eval-or-val {:variables variables}) template)))
