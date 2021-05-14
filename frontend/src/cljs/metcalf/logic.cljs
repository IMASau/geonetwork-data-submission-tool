(ns metcalf.logic
  (:require
    [cljs.core.match :refer-macros [match]]
    [clojure.walk :refer [postwalk]]
    [condense.fields :refer [validate-required-field]]
    [condense.utils :refer [keys-in int-assoc-in fmap]]
    [metcalf.progress :refer [progress-score]]
    [om-tick.field :refer [tree-edit field-edit field-zipper field?]]))


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
    (map? form) (outer (fmap inner form))
    (seq? form) (outer (doall (map inner form)))
    ; Lists (e.g. many value list)
    (coll? form) (outer (into (empty form) (map inner form)))
    :else (outer form)))


(defn field-postwalk
  [f form]
  (field-walk (partial field-postwalk f) f form))


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


(defn dirty-form-check
  "Check if the form contains unsaved data by comparing :data with field :values"
  [{:keys [data fields] :as form}]
  (assoc form :dirty (not= data (extract-field-values fields))))


(defn is-required-field?
  "Identifies walker nodes which are fields relevant to require logic"
  [m]
  (and (map? m)
       (:required m)
       (contains? m :value)))

(defn validate-required-fields
  "Derive errors associated with missing required fields"
  [state]
  (field-postwalk
    #(if (is-required-field? %) (validate-required-field %) %)
    state))

(defn vertical-required-logic
  "Vertical fields are required / included based on vertical extent checkbox"
  [verticalElement]
  (let [shown? (get-in verticalElement [:hasVerticalExtent :value])]
    (if shown?
      (-> verticalElement
          (update-in [:minimumValue] assoc :required true)
          (update-in [:maximumValue] assoc :required true)
          (update-in [:verticalCRS] assoc :required true))
      (-> verticalElement
          (update-in [:minimumValue] assoc :required false :disabled true :value nil)
          (update-in [:maximumValue] assoc :required false :disabled true :value nil)
          (update-in [:verticalCRS] assoc :required false :disabled true :value nil)))))

(defn end-position-logic
  "End position is required if the status is onGoing"
  [state]
  (let [complete? (= "onGoing" (get-in state [:form :fields :identificationInfo :status :value]))]
    (-> (if complete?
          (update-in state [:form :fields :identificationInfo :endPosition] assoc :required false :disabled true :value nil)
          (update-in state [:form :fields :identificationInfo :endPosition] assoc :required true :disabled false)))))

(defn maint-freq-logic
  "
  Maintenance resource frequency is a drop dropdown.

  If status is complete then it's hardwired to NONE PLANNED and displayed as read only value.
  "
  [state]
  (let [status-value (get-in state [:form :fields :identificationInfo :status :value])]
    (update-in state [:form :fields :identificationInfo :maintenanceAndUpdateFrequency] merge
               (match [status-value]
                      ["onGoing"] {:is-hidden false :disabled false :required true}
                      ["complete"] {:is-hidden false :disabled true :value "none-planned" :required false}
                      :else {:is-hidden true :disabled true :value "" :required false}))))

(defn license-logic
  "
  License is a drop dropdown.

  If “Other” is chosen then extra text field is displayed to enter custom license.
  "
  [state]
  (let [license-value (get-in state [:form :fields :identificationInfo :creativeCommons :value])]
    (update-in state [:form :fields :identificationInfo :otherConstraints] merge
               (match [license-value]
                 ["http://creativecommons.org/licenses/other"]
                 {:is-hidden false :disabled false :required true}

                 :else {:is-hidden true :disabled true :required false}))))

(defn derive-vertical-required [state]
  (update-in state [:form :fields :identificationInfo :verticalElement] vertical-required-logic))

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
      derive-vertical-required
      end-position-logic
      maint-freq-logic
      license-logic
      (update-in [:form :fields] validate-required-fields)
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