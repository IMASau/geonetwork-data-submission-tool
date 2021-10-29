(ns metcalf3.logic
  (:require [clojure.zip :as zip]
            [metcalf3.content :refer [contact-groups]]
            [metcalf3.utils :as utils]
            [metcalf4.blocks :as blocks]
            [metcalf4.rules :as rules]
            [metcalf4.schema :as schema]))

(def active-status-filter #{"Draft" "Submitted"})

(defn field-zipper [node]
  (zip/zipper

    (fn branch? [x]
      (cond
        (map? x) (cond (contains? x :value) (or (:many x) (:fields x))
                       :else true)
        (coll? x) true
        :else false))

    (fn children [x]
      (cond
        (map? x) (cond

                   (and (contains? x :value) (:many x))
                   (map :value (:value x))

                   (and (contains? x :value) (:fields x))
                   (:value x)

                   :else (map (juxt key val) x))
        (coll? x) (seq x)
        :else nil))

    (fn make-node [node children]
      (cond
        (map? node) (if (contains? node :value)
                      (assoc node :value children)
                      (into (empty node) children))
        (coll? node) (into (empty node) children)
        :else (into (empty node) children)))

    node))

(defn page-field-zipper
  "page field zipper doesn't descend into many fields"
  [node]
  (zip/zipper

    (fn branch? [x]
      (cond
        (map? x) (cond (contains? x :value) false
                       :else true)
        (coll? x) true
        :else false))

    (fn children [x]
      (cond
        (map? x) (seq x)
        (coll? x) (seq x)
        :else nil))

    (fn make-node [node children]
      (cond
        (map? node) (if (contains? node :value)
                      (assoc node :value children)
                      (into (empty node) children))
        (coll? node) (into (empty node) children)
        :else (into (empty node) children)))

    node))

(defn field? [m]
  (and (map? m)
       (or (contains? m :value)
           (contains? m :type))))

(defn page-fields
  [state page]
  (let [zipper (page-field-zipper state)]
    (loop [loc zipper
           acc []]
      (if (zip/end? loc)
        acc
        (let [node (zip/node loc)]
          (recur (zip/next loc) (if (and (field? node) (= page (:page node)))
                                  (conj acc node)
                                  acc)))))))

(defn has-value? [{:keys [many value]}]
  (cond
    many (seq value)
    (nil? value) false
    (string? value) (seq value)
    :else value))


(defn process-node [{:keys [errors page required disabled] :as field} counter]
  (if-not disabled
    (let [errors? (not-empty errors)]
      (cond-> counter
        true (update :fields inc)
        (not (has-value? field)) (update :empty inc)
        errors? (update :errors inc)
        required (update :required inc)
        (and errors? required) (update :required-errors inc)
        (and errors? page) (update-in [:page-errors page] inc)))
    counter))

(defn progress-score
  [state]
  (js/console.log ::progress-score.TODO "rebuild me as walker")
  {:fields   0
   :empty    0
   :errors   0
   :required 0}
  #_
  (let [zipper (field-zipper state)]
    (loop [loc zipper
           counter {:fields   0
                    :empty    0
                    :errors   0
                    :required 0}]
      (if (zip/end? loc)
        counter
        (let [node (zip/node loc)]
          (recur (zip/next loc) (if (field? node)
                                  (process-node node counter)
                                  counter)))))))

(defn reset-field [field]
  (assoc field
    :value (cond
             (contains? field :initial) (:initial field)
             (:many field) []
             :else nil)))

(defn field-edit
  ([zipper editor]
   (field-edit zipper (constantly true) editor))
  ([zipper matcher editor]
   (loop [loc zipper]
     (if (zip/end? loc)
       (zip/root loc)
       (if (field? (zip/node loc))
         (if (matcher (zip/node loc))
           (recur (zip/next (zip/edit loc editor)))
           (recur (zip/next loc)))
         (recur (zip/next loc)))))))

(defn field-reduce
  [zipper f val]
  (loop [loc zipper
         acc val]
    (if (zip/end? loc)
      acc
      (let [node (zip/node loc)]
        (recur (zip/next loc) (if (field? node)
                                (f acc node)
                                acc))))))

(defn reset-form [{:keys [fields] :as form}]
  (assoc form :fields (field-edit (field-zipper fields) reset-field)))


(defn extract-data
  "Extract current values from form as a map"
  [{:keys [fields]}]
  (into {} (->> fields (utils/fmap :value))))

(defn new-value-field
  [many-field]
  ; TODO: use field-postwalk to avoid inefficiencies with long options lists
  (let [fields (:fields many-field)]
    {:value (field-edit (field-zipper fields) reset-field)}))

; TODO: Use field-zipper.
(defn clear-errors
  "Clear errors stored against field (from server etc)"
  [form]
  (let [field-keys (-> form :fields keys)]
    (reduce
      (fn [s field-key]
        (assoc-in s [:fields field-key :errors] []))
      (assoc form :non_field_errors [])
      field-keys)))


; TODO: Store non-field errors (:non_field_errors form)
(defn load-errors [form field-errors]
  (let [field-keys (keys field-errors)]
    (reduce
      (fn [s field-key]
        (assoc-in s [:fields field-key :errors]
                  (get field-errors field-key [])))
      (clear-errors form) field-keys)))

(defn is-valid? [{:keys [fields non_field_errors]}]
  (and (empty? non_field_errors)
       (field-reduce (field-zipper fields)
                     (fn [acc {:keys [errors]}] (and acc (empty? errors)))
                     true)))


(defn field-walk
  "Tweaked walker which treats fields/branches in a specific way"
  [inner outer form]
  (cond
    ; Field
    (field? form) (outer (if (:many form)
                           (into form
                                 {:value
                                  (mapv (comp (fn [x] {:value x}) inner :value)
                                        (:value form))})
                           form))
    ; Branch
    (map? form) (outer (utils/fmap inner form))
    (seq? form) (outer (doall (map inner form)))
    ; Lists (e.g. many value list)
    (coll? form) (outer (into (empty form) (map inner form)))
    :else (outer form)))


(defn field-postwalk
  [f form]
  (field-walk (partial field-postwalk f) f form))


(defn tree-edit
  [zipper matcher editor]
  (loop [loc zipper]
    (if (zip/end? loc)
      (zip/root loc)
      (if (matcher (zip/node loc))
        (recur (zip/next (zip/edit loc editor)))
        (recur (zip/next loc))))))


(defn extract-field-values
  "Extract the values out of the form fields state"
  [fields]
  (tree-edit
    (field-zipper fields)
    map?
    (fn [m]
      (if (field? m)
        (:value m)
        (into {} (filter (comp map? second) m))))))

(defn extract-field-mask
  "Extract the values out of the form fields state"
  [fields]
  (tree-edit
    (field-zipper fields)
    map?
    (fn [m]
      (if (field? m)
        (not (:disabled m))
        (into {} (filter (comp map? second) m))))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; https://github.com/Roxxi/clojure-common-utils

(declare mask-map)

(defn mask-map-triage-kv [kv a-mask]
  (let [[k v] [(key kv) (val kv)]]
    (when-let [mask-v (get a-mask k)]
      (cond (fn? mask-v) {k (mask-v v)}
            (and (map? mask-v) (map? v))
            {k (mask-map v mask-v)}
            :else {k v}))))

(defn mask-map
  "Given a mask-map whose structure is some subset of some-map's
   structure, extract the structure specified. For a path to be extracted
   the terminal value in the mask-map must be a non-false yielding value.

   If a function is provided as a terminal value in the mask, the function
   will be applied to the value in the source location, before being
   carried over to the resulting map.

   If the mask yields no values, nil will be returned."
  [some-map map-mask]
  (apply merge (remove nil? (map #(mask-map-triage-kv % map-mask) some-map))))

;;;;;;;;;;;;;;;;;;;;;;;;

(defn dirty-form-check
  "Check if the form contains unsaved data by comparing :data with field :values"
  [{:keys [data fields] :as form}]
  (let [values (extract-field-values fields)
        mask (extract-field-mask fields)
        masked-data (mask-map data mask)
        masked-values (mask-map values mask)]
    (assoc form :dirty (not= masked-data masked-values))))

(defn is-required-field?
  "Identifies walker nodes which are fields relevant to require logic"
  [m]
  (and (map? m)
       (:required m)
       (contains? m :value)))

(def empty-values #{nil "" [] {} #{}})

(defn validate-required-field
  "Validate required field adding error message to list if it exists"
  [field]
  (let [{:keys [required value errors]} field]
    (if (and required (contains? empty-values value))
      (assoc field :errors (conj errors "This field is required"))
      field)))

(defn validate-required-fields
  "Derive errors associated with missing required fields"
  [state]
  (blocks/postwalk
    #(if (is-required-field? %) (validate-required-field %) %)
    state))

(defn validate-rules
  [state]
  (blocks/postwalk rules/apply-rules state))

(defn author-role-logic
  "
  At least one of the contacts has to be an author. Generate an error if none are.
  "
  [state]
  (let [rule-path [:form :fields :who-authorRequired]
        roles (for [[_ {:keys [path]}] (utils/enum contact-groups)]
                (for [field (get-in state (conj path :value))]
                  (get-in field [:value :role :value])))
        roles (apply concat roles)
        has-author (some #(= "author" %) roles)]
    (if has-author
      (assoc-in state (conj rule-path :errors) nil)
      (assoc-in state (conj rule-path :errors) ["at least one contact must have the author role"]))))

; NOTE: hard to translate since the schema doesn't separate array from object in many case
(defn data-service-logic-helper
  [data-service]
  (let [protocol-value (-> data-service :value :protocol :value)]
    (if (contains? #{"OGC:WCS-1.1.0-http-get-capabilities"
                     "OGC:WMS-1.3.0-http-get-map"} protocol-value)
      (update-in data-service [:value :name]
                 assoc :required true)
      (update-in data-service [:value :name]
                 assoc :required false :disabled true :placeholder "" :value nil))))

(defn data-service-logic
  "
  If data service protocol 'Other' is chosen then layer field is disabled.
  "
  [state]
  (update-in state [:form :fields :dataSources :value]
             #(mapv data-service-logic-helper %)))

(defn calculate-progress [state form-path]
  (assoc state :progress (progress-score (get-in state form-path))))

(def disabled-statuses #{"Archived" "Deleted" "Uploaded"})

(defn disable-form-when-submitted [state]
  (assoc-in state [:form :disabled] (contains? disabled-statuses (get-in state [:context :document :status]))))

(defn disabled-form-logic [{:keys [disabled] :as form}]
  (if disabled
    (update form :fields
            (fn [fs]
              (field-postwalk #(if (field? %) (assoc % :disabled true) %) fs)))
    form))

(defn derive-data-state [state]
  (-> state
      data-service-logic
      author-role-logic
      (update-in [:form :fields] validate-required-fields)
      (update-in [:form :state] validate-rules)
      disable-form-when-submitted
      (update-in [:form] disabled-form-logic)
      (calculate-progress [:form])
      (update :form dirty-form-check)))

(defn derived-state
  "Used to include derived state for use by components."
  [{:keys [data create_form] :as state}]
  (cond-> state
    data derive-data-state
    create_form (update-in [:create_form] validate-required-fields)))

(defn path-fields [data]
  (into (sorted-set)
        (keep (fn [path]
                (let [[parent [i k]] (split-with (complement integer?) path)]
                  (when k
                    (let [parent (vec parent)]
                      [(conj parent :fields k)
                       (conj parent :value i :value k)]))))
              (utils/keys-in data))))

(defn reduce-many-field-templates
  "For each many field value "
  [fields values]
  (reduce (fn [m [tpl-path value-path]]
            (try
              (utils/int-assoc-in m value-path (get-in fields tpl-path))
              (catch js/Error _ m)))
          fields (path-fields values)))

(defn path-values
  [data]
  (let [get-value #(get-in data %)
        get-path #(mapcat concat (partition-by number? %) (repeat [:value]))]
    (map (juxt get-path get-value)
         (utils/keys-in data))))

(defn reduce-field-values [fields values]
  (reduce (fn [m [p v]]
            (try (utils/int-assoc-in m p v)
                 (catch :default e
                   (js/console.error (clj->js [m p v]) e)
                   m)))
          fields (path-values values)))

; TODO: remove or replace?
(defn initialise-form
  ([{:keys [data] :as form}]
   (initialise-form form data))
  ([form data]
   (-> (reset-form form)
       (assoc :data data)
       (update :fields reduce-many-field-templates data)
       (update :fields reduce-field-values data))))

(defn massage-form
  [{:keys [data schema url]}]
  (let [data (schema/massage-data-payload data)
        schema (schema/massage-schema-payload schema)]
    {:data   data
     :schema schema
     :state  (blocks/as-blocks {:data data :schema schema})
     :url    url}))

(defn initial-state
  "Massage raw payload for use as app-state"
  [{:keys [form] :as payload}]
  (let [URL_ROOT (-> payload :context :URL_ROOT (or ""))]
    (-> payload
        (cond-> form (assoc :form (massage-form form)))
        (assoc :alert [])
        ; TODO: make deployment specific (put in init-db handler)
        (assoc :api {:parametername        {:uri     (str URL_ROOT "/api/ternparameters")
                                            :options nil
                                            :schema  {:type       "object",
                                                      :properties {:tree_id           {:type "integer"},
                                                                   :children_count    {:type "integer"},
                                                                   :vocabularyVersion {:type "string"},
                                                                   :Definition        {:type "string"},
                                                                   :is_selectable     {:type "boolean"},
                                                                   :lft               {:type "integer"},
                                                                   :term              {:type "string"},
                                                                   :URI               {:type "string"},
                                                                   :vocabularyTermURL {:type "string"},
                                                                   :id                {:type "integer"},
                                                                   :Name              {:type "string"},
                                                                   :depth             {:type "integer"},
                                                                   :termDefinition    {:type "string"},
                                                                   :Version           {:type "string"},
                                                                   :rgt               {:type "integer"}}}}
                     :parameterunit        {:uri     (str URL_ROOT "/api/qudtunits")
                                            :options nil
                                            :schema  {:type       "object",
                                                      :properties {:tree_id           {:type "integer"},
                                                                   :children_count    {:type "integer"},
                                                                   :vocabularyVersion {:type "string"},
                                                                   :Definition        {:type "string"},
                                                                   :is_selectable     {:type "boolean"},
                                                                   :lft               {:type "integer"},
                                                                   :term              {:type "string"},
                                                                   :URI               {:type "string"},
                                                                   :vocabularyTermURL {:type "string"},
                                                                   :id                {:type "integer"},
                                                                   :Name              {:type "string"},
                                                                   :depth             {:type "integer"},
                                                                   :termDefinition    {:type "string"},
                                                                   :Version           {:type "string"},
                                                                   :rgt               {:type "integer"}}}}
                     :parameterinstrument  {:uri     (str URL_ROOT "/api/terninstruments")
                                            :options nil
                                            :schema  {:type       "object",
                                                      :properties {:tree_id           {:type "integer"},
                                                                   :children_count    {:type "integer"},
                                                                   :vocabularyVersion {:type "string"},
                                                                   :Definition        {:type "string"},
                                                                   :is_selectable     {:type "boolean"},
                                                                   :lft               {:type "integer"},
                                                                   :term              {:type "string"},
                                                                   :URI               {:type "string"},
                                                                   :vocabularyTermURL {:type "string"},
                                                                   :id                {:type "integer"},
                                                                   :Name              {:type "string"},
                                                                   :depth             {:type "integer"},
                                                                   :termDefinition    {:type "string"},
                                                                   :Version           {:type "string"},
                                                                   :rgt               {:type "integer"}}}}
                     :parameterplatform    {:uri     (str URL_ROOT "/api/ternplatforms")
                                            :options nil
                                            :schema  {:type       "object",
                                                      :properties {:tree_id           {:type "integer"},
                                                                   :children_count    {:type "integer"},
                                                                   :vocabularyVersion {:type "string"},
                                                                   :Definition        {:type "string"},
                                                                   :is_selectable     {:type "boolean"},
                                                                   :lft               {:type "integer"},
                                                                   :term              {:type "string"},
                                                                   :URI               {:type "string"},
                                                                   :vocabularyTermURL {:type "string"},
                                                                   :id                {:type "integer"},
                                                                   :Name              {:type "string"},
                                                                   :depth             {:type "integer"},
                                                                   :termDefinition    {:type "string"},
                                                                   :Version           {:type "string"},
                                                                   :rgt               {:type "integer"}}}}
                     :rolecode             {:uri     (str URL_ROOT "/api/rolecode.json")
                                            :options nil
                                            :schema  {:type       "object",
                                                      :properties {:UUID        {:type "string"},
                                                                   :Identifier  {:type "string"},
                                                                   :Description {:type "string"}}}}
                     :samplingFrequency    {:uri     (str URL_ROOT "/api/samplingfrequency.json")
                                            :options nil
                                            :schema  {:type       "object",
                                                      :properties {:uri               {:type "string"},
                                                                   :prefLabel         {:type "string"},
                                                                   :prefLabelSortText {:type "string"}}}}
                     :horizontalResolution {:uri     (str URL_ROOT "/api/horizontalresolution.json")
                                            :options nil
                                            :schema  {:type       "object",
                                                      :properties {:uri               {:type "string"},
                                                                   :prefLabel         {:type "string"},
                                                                   :prefLabelSortText {:type "string"}},}
                                            }
                     :person               {:uri     (str URL_ROOT "/api/person.json")
                                            :options nil
                                            :schema  {:type       "object",
                                                      :properties {:prefLabel             {:type "string"},
                                                                   :isUserAdded           {:type "boolean"},
                                                                   :honorificPrefix       {:type "string"},
                                                                   :orcid                 {:type "string"},
                                                                   :familyName            {:type "string"},
                                                                   :electronicMailAddress {:type "string"},
                                                                   :id                    {:type "integer"},
                                                                   :givenName             {:type "string"},
                                                                   :orgUri                {:type "string"},
                                                                   :uri                   {:type "string"}},}}
                     :institution          {:uri     (str URL_ROOT "/api/institution.json")
                                            :options nil
                                            :schema  {:type       "object",
                                                      :properties {:exactMatch         {:type "string"},
                                                                   :prefLabel          {:type "string"},
                                                                   :deliveryPoint2     {:type "string"},
                                                                   :isUserAdded        {:type "boolean"},
                                                                   :altLabel           {:type "string"},
                                                                   :city               {:type "string"},
                                                                   :administrativeArea {:type "string"},
                                                                   :deliveryPoint      {:type "string"},
                                                                   :id                 {:type "integer"},
                                                                   :postalCode         {:type "string"},
                                                                   :uri                {:type "string"},
                                                                   :organisationName   {:type "string"},
                                                                   :country            {:type "string"}}}}
                     :topiccategory        {:uri     (str URL_ROOT "/api/topiccategory.json")
                                            :options nil
                                            :schema  {:type       "object",
                                                      :properties {:identifier {:type "string"},
                                                                   :name       {:type "string"}},}}})
        (update :form initialise-form))))
