(ns metcalf.common.logic3
  (:require [clojure.string :as string]
            [metcalf.common.blocks4 :as blocks4]
            [metcalf.common.logic4 :as logic4]
            [metcalf.common.rules4 :as rules4]
            [metcalf.common.utils3 :as utils3]))

(def active-status-filter #{"Draft" "Submitted"})

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

(def disabled-statuses #{"Archived" "Deleted" "Uploaded"})

(defn disable-form-when-submitted [state]
  (assoc-in state [:form :disabled] (contains? disabled-statuses (get-in state [:context :document :status]))))

(defn derive-data-state [state]
  (-> state
      (update-in [:form :state] validate-rules)
      disable-form-when-submitted
      ))

(defn derived-state
  "Used to include derived state for use by components."
  [{:keys [data] :as state}]
  (cond-> state data derive-data-state))

(defn path-values
  [data]
  (let [get-value #(get-in data %)
        get-path #(mapcat concat (partition-by number? %) (repeat [:value]))]
    (map (juxt get-path get-value)
         (utils3/keys-in data))))

(defn setup-form-action
  "Massage raw payload for use as app-state"
  [s form]
  (cond-> s form (assoc-in [:db :form] (logic4/massage-form form))))

(defn setup-alerts
  [s]
  (assoc-in s [:db :modal/stack] []))
