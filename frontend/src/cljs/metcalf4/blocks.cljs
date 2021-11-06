(ns metcalf4.blocks
  (:require [cljs.spec.alpha :as s]
            [metcalf4.schema :as schema]
            [metcalf4.utils :as utils4]
            [clojure.string :as string]))


(s/def ::type string?)
(s/def ::props map?)
(s/def ::content
  (s/or :arr (s/coll-of ::block)
        :obj (s/map-of string? ::block)))
(s/def ::block
  (s/keys :opt-un [::type ::props ::content]))


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
  (schema/postwalk-schema-data2
    (fn [{:keys [data schema]}]
      (let [type (:type schema)
            block-map (select-keys schema [:type])
            props-map (select-keys schema [:label])]
        (merge block-map
               (case type
                 "array" {:content (or data []) :props props-map}
                 "object" {:content (or data {}) :props props-map}
                 {:props (assoc props-map :value data)})
               (when-let [rules (:rules schema)]
                 {:rules rules}))))
    {:data data :schema schema}))


(defn roll-up-data
  "Roll up data pruning empty arrays, objects and nil value blocks."
  [{:keys [type props content]}]
  (case type
    "array" (when (seq content)
              {::data (mapv ::data content)})
    "object" (let [data (for [[k b] content :when (contains? b ::data)]
                          [k (::data b)])]
               (when (seq data)
                 {::data (into {} data)}))
    (when-not (nil? (:value props))
      {::data (:value props)})))

(defn as-data
  [block]
  (::data (postwalk roll-up-data block)))


(defn block-path [data-path]
  (s/assert ::utils4/data-path data-path)
  (vec (interleave (repeat :content) data-path)))


; TODO: no support for initial/default values yet
(defn new-item
  [schema]
  (as-blocks {:data nil :schema schema}))


(defn clear-error-props
  "clear out state related to errors on a block"
  [block]
  (cond-> block
    (contains? block :props)
    (update :props dissoc :errors :show-errors)))


(defn set-error-prop
  [block data-path error]
  (let [path (utils4/as-path [(block-path data-path) :props :errors])]
    (assoc-in block path error)))

(defn propagate-disabled
  "Prewalk analysis.  Marks contents disabled if block disabled"
  [{:keys [type content props] :as block}]
  (letfn [(set-disabled [b] (assoc-in b [:props :disabled] true))]
    (if (:disabled props)
      (case type
        "array" (assoc block :content (mapv set-disabled content))
        "object" (assoc block :content (zipmap (keys content) (map set-disabled (vals content))))
        block)
      block)))

(defn add-scores [scores]
  (s/assert (s/map-of keyword? number?))
  (apply merge-with + scores))

(defn score-props
  [{:keys [props]}]
  (let [{:keys [required errors]} props]
    (-> {:fields 1}
        (cond-> required (assoc :required 1))
        (cond-> (seq errors) (assoc :errors 1))
        (cond-> (and required (seq errors)) (assoc :required-errors 1)))))

(defn score-object
  [{:keys [content]}]
  (add-scores (map :progress-score (vals content))))

(defn score-array
  [{:keys [content]}]
  (add-scores (map :progress-score content)))

(defn score-value
  [block]
  (when (string/blank? (get-in block [:props :value]))
    {:empty 1}))

(defn score-block
  [{:keys [type] :as block}]
  (case type
    "array" (add-scores [(score-array block) (score-props block)])
    "object" (add-scores [(score-object block) (score-props block)])
    (add-scores [(score-value block) (score-props block)])))

(defn progess-score-analysis
  "Postwalk analysis.  Score block considering props and content"
  [{:keys [props] :as block}]
  (if-not (:disabled props)
    (assoc block :progress-score (score-block block))
    block))
