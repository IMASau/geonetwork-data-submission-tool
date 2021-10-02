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

(defn form-group
  [config & children]
  (let [ctx (utils4/get-ctx config)
        config-keys [:label :placeholder :helperText :toolTip]
        logic @(rf/subscribe [::get-block-props ctx])
        props (merge logic (select-keys config config-keys))
        {:keys [label helperText toolTip required disabled show-errors errors]} props
        hasError (when (and show-errors (seq errors)) true)]
    (into [ui/FormGroup
           {:label      label
            :required   required
            :disabled   disabled
            :hasError   hasError
            :helperText (if hasError (string/join ". " errors) helperText)
            :toolTip    toolTip}]
          children)))

(defn input-field
  [config]
  (let [config-keys [:placeholder :maxLength]
        ctx (utils4/get-ctx config)
        logic @(rf/subscribe [::get-block-props ctx])
        onChange #(rf/dispatch [::input-field-value-changed ctx %])
        props (merge logic (select-keys config config-keys))
        {:keys [placeholder maxLength value disabled show-errors errors]} props
        hasError (when (and show-errors (seq errors)) true)]
    [ui/InputField
     {:value       (or value "")                            ; TODO: should be guaranteed by sub
      :placeholder placeholder
      :maxLength   maxLength
      :disabled    disabled
      :hasError    hasError
      :onChange    onChange}]))

(defn input-field-with-label
  [config]
  [form-group config
   [input-field config]])

(defn textarea-field
  [config]
  (let [config-keys [:placeholder :rows :maxLength]
        ctx (utils4/get-ctx config)
        logic @(rf/subscribe [::get-block-props ctx])
        onChange #(rf/dispatch [::textarea-field-value-changed ctx %])
        props (merge logic (select-keys config config-keys))
        {:keys [placeholder rows maxLength value disabled show-errors errors]} props
        hasError (when (and show-errors (seq errors)) true)]
    [ui/TextareaField
     {:value       (or value "")                            ; TODO: should be guaranteed by sub
      :placeholder placeholder
      :disabled    disabled
      :hasError    hasError
      :maxLength   maxLength
      :rows        rows
      :onChange    onChange}]))

(defn textarea-field-with-label
  [config]
  [form-group config
   [textarea-field config]])

(defn date-field
  [config]
  (let [ctx (utils4/get-ctx config)
        config-keys [:minDate :maxDate]
        logic @(rf/subscribe [::get-block-props ctx])
        onChange #(rf/dispatch [::date-field-value-changed ctx (date/to-value %)])
        props (merge logic (select-keys config config-keys))
        {:keys [minDate maxDate value disabled errors show-errors]} props
        hasError (when (and show-errors (seq errors)) true)]
    [ui/DateField
     {:value    (date/from-value value)
      :disabled disabled
      :onChange onChange
      :hasError hasError
      :minDate  (date/from-value minDate)
      :maxDate  (date/from-value maxDate)}]))

(defn date-field-with-label
  [config]
  [form-group config
   [date-field config]])

(defn select-option
  [config]
  (let [ctx (utils4/get-ctx config)
        config-keys [:options :placeholder]
        logic @(rf/subscribe [::get-block-props ctx])
        value @(rf/subscribe [::get-block-data ctx])
        onChange #(rf/dispatch [::select-option-value-changed ctx %])
        props (merge logic (select-keys config config-keys))
        {:keys [placeholder options disabled errors show-errors]} props
        hasError (when (and show-errors (seq errors)) true)]
    [ui/SelectOptionField
     {:value       value
      :options     options
      :placeholder placeholder
      :disabled    disabled
      :hasError    (seq hasError)
      :onChange    onChange}]))

(defn select-option-with-label
  [config]
  [form-group config
   [select-option config]])

(defn async-select-option
  [config]
  (let [ctx (utils4/get-ctx config)
        config-keys [:uri]
        logic @(rf/subscribe [::get-block-props ctx])
        value @(rf/subscribe [::get-block-data ctx])
        onChange #(rf/dispatch [::async-select-option-value-changed ctx %])
        props (merge logic (select-keys config config-keys))
        {:keys [placeholder uri disabled errors show-errors]} props
        hasError (when (and show-errors (seq errors)) true)]
    [ui/AsyncSelectOptionField
     {:value       value
      :loadOptions #(utils4/fetch-post {:uri uri :body {:query %}})
      :placeholder placeholder
      :disabled    disabled
      :hasError    (seq hasError)
      :onChange    onChange}]))

(defn async-select-option-with-label
  [config]
  [form-group config
   [async-select-option config]])

(defn select-value
  [config]
  (let [ctx (utils4/get-ctx config)
        config-keys [:options]
        logic @(rf/subscribe [::get-block-props ctx])
        onChange #(rf/dispatch [::select-value-changed ctx %])
        props (merge logic (select-keys config config-keys))
        {:keys [options value disabled errors show-errors]} props
        hasError (when (and show-errors (seq errors)) true)
        value (or value "")]
    [ui/SelectValueField
     {:value    value
      :disabled disabled
      :options  options
      :hasError hasError
      :onChange onChange}]))

(defn select-value-with-label
  [config]
  [form-group config
   [select-value config]])

(defn yes-no-field
  [config]
  (let [ctx (utils4/get-ctx config)
        config-keys [:options :label]
        logic @(rf/subscribe [::get-yes-no-field-props ctx])
        onChange #(rf/dispatch [::yes-no-field-value-changed ctx %])
        props (merge logic (select-keys config config-keys))
        {:keys [label value disabled errors show-errors]} props
        hasError (when (and show-errors (seq errors)) true)]
    [ui/YesNoRadioGroup
     {:value    value
      :label    label
      :disabled disabled
      :hasError (seq hasError)
      :onChange onChange}]))

; FIXME: Is :label for form group or yes/no field?
(defn yes-no-field-with-label
  [config]
  (let [config-keys [:options :label]
        ctx (utils4/get-ctx config)
        logic @(rf/subscribe [::get-yes-no-field-with-label-props ctx])
        onChange #(rf/dispatch [::yes-no-field-with-label-value-changed ctx %])
        props (merge logic (select-keys config config-keys))
        {:keys [label value errors show-errors]} props
        hasError (when (and show-errors (seq errors)) true)]
    [form-group config
     [ui/YesNoRadioGroup
      {:value    value
       :label    label
       :disabled false
       :hasError (seq hasError)
       :onChange onChange}]]))

(defn simple-selection-list
  [config]
  (let [config-keys [:labelKey :valueKey]
        ctx (utils4/get-ctx config)
        ;onRemove #(rf/dispatch [::selection-list-remove-click ctx %])
        onReorder (fn [src-idx dst-idx] (rf/dispatch [::selection-list-reorder ctx src-idx dst-idx]))
        logic @(rf/subscribe [::get-block-props ctx])
        {:keys [key disabled labelKey valueKey]} (merge logic (select-keys config config-keys))
        items @(rf/subscribe [::get-block-data ctx])]
    [ui/SimpleSelectionList
     {:key       key
      :items     items
      :labelKey  labelKey
      :valueKey  valueKey
      :disabled  disabled
      :onReorder onReorder}]))

(defn breadcrumb-selection-list
  [config]
  (let [config-keys [:labelKey :valueKey :breadcrumbKey]
        ctx (utils4/get-ctx config)
        ;onRemove #(rf/dispatch [::selection-list-remove-click ctx %])
        onReorder (fn [src-idx dst-idx] (rf/dispatch [::selection-list-reorder ctx src-idx dst-idx]))
        logic @(rf/subscribe [::get-block-props ctx])
        {:keys [key disabled labelKey valueKey breadcrumbKey]} (merge logic (select-keys config config-keys))
        items @(rf/subscribe [::get-block-data ctx])]
    [ui/BreadcrumbSelectionList
     {:key           key
      :items         items
      :disabled      disabled
      :onReorder     onReorder
      :breadcrumbKey breadcrumbKey
      :labelKey      labelKey
      :valueKey      valueKey}]))

(defn table-selection-list
  [config]
  (let [config-keys [:columns :valueKey]
        ctx (utils4/get-ctx config)
        ;onRemove #(rf/dispatch [::selection-list-remove-click ctx %])
        onReorder (fn [src-idx dst-idx] (rf/dispatch [::selection-list-reorder ctx src-idx dst-idx]))
        logic @(rf/subscribe [::get-block-props ctx])
        {:keys [key disabled columns valueKey]} (merge logic (select-keys config config-keys))
        items @(rf/subscribe [::get-block-data ctx])]
    [ui/TableSelectionList
     {:key       key
      :items     items
      :disabled  disabled
      :onReorder onReorder
      :columns   columns
      :valueKey  valueKey}]))

(defn selection-list-picker
  [config]
  (let [ctx (utils4/get-ctx config)
        config-keys [:options :placeholder]
        logic @(rf/subscribe [::get-block-props ctx])
        onChange #(rf/dispatch [::selection-list-picker-change ctx %])
        props (merge logic (select-keys config config-keys))
        {:keys [placeholder options disabled errors show-errors]} props
        hasError (when (and show-errors (seq errors)) true)]
    [ui/SelectOptionField
     {:value       nil
      :options     options
      :placeholder placeholder
      :disabled    disabled
      :hasError    (seq hasError)
      :onChange    onChange}]))

