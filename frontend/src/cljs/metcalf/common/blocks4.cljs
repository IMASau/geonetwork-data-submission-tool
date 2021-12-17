(ns metcalf.common.blocks4
  (:require [cljs.spec.alpha :as s]
            [clojure.string :as string]
            [cljs.test :as test]
            [metcalf.common.schema4 :as schema4]
            [metcalf.common.utils4 :as utils4]))


(s/def ::type string?)
(s/def ::props map?)
(s/def ::content
  (s/or :arr (s/coll-of ::block)
        :obj (s/map-of string? ::block)))
(s/def ::block
  (s/keys :opt-un [::type ::props ::content]))


(defn update-vals [m f] (zipmap (keys m) (map f (vals m))))


(defn walk
  "Traverses blocks.  inner and outer are functions.
   Applies inner to each element of form then applies outer to the result."
  [inner outer block]
  (case (:type block)
    "array" (outer (update block :content #(mapv inner %)))
    "object" (outer (update block :content update-vals inner))
    (outer block)))


(defn postwalk
  "Performs a depth-first, post-order traversal of blocks.  Calls f on
   each sub-form, uses f's return value in place of the original."
  [f form]
  (walk (partial postwalk f) f form))


(defn prewalk
  "Like postwalk, but does pre-order traversal."
  [f form]
  (walk (partial prewalk f) identity (f form)))


(defn as-blocks
  "Return blocks given data and a schema."
  [{:keys [data schema]}]
  (schema4/postwalk-schema-data2
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

(test/deftest as-blocks-examples
  (test/is (= (as-blocks {:schema {:type "string"} :data "roar"})
              {:type "string" :props {:value "roar"}}))
  (test/is (= (as-blocks {:schema {:type "array" :items {:type "string"}} :data ["roar" "roar2"]})
              {:type    "array"
               :content [{:type "string" :props {:value "roar"}}
                         {:type "string" :props {:value "roar2"}}]}))
  (test/is (= (as-blocks {:schema {:type "object" :props {"name" {:type "string"}}} :data {"name" "Joe"}})
              {:type    "object"
               :content {"name" {:type "string" :props {:value "Joe"}}}}))
  (test/is (= (as-blocks {:schema {} :data {"some" "extra" "data" 123 "etc" [1 2 3]}})
              {:type    "object"
               :content {"some" {:type "string" :props {:value "extra"}}
                         "data" {:type "number" :props {:value 123}}
                         "etc"  {:type "array" :content [{:type "number" :props {:value 1}}
                                                         {:type "number" :props {:value 2}}
                                                         {:type "number" :props {:value 3}}]}}})))


(defn roll-up-data
  "Postwalk analysis.  Extracts :value data from blocks."
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
  "To extract the data we walk the blocks, drilling into each :content, and pick out values."
  [block]
  (::data (postwalk roll-up-data block)))

(test/deftest as-data-examples
  (doseq [egs [{:schema {:type "string"} :data "roar"}
               {:schema {:type "array" :items {:type "string"}}
                :data   ["roar" "roar2"]}
               {:schema {:type "object" :props {"name" {:type "string"}}}
                :data   {"name" "Joe"}}
               {:schema {}
                :data   {"some" "extra" "data" 123 "etc" [1 2 3]}}]]
    (test/is (= (as-data (as-blocks egs)) (:data egs)))))


(defn block-path
  "If you know a data-path then working out the block-path is just a matter of interleaving :content in it.
  Useful for getting/setting values from app-db for the user."
  [data-path]
  (s/assert ::utils4/data-path data-path)
  (vec (interleave (repeat :content) data-path)))

(test/deftest block-path-examples
  (test/is (= (block-path ["identifyingInfo" "title"]) [:content "identifyingInfo" :content "title"]))
  (test/is (= (block-path ["attachment" 3]) [:content "attachment" :content 3])))


(defn clear-error-props
  "Clear out state related to errors on a block.
   Useful for clearing server errors stored using blocks/set-error-prop"
  [block]
  (cond-> block
    (contains? block :props)
    (update :props dissoc :errors :show-errors)))


(defn set-error-prop
  "Set :error prop for a block.  Useful to store errors reported by server."
  [block data-path error]
  (let [path (utils4/as-path [(block-path data-path) :props :errors])]
    (assoc-in block path error)))

(defn propagate-disabled
  "Prewalk analysis (push down).  Marks contents disabled if block disabled"
  [{:keys [type content props] :as block}]
  (letfn [(set-disabled [b] (assoc-in b [:props :disabled] true))]
    (if (:disabled props)
      (case type
        "array" (assoc block :content (mapv set-disabled content))
        "object" (assoc block :content (zipmap (keys content) (map set-disabled (vals content))))
        block)
      block)))

(s/def :progress/score (s/keys :opt [:progress/required :progress/errors :progress/fields :progress/required-errors]))
(s/def :progress/required nat-int?)
(s/def :progress/errors nat-int?)
(s/def :progress/fields nat-int?)
(s/def :progress/required-errors nat-int?)
(s/def :progress/scores (s/coll-of (s/nilable :progress/score)))

(defn add-scores
  "Helper to add up scores.  Used in postwalk analysis"
  [scores]
  (s/assert :progress/scores scores)
  (apply merge-with + scores))

(defn score-props
  "Generate score based on block props.  Considers :required and :errors."
  [{:keys [props]}]
  (let [{:keys [required errors]} props]
    (-> {:progress/fields 1}
        (cond-> required (assoc :progress/required 1))
        (cond-> (seq errors) (assoc :progress/errors 1))
        (cond-> (and required (seq errors)) (assoc :progress/required-errors 1)))))

(defn score-object
  "Generate score for block object based on :content scores."
  [{:keys [content]}]
  (add-scores (map :progress/score (vals content))))

(defn score-array
  "Generate score for block array based on :content scores."
  [{:keys [content]}]
  (add-scores (map :progress/score content)))

(defn score-value
  "Generate score based on block :value prop."
  [block]
  (when (string/blank? (get-in block [:props :value]))
    {:progress/empty 1}))

(defn score-block
  "Generate score for a block."
  [{:keys [type] :as block}]
  (case type
    "array" (add-scores [(score-array block) (score-props block)])
    "object" (add-scores [(score-object block) (score-props block)])
    (add-scores [(score-value block) (score-props block)])))

(defn progress-score-analysis
  "Postwalk analysis.  Score block considering props and content"
  [{:keys [props] :as block}]
  (if-not (:disabled props)
    (assoc block :progress/score (score-block block))
    block))
