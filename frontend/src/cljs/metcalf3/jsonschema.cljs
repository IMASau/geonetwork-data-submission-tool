(ns metcalf3.jsonschema
  (:require [clojure.string :as string]))

(defn walk-schema-data
  "Given a schema and some data, walk the data"
  [inner outer {:keys [schema data] :as form}]
  (let [data' (case (:type schema)
                "array"
                (letfn [(inner-item-data [item-data]
                          (inner {:schema (:items schema) :data item-data}))]
                  (mapv inner-item-data data))

                "object"
                (letfn [(inner-prop [data prop-name prop-schema]
                          (let [prop-data (get data prop-name)
                                prop-val (inner {:schema prop-schema :data prop-data})]
                            (assoc data prop-name prop-val)))]
                  (reduce-kv inner-prop data (:properties schema)))

                ;other
                data)]

    (outer (assoc form :data data'))))

(defn postwalk-schema-data
  [f form]
  (walk-schema-data (partial postwalk-schema-data f) f form))

(defn set-parent [m parent ids]
  (reduce (fn [m id] (assoc-in m [id :parent] parent)) m ids))

(defn as-state
  "
  Generate state representation of data for use in app.
  Provides a location for properties and values.
  "
  [{:keys [data schema]}]
  (let [*blocks (atom {})
        *id (atom nil)
        genid_counter (atom 100)
        genid (fn [] (swap! genid_counter inc))]
    (postwalk-schema-data
      (fn [{:keys [schema data]}]
        (let [{:keys [type rules]} schema
              id (reset! *id (genid))
              content (case type
                        "array" (let [ids (mapv :id data)]
                                  (swap! *blocks set-parent id ids)
                                  {:content ids})
                        "object" (let [ids (map :id (vals data))]
                                   (swap! *blocks set-parent id ids)
                                   {:content (zipmap (keys data) ids)})
                        {:properties {:value data}})
              block (merge {:id id :type type}
                           (when rules {:rules rules})
                           content)]
          (swap! *blocks assoc id block)
          block))
      {:schema schema :data data})
    {:id @*id :blocks @*blocks}))

(comment
  (as-state
    {:data   {"title" "My title"
              "alist" ["a" 1 []]
              }
     :schema {:type  "object"
              :rules [{:rule-id "ordered" :field1 "begin" :field2 "end"}]
              :properties
                     {"title" {:type  "string"
                               :rules [{:rule-id "required-field"}]}
                      "begin" {:type  "number"
                               :rules [{:rule-id "required-field"}]}
                      "end"   {:type    "number"
                               :rules   [{:rule-id "required-field"}]
                               :initial 10}
                      "alist" {:type  "array"
                               :items {:type  "string"
                                       :rules [{:rule-id "valid-type"}]}}
                      "anobj" {:type "object"
                               :properties
                                     {"a" {:type "number"}}}}}})
  =>
  {:id     130,
   :blocks {121 {:type       "string",
                 :rules      [{:rule-id "required-field"}],
                 :id         121,
                 :properties {:value "My title"},
                 :parent     130},
            130 {:type    "object",
                 :rules   [{:rule-id "ordered", :field1 "begin", :field2 "end"}],
                 :id      130,
                 :content {"title" 121, "alist" 127, "begin" 122, "end" 123, "anobj" 129}},
            128 {:type "number", :id 128, :properties {:value nil}, :parent 129},
            129 {:type "object", :id 129, :content {"a" 128}, :parent 130},
            122 {:type "number", :rules [{:rule-id "required-field"}], :id 122, :properties {:value nil}, :parent 130},
            125 {:type "string", :rules [{:rule-id "valid-type"}], :id 125, :properties {:value 1}, :parent 127},
            127 {:type "array", :id 127, :content [124 125 126], :parent 130},
            123 {:type "number", :rules [{:rule-id "required-field"}], :id 123, :properties {:value nil}, :parent 130},
            126 {:type "string", :rules [{:rule-id "valid-type"}], :id 126, :properties {:value []}, :parent 127},
            124 {:type "string", :rules [{:rule-id "valid-type"}], :id 124, :properties {:value "a"}, :parent 127}}}
  )

(defn as-data
  [{:keys [blocks id]}]
  (let [{:keys [type properties content]} (get blocks id)]
    (case type
      "array" (mapv (as-data {:blocks blocks :id id}) content)
      "object" (zipmap (keys content) (map (fn [id] (as-data {:blocks blocks :id id})) (vals content)))
      (:value properties))))

(comment
  (let [state
        (as-state
          {:data   {"title" "My title"
                    ;"begin" 1
                    ;"end"   -2
                    "alist" ["a" 1 []]
                    }
           :schema {:type  "object"
                    :rules [{:rule-id "ordered" :field1 "begin" :field2 "end"}]
                    :properties
                           {"title" {:type  "string"
                                     :rules [{:rule-id "required-field"}]}
                            "begin" {:type  "number"
                                     :rules [{:rule-id "required-field"}]}
                            "end"   {:type    "number"
                                     :rules   [{:rule-id "required-field"}]
                                     :initial 10}
                            "alist" {:type  "array"
                                     :items {:type  "string"
                                             :rules [{:rule-id "valid-type"}]}}
                            "anobj" {:type "object"
                                     :properties
                                           {"a" {:type "number"}}}}}})]
    (as-data state))

  => {"title" "My title", "alist" ["a" 1 []], "begin" nil, "end" nil, "anobj" {"a" nil}})

;; rules

(defn required-field-rule
  [blocks id {:keys [rule-id]}]
  (let [{:keys [type properties content]} (get blocks id)
        {:keys [value]} properties
        not-set? (case type
                   "string" (string/blank? value)
                   "array" (empty? content)
                   "object" (empty? content)
                   (nil? value))]
    (-> blocks
        (assoc-in [id :properties :required] true)
        (cond-> not-set?
                (update-in [id :errors] assoc rule-id "This field is required")))))

(defn valid-type-rule
  [blocks id {:keys [rule-id]}]
  (let [{:keys [type properties content]} (get blocks id)
        not-valid? (case type
                     "string" (not (string? (:value properties)))
                     "number" (not (number? (:value properties)))
                     "array" (not (vector? content))
                     "object" (not (map? content))
                     (nil? (:value properties)))]
    (cond-> blocks
      not-valid? (update-in [id :errors] assoc rule-id (str "Invalid value, expected " type)))))

(defn ordered-rule
  [blocks id {:keys [rule-id field1 field2]}]
  (let [id1 (get-in blocks [id :content field1])
        id2 (get-in blocks [id :content field2])
        value1 (get-in blocks [id1 :properties :value])
        value2 (get-in blocks [id2 :properties :value])
        out-of-order? (and value1 value2 (<= value2 value1))]
    (-> blocks
        (cond-> value1
                (assoc-in [id2 :properties :minValue] value1))
        (cond-> value2
                (assoc-in [id1 :properties :minValue] value2))
        (cond-> out-of-order?
                (update-in [id2 :errors] assoc rule-id (str "Invalid value, must be larger than " value1))))))

(def rule-registry (atom {}))
(swap! rule-registry assoc "required-field" required-field-rule)
(swap! rule-registry assoc "ordered" ordered-rule)
(swap! rule-registry assoc "valid-type" valid-type-rule)

(defn apply-rule
  [blocks id {:keys [rule-id] :as rule}]
  (if-let [handler (get @rule-registry rule-id)]
    (handler blocks id rule)
    (do (println (str "no rule registered for " (pr-str rule-id)))
        blocks)))

(defn all-ids
  ([state]
   (lazy-cat (all-ids state (:id state)) [(:id state)]))
  ([state id]
   (let [blocks (:blocks state)
         ids (case (get-in blocks [id :type])
               "array" (get-in blocks [id :content])
               "object" (vals (get-in blocks [id :content]))
               nil)
         child-ids (mapcat (fn [id] (all-ids state id)) ids)]
     (lazy-cat child-ids ids))))

(comment
  (all-ids
    (as-state
      {:data   {"title" "My title"
                "alist" ["a" 1 []]
                }
       :schema {:type  "object"
                :rules [{:rule-id "ordered" :field1 "begin" :field2 "end"}]
                :properties
                       {"title" {:type  "string"
                                 :rules [{:rule-id "required-field"}]}
                        "begin" {:type  "number"
                                 :rules [{:rule-id "required-field"}]}
                        "end"   {:type    "number"
                                 :rules   [{:rule-id "required-field"}]
                                 :initial 10}
                        "alist" {:type  "array"
                                 :items {:type  "string"
                                         :rules [{:rule-id "valid-type"}]}}
                        "anobj" {:type "object"
                                 :properties
                                       {"a" {:type "number"}}}}}})))

(defn apply-rules
  [state]
  (reduce
    (fn [blocks id]
      (let [block (get blocks id)]
        (reduce (fn [blocks rule]
                  (apply-rule blocks id rule))
                blocks
                (:rules block))))
    (:blocks state)
    (all-ids state)))

(comment

  (pprint/pprint
    (apply-rules
      (as-state {:schema {:type  "string"
                          :rules [{:rule-id "required-field"}]}
                 :data   nil})))

  =>
  {101
   {:type       "string",
    :rules      [{:rule-id "required-field"}],
    :id         101,
    :properties {:value nil, :required true},
    :errors     {"required-field" "This field is required"}}}

  (apply-rules
    (as-state
      {:schema {:type  "object"
                :rules [{:rule-id "ordered" :field1 "begin" :field2 "end"}]
                :properties
                       {"title" {:type  "string"
                                 :rules [{:rule-id "required-field"}]}
                        "begin" {:type  "number"
                                 :rules [{:rule-id "required-field"}]}
                        "end"   {:type  "number"
                                 :rules [{:rule-id "required-field"}]}
                        "alist" {:type  "array"
                                 :items {:type  "string"
                                         :rules [{:rule-id "valid-type"}]}}
                        "anobj" {:type "object"
                                 :properties
                                       {"a" {:type "number"}}}}}
       :data   {"title" "My title"
                "begin" 1
                "end"   -2
                "alist" ["a" 1 []]
                }}))
  =>
  {110 {:type    "object",
        :rules   [{:rule-id "ordered", :field1 "begin", :field2 "end"}],
        :id      110,
        :content {"title" 101, "begin" 102, "end" 103, "alist" 107, "anobj" 109}},
   101 {:type       "string",
        :rules      [{:rule-id "required-field"}],
        :id         101,
        :properties {:value "My title", :required true},
        :parent     110},
   102 {:type       "number",
        :rules      [{:rule-id "required-field"}],
        :id         102,
        :properties {:value 1, :required true, :minValue -2},
        :parent     110},
   106 {:type "string", :rules [{:rule-id "valid-type"}], :id 106, :properties {:value []}, :parent 107},
   104 {:type "string", :rules [{:rule-id "valid-type"}], :id 104, :properties {:value "a"}, :parent 107},
   108 {:type "number", :id 108, :properties {:value nil}, :parent 109},
   109 {:type "object", :id 109, :content {"a" 108}, :parent 110},
   103 {:type       "number",
        :rules      [{:rule-id "required-field"}],
        :id         103,
        :properties {:value -2, :required true, :minValue 1},
        :parent     110,
        :errors     {"ordered" "Invalid value, must be larger than 1"}},
   107 {:type "array", :id 107, :content [104 105 106], :parent 110},
   105 {:type "string", :rules [{:rule-id "valid-type"}], :id 105, :properties {:value 1}, :parent 107}}

  )
