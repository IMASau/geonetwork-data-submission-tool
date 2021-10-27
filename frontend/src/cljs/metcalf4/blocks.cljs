(ns metcalf4.blocks
  (:require [cljs.spec.alpha :as s]
            [metcalf4.schema :as schema]
            [metcalf4.utils :as utils4]))


(s/def ::type string?)
(s/def ::properties map?)
(s/def ::content
  (s/or :arr (s/coll-of ::block)
        :obj (s/map-of string? ::block)))
(s/def ::block
  (s/keys :opt-un [::type ::properties ::content]))


(defn update-vals [m f] (zipmap (keys m) (map f (vals m))))


(defn walk
  [inner outer block]
  (case (:type block)
    "array" (outer (update block :content #(mapv inner %)))
    "object" (outer (update block :content update-vals inner))
    (outer block)))


(defn prewalk [f form]
  (walk (partial prewalk f) identity (f form)))


(defn postwalk [f form]
  (walk (partial postwalk f) f form))


(defn as-blocks
  "Return blocks given data and a schema."
  [{:keys [data schema]}]
  (schema/postwalk-schema-data
    (fn [{:keys [data schema]}]
      (let [type (:type schema)
            block-map (select-keys schema [:type])
            props-map (select-keys schema [:label])]
        (merge block-map
               (case type
                 "array" {:content data :props props-map}
                 "object" {:content data :props props-map}
                 {:props (assoc props-map :value data)})
               (when-let [rules (:rules schema)]
                 {:rules rules}))))
    {:data data :schema schema}))


(defn as-data
  "Return data given blocks"
  [{:keys [type props content] :as value}]
  (case type
    "array" (mapv as-data content)
    "object" (zipmap (keys content) (map as-data (vals content)))
    (:value props)))


(defn block-path [data-path]
  (s/assert ::utils4/data-path data-path)
  (vec (interleave (repeat :content) data-path)))


; TODO: no support for initial/default values yet
(defn new-item
  [schema]
  (as-blocks {:data nil :schema schema}))

