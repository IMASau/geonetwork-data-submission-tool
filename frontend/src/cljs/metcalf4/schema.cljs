(ns metcalf4.schema
  (:require [cljs.spec.alpha :as s]))


(s/def ::schema (s/keys :opt-un [::type ::items ::properties]))
(s/def ::type string?)
(s/def ::items ::schema)
(s/def ::properties (s/map-of string? ::schema))


(defn walk-schema-data
  "Given a schema and some data, walk the data"
  [inner outer form]
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


(defn assert-schema-data-error
  [spec form]
  (let [ed (merge (assoc (s/explain-data* spec [] (:path form) [] form)
                    ::s/failure :assertion-failed))]
    (str "Spec assertion failed\n" (with-out-str (s/explain-out ed)))))


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
        (report-schema-error (assert-schema-data-error schema-data-valid? form)))
      form)
    {:schema schema :data data :path []}))


(defn compatible-schema-type?
  [{:keys [schema1 schema2]}]
  (or (nil? (:type schema1))
      (= (:type schema1) (:type schema2))))


(defn assert-schema-compatible-error
  [form]
  (let [ed (merge (assoc (s/explain-data* compatible-schema-type? [] (:path form) [] form)
                    ::s/failure :assertion-failed))]
    (str "Spec assertion failed\n" (with-out-str (s/explain-out ed)))))


(defn assert-compatible-schema
  "Confirm schema2 is a compatible subset of schema1"
  [{:keys [schema1 schema2 path] :as form}]
  (when-not (s/valid? compatible-schema-type? form)
    (report-schema-error (assert-schema-compatible-error form)))
  (case (:type schema2)
    "object" (dorun (map (fn [k]
                           (assert-compatible-schema
                             {:schema1 (get-in schema1 [:properties k])
                              :schema2 (get-in schema2 [:properties k])
                              :path    (conj path :properties k)}))
                         (keys (:properties schema2))))
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
  (vec (mapcat schema-step data-path)))
