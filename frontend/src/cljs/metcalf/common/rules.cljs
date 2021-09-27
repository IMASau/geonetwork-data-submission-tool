(ns metcalf.common.rules
  (:require [cljs.spec.alpha :as s]
            [clojure.string :as string]
            [interop.date :as date]))

(def ^:dynamic rule-registry {})

(defn get-rule-handler
  [rule-id]
  (get-in rule-registry [(name rule-id) :handler] identity))

(defn apply-rule
  [block rule-id rule-data]
  ((get-rule-handler rule-id) block rule-data))

(defn seq-rules
  "Support rules as string, map and collection"
  [rules]
  (cond (string? rules) (seq {rules true})
        (map? rules) (seq rules)
        (vector? rules) (mapcat seq-rules rules)))

(defn apply-rules
  [block]
  (reduce-kv apply-rule block (seq-rules (:rules block))))

(def empty-values #{nil "" [] {} #{}})

(defn required-field
  [block required]
  (s/assert #{true} required)
  (let [value (get-in block [:properties :value])]
    (-> block
        (assoc-in [:properties :required] true)
        (cond-> (contains? empty-values value)
                (update-in [:properties :errors] conj "This field is required")))))

(defn max-length
  [block maxLength]
  (assoc-in block [:properties :maxLength] maxLength))

(defn date-order
  "Start date should be fore end date"
  [block {:keys [field0 field1]}]
  (let [k0 (keyword field0)                                 ; FIXME: ugly converions to keyword
        k1 (keyword field1)                                 ; FIXME: ugly converions to keyword
        d0 (get-in block [:content k0 :properties :value])
        d1 (get-in block [:content k1 :properties :value])
        r [d0 d1]
        out-of-order? (and (not (string/blank? d0))         ; FIXME: payload includes "" instead of null
                           (not (string/blank? d1))         ; FIXME: payload includes "" instead of null
                           (not= r (sort r)))]
    (cond-> block
      out-of-order?
      (update-in [:content k1 :properties :errors] conj
                 (str "Must be after " (date/to-string (date/from-value d0)))))))

(defn geography-required
  "Geography fields are required / included based on geographic coverage checkbox"
  [geographicElement]
  (let [shown? (get-in geographicElement [:content :hasGeographicCoverage :properties :value])
        properties (if shown?
                     {:required true}
                     {:required false :disabled true})]
    (s/assert boolean? shown?)
    (update-in geographicElement [:content :boxes :properties] merge properties)))

(defn license-other
  [identificationInfo]
  (let [license-value (get-in identificationInfo [:content :creativeCommons :properties :value])
        other? (= license-value "http://creativecommons.org/licenses/other")
        properties (if other?
                     {:is-hidden false :disabled false :required true}
                     {:is-hidden true :disabled true :required false})]
    (update-in identificationInfo [:content :otherConstraints :properties] merge properties)))

(defn end-position
  "End position is required if the status is ongoing"
  [identificationInfo]
  (let [value (get-in identificationInfo [:content :status :properties :value])
        properties (if (contains? #{"onGoing" "planned"} value)
                     {:required false :disabled true :value nil}
                     {:required true :disabled false})]
    (update-in identificationInfo [:content :endPosition :properties] merge properties)))

(defn maint-freq
  [identificationInfo]
  (let [status-value (get-in identificationInfo [:content :status :properties :value])
        properties (case status-value
                     "onGoing" {:is-hidden false :disabled false :required true}
                     "planned" {:is-hidden false :disabled false :required true}
                     "completed" {:is-hidden false :disabled true :value "notPlanned" :required false}
                     {:is-hidden true :disabled true :value "" :required false})]
    (update-in identificationInfo [:content :maintenanceAndUpdateFrequency :properties] merge properties)))

(defn vertical-required
  "Vertical fields are required / included based on vertical extent checkbox"
  [verticalElement]
  (let [shown? (get-in verticalElement [:hasVerticalExtent :value])]
    (if shown?
      (-> verticalElement
          (assoc-in [:content :elevation :properties :required] true)
          (assoc-in [:content :maximumValue :properties :required] true)
          (assoc-in [:content :method :properties :required] true)
          (assoc-in [:content :minimumValue :properties :required] true))
      (-> verticalElement
          ; TODO: make required false by default
          (assoc-in [:content :elevation :properties :required] false)
          (assoc-in [:content :maximumValue :properties :required] false)
          (assoc-in [:content :method :properties :required] false)
          (assoc-in [:content :minimumValue :properties :required] false)
          (assoc-in [:content :elevation :properties :disabled] true)
          (assoc-in [:content :maximumValue :properties :disabled] true)
          (assoc-in [:content :method :properties :disabled] true)
          (assoc-in [:content :minimumValue :properties :disabled] true)))))