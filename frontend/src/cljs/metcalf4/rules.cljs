(ns metcalf4.rules
  (:require [cljs.spec.alpha :as s]
            [clojure.string :as string]
            [interop.date :as date]
            [metcalf4.blocks :as blocks]))

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

;NOTE: It's possible that require-field is poor fit for objects/arrays
(defn required-field
  [block required]
  (s/assert boolean? required)
  (if required
    (let [value (blocks/as-data block)]
      (-> block
          (assoc-in [:props :required] true)
          (cond-> (contains? empty-values value)
                  (update-in [:props :errors] conj "This field is required"))))
    block))

(defn required-all-or-nothing
  "Handles cases where a group of fields are mandatory if set; ie if you
  set one, they should all be set"
  [block {:keys [fields-list]}]
  (let [vals (map #(get-in block [:content % :props :value]) fields-list)
        required? (some (complement nil?) vals)
        kvs (zipmap fields-list vals)]
    (reduce
     (fn [blk [fld val]]
       (cond-> blk
         true
         (assoc-in [:content fld :props :required] required?)
         (and (not val) required?)
         (update-in [:content fld :props :errors] conj "This field is required")))
     block
     kvs)))

; TODO: consider renaming - doing more than required flag (disable/hide/clear)
(defn required-when-yes
  [block {:keys [bool-field opt-field]}]
  (let [required? (get-in block [:content bool-field :props :value])
        opt-type (get-in block [:content opt-field :type])
        props (if required?
                {:required true :is-hidden false}
                {:required false :is-hidden true :disabled true :value nil})]
    (-> block
        (update-in [:content opt-field :props] merge props)
        (cond->
          (and (not required?)
               (= "object" opt-type))
          (assoc-in [:content opt-field :content] {})))))

(defn max-length
  [block maxLength]
  (assoc-in block [:props :maxLength] maxLength))

(defn numeric-order
  "Two numeric fields (eg a min and a max) should appear in order."
  [block {:keys [field-min field-max]}]
  (let [fmin (get-in block [:content field-min :props :value])
        fmax (get-in block [:content field-max :props :value])
        out-of-order? (and (not (string/blank? fmin))         ; FIXME: payload includes "" instead of null
                           (not (string/blank? fmax))         ; FIXME: payload includes "" instead of null
                           (< fmax fmin))]
    (cond-> block
      out-of-order?
      (update-in [:content field-max :props :errors]
                 conj (str "Must be greater than " fmin)))))

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
          (assoc-in [:content "verticalCRS" :props :required] true)
          (update-in [:content "maximumValue"] required-field true)
          (update-in [:content "minimumValue"] required-field true)
          (update-in [:content "verticalCRS"] required-field true))
      (-> verticalElement
          (assoc-in [:content "maximumValue" :props :required] false)
          (assoc-in [:content "minimumValue" :props :required] false)
          (assoc-in [:content "verticalCRS" :props :required] false)
          (assoc-in [:content "maximumValue" :props :disabled] true)
          (assoc-in [:content "minimumValue" :props :disabled] true)
          (assoc-in [:content "verticalCRS" :props :disabled] true)
          (assoc-in [:content "maximumValue" :props :is-hidden] true)
          (assoc-in [:content "minimumValue" :props :is-hidden] true)
          (assoc-in [:content "verticalCRS" :props :is-hidden] true)))))

(defn license-other
  [identificationInfo]
  (let [license-value (get-in identificationInfo [:content "creativeCommons" :props :value])
        other? (= license-value "http://creativecommons.org/licenses/other")
        props (if other?
                {:is-hidden false :required true}
                {:is-hidden true :disabled true :required false :value nil})]
    (-> identificationInfo
        (update-in [:content "otherConstraints" :props] merge props)
        (update-in [:content "otherConstraints"] required-field (:required props)))))

(defn end-position
  "End position is required if the status is ongoing"
  [identificationInfo]
  (let [value (get-in identificationInfo [:content "status" :props :value])
        props (if (contains? #{"onGoing" "planned"} value)
                {:required false :disabled true :value nil}
                {:required true})]
    (-> identificationInfo
        (update-in [:content "endPosition" :props] merge props)
        (update-in [:content "endPosition"] required-field (:required props)))))

(defn maint-freq
  [identificationInfo]
  (let [status-value (get-in identificationInfo [:content "status" :props :value])
        props (case status-value
                "onGoing" {:is-hidden false :required true}
                "planned" {:is-hidden false :required true}
                "completed" {:is-hidden false :disabled true :value "notPlanned" :required false}
                {:is-hidden true :disabled true :value nil :required false})]
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
