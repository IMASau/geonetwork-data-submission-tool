(ns metcalf4.schema
  (:require [cljs.spec.alpha :as s]
            [clojure.set :as set]
            [metcalf4.utils :as utils4]))


(s/def ::schema (s/keys :opt-un [::type ::items ::properties]))
(s/def ::type string?)
(s/def ::items ::schema)
(s/def ::properties (s/map-of string? ::schema))
(s/def ::path (s/coll-of (s/or :s string? :i int?) :kind vector?))
(s/def ::form (s/keys :opt-un [::schema ::data ::path]))


(defn massage-schema-payload
  "Ensures property names are strings"
  [{:keys [type items properties] :as schema}]
  (case type
    "array" (assoc schema :items (massage-schema-payload items))
    "object" (assoc schema :properties (zipmap (map name (keys properties))
                                               (map massage-schema-payload (vals properties))))
    schema))


(defn massage-data-payload
  "Ensures property names are strings"
  [data]
  (cond (vector? data)
        (mapv massage-data-payload data)

        (map? data)
        (zipmap (map name (keys data))
                (map massage-data-payload (vals data)))

        :else
        data))

(defn validate-data
  [{:keys [schema data path]}]
  (when-not (case (:type schema)
              "array" (vector? data)
              "object" (map? data)
              "string" (string? data)
              "integer" (int? data)
              "number" (number? data)
              "boolean" (boolean? data)
              "null" (nil? data)
              true)
    (utils4/console-error (str "Invalid " (:type schema) " value: " (pr-str data))
                          {:data data :schema schema :path path})))

(defn walk-schema-data
  "Given a schema and some data, walk the data"
  [inner outer form]
  (s/assert ::form form)
  (let [{:keys [schema data path]} form
        path (or path [])
        data' (case (:type schema)
                "array"
                (letfn [(inner-item-data [idx item-data]
                          (inner {:schema (:items schema)
                                  :data   item-data
                                  :path   (conj path idx)}))]
                  (vec (map-indexed inner-item-data data)))

                "object"
                (letfn [(inner-prop [acc prop-name prop-schema]
                          (let [prop-data (get data prop-name)
                                prop-val (inner {:schema prop-schema
                                                 :data   prop-data
                                                 :path   (conj path prop-name)})]
                            (assoc acc prop-name prop-val)))]
                  (reduce-kv inner-prop {} (:properties schema)))

                ;other
                data)]

    (outer (assoc form :data data'))))


(defn prewalk-schema-data
  [f form]
  (walk-schema-data (partial prewalk-schema-data f) identity (f form)))


(defn postwalk-schema-data
  [f form]
  (walk-schema-data (partial postwalk-schema-data f) f form))


(defn schema-data-valid?
  [{:keys [schema data]}]
  (case (:type schema)
    "array" (vector? data)
    "object" (map? data)
    "string" (string? data)
    "number" (number? data)
    true))


(defn report-schema-error
  [msg]
  #_(if *assert*
      (throw (js/Error. msg))
      (js/console.error msg))
  (js/console.warn msg))


(defn assert-schema-data
  [schema data]
  (prewalk-schema-data
    (fn [form]
      (when-not (s/valid? schema-data-valid? form)
        (report-schema-error (utils4/spec-error-at-path schema-data-valid? form (:path form))))
      form)
    {:schema schema :data data :path []}))


(defn compatible-schema-type?
  [{:keys [schema1 schema2]}]
  (or (nil? (:type schema1))
      (nil? (:type schema2))
      (= (:type schema1) (:type schema2))))


(defn object-properties-subset?
  "Check that schema2 doesn't introduce new properties"
  [{:keys [schema1 schema2]}]
  (let [ks1 (keys (:properties schema1))
        ks2 (keys (:properties schema2))]
    (set/subset? (set ks2) (set ks1))))


(defn assert-compatible-schema
  "Confirm schema2 is a compatible subset of schema1"
  [{:keys [schema1 schema2 path] :or {path []} :as form}]
  (when-not (s/valid? compatible-schema-type? form)
    (report-schema-error (utils4/spec-error-at-path compatible-schema-type? form (:path form))))
  (case (:type schema2)
    "object" (doseq [k (keys (:properties schema2))]
               (when-not (s/valid? object-properties-subset? form)
                 (report-schema-error (utils4/spec-error-at-path object-properties-subset? form (:path form))))
               (assert-compatible-schema
                 {:schema1 (get-in schema1 [:properties k])
                  :schema2 (get-in schema2 [:properties k])
                  :path    (conj path :properties k)}))
    "array" (assert-compatible-schema
              {:schema1 (:items schema1)
               :schema2 (:items schema2)
               :path    (conj path :items)})
    nil))


(defn schema-step
  [k]
  (if (int? k)
    [:items]
    [:properties k]))


(defn schema-path
  [data-path]
  (s/assert ::utils4/data-path data-path)
  (vec (mapcat schema-step data-path)))


(defn contains-path?
  [{:keys [schema path]}]
  (utils4/contains-path? schema (schema-path path)))
