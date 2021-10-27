(ns metcalf4.schema
  (:require [cljs.spec.alpha :as s]
            [clojure.set :as set]
            [metcalf4.utils :as utils4]
            [clojure.string :as string]))


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
  (when-not (or (nil? data)
                (case (:type schema)
                  "array" (vector? data)
                  "object" (map? data)
                  "string" (string? data)
                  "integer" (int? data)
                  "number" (number? data)
                  "boolean" (boolean? data)
                  "null" (nil? data)
                  true))
    (utils4/console-error (str "Invalid " (:type schema) " value: " (pr-str data))
                          {:data data :schema schema :path path})))

(defn validate-object-properties
  [{:keys [schema data path]}]
  (when (= "object" (:type schema))
    (let [prop-ks (set (keys (:properties schema)))
          data-ks (set (keys data))]
      (when-let [extra-ks (seq (set/difference data-ks prop-ks))]
        (utils4/console-error (str "Unexpected object properties: " (string/join ", " (map pr-str extra-ks)))
                              {:extra-ks extra-ks :data data :schema schema :path path})))))

(defn check-required-properties
  [{:keys [schema data path]}]
  (let [{:keys [type required]} schema]
    (when (and (= "object" type) required)

      (if-not (s/valid? (s/coll-of string?) (:required schema))
        (utils4/console-error (str "Invalid required annotation on object: " (pr-str (:required schema)))
                              {:required (:required schema) :data data :schema schema :path path})

        (let [missing (apply disj (set required) (keys data))]
          (when (seq missing)
            (utils4/console-error (str "Required property missing: " (string/join ", " (map pr-str missing)))
                                  {:missing missing :data data :schema schema :path path})))))))

(defn schema-data-valid?
  [{:keys [schema data]}]
  (case (:type schema)
    "array" (vector? data)
    "object" (map? data)
    "string" (string? data)
    "number" (number? data)
    "boolean" (boolean? data)
    true))

(defn check-data-type
  [form]
  (when-not (s/valid? schema-data-valid? form)
    (utils4/console-error (utils4/spec-error-at-path schema-data-valid? form (:path form)) form)))

(defn massage-form
  [{:keys [path] :as form}]
  (let [path (or path [])
        form' (assoc form :path path)]
    (validate-data form')
    form'))

(defn walk-schema-data
  "Given a schema and some data, walk the data.  Warn about unexpected data."
  [inner outer raw-form]
  (let [{:keys [schema data path] :as form} (massage-form raw-form)
        data' (case (:type schema)
                "array"
                (letfn [(inner-item-data [idx item-data]
                          (inner {:schema (:items schema)
                                  :data   item-data
                                  :path   (conj path idx)}))]
                  (vec (map-indexed inner-item-data data)))

                "object"
                (letfn [(inner-prop [acc prop-name]
                          (let [prop-schema (get-in schema [:properties prop-name])
                                prop-data (get data prop-name)
                                prop-val (inner {:schema prop-schema
                                                 :data   prop-data
                                                 :path   (conj path prop-name)})]
                            (assoc acc prop-name prop-val)))]
                  (let [data-ks (keys data)
                        prop-ks (keys (:properties schema))]
                    (reduce inner-prop {} (set/intersection (set data-ks) (set prop-ks)))))

                ;other
                data)]

    (outer (assoc form :data data'))))

(defn infer-schema
  [data]
  (cond (vector? data) {:type "array" :items {}}
        (map? data) {:type "object" :properties {}}
        (string? data) {:type "string"}
        (int? data) {:type "integer"}
        (number? data) {:type "number"}
        (boolean? data) {:type "boolean"}
        (nil? data) {:type "null"}
        :else {}))

(defn massage-form2
  [{:keys [schema data path] :as form}]
  (let [path (or path [])
        schema (or schema (infer-schema data))
        form' (assoc form :schema schema :path path)]
    (validate-data form')
    form'))

; NOTE: Experimental.  Not sure we need/want it.
(defn walk-schema-data2
  "Given a schema and some data, walk the data.  Infer when schema not-defined."
  [inner outer raw-form]
  (let [{:keys [schema data path] :as form} (massage-form2 raw-form)
        data' (case (:type schema)

                "array"
                (letfn [(inner-item-data [idx item-data]
                          (let [data-schema (infer-schema (get data idx))
                                item-schema (get schema :items data-schema)]
                            (inner {:schema item-schema
                                    :data   item-data
                                    :path   (conj path idx)})))]
                  (vec (map-indexed inner-item-data data)))

                "object"
                (letfn [(inner-prop [acc prop-name]
                          (let [data-schema (infer-schema (get data prop-name))
                                prop-schema (get-in schema [:properties prop-name] data-schema)
                                prop-data (get data prop-name)
                                prop-val (inner {:schema prop-schema
                                                 :data   prop-data
                                                 :path   (conj path prop-name)})]
                            (assoc acc prop-name prop-val)))]
                  (let [prop-ks (set (keys (:properties schema)))
                        data-ks (set (keys data))]
                    (reduce inner-prop {} (set/union prop-ks data-ks))))

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
  (or (nil? data)
      (case (:type schema)
        "array" (vector? data)
        "object" (map? data)
        "string" (string? data)
        "number" (number? data)
        "boolean" (boolean? data)
        true)))


(defn report-schema-error
  [msg]
  #_(if *assert*
      (throw (js/Error. msg))
      (js/console.error msg))
  (js/console.warn msg))


(defn assert-schema-data
  "Check data against schema reporting any incompatibilities like bad data types or extra props"
  [{:keys [schema data path] :or {path []}}]
  (prewalk-schema-data
    (fn [form]
      (validate-object-properties form)
      (when-not (s/valid? schema-data-valid? form)
        (utils4/console-error (utils4/spec-error-at-path schema-data-valid? form (:path form)) form))
      form)
    {:schema schema :data data :path path}))


(comment
  (assert-schema-data {:schema {:type "string"} :data "roar" :path []})
  (assert-schema-data {:schema {:type "string"} :data 1 :path []})
  (assert-schema-data {:schema {:type "object" :properties {"a" {:type "number"}}} :data {"a" 1 "b" 2} :path []}))


(defn compatible-schema-type?
  [{:keys [schema1 schema2]}]
  (or (nil? (:type schema1))
      (nil? (:type schema2))
      (= (:type schema1) (:type schema2))))


(defn can-compare-schema?
  "Check we have enough data to compare schemas.
  If schema2 is nil everything is okay, else we need a type for schema1 to check against."
  [{:keys [schema1 schema2]}]
  (or (nil? (:type schema2))
      (and (:type schema1) (:type schema2))))


(s/def ::compatible-schema-type?
  (s/and can-compare-schema? compatible-schema-type?))


(defn object-properties-subset?
  "Check that schema2 doesn't introduce new properties"
  [{:keys [schema1 schema2]}]
  (let [ks1 (keys (:properties schema1))
        ks2 (keys (:properties schema2))]
    (set/subset? (set ks2) (set ks1))))


(defn assert-compatible-schema
  "Confirm schema2 is a compatible subset of schema1"
  [{:keys [schema1 schema2 path] :or {path []} :as form}]
  (when-not (s/valid? ::compatible-schema-type? form)
    (report-schema-error (utils4/spec-error-at-path ::compatible-schema-type? form (:path form))))
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
