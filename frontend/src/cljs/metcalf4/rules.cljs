(ns metcalf4.rules
  (:require [cljs.spec.alpha :as s]
            [clojure.string :as string]
            [interop.date :as date]))

(def ^:dynamic rule-registry {})

(defn get-rule-handler
  [rule-id]
  (assert (get rule-registry (name rule-id)) (str "No rule handler found for " (pr-str rule-id)))
  (get rule-registry (name rule-id) identity))

(defn apply-rule
  [block [rule-id rule-data]]
  ((get-rule-handler rule-id) block rule-data))

(defn seq-rules
  "Support rules as string, map and collection"
  [rules]
  (cond (string? rules) (seq {rules true})
        (map? rules) (seq rules)
        (vector? rules) (mapcat seq-rules rules)))

(defn apply-rules
  [block]
  (reduce apply-rule block (seq-rules (:rules block))))

(def empty-values #{nil "" [] {} #{}})

(defn required-field
  [block required]
  (s/assert boolean? required)
  (if required
    (let [value (if (= (:type block) "array")
                  (get-in block [:content])
                  (get-in block [:props :value]))]
      (-> block
          (assoc-in [:props :required] true)
          (cond-> (contains? empty-values value)
                  (update-in [:props :errors] conj "This field is required"))))
    block))

(defn max-length
  [block maxLength]
  (assoc-in block [:props :maxLength] maxLength))

(defn date-order
  "Start date should be before end date"
  [block {:keys [field0 field1]}]
  (let [d0 (get-in block [:content field0 :props :value])
        d1 (get-in block [:content field1 :props :value])
        r [d0 d1]
        out-of-order? (and (not (string/blank? d0))         ; FIXME: payload includes "" instead of null
                           (not (string/blank? d1))         ; FIXME: payload includes "" instead of null
                           (not= r (sort r)))]
    (cond-> block
            out-of-order?
            (update-in [:content field1 :props :errors] conj
                       (str "Must be after " (date/to-string (date/from-value d0)))))))

(defn geography-required
  "Geography fields are required / included based on geographic coverage checkbox"
  [geographicElement]
  (let [shown? (get-in geographicElement [:content "hasGeographicCoverage" :props :value])
        props (if shown?
                {:required true :is-hidden false}
                {:required false :disabled true :is-hidden true})]
    (s/assert boolean? shown?)
    (-> geographicElement
        (update-in [:content "boxes" :props] merge props)
        (update-in [:content "boxes"] required-field (:required props)))))

(defn imas-vertical-required
  "Vertical fields are required / included based on vertical extent checkbox"
  [verticalElement]
  (let [shown? (get-in verticalElement [:content "hasVerticalExtent" :props :value])]
    (if shown?
      (-> verticalElement
          (assoc-in [:content "maximumValue" :props :required] true)
          (assoc-in [:content "minimumValue" :props :required] true)
          (update-in [:content "maximumValue"] required-field true)
          (update-in [:content "minimumValue"] required-field true))
      (-> verticalElement
          (assoc-in [:content "maximumValue" :props :required] false)
          (assoc-in [:content "minimumValue" :props :required] false)
          (assoc-in [:content "maximumValue" :props :disabled] true)
          (assoc-in [:content "minimumValue" :props :disabled] true)))))

(defn license-other
  [identificationInfo]
  (let [license-value (get-in identificationInfo [:content "creativeCommons" :props :value])
        other? (= license-value "http://creativecommons.org/licenses/other")
        props (if other?
                {:is-hidden false :disabled false :required true}
                {:is-hidden true :disabled true :required false})]
    (-> identificationInfo
      (update-in [:content "otherConstraints" :props] merge props)
      (update-in [:content "otherConstraints"] required-field (:required props)))))

(defn end-position
  "End position is required if the status is ongoing"
  [identificationInfo]
  (let [value (get-in identificationInfo [:content "status" :props :value])
        props (if (contains? #{"onGoing" "planned"} value)
                {:required false :disabled true :value nil}
                {:required true :disabled false})]
    (-> identificationInfo
        (update-in [:content "endPosition" :props] merge props)
        (update-in [:content "endPosition"] required-field (:required props)))))

(defn maint-freq
  [identificationInfo]
  (let [status-value (get-in identificationInfo [:content "status" :props :value])
        props (case status-value
                "onGoing" {:is-hidden false :disabled false :required true}
                "planned" {:is-hidden false :disabled false :required true}
                "completed" {:is-hidden false :disabled true :value "notPlanned" :required false}
                {:is-hidden true :disabled true :value "" :required false})]
    (-> identificationInfo
        (update-in [:content "maintenanceAndUpdateFrequency" :props] merge props)
        (update-in [:content "maintenanceAndUpdateFrequency"] required-field (:required props)))))

(defn vertical-required
  "Vertical fields are required / included based on vertical extent checkbox"
  [verticalElement]
  (let [shown? (get-in verticalElement [:hasVerticalExtent :value])]
    (if shown?
      (-> verticalElement
          (assoc-in [:content "elevation" :props :required] true)
          (assoc-in [:content "maximumValue" :props :required] true)
          (assoc-in [:content "method" :props :required] true)
          (assoc-in [:content "minimumValue" :props :required] true))
      (-> verticalElement
          ; TODO: make required false by default
          (assoc-in [:content "elevation" :props :required] false)
          (assoc-in [:content "maximumValue" :props :required] false)
          (assoc-in [:content "method" :props :required] false)
          (assoc-in [:content "minimumValue" :props :required] false)
          (assoc-in [:content "elevation" :props :disabled] true)
          (assoc-in [:content "maximumValue" :props :disabled] true)
          (assoc-in [:content "method" :props :disabled] true)
          (assoc-in [:content "minimumValue" :props :disabled] true)))))
