(ns metcalf4.components
  (:require [cljs.spec.alpha :as s]
            [clojure.string :as string]
            [interop.date :as date]
            [interop.ui :as ui]
            [metcalf4.blocks :as blocks]
            [metcalf4.subs :as common-subs]
            [metcalf4.utils :as utils4]
            [re-frame.core :as rf]))

(defn has-error?
  "Given the current form state, and a data path, check if
  the field for that data path has errors."
  [form-state data-path]
  (let [path (blocks/block-path data-path)
        field (get-in form-state path)
        errors (-> field :props :errors)]
    (seq errors)))

(defn page-errors
  [{:keys [form-id data-paths]}]
  (letfn [(field-error [field]
            (let [{:keys [errors label]} (:props field)]
              [:span.FieldError label ": " (first errors)]))
          (many-field-error [field]
            (let [{:keys [errors label]} (:props field)]
              [:span.FieldError label ": " (or (first errors) "check field errors")]))]
    (let [form-state @(rf/subscribe [::common-subs/get-form-state form-id])
          paths-to-check-for-errors (remove #(not (has-error? form-state %)) data-paths)
          msgs (for [data-path paths-to-check-for-errors]
                 (let [path (blocks/block-path data-path)
                       field (get-in form-state path)]
                   (if (-> field :props :many)
                     [many-field-error field]
                     [field-error field])))]
      (when (seq msgs)
        [:div.alert.alert-warning
         (if (> (count msgs) 1)
           [:div
            [:b "There are multiple fields on this page that require your attention:"]
            (into [:ul] (for [msg msgs] [:li msg]))]
           (first msgs))]))))

(defn input-field-with-label
  [config]
  (let [ctx (utils4/get-ctx config)
        logic @(rf/subscribe [::get-input-field-with-label-props ctx])
        onChange #(rf/dispatch [::input-field-with-label-value-changed ctx %])
        props (merge logic (select-keys config [:label :placeholder :helperText :toolTip]))
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
  (let [ctx (utils4/get-ctx config)
        logic @(rf/subscribe [::get-textarea-field-with-label-props ctx])
        onChange #(rf/dispatch [::textarea-field-with-label-value-changed ctx %])
        config-keys [:label :placeholder :helperText :toolTip :rows]
        props (merge logic (select-keys config config-keys))
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
  (let [ctx (utils4/get-ctx config)
        config-keys [:label :helperText :toolTip :minDate :maxDate]
        logic @(rf/subscribe [::get-date-field-with-label-props ctx])
        onChange #(rf/dispatch [::date-field-with-label-value-changed ctx (date/to-value %)])
        props (merge logic (select-keys config config-keys))
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

(defn select-option-with-label
  [config]
  (let [ctx (utils4/get-ctx config)
        config-keys [:options :label]
        logic @(rf/subscribe [::get-select-option-with-label-props ctx])
        onChange #(rf/dispatch [::select-option-with-label-value-changed ctx %])
        props (merge logic (select-keys config config-keys))
        {:keys [label required placeholder helperText toolTip options
                value disabled errors show-errors]} props
        hasError (when (and show-errors (seq errors)) true)]
    [ui/FormGroup
     {:label      label
      :required   required
      :disabled   disabled
      :hasError   hasError
      :helperText (if hasError (string/join ". " errors) helperText)
      :toolTip    toolTip}
     [ui/SelectField
      {:value       value
       :options     options
       :placeholder placeholder
       :disabled    disabled
       :hasError    (seq hasError)
       :onChange    onChange}]]))

(defn select-value-with-label
  [config]
  (let [ctx (utils4/get-ctx config)
        config-keys [:options :label]
        logic @(rf/subscribe [::get-select-value-with-label-props ctx])
        onChange #(rf/dispatch [::select-value-with-label-changed ctx %])
        props (merge logic (select-keys config config-keys))
        {:keys [label required helperText toolTip options value disabled errors show-errors]} props
        hasError (when (and show-errors (seq errors)) true)
        value (or value "")]
    [ui/FormGroup
     {:label      label
      :required   required
      :disabled   disabled
      :hasError   hasError
      :helperText (if hasError (string/join ". " errors) helperText)
      :toolTip    toolTip}
     [ui/SelectValueField
      {:value    value
       :disabled disabled
       :options  options
       :hasError hasError
       :onChange onChange}]]))