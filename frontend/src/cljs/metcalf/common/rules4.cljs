(ns metcalf.common.rules4
  (:require [cljs.spec.alpha :as s]
            [clojure.string :as string]
            [interop.cljs-time :as cljs-time]
            [metcalf.common.blocks4 :as blocks4]
            [metcalf.common.utils4 :refer [log]]))

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
    (let [value (blocks4/as-data block)]
      (-> block
          (assoc-in [:props :required] true)
          (cond-> (contains? empty-values value)
                  (update-in [:props :errors] conj "This field is required"))))
    block))

(defn first-comma-last
  "Raise an error if the string field value isn't formatted as 'last name, first name'"
  [block]
  (let [value (blocks4/as-data block)]
    (cond-> block
      (and (not (string/blank? value))
           (not (string/includes? value ", ")))
      (update-in [:props :errors] conj "Name must be formatted 'Last name, First name'"))))

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

(defn- -enforce-required-subfields
  [block field-list]
  (reduce
    (fn [blk fld]
      (let [val (blocks4/as-data (get-in blk (blocks4/block-path fld)))]
        (cond-> blk
          (contains? empty-values val)
          (update-in (conj (blocks4/block-path fld) :props)
                     merge {:required true
                            :errors   ["Field is required"]}))))
    block
    field-list))

(defn tern-org-or-person
  "Certain entities (responsible party, point-of-contact) can be either
  organisations or people. This means we can't just make all fields
  required as the non-selected type would raise an error, so we
  hard-code the required fields here and select based on party-type."
  [block]
  (let [party-type (get-in block [:content "partyType" :props :value])]
    (case party-type
      "person"
      (-enforce-required-subfields block (map #(vector "contact" %) ["given_name" "surname" "email"]))

      "organisation"
      (-enforce-required-subfields block (map #(vector "organisation" %) ["name"]))

      ;; default
      (do (log {:level :warn
                :msg   "Unexpected partyType"
                :data  party-type})
          block))))


(defn required-at-least-one
  "Sometimes a requirement can have multiple possibilities, for example
  a contact could be a person or an organisation. This allows us to
  say that at least one is mandatory."
  [block {:keys [fields-list]}]
  (let [invalid? (->> fields-list
                      (map (fn [fld] (blocks4/as-data (get-in block (blocks4/block-path fld)))))
                      (every? (partial contains? empty-values)))]
    (cond-> block
      invalid? (update-in [:props :errors] conj "Missing required field"))))

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

(defn force-positive
  "A numeric field should have a positive value (does not assume that a
  value exists)"
  [block]
  (let [val (get-in block [:props :value])
        negative? (and val (> 0 (js/parseFloat val)))]
    (cond-> block
      negative?
      (update-in [:props :errors] conj "Value must be positive"))))

(defn numeric-order
  "Two numeric fields (eg a min and a max) should appear in order."
  [block {:keys [field-min field-max]}]
  (let [fmin (get-in block [:content field-min :props :value])
        fmax (get-in block [:content field-max :props :value])
        out-of-order? (and (not (string/blank? fmin))       ; FIXME: payload includes "" instead of null
                           (not (string/blank? fmax))       ; FIXME: payload includes "" instead of null
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
                 (str "Must be after " (cljs-time/humanize-date (cljs-time/value-to-date d0)))))))

(defn date-before-today
  "Date must be historic, ie before current date"
  [date-block]
  (if-let [value (cljs-time/value-to-date (blocks4/as-data date-block))]
    (-> date-block
        (cond-> (> value (js/Date.))
                (update-in [:props :errors] conj "Date must be before today")))
    date-block))

(defn geography-required
  "Geography fields are required / included based on geographic coverage checkbox"
  [geographicElement]
  (let [shown? (get-in geographicElement [:content "hasGeographicCoverage" :props :value])
        props (if shown?
                {:required true :is-hidden false}
                {:required false :disabled true :is-hidden true})]
    (s/assert (s/nilable boolean?) shown?)
    (-> geographicElement
        (update-in [:content "boxes" :props] merge props)
        (update-in [:content "boxes"] required-field (:required props))
        (cond-> (not shown?) (assoc-in [:content "boxes" :content] [])))))

(defn spatial-resolution-units
  "Depending on the resolution attribute chosen, the units for the value
  field should change"
  [spatial-block]
  (let [resolution-attribute (get-in spatial-block [:content "ResolutionAttribute" :props :value])
        units (case resolution-attribute
                "None" ""
                "Denominator scale" "Unitless"
                "Angular distance" "Degrees"
                "Metres")]
    (-> spatial-block
        (assoc-in [:content "ResolutionAttributeUnits" :props :value] units)
        (update-in [:content "ResolutionAttributeValue" :props]
                   merge {:required (not= resolution-attribute "None")
                          :disabled (= resolution-attribute "None")}))))

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
          (update-in [:content "maximumValue" :props] dissoc :value)
          (update-in [:content "minimumValue" :props] dissoc :value)
          (update-in [:content "verticalCRS"] dissoc :content)
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
                {:disabled true :value nil :required false})]
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
