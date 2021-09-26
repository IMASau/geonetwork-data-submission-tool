(ns metcalf.common.schema
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


(defn assert-schema-data
  [{:keys [data schema]}]
  (letfn [(test [data spec x]
            (when-not (s/valid? spec x)
              (throw (ex-info (s/explain-str spec x) data))))]
    (prewalk-schema-data
      (fn [{:keys [schema data] :as form}]
        (case (:type schema)
          "array" (test form (s/nilable vector?) data)
          "object" (test form (s/nilable map?) data)
          "string" (test form (s/nilable string?) data)
          "number" (test form (s/nilable number?) data)
          nil)
        form)
      {:schema schema :data data :path []})))
