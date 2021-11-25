(ns metcalf.common.logic3
  (:require [clojure.string :as string]
            [metcalf.common.blocks4 :as blocks4]
            [metcalf.common.logic4 :as logic4]
            [metcalf.common.rules4 :as rules4]
            [metcalf.common.utils3 :as utils3]))

(def active-status-filter #{"Draft" "Submitted"})

(defn score-object [block]
  (let [{:keys [content]} block]
    (apply merge-with + (map ::score (vals content)))))

(defn score-array [block]
  (let [{:keys [content]} block]
    (apply merge-with + (map ::score content))))

(defn score-value [block]
  (let [{:keys [props]} block
        {:keys [value]} props]
    (when (string/blank? value)
      {:empty 1})))

(defn score-props [block]
  (let [{:keys [props]} block
        {:keys [required errors]} props]
    (-> {:fields 1}
        (cond-> required (assoc :required 1))
        (cond-> (seq errors) (assoc :errors 1))
        (cond-> (and required (seq errors)) (assoc :required-errors 1)))))

(defn score-block
  "Score block considering props and content"
  [block]
  (let [{:keys [type props]} block
        {:keys [disabled]} props
        score (when-not disabled
                (case type
                  "array" (merge-with + (score-array block) (score-props block))
                  "object" (merge-with + (score-object block) (score-props block))
                  (merge-with + (score-value block) (score-props block))))]
    (assoc block ::score score)))

; TODO: Use field-zipper.
; TODO: Store non-field errors (:non_field_errors form)
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

(defn validate-rules
  [state]
  (blocks4/postwalk rules4/apply-rules state))

(def contact-groups
  [{:path  [:form :fields :identificationInfo :pointOfContact]
    :title "Point of contact for dataset"}
   {:path  [:form :fields :identificationInfo :citedResponsibleParty]
    :title "Responsible parties for creating dataset"}])

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

(defn calculate-progress [db form-path]
  (let [form (get-in db form-path)
        state (:state form)
        state' (blocks4/postwalk score-block state)]
    (assoc db :progress (::score state'))))

(def disabled-statuses #{"Archived" "Deleted" "Uploaded"})

(defn disable-form-when-submitted [state]
  (assoc-in state [:form :disabled] (contains? disabled-statuses (get-in state [:context :document :status]))))

(defn derive-data-state [state]
  (-> state
      data-service-logic
      (update-in [:form :state] validate-rules)
      disable-form-when-submitted
      ;(update-in [:form] disabled-form-logic)
      (calculate-progress [:form])
      ))

(defn derived-state
  "Used to include derived state for use by components."
  [{:keys [data] :as state}]
  (cond-> state
    data derive-data-state
    ))

(defn path-fields [data]
  (into (sorted-set)
        (keep (fn [path]
                (let [[parent [i k]] (split-with (complement integer?) path)]
                  (when k
                    (let [parent (vec parent)]
                      [(conj parent :fields k)
                       (conj parent :value i :value k)]))))
              (utils3/keys-in data))))

(defn reduce-many-field-templates
  "For each many field value "
  [fields values]
  (reduce (fn [m [tpl-path value-path]]
            (try
              (utils3/int-assoc-in m value-path (get-in fields tpl-path))
              (catch js/Error _ m)))
          fields (path-fields values)))

(defn path-values
  [data]
  (let [get-value #(get-in data %)
        get-path #(mapcat concat (partition-by number? %) (repeat [:value]))]
    (map (juxt get-path get-value)
         (utils3/keys-in data))))

(defn reduce-field-values [fields values]
  (reduce (fn [m [p v]]
            (try (utils3/int-assoc-in m p v)
                 (catch :default e
                   (js/console.error (clj->js [m p v]) e)
                   m)))
          fields (path-values values)))

; TODO: remove or replace?
(defn initialise-form
  ([{:keys [data] :as form}]
   (initialise-form form data))
  ([form data]
   (-> form
       ;(reset-form)
       (assoc :data data)
       (update :fields reduce-many-field-templates data)
       (update :fields reduce-field-values data))))

(defn setup-form-action
  "Massage raw payload for use as app-state"
  [s form]
  (-> s
      (cond-> form (assoc-in [:db :form] (logic4/massage-form form)))
      (update-in [:db :form] initialise-form)))

(defn setup-alerts
  [s]
  (assoc-in s [:db :modal/stack] []))

(defn initial-state-action
  "Massage raw payload for use as app-state"
  [s {:keys [form] :as payload}]
  (-> s
      (assoc :db payload)
      (setup-form-action form)
      (setup-alerts)))
