(ns metcalf4.components
  (:require [cljs.spec.alpha :as s]
            [clojure.string :as string]
            [interop.date :as date]
            [interop.ui :as ui]
            [re-frame.core :as rf]))

(s/def ::form-id vector?)
(s/def ::data-path vector?)
(s/def ::ctx (s/keys :req-un [::form-id ::data-path]))

(defn get-ctx
  [{:keys [form-id data-path]}]
  {:form-id form-id :data-path data-path})

(defn input-field-with-label
  [config]
  (s/assert ::ctx config)
  (let [ctx (get-ctx config)
        logic @(rf/subscribe [::get-input-field-with-label-props ctx])
        onChange #(rf/dispatch [::input-field-with-label-value-changed ctx %])
        props (merge (select-keys config [:label :placeholder :helperText :toolTip]) logic)
        {:keys [label placeholder helperText toolTip maxLength required value disabled show-errors errors]} props
        hasError (when (and show-errors (seq errors)) true)]
    [ui/FormGroup
     {:label      label
      :required   required
      :disabled   disabled
      :hasError   hasError
      :helperText (if hasError (string/join ". " errors) helperText)
      :toolTip    toolTip}
     [ui/InputField
      {:value       (or value "")                           ; TODO: should be guaranteed by sub
       :placeholder placeholder
       :maxLength   maxLength
       :disabled    disabled
       :hasError    hasError
       :onChange    onChange}]]))


(defn textarea-field-with-label
  [config]
  (s/assert ::ctx config)
  (let [ctx (get-ctx config)
        logic @(rf/subscribe [::get-textarea-field-with-label-props ctx])
        onChange #(rf/dispatch [::textarea-field-with-label-value-changed ctx %])
        config-keys [:label :placeholder :helperText :toolTip :rows]
        props (merge (select-keys config config-keys) logic)
        {:keys [label placeholder helperText toolTip rows
                maxLength required value disabled show-errors errors]} props
        hasError (when (and show-errors (seq errors)) true)]
    [ui/FormGroup
     {:label      label
      :required   required
      :disabled   disabled
      :hasError   hasError
      :helperText (if hasError (string/join ". " errors) helperText)
      :toolTip    toolTip}
     [ui/TextareaField
      {:value       (or value "")                           ; TODO: should be guaranteed by sub
       :placeholder placeholder
       :disabled    disabled
       :hasError    hasError
       :maxLength   maxLength
       :rows        rows
       :onChange    onChange}]]))


(defn date-field-with-label
  [config]
  (s/assert ::ctx config)
  (let [path (get-ctx config)
        config-keys [:label :required :helperText :toolTip :minDate :maxDate]
        logic @(rf/subscribe [::get-date-field-with-label-props path])
        onChange #(rf/dispatch [::date-field-with-label-value-changed path (date/to-value %)])
        props (merge (select-keys config config-keys) logic)
        {:keys [label required helperText toolTip minDate maxDate
                value disabled errors show-errors]} props
        hasError (when (and show-errors (seq errors)) true)]
    [ui/FormGroup
     {:label      label
      :required   required
      :disabled   disabled
      :hasError   hasError
      :helperText (if hasError (string/join ". " errors) helperText)
      :toolTip    toolTip}
     [ui/DateField
      {:value    (date/from-value value)
       :disabled disabled
       :onChange onChange
       :hasError hasError
       :minDate  (date/from-value minDate)
       :maxDate  (date/from-value maxDate)}]]))
