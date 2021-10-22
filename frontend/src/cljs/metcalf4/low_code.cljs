(ns metcalf4.low-code
  (:require [cljs.spec.alpha :as s]
            [clojure.walk :as walk]
            [metcalf4.schema :as schema]
            [metcalf4.utils :as utils4]
            [re-frame.core :as rf]))

(def ^:dynamic component-registry {})
(def ^:dynamic template-registry {})
(def ^:dynamic not-found-hiccup '[:div "not found"])

; Private use template cache
(def *build-template-cache (atom {}))

(defn init!
  [{:keys [low-code/templates]}]
  (when (map? templates)
    (reset! *build-template-cache ({}))
    (set! template-registry templates)))

(defn report-unregistered-syms
  [x]
  (when (symbol? x)
    (utils4/console-error (str "Symbol not resolved: " (pr-str x)) {:sym x}))
  x)

(defn check-missing-keys
  [{:keys [config settings]}]
  (when-let [req-ks (get-in settings [::req-ks])]
    (let [missing-ks (remove (set (keys config)) req-ks)]
      (doseq [k missing-ks]
        (utils4/console-error (str "Missing required key (" (pr-str k) ") in config") {:config config :settings settings})))))

(defn check-compatible-schema
  [{:keys [settings schema]}]
  (when-let [schema2 (get-in settings [::schema])]
    (schema/assert-compatible-schema {:schema1 schema :schema2 schema2})))

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

(defn replace-variable-syms
  [smap x]
  (if (contains? smap x)
    (get smap x)
    x))

(defn replace-component-syms
  [smap x]
  (if (contains? smap x)
    (build-component x (get smap x))
    x))

(defn render-template
  [{:keys [template-id variables]}]
  (let [form (get template-registry template-id not-found-hiccup)]
    (walk/postwalk
      (comp (partial report-unregistered-syms)
            (partial replace-component-syms component-registry)
            (partial replace-variable-syms variables))
      form)))
