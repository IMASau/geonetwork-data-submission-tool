(ns metcalf4.blocks
  (:require [cljs.spec.alpha :as s]
            [metcalf4.schema :as schema]))


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
    "array" (update block :content #(mapv inner %))
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
      (let [type (:type schema)]
        (merge (select-keys schema [:type])
               (case type
                 "array" {:content data}
                 "object" {:content data}
                 {:props {:value data :label (:label schema)}})
               (when-let [rules (:rules schema)]
                 {:rules rules}))))
    {:data data :schema schema}))


(defn as-data
  "Return data given blocks"
  [{:keys [type props content]}]
  (case type
    "array" (mapv as-data content)
    "object" (zipmap (keys content) (map as-data (vals content)))
    (:value props)))


(defn block-path [data-path]
  (vec (interleave (repeat :content) data-path)))


; TODO: no support for initial/default values yet
(defn new-item
  [schema]
  (as-blocks {:data nil :schema schema}))

