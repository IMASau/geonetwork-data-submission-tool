(ns metcalf.common.components4
  (:require [cljs.spec.alpha :as s]
            [clojure.string :as string]
            [interop.cljs-time :as cljs-time]
            [interop.ui-controls :as ui-controls]
            [metcalf.common.blocks4 :as blocks4]
            [metcalf.common.low-code4 :as low-code4]
            [metcalf.common.subs4 :as subs4]
            [metcalf.common.utils4 :as utils4]
            [metcalf.common.views4 :as views4]
            [re-frame.core :as rf]
            [reagent.core :as r]))

(s/def ::obj-path (s/coll-of string? :min-count 1))
(s/def ::value-path string?)

(defn page-errors-settings
  "Settings for page-errors component."
  [{:keys [data-paths]}]
  {::low-code4/req-ks       [:form-id :data-path :data-paths]
   ::low-code4/opt-ks       []
   ::low-code4/schema-paths data-paths})

(defn ^:deprecated page-errors
  "Display a list of errors associated with data-paths.
   NOTE: Depends on label in schema"
  [config]
  (let [{:keys [msgs]} @(rf/subscribe [::get-page-errors-props config])]
    (when (seq msgs)
      [:div.alert.alert-warning
       (if (> (count msgs) 1)
         [:div
          [:b "There are multiple fields on this page that require your attention:"]
          (into [:ul] (for [msg msgs] [:li msg]))]
         (first msgs))])))

(defn form-group-settings
  "Settings for form group"
  [{:keys [data-path]}]
  {::low-code4/req-ks       []
   ::low-code4/opt-ks       [:label :form-id :data-path :helperText :toolTip]
   ::low-code4/schema-paths [data-path]})

(defn form-group
  "This component is a lightweight wrapper around its children with props for the label above and helper text below.

   The props allow control of
   * label is an optional string displayed above the controls
   * helperText is an optional string displayed below the controls
   * toolTip is a string or hiccup.  Only displayed if label is set.

   Logic can control how the form-group is rendered.  Uses form-id and data-path to access block props.
   * required - show that field is required
   * disabled - styles control to indicate it's disabled
   * show-errors? - styles control to indicate data entry errors
   * errors - any data entry errors are displayed in place of helper text
   * is-hidden - hide form-group and children entirely

   Note: label is a special case, if defined in config it overrides logic
   "
  [config & children]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [label helperText toolTip required disabled is-hidden show-errors? errors]} props
        label (get config :label label)]
    (when-not is-hidden
      (into [ui-controls/FormGroup
             {:label      label
              :required   required
              :disabled   disabled
              :hasError   show-errors?
              :helperText (if show-errors? (string/join ". " errors) helperText)
              :toolTip    (r/as-element toolTip)
              :isAdded    false}]
            children))))

(defn inline-form-group-settings
  "Settings for inline-form-group component"
  [_]
  {::low-code4/req-ks [:label]
   ::low-code4/opt-ks [:form-id :data-path :helperText :toolTip]})

(defn inline-form-group
  "This component is a lightweight wrapper around its children with props for the label to the left and helper text below.

   It's similar to form-group with a different layout and some additional constraints.

   The props allow control of
   * label is a string displayed to the left of the controls
   * helperText is an optional string displayed below the controls
   * toolTip is a string or hiccup

   Logic can control how the form-group is rendered.  Uses form-id and data-path to access block props.
   * required - show that field is required
   * disabled - styles control to indicate it's disabled
   * show-errors? - styles control to indicate data entry errors
   * errors - any data entry errors are displayed in place of helper text
   * is-hidden - hide form-group and children entirely

   Note: label is a special case, if defined in config it overrides logic
   "
  [config & children]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [label helperText toolTip required disabled is-hidden errors show-errors?]} props
        label (get config :label label)]
    (when-not is-hidden
      (into [ui-controls/InlineFormGroup
             {:label      label
              :required   required
              :disabled   disabled
              :hasError   show-errors?
              :helperText (if show-errors? (string/join ". " errors) helperText)
              :toolTip    (r/as-element toolTip)}]
            children))))

(defn list-edit-dialog-settings
  "Settings for list-edit-dialog"
  [_]
  {::low-code4/req-ks [:form-id :data-path :title :template-id]
   ::low-code4/opt-ks []})

(defn list-edit-dialog
  "This component displays an edit dialog for a selected list item with a title, a rendered template,
   and buttons to save or cancel out.

   The props allow control of
   * form-id and data-path identify the list.
   * title (string) to display.
   * template-id (keyword) which identifies the template used to render body.

   The edit dialog has specific behaviour
   * Can save when the selected list item has no errors.
   * Can clear & close at any time.  Changes made in form are not kept.

   The template is rendered with ?form-id and ?data-path variables for the selected item.
  "
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        errors? @(rf/subscribe [::has-selected-block-errors? config])
        {:keys [form-id data-path list-item-selected-idx title template-id]} props
        item-data-path (conj data-path list-item-selected-idx)]
    [ui-controls/EditDialog
     {:isOpen  (boolean list-item-selected-idx)
      :title   title
      :onClose #(rf/dispatch [::list-edit-dialog-close config])
      :onClear #(rf/dispatch [::list-edit-dialog-cancel config])
      :onSave  #(rf/dispatch [::list-edit-dialog-save config])
      :canSave (not errors?)}
     (low-code4/render-template
       {:template-id template-id
        :variables   {'?form-id   form-id
                      '?data-path item-data-path}})]))

(defn typed-list-edit-dialog-settings
  "Settings for typed-list-edit-dialog"
  [{:keys [data-path type-path]}]
  {::low-code4/req-ks       [:form-id :data-path :type-path :templates]
   ::low-code4/opt-ks       []
   ::low-code4/schema       {:type "array" :items {:type "object"}}
   ::low-code4/schema-paths [data-path type-path]})

(defn typed-list-edit-dialog
  "This component displays an edit dialog for a selected list item with a title,
   a rendered template, and buttons to save or cancel out.

   The props allow control of
   * form-id and data-path identify the list.
   * title (string) to display.
   * type-path identifies the value used to select the template
   * templates which maps values to template-id.

   The edit dialog has specific behaviour
   * Can save when the selected list item has no errors.
   * Can clear & close at any time.  Changes made in form are not kept.

   The template is rendered with ?form-id and ?data-path variables for the selected item."

  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        errors? @(rf/subscribe [::has-selected-block-errors? config])
        {:keys [form-id data-path type-path list-item-selected-idx templates]} props
        item-data-path (conj data-path list-item-selected-idx)
        value @(rf/subscribe [::get-block-data config])
        item-type (get-in value (into [list-item-selected-idx] type-path))
        {:keys [title template-id]} (get templates item-type)]
    [ui-controls/EditDialog
     {:isOpen  (boolean list-item-selected-idx)
      :title   (or title "")
      :onClose #(rf/dispatch [::list-edit-dialog-close config])
      :onClear #(rf/dispatch [::list-edit-dialog-cancel config])
      :onSave  #(rf/dispatch [::list-edit-dialog-save config])
      :canSave (not errors?)}

     (low-code4/render-template
       {:template-id template-id
        :variables   {'?form-id   form-id
                      '?data-path item-data-path}})]))

(defn edit-dialog-settings
  "Settings for item-edit-dialog"
  [_]
  {::low-code4/req-ks [:form-id :data-path :title :template-id]
   ::low-code4/opt-ks []})

(defn edit-dialog
  "This component displays an edit dialog with a title, a rendered template,
   and buttons to save or cancel out.

   The props allow control of
   * form-id and data-path identify the block being edited.
   * title (string) to display.
   * template-id (keyword) which identifies the template used to render body.

   The edit dialog has specific behaviour
   * Can save when the data-path has no errors.
   * Can clear & close at any time.  Changes made in form are not kept.

   The template is rendered with ?form-id and ?data-path variables for the selected item.
  "
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        errors? @(rf/subscribe [::has-selected-block-errors? config])
        {:keys [form-id data-path open-dialog? title template-id]} props]
    [ui-controls/EditDialog
     {:isOpen  open-dialog?
      :title   title
      :onClose #(rf/dispatch [::edit-dialog-close config])
      :onClear #(rf/dispatch [::edit-dialog-cancel config])
      :onSave  #(rf/dispatch [::edit-dialog-save config])
      :canSave (not errors?)}
     (low-code4/render-template
       {:template-id template-id
        :variables   {'?form-id   form-id
                      '?data-path data-path}})]))

(defn input-field-settings
  "Settings for input-field"
  [_]
  {::low-code4/req-ks [:form-id :data-path]
   ::low-code4/opt-ks [:placeholder :maxLength]
   ::low-code4/schema {:type "string"}})

(defn input-field
  "This component displays an input field for entering a single line of text.

   The props allow control of
   * placeholder (string) displayed in input if empty
   * maxLength (int) constraints the length of text entered

   Logic can control aspects of how the component is rendered using form-id and data-path to access block props.
   * disabled - styles control to indicate it's disabled
   * show-errors? - styles control to indicate data entry errors
   * is-hidden - hides component entirely
  "
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [placeholder maxLength value disabled is-hidden show-errors?]} props]
    (when-not is-hidden
      [ui-controls/InputField
       {:value       (or value "")                          ; TODO: should be guaranteed by sub
        :placeholder placeholder
        :maxLength   maxLength
        :disabled    disabled
        :hasError    show-errors?
        :onChange    #(rf/dispatch [::value-changed config %])}])))

(defn when-data-settings
  "Settings for when-data component"
  [_]
  {::low-code4/req-ks [:form-id :data-path :pred]
   ::low-code4/opt-ks []})

(defn when-data
  "This component displays children when the data satisfies the pred.

   Config allows control of:
   * data tested is identified by form-id and data-path
   * pred is a valid clojure spec used to test the data.  Use pre-registered keywords or a set of values.

   Logic influences behaviour
   * component is not rendered if is-hidden is set"

  [config & children]
  (let [{:keys [pred is-hidden]} @(rf/subscribe [::get-block-props config])
        value @(rf/subscribe [::get-block-data config])]
    (when-not is-hidden
      (when (s/valid? pred value)
        (into [:div] children)))))

(defn get-data-settings
  "Settings for get-data component"
  [_]
  {::low-code4/req-ks [:form-id :data-path]
   ::low-code4/opt-ks []})

(defn get-data
  "This component displays form data as a string.

   Config allows control of:
   * data is identified by form-id and data-path."
  [config]
  (str @(rf/subscribe [::get-block-data config])))

(defn numeric-input-field-settings
  "Settings for numeric-input-field component"
  [_]
  {::low-code4/req-ks [:form-id :data-path]
   ::low-code4/opt-ks [:placeholder :hasButtons :unit]
   ::low-code4/schema {:type "number"}})

(defn numeric-input-field
  "This component displays a numeric input.  Values entered are stored as numbers.

   The props allow control of
   * placeholder (string) displayed in input if empty
   * hasButtons (boolean) shows nudge buttons
   * unit (string) displayed to right of input

   Logic can control aspects of how the component is rendered using form-id and data-path to access block props.
   * disabled - styles control to indicate it's disabled
   * show-errors? - styles control to indicate data entry errors
   * is-hidden - hides component entirely
  "
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [placeholder hasButtons unit value disabled is-hidden show-errors?]} props]
    (when-not is-hidden
      ; TODO: move styling to css
      [:div {:style {:display "flex" :flex-direction "row" :align-items "center"}}
       [ui-controls/NumericInputField
        {:value       value
         :placeholder placeholder
         :disabled    disabled
         :hasError    show-errors?
         :hasButtons  hasButtons
         :onChange    #(rf/dispatch [::value-changed config %])}]
       [:div {:style {:padding-left "0.5em"}} unit]])))

(defn textarea-field-settings
  "Settings for textarea-field component"
  [_]
  {::low-code4/req-ks [:form-id :data-path]
   ::low-code4/opt-ks [:placeholder :rows :maxLength]
   ::low-code4/schema {:type "string"}})

(defn textarea-field
  "This component renders a textarea field which automatically grow vertically to accommodate content.

   The props allow control of
   * placeholder (string) displayed in input if empty
   * rows (int) determines the initial height
   * maxLength (int) constraints the length of text entered

   Logic can control aspects of how the component is rendered using form-id and data-path to access block props.
   * disabled - styles control to indicate it's disabled
   * show-errors? - styles control to indicate data entry errors
   * is-hidden - hides component entirely
   "
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [placeholder rows maxLength value disabled show-errors? is-hidden]} props]
    (when-not is-hidden
      [ui-controls/TextareaField
       {:value       (or value "")                          ; TODO: should be guaranteed by sub
        :placeholder placeholder
        :disabled    disabled
        :hasError    show-errors?
        :maxLength   maxLength
        :rows        rows
        :onChange    #(rf/dispatch [::value-changed config %])}])))

(defn checkbox-field-settings
  "Settings for checkbox-field component"
  [_]
  {::low-code4/req-ks [:form-id :data-path :label]
   ::low-code4/opt-ks []
   ::low-code4/schema {:type "boolean"}})

(defn checkbox-field
  "This component renders a checkbox.  Values are stored as boolean values.

   Props allow control of
   * label (string) to be displayed

   Logic can control aspects of how the component is rendered using form-id and data-path to access block props.
   * value - value defaults to false if not set
   * disabled - styles control to indicate it's disabled
   * show-errors? - styles control to indicate data entry errors
   * is-hidden - hides component entirely
   "
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [label value disabled show-errors? is-hidden]} props]
    (when-not is-hidden
      [ui-controls/CheckboxField
       {:label    label
        :checked  (or value false)                          ; TODO: should be guaranteed by sub?
        :disabled disabled
        :hasError show-errors?
        :onChange #(rf/dispatch [::value-changed config %])}])))

(defn date-field-settings
  "Settings for date-field component"
  [_]
  {::low-code4/req-ks [:form-id :data-path :minDate :maxDate]
   ::low-code4/schema {:type "string"}})

(defn date-field
  "This component renders a date field within a defined date range.  The value is a date string (yyyy-MM-dd).

   Props control
   * minDate (date string) is required
   * maxDate (date string) is required

   Logic can control aspects of how the component is rendered using form-id and data-path to access block props.
   * value - value defaults to false if not set
   * disabled - styles control to indicate it's disabled
   * show-errors? - styles control to indicate data entry errors
   * is-hidden - hides component entirely
   "
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [minDate maxDate value disabled show-errors? is-hidden]} props]
    (when-not is-hidden
      [ui-controls/DateField
       {:value    (cljs-time/value-to-date value)
        :disabled disabled
        :hasError show-errors?
        :minDate  (cljs-time/value-to-date minDate)
        :maxDate  (cljs-time/value-to-date maxDate)
        :onChange #(rf/dispatch [::value-changed config (cljs-time/date-to-value %)])}])))

(defn simple-select-option-settings
  "Settings for simple-select-option component"
  [{:keys [value-path label-path added-path]}]
  {::low-code4/req-ks       [:form-id :data-path :options :value-path :label-path]
   ::low-code4/opt-ks       [:placeholder]
   ::low-code4/schema       {:type "object" :properties {}}
   ::low-code4/schema-paths [value-path label-path added-path]})

(defn simple-select-option
  "This component renders a select control with options.  The dropdown displays options as single lines of text.
   The value is option data.

   Props configure the component
   * options (maps) is a list of option data
   * value-path (vector) describes where the value in the option data.  Values must be unique.
   * label-path (vector) describes where the label is in the option data
   * placeholder (string) will be displayed when no option is selected

   Logic can control aspects of how the component is rendered using form-id and data-path to access block props.
   * disabled - styles control to indicate it's disabled
   * show-errors? - styles control to indicate data entry errors
   * is-hidden - hides component entirely
   "
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        value @(rf/subscribe [::get-block-data config])
        {:keys [options value-path label-path placeholder disabled show-errors? is-hidden]} props]
    (when-not is-hidden
      [ui-controls/SimpleSelectField
       {:value       value
        :options     options
        :placeholder placeholder
        :disabled    disabled
        :getValue    (ui-controls/obj-path-getter value-path)
        :getLabel    (ui-controls/obj-path-getter label-path)
        :hasError    show-errors?
        :onChange    #(rf/dispatch [::option-change config (ui-controls/get-option-data %)])}])))

(defn table-select-option-settings
  "Settings for table-select-option component"
  [{:keys [label-path value-path added-path columns]}]
  {::low-code4/req-ks       [:form-id :data-path :options :label-path :value-path :columns]
   ::low-code4/opt-ks       [:placeholder]
   ::low-code4/schema       {:type "object" :properties {}}
   ::low-code4/schema-paths (into [label-path value-path added-path]
                                  (map :label-path columns))})

(defn table-select-option
  "This component renders a select control with options.  The dropdown displays options in a table.
   The value is option data.

   Props configure the component
   * options (maps) - option data
   * value-path (vector) - where the value in the option data.  Must be unique.
   * label-path (vector) - where the label is in the option data
   * columns (maps) - column metadata used when rendering options
     * label-path - where the column label is in the option data
     * flex - how much space this column should use.
   * placeholder (string) will be displayed when no option is selected

   Logic can control aspects of how the component is rendered using form-id and data-path to access block props.
   * disabled - styles control to indicate it's disabled
   * show-errors? - styles control to indicate data entry errors
   * is-hidden - hides component entirely
   "
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        value @(rf/subscribe [::get-block-data config])
        {:keys [options value-path label-path columns placeholder disabled show-errors? is-hidden]} props]
    (when-not is-hidden
      [ui-controls/TableSelectField
       {:value       value
        :options     options
        :placeholder placeholder
        :disabled    disabled
        :hasError    show-errors?
        :getLabel    (ui-controls/obj-path-getter label-path)
        :getValue    (ui-controls/obj-path-getter value-path)
        :columns     (for [{:keys [flex label-path]} columns]
                       {:flex     flex
                        :getLabel (ui-controls/obj-path-getter label-path)})
        :onChange    #(rf/dispatch [::option-change config (ui-controls/get-option-data %)])}])))

(defn breadcrumb-select-option-settings
  "Settings for breadcrumb-select-option component"
  [{:keys [label-path value-path breadcrumb-path added-path]}]
  {::low-code4/req-ks       [:form-id :data-path :options :label-path :value-path :breadcrumb-path]
   ::low-code4/opt-ks       [:placeholder :added-path]
   ::low-code4/schema       {:type "object" :properties {}}
   ::low-code4/schema-paths [label-path value-path added-path breadcrumb-path]})

(defn breadcrumb-select-option
  "This component renders a select control with options.  The dropdown displays options with breadcrumbs and a label.
   The value is option data.

   Props configure the component
   * options (maps) - option data
   * value-path (vector) - where the value in the option data.
   * label-path (vector) - where the label is in the option data.  Label data must be a string.
   * breadcrumb-path (vector) - where the breadcrumbs are in the option data.  Breadcrumbs data must be a list of string.
   * placeholder (string) will be displayed when no option is selected.

   Logic can control aspects of how the component is rendered using form-id and data-path to access block props.
   * disabled - styles control to indicate it's disabled
   * show-errors? - styles control to indicate data entry errors
   * is-hidden - hides component entirely
   "
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        value @(rf/subscribe [::get-block-data config])
        {:keys [options value-path label-path breadcrumb-path placeholder disabled show-errors? is-hidden]} props]
    (when-not is-hidden
      [ui-controls/BreadcrumbSelectField
       {:value         value
        :options       options
        :placeholder   placeholder
        :disabled      disabled
        :hasError      show-errors?
        :getLabel      (ui-controls/obj-path-getter label-path)
        :getValue      (ui-controls/obj-path-getter value-path)
        :getBreadcrumb (ui-controls/obj-path-getter breadcrumb-path)
        :onChange      #(rf/dispatch [::option-change config (ui-controls/get-option-data %)])}])))

(defn item-dialog-button-settings
  "Settings for item-dialog-button component"
  [_]
  {::low-code4/req-ks [:form-id :data-path :value-path :added-path]
   ::low-code4/opt-ks []
   ::low-code4/schema {:type "object"}})

; TODO: Consider a view mode when it's not user defined.
(defn item-dialog-button
  "Add or edit a user defined item in a dialog"
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        added? @(rf/subscribe [::is-item-added? config])
        {:keys [value-path added-path disabled is-hidden]} props]
    (s/assert ::obj-path value-path)
    (s/assert ::obj-path added-path)
    (when-not is-hidden
      (if added?
        [:button.bp3-button.bp3-intent-primary
         {:disabled disabled
          :onClick  #(rf/dispatch [::item-dialog-button-edit-click config])}
         "Edit"]
        [:button.bp3-button.bp3-intent-primary
         {:disabled disabled
          :onClick  #(rf/dispatch [::item-dialog-button-add-click config])}
         "Add"]))))

(defn item-add-button-settings
  "Settings for item-add-button component"
  [_]
  {::low-code4/req-ks [:form-id :data-path :value-path :added-path]
   ::low-code4/opt-ks []
   ::low-code4/schema {:type "object"}})

(defn item-add-button
  "Add user defined item as value"
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [value-path added-path disabled is-hidden]} props]

    (s/assert ::obj-path value-path)
    (s/assert ::obj-path added-path)
    (when-not is-hidden
      [:button.bp3-button.bp3-intent-primary
       {:disabled disabled
        :onClick  #(rf/dispatch [::item-add-button-click config])}
       "Add"])))

(defn list-add-button-settings
  "Settings for list-add-button component"
  [_]
  {::low-code4/req-ks [:form-id :data-path :value-path :added-path :button-text]
   ::low-code4/opt-ks [:item-defaults]
   ::low-code4/schema {:type "array"}})

(defn list-add-button
  "Add user defined item to list"
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [disabled is-hidden button-text]} props]
    (when-not is-hidden
      [:button.bp3-button.bp3-intent-primary
       {:disabled disabled
        :onClick  #(rf/dispatch [::list-add-with-defaults-click-handler config])}
       button-text])))

(defn text-add-button-settings
  "Settings for text-add-button component"
  [_]
  {::low-code4/req-ks [:form-id :data-path :button-text]
   ::low-code4/opt-ks []
   ::low-code4/schema {:type "array"}})

(defn text-add-button
  "Add user defined item to list"
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [disabled is-hidden button-text]} props]
    (when-not is-hidden
      [ui-controls/TextAddField
       {:buttonText (r/as-element button-text)
        :disabled   disabled
        :onAddClick #(rf/dispatch [::text-value-add-click-handler config %])}])))


(defn record-add-button-settings
  [{:keys [columns]}]
  {::low-code4/req-ks       [:form-id :data-path :button-text :columns]
   ::low-code4/opt-ks       []
   ::low-code4/schema       {:type "array"}
   ::low-code4/schema-paths (mapv :value-path columns)})

(defn record-add-button
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [disabled is-hidden button-text columns]} props]
    (when-not is-hidden
      [ui-controls/RecordAddField
       {:buttonText (r/as-element button-text)
        :disabled   disabled
        :columns    (for [{:keys [flex placeholder]} columns]
                      {:flex        flex
                       :placeholder placeholder})
        :onAddClick #(rf/dispatch [::option-change config %])}])))

(defn async-select-value-settings
  [{:keys [value-path label-path]}]
  {::low-code4/req-ks       [:form-id :data-path :uri :value-path :label-path :results-path]
   ::low-code4/opt-ks       [:placeholder]
   ::low-code4/schema       {:type "object" :properties {}}
   ::low-code4/schema-paths [value-path label-path]})

(defn async-select-value
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [placeholder value value-path label-path disabled is-hidden show-errors?]} props]
    (when-not is-hidden
      [ui-controls/AsyncSimpleSelectField
       {:value       value
        :placeholder placeholder
        :disabled    disabled
        :hasError    show-errors?
        :loadOptions (partial utils4/load-options config)
        :getValue    (ui-controls/obj-path-getter value-path)
        :getLabel    (ui-controls/obj-path-getter label-path)
        :onChange    #(rf/dispatch [::value-changed config %])}])))

(defn async-select-option-simple-settings
  [{:keys [value-path label-path]}]
  {::low-code4/req-ks       [:form-id :data-path :uri :value-path :label-path]
   ::low-code4/opt-ks       [:placeholder :added-path]
   ::low-code4/schema       {:type "object" :properties {}}
   ::low-code4/schema-paths [value-path label-path]})

(defn async-select-option-simple
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        value @(rf/subscribe [::get-block-data config])
        {:keys [placeholder value-path label-path added-path disabled is-hidden show-errors?]} props]
    (when-not is-hidden
      [ui-controls/AsyncSimpleSelectField
       {:value       value
        :placeholder placeholder
        :disabled    disabled
        :hasError    show-errors?
        :loadOptions (partial utils4/load-options config)
        :getValue    (ui-controls/obj-path-getter value-path)
        :getLabel    (ui-controls/obj-path-getter label-path)
        :getAdded    (when added-path (ui-controls/obj-path-getter added-path))
        :onChange    #(rf/dispatch [::option-change config (ui-controls/get-option-data %)])}])))

(defn async-select-option-breadcrumb-settings
  [{:keys [value-path label-path breadcrumb-path]}]
  {::low-code4/req-ks       [:form-id :data-path :uri :value-path :label-path :breadcrumb-path]
   ::low-code4/opt-ks       [:placeholder :added-path]
   ::low-code4/schema       {:type "object" :properties {}}
   ::low-code4/schema-paths [value-path label-path breadcrumb-path]})

(defn async-select-option-breadcrumb
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        value @(rf/subscribe [::get-block-data config])
        {:keys [placeholder disabled is-hidden value-path label-path breadcrumb-path added-path show-errors?]} props]
    (when-not is-hidden
      [ui-controls/AsyncBreadcrumbSelectField
       {:value         value
        :loadOptions   (partial utils4/load-options config)
        :getValue      (ui-controls/obj-path-getter value-path)
        :getLabel      (ui-controls/obj-path-getter label-path)
        :getAdded      (when added-path (ui-controls/obj-path-getter added-path))
        :getBreadcrumb (ui-controls/obj-path-getter breadcrumb-path)
        :placeholder   placeholder
        :disabled      disabled
        :hasError      show-errors?
        :onChange      #(rf/dispatch [::option-change config (ui-controls/get-option-data %)])}])))

(defn async-select-option-table-settings
  "Settings for async-table-select-option component"
  [_]
  {::low-code4/req-ks [:form-id :data-path :uri :value-path :label-path :columns]
   ::low-code4/opt-ks [:placeholder :added-path]
   ::low-code4/schema {:type "object" :properties {}}})

(defn async-select-option-table
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        value @(rf/subscribe [::get-block-data config])
        {:keys [placeholder disabled is-hidden value-path label-path added-path columns show-errors?]} props]
    (when-not is-hidden
      [ui-controls/AsyncTableSelectField
       {:value       value
        :placeholder placeholder
        :disabled    disabled
        :hasError    show-errors?
        :loadOptions (partial utils4/load-options config)
        :getValue    (ui-controls/obj-path-getter value-path)
        :getLabel    (ui-controls/obj-path-getter label-path)
        :getAdded    (when added-path (ui-controls/obj-path-getter added-path))
        :columns     (for [{:keys [flex label-path]} columns]
                       {:flex     flex
                        :getLabel (ui-controls/obj-path-getter label-path)})
        :onChange    #(rf/dispatch [::option-change config (ui-controls/get-option-data %)])}])))

(defn select-value-settings
  "Settings for select-value component"
  [_]
  {::low-code4/req-ks [:form-id :data-path :options :label-path :value-path]
   ::low-code4/opt-ks []})

(defn select-value
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [value options label-path value-path disabled is-hidden show-errors?]} props
        value (or value "")]
    (when-not is-hidden
      [ui-controls/SelectValueField
       {:value    value
        :disabled disabled
        :options  options
        :getLabel (ui-controls/obj-path-getter label-path)
        :getValue (ui-controls/obj-path-getter value-path)
        :hasError show-errors?
        :onChange #(rf/dispatch [::value-changed config %])}])))

(defn yes-no-field-settings
  "Settings for yes-no-field component"
  [_]
  {::low-code4/req-ks [:form-id :data-path :label]
   ::low-code4/opt-ks []
   ::low-code4/schema {:type "boolean"}})

(defn yes-no-field
  [config]
  (let [props @(rf/subscribe [::get-yes-no-field-props config])
        {:keys [label value disabled is-hidden show-errors?]} props]
    (when-not is-hidden
      [ui-controls/YesNoRadioGroup
       {:value    value
        :label    label
        :disabled disabled
        :hasError show-errors?
        :onChange #(rf/dispatch [::value-changed config %])}])))

(defn simple-selection-list-settings
  [{:keys [label-path value-path added-path]}]
  {::low-code4/req-ks       [:form-id :data-path :label-path :value-path]
   ::low-code4/opt-ks       [:added-path]
   ::low-code4/schema       {:type "array" :items {:type "object"}}
   ::low-code4/schema-paths [label-path value-path added-path]})

(defn simple-selection-list
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        items @(rf/subscribe [::get-block-data config])
        {:keys [key disabled is-hidden label-path value-path added-path]} props]
    (when-not is-hidden
      [ui-controls/SimpleSelectionList
       {:key           key
        :items         (or items [])
        :disabled      disabled
        :getLabel      (ui-controls/obj-path-getter label-path)
        :getValue      (ui-controls/obj-path-getter value-path)
        :getAdded      (when added-path (ui-controls/obj-path-getter added-path))
        :onReorder     (fn [src-idx dst-idx] (rf/dispatch [::selection-list-reorder props src-idx dst-idx]))
        :onItemClick   (fn [idx] (rf/dispatch [::selection-list-item-click props idx]))
        :onRemoveClick (fn [idx] (rf/dispatch [::selection-list-remove-click props idx]))}])))

(defn value-selection-list-settings
  [_]
  {::low-code4/req-ks [:form-id :data-path]
   ::low-code4/opt-ks []
   ::low-code4/schema {:type "array" :items {:type "string"}}})

(defn value-selection-list
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        labels @(rf/subscribe [::get-block-data config])
        {:keys [key disabled is-hidden]} props
        items (map (fn [label] {:value (gensym) :label label}) labels)]
    (when-not is-hidden
      [ui-controls/SimpleSelectionList
       {:key           key
        :items         items
        :disabled      disabled
        :getLabel      (ui-controls/obj-path-getter ["label"])
        :getValue      (ui-controls/obj-path-getter ["value"])
        :onReorder     (fn [src-idx dst-idx] (rf/dispatch [::value-selection-list-reorder props src-idx dst-idx]))
        :onRemoveClick (fn [idx] (rf/dispatch [::value-selection-list-remove-click props idx]))}])))

(defn selection-list-settings
  [{:keys [value-path added-path]}]
  {::low-code4/req-ks       [:form-id :data-path :value-path :template-id]
   ::low-code4/opt-ks       [:added-path]
   ::low-code4/schema       {:type "array" :items {:type "object"}}
   ::low-code4/schema-paths [value-path added-path]})

(defn selection-list
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        items @(rf/subscribe [::get-block-data config])
        {:keys [form-id data-path key disabled is-hidden template-id value-path added-path]} props]
    (when-not is-hidden
      [ui-controls/SelectionList
       {:key           key
        :items         (or items [])
        :disabled      disabled
        :renderItem    (fn [args]
                         (let [index (ui-controls/obj-path-value args ["index"])]
                           (r/as-element
                             (low-code4/render-template
                               {:template-id template-id
                                :variables   {'?form-id   form-id
                                              '?data-path (conj data-path index)}}))))
        :getValue      (ui-controls/obj-path-getter value-path)
        :getAdded      (when added-path (ui-controls/obj-path-getter added-path))
        :onReorder     (fn [src-idx dst-idx] (rf/dispatch [::selection-list-reorder props src-idx dst-idx]))
        :onItemClick   (fn [idx] (rf/dispatch [::selection-list-item-click props idx]))
        :onRemoveClick (fn [idx] (rf/dispatch [::selection-list-remove-click props idx]))}])))

(defn simple-list-settings
  [_]
  {::low-code4/req-ks       [:form-id :data-path :template-id]
   ::low-code4/opt-ks       []
   ::low-code4/schema       {:type "array" :items {:type "object"}}
   ::low-code4/schema-paths []})

(defn simple-list
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        items @(rf/subscribe [::get-block-data config])
        {:keys [form-id data-path is-hidden template-id]} props]
    (when-not is-hidden
      [:<>
       (for [index (range (count items))]
         (low-code4/render-template
           {:template-id template-id
            :variables   {'?form-id   form-id
                          '?data-path (conj data-path index)}}))])))

(defn breadcrumb-selection-list-settings
  [{:keys [label-path value-path breadcrumb-path added-path]}]
  {::low-code4/req-ks       [:form-id :data-path :label-path :value-path :breadcrumb-path]
   ::low-code4/opt-ks       [:added-path]
   ::low-code4/schema       {:type "array" :items {:type "object"}}
   ::low-code4/schema-paths [label-path value-path breadcrumb-path added-path]})

(defn breadcrumb-selection-list
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        items @(rf/subscribe [::get-block-data config])
        {:keys [key disabled is-hidden label-path value-path added-path breadcrumb-path]} props]
    (when-not is-hidden
      [ui-controls/BreadcrumbSelectionList
       {:key           key
        :items         (or items [])
        :disabled      disabled
        :getBreadcrumb (ui-controls/obj-path-getter breadcrumb-path)
        :getLabel      (ui-controls/obj-path-getter label-path)
        :getValue      (ui-controls/obj-path-getter value-path)
        :getAdded      (when added-path (ui-controls/obj-path-getter added-path))
        :onReorder     (fn [src-idx dst-idx] (rf/dispatch [::selection-list-reorder props src-idx dst-idx]))
        :onItemClick   (fn [idx] (rf/dispatch [::selection-list-item-click props idx]))
        :onRemoveClick (fn [idx] (rf/dispatch [::selection-list-remove-click props idx]))}])))

(defn table-selection-list-settings
  [{:keys [value-path columns added-path]}]
  {::low-code4/req-ks       [:form-id :data-path :columns :value-path]
   ::low-code4/opt-ks       [:added-path]
   ::low-code4/schema       {:type "array" :items {:type "object"}}
   ::low-code4/schema-paths (into [value-path added-path] (map :label-path columns))})

(defn table-selection-list
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [key disabled is-hidden columns value-path added-path]} props
        items @(rf/subscribe [::get-block-data config])]
    (when-not is-hidden
      [ui-controls/TableSelectionList
       {:key           key
        :items         (or items [])
        :disabled      disabled
        :columns       (for [{:keys [flex label-path columnHeader]} columns]
                         {:flex         flex
                          :getLabel     (ui-controls/obj-path-getter label-path)
                          :columnHeader (or columnHeader "None")})
        :getValue      (ui-controls/obj-path-getter value-path)
        :getAdded      (when added-path (ui-controls/obj-path-getter added-path))
        :onReorder     (fn [src-idx dst-idx] (rf/dispatch [::selection-list-reorder config src-idx dst-idx]))
        :onItemClick   (fn [idx] (rf/dispatch [::selection-list-item-click config idx]))
        :onRemoveClick (fn [idx] (rf/dispatch [::selection-list-remove-click config idx]))}])))

(defn simple-list-option-picker-settings
  "Settings for simple-list-option-picker component"
  [_]
  {::low-code4/req-ks [:form-id :data-path :options :value-path :label-path]
   ::low-code4/opt-ks [:placeholder]
   ::low-code4/schema {:type "array" :items {:type "object"}}})

(defn simple-list-option-picker
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [placeholder options disabled is-hidden value-path label-path show-errors?]} props]
    (when-not is-hidden
      [ui-controls/SimpleSelectField
       {:value       nil
        :options     options
        :placeholder placeholder
        :disabled    disabled
        :hasError    show-errors?
        :getLabel    (ui-controls/obj-path-getter label-path)
        :getValue    (ui-controls/obj-path-getter value-path)
        :onChange    #(rf/dispatch [::list-option-picker-change config (ui-controls/get-option-data %)])}])))

(defn breadcrumb-list-option-picker-settings
  "Settings for breadcrumb-list-option-picker component"
  [_]
  {::low-code4/req-ks [:form-id :data-path :options :value-path :label-path :breadcrumb-path]
   ::low-code4/opt-ks [:placeholder]
   ::low-code4/schema {:type "array" :items {:type "object"}}})

(defn breadcrumb-list-option-picker
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [placeholder options disabled is-hidden value-path label-path breadcrumb-path show-errors?]} props]
    (when-not is-hidden
      [ui-controls/BreadcrumbSelectField
       {:value         nil
        :options       options
        :placeholder   placeholder
        :disabled      disabled
        :hasError      show-errors?
        :getValue      (ui-controls/obj-path-getter value-path)
        :getLabel      (ui-controls/obj-path-getter label-path)
        :getBreadcrumb (ui-controls/obj-path-getter breadcrumb-path)
        :onChange      #(rf/dispatch [::list-option-picker-change config (ui-controls/get-option-data %)])}])))

(defn table-list-option-picker-settings
  "Settings for table-list-option-picker component"
  [_]
  {::low-code4/req-ks [:form-id :data-path :options :value-path :label-path :columns]
   ::low-code4/opt-ks [:placeholder]
   ::low-code4/schema {:type "array" :items {:type "object"}}})

(defn table-list-option-picker
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [placeholder options disabled is-hidden value-path label-path columns show-errors?]} props]
    (when-not is-hidden
      [ui-controls/TableSelectField
       {:value       nil
        :options     options
        :placeholder placeholder
        :disabled    disabled
        :hasError    show-errors?
        :getLabel    (ui-controls/obj-path-getter label-path)
        :getValue    (ui-controls/obj-path-getter value-path)
        :columns     (for [{:keys [flex label-path]} columns]
                       {:flex     flex
                        :getLabel (ui-controls/obj-path-getter label-path)})
        :onChange    #(rf/dispatch [::list-option-picker-change config (ui-controls/get-option-data %)])}])))

(defn async-list-option-picker-simple-settings
  "Settings for async-simple-list-option-picker component"
  [_]
  {::low-code4/req-ks [:form-id :data-path :uri :value-path :label-path]
   ::low-code4/opt-ks [:placeholder]
   ::low-code4/schema {:type "array" :items {:type "object"}}})

(defn async-list-option-picker-simple
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [placeholder is-hidden disabled value-path label-path show-errors?]} props]
    (when-not is-hidden
      [ui-controls/AsyncSimpleSelectField
       {:value       nil
        :placeholder placeholder
        :disabled    disabled
        :hasError    show-errors?
        :getValue    (ui-controls/obj-path-getter value-path)
        :getLabel    (ui-controls/obj-path-getter label-path)
        :loadOptions (partial utils4/load-options config)
        :onChange    #(rf/dispatch [::list-option-picker-change config (ui-controls/get-option-data %)])}])))

(defn async-simple-item-option-picker-settings
  "Settings for async-simple-item-option-picker component"
  [_]
  {::low-code4/req-ks [:form-id :data-path :uri :value-path :label-path]
   ::low-code4/opt-ks [:placeholder]
   ::low-code4/schema {:type "array" :items {:type "object"}}})

(defn async-simple-item-option-picker
  "async-option-picker which displays options as simple labels"
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [placeholder disabled is-hidden value-path label-path show-errors?]} props]
    (when-not is-hidden
      [ui-controls/AsyncSimpleSelectField
       {:value       nil
        :placeholder placeholder
        :disabled    disabled
        :hasError    show-errors?
        :getValue    (ui-controls/obj-path-getter value-path)
        :getLabel    (ui-controls/obj-path-getter label-path)
        :loadOptions (partial utils4/load-options config)
        :onChange    #(rf/dispatch [::item-option-picker-change config (ui-controls/get-option-data %)])}])))

(defn async-list-option-picker-breadcrumb-settings
  "Settings for async-breadcrumb-list-option-picker component"
  [_]
  {::low-code4/req-ks [:form-id :data-path :uri :value-path :label-path :breadcrumb-path]
   ::low-code4/opt-ks [:placeholder]
   ::low-code4/schema {:type "array" :items {:type "object"}}})

(defn async-list-option-picker-breadcrumb
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [placeholder disabled is-hidden value-path label-path breadcrumb-path show-errors?]} props]
    (when-not is-hidden
      [ui-controls/AsyncBreadcrumbSelectField
       {:value         nil
        :placeholder   placeholder
        :disabled      disabled
        :hasError      show-errors?
        :getValue      (ui-controls/obj-path-getter value-path)
        :getLabel      (ui-controls/obj-path-getter label-path)
        :getBreadcrumb (ui-controls/obj-path-getter breadcrumb-path)
        :loadOptions   (partial utils4/load-options config)
        :onChange      #(rf/dispatch [::list-option-picker-change config (ui-controls/get-option-data %)])}])))

(defn async-list-option-picker-table-settings
  "Settings for async-table-list-option-picker component"
  [_]
  {::low-code4/req-ks [:form-id :data-path :uri :value-path :label-path :columns]
   ::low-code4/opt-ks [:placeholder]
   ::low-code4/schema {:type "array" :items {:type "object"}}})

(defn async-list-option-picker-table
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [placeholder disabled is-hidden value-path label-path columns show-errors?]} props]
    (when-not is-hidden
      [ui-controls/AsyncTableSelectField
       {:value       nil
        :placeholder placeholder
        :disabled    disabled
        :hasError    show-errors?
        :getLabel    (ui-controls/obj-path-getter label-path)
        :getValue    (ui-controls/obj-path-getter value-path)
        :columns     (for [{:keys [flex label-path]} columns]
                       {:flex     flex
                        :getLabel (ui-controls/obj-path-getter label-path)})
        :loadOptions (partial utils4/load-options config)
        :onChange    #(rf/dispatch [::list-option-picker-change config (ui-controls/get-option-data %)])}])))

(defmulti async-item-picker-settings :kind)
(defmethod async-item-picker-settings :default [config] (async-simple-item-option-picker-settings config))
;(defmethod async-item-picker-settings :breadcrumb [config] (async-breadcrumb-item-option-picker-settings config))
;(defmethod async-item-picker-settings :table [config] (async-table-item-option-picker-settings config))

(defmulti async-item-picker
          "Use case: As a user, I want to prepopulate a bunch of editable fields by picking from a list.
           Use :kind to control how options are displayed."
          :kind)
(defmethod async-item-picker :default [config] (async-simple-item-option-picker config))
;(defmethod async-item-picker :breadcrumb [config] (async-breadcrumb-item-option-picker config))
;(defmethod async-item-picker :table [config] (async-table-item-option-picker config))

(defn expanding-control-settings
  "Settings for expanding-control component"
  [_]
  {::low-code4/req-ks [:label]
   ::low-code4/opt-ks [:form-id :data-path :required :defaultOpen]})

(defn expanding-control
  [config & children]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [label required defaultOpen]} props]
    (into [ui-controls/ExpandingControl
           {:label       label
            :required    required
            :defaultOpen defaultOpen}]
          children)))

(defn coord-field
  [path]
  [:div.CoordField
   [:div.row
    [:div.col-sm-6.col-sm-offset-3.col-lg-4.col-lg-offset-2
     [:div.n-block
      [form-group
       {:form-id   [:form]
        :data-path (conj path "northBoundLatitude")
        :required  true}
       [numeric-input-field
        {:form-id   [:form]
         :data-path (conj path "northBoundLatitude")}]]]]]
   [:div.row
    [:div.col-sm-6.col-lg-4
     [:div.w-block
      [form-group
       {:form-id   [:form]
        :data-path (conj path "westBoundLongitude")
        :required  true}
       [numeric-input-field
        {:form-id   [:form]
         :data-path (conj path "westBoundLongitude")}]]]]
    [:div.col-sm-6.col-lg-4
     [:div.e-block
      [form-group
       {:form-id   [:form]
        :data-path (conj path "eastBoundLongitude")}
       [numeric-input-field
        {:form-id   [:form]
         :data-path (conj path "eastBoundLongitude")}]]]]]
   [:div.row
    [:div.col-sm-6.col-sm-offset-3.col-lg-4.col-lg-offset-2
     [:div.s-block
      [form-group
       {:form-id   [:form]
        :data-path (conj path "southBoundLatitude")}
       [numeric-input-field
        {:form-id   [:form]
         :data-path (conj path "southBoundLatitude")}]]]]]])

(defn boxmap-field-settings
  "Settings for boxmap-field component"
  [_]
  {::low-code4/req-ks [:form-id :data-path]
   ::low-code4/opt-ks []})

(defn boxmap-field
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        data @(rf/subscribe [::get-block-data config])
        elements (ui-controls/boxes->elements data)
        {:keys [disabled is-hidden key]} props]
    (when-not is-hidden
      [ui-controls/BoxMap
       {:elements (filter ui-controls/valid-element? elements)
        :disabled (not disabled)
        :tickId   key
        :onChange #(rf/dispatch [::boxes-changed config (ui-controls/get-geojson-data %)])}])))

(defn coordinates-modal-field-settings
  "Settings for coordinates-modal-field component"
  [_]
  {::low-code4/req-ks [:form-id :data-path]
   ::low-code4/opt-ks [:help]})

; TODO: replace or refactor to use edit dialog component
(defn coordinates-modal-field
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        data @(rf/subscribe [::get-block-data config])
        {:keys [data-path is-hidden disabled help]} props

        pretty-print (fn [x] (if (nil? x) "--" (if (number? x) (.toFixed x 3) (pr-str x))))
        ths ["North limit" "West limit" "South limit" "East limit"]
        tds-fn (fn [geographicElement]
                 (let [{:strs [northBoundLatitude westBoundLongitude
                               eastBoundLongitude southBoundLatitude]} geographicElement]
                   [(pretty-print northBoundLatitude)
                    (pretty-print westBoundLongitude)
                    (pretty-print southBoundLatitude)
                    (pretty-print eastBoundLongitude)]))
        new-item-with-values {"northBoundLatitude" 0
                              "southBoundLatitude" 0
                              "eastBoundLongitude" 0
                              "westBoundLongitude" 0}]
    (letfn [(new-fn [] (when-not disabled
                         (rf/dispatch [::boxmap-coordinates-open-add-modal
                                       {:ctx          config
                                        :coord-field  coord-field
                                        :initial-data new-item-with-values
                                        :idx          (count data)
                                        :on-close     #(rf/dispatch [::boxmap-coordinates-list-delete config %])
                                        :on-save      #(rf/dispatch [::coordinates-modal-field-close-modal])}])))
            (delete-fn [idx] (rf/dispatch [::boxmap-coordinates-list-delete config idx]))
            (try-delete-fn [idx] (rf/dispatch [::boxmap-coordinates-click-confirm-delete #(delete-fn idx)]))
            (open-edit-fn [indexed-data-path] (when-not disabled
                                                (rf/dispatch [::boxmap-coordinates-open-edit-modal
                                                              {:ctx         (assoc config :data-path indexed-data-path)
                                                               :coord-field coord-field
                                                               :on-delete   #(try-delete-fn (last indexed-data-path))
                                                               :on-save     #(rf/dispatch [::coordinates-modal-field-close-modal])
                                                               :on-cancel   #(rf/dispatch [::coordinates-modal-field-close-modal])}])))
            (has-error? [form-state data-path]
              (let [path (blocks4/block-path data-path)
                    field (get-in form-state path)
                    errors (-> field :props :errors)]
                (seq errors)))]
      (when-not is-hidden
        [:div.TableInlineEdit
         (when help [:p.help-block help])
         (when (pos? (count data))
           [:table.table {:class (when-not (or disabled (empty? data)) "table-hover")}
            [:thead
             (-> [:tr]
                 (into (for [th ths] [:th th]))
                 (conj [:th.xcell " "]))]
            (if (not-empty data)
              (into [:tbody]
                    (for [[idx field] (map-indexed vector data)]
                      (let [data-path (conj data-path idx)
                            form-state @(rf/subscribe [::subs4/get-form-state (:form-id config)])
                            has-error? (or (has-error? form-state (conj data-path "northBoundLatitude"))
                                           (has-error? form-state (conj data-path "southBoundLatitude"))
                                           (has-error? form-state (conj data-path "eastBoundLongitude"))
                                           (has-error? form-state (conj data-path "westBoundLongitude")))]
                        (-> [:tr.clickable-text {:class    (when has-error? "warning")
                                                 :ref      (str data-path)
                                                 :on-click #(open-edit-fn data-path)}]
                            (into (for [td-value (tds-fn field)]
                                    [:td (if (empty? td-value) [:span {:style {:color "#ccc"}} "--"] td-value)]))
                            (conj [:td.xcell
                                   (when has-error?
                                     [:span.glyphicon.glyphicon-alert.text-danger])])))))
              [:tbody (-> [:tr.noselect {:on-click #(when-not disabled new-fn)}]
                          (into (for [_ (or ths [nil])] [:td {:style {:color "#ccc"}} "--"]))
                          (conj [:td.xcell]))])])
         [:button.btn.btn-primary.btn-sm
          {:disabled disabled
           :on-click new-fn}
          [:span.glyphicon.glyphicon-plus] " Add new"]]))))

(def create-document-modal-template
  '[:div.NewDocumentForm

    [m4/form-group
     {:form-id   ?form-id
      :data-path ["title"]
      :label     "Title"}
     [m4/input-field
      {:form-id   ?form-id
       :data-path ["title"]}]]

    [m4/form-group
     {:form-id   ?form-id
      :data-path ["template"]
      :label     "Template"}
     [m4/async-select-option
      {:form-id    ?form-id
       :data-path  ["template"]
       :value-path ["id"]
       :label-path ["name"]
       :uri        "/api/metadata-template"}]]])

(defn create-document-modal
  "Modal form for creating new documents"
  [_]
  [ui-controls/EditDialog
   {:isOpen  true
    :title   "Create a new record"
    :onClose #(rf/dispatch [::create-document-modal-close-click])
    :onClear #(rf/dispatch [::create-document-modal-clear-click])
    :onSave  #(rf/dispatch [::create-document-modal-save-click])
    :canSave @(rf/subscribe [::create-document-modal-can-save?])}
   [low-code4/render-template
    {:template-id ::create-document-modal-form
     :variables   '{?form-id [:create_form]}}]])

(defn contributors-modal
  [{:keys [uuid]}]
  [ui-controls/ModalDialog
   {:isOpen  true
    :title   "Sharing"
    :onClose #(rf/dispatch [::create-document-modal-close-click])}
   (let [{:keys [emails]} @(rf/subscribe [:app/contributors-modal-props uuid])]
     [views4/collaborator-form
      {:uuid          uuid
       :emails        emails
       :onRemoveClick (fn [idx] (rf/dispatch [:app/contributors-modal-unshare-click {:uuid uuid :idx idx}]))
       :onAddClick    (fn [email] (rf/dispatch [:app/contributors-modal-share-click {:uuid uuid :email email}]))}])])

(defn upload-files-settings
  "Settings for upload-files component"
  [_]
  {::low-code4/req-ks [:form-id :data-path :value-path :placeholder]
   ::low-code4/opt-ks []})

(defn upload-files
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        items @(rf/subscribe [::get-block-data config])
        {:keys [disabled is-hidden value-path placeholder]} props]
    (when-not is-hidden
      [:div
       [ui-controls/SimpleSelectionList
        {:key           key
         :items         (or items [])
         :disabled      disabled
         :getLabel      (ui-controls/obj-path-getter "name")
         :getValue      (ui-controls/obj-path-getter value-path)
         :getAdded      (constantly true)
         :onReorder     (fn [src-idx dst-idx] (rf/dispatch [::selection-list-reorder props src-idx dst-idx]))
         :onItemClick   (fn [idx] (rf/dispatch [::selection-list-item-click props idx]))
         :onRemoveClick (fn [idx] (rf/dispatch [::selection-list-remove-click props idx]))}]
       [ui-controls/Dropzone
        {:disabled    disabled
         :placeholder (r/as-element placeholder)
         :onDrop      #(rf/dispatch [::upload-files-drop config (js->clj % :keywordize-keys true)])}]])))

(defn yes-no-radios-simple-settings
  "Settings for yes-no-radios-simple component"
  [_]
  {::low-code4/req-ks [:form-id :data-path :inline]
   ::low-code4/opt-ks []
   ::low-code4/schema {:type "boolean"}
   })

(defn yes-no-radios-simple
  [config]
  (let [props @(rf/subscribe [:metcalf.common.components4/get-block-props config])
        {:keys [value inline disabled is-hidden show-errors?]} props]
    (when-not is-hidden
      [ui-controls/RadioGroupSimple
       {:value    value
        :disabled disabled
        :inline   inline
        :options  [{:desc "Yes" :value true} {:desc "No" :value false}]
        :getLabel (ui-controls/obj-path-getter ["desc"])
        :getValue (ui-controls/obj-path-getter ["value"])
        :hasError show-errors?
        :onChange (fn [option]
                    (rf/dispatch [:metcalf.common.components4/value-changed config (ui-controls/obj-path-value option ["value"])]))}])))

(defn yes-no-radios-settings
  "Settings for yes-no-radios component"
  [_]
  {::low-code4/req-ks [:form-id :data-path]
   ::low-code4/opt-ks []
   ;::low-code4/schema {:type "boolean"}
   })

(defn yes-no-radios
  [config]
  (let [props @(rf/subscribe [:metcalf.common.components4/get-block-props config])
        data @(rf/subscribe [:metcalf.common.components4/get-block-data config])
        {:keys [disabled is-hidden show-errors?]} props
        ]
    (when-not is-hidden
      [ui-controls/RadioGroup
       {:value    data
        :disabled disabled
        :inline   true
        :options  [{:desc "Yes" :value true} {:desc "No" :value false}]
        :getLabel (ui-controls/obj-path-getter ["desc"])
        :getValue (ui-controls/obj-path-getter ["value"])
        :hasError show-errors?
        :onChange (fn [option]
                    (rf/dispatch [:metcalf.common.components4/option-change config (ui-controls/obj-path-value option ["value"])]))}])))


(defn radio-group-settings
  [{:keys [value-path label-path]}]
  {::low-code4/req-ks       [:form-id :data-path :options :value-path :label-path :inline]
   ::low-code4/opt-ks       [:placeholder]
   ::low-code4/schema       {:type "object" :properties {}}
   ::low-code4/schema-paths [value-path label-path]})

(defn radio-group
  [config]
  (let [props @(rf/subscribe [:metcalf.common.components4/get-block-props config])
        data @(rf/subscribe [:metcalf.common.components4/get-block-data config])
        {:keys [inline options label-path value-path disabled is-hidden show-errors?]} props]
    (when-not is-hidden
      [ui-controls/RadioGroup
       {:value    data
        :disabled disabled
        :options  options
        :inline   inline
        :getLabel (ui-controls/obj-path-getter label-path)
        :getValue (ui-controls/obj-path-getter value-path)
        :hasError show-errors?
        :onChange #(rf/dispatch [:metcalf.common.components4/option-change config (ui-controls/get-option-data %)])}])))

(defn radio-group-simple-settings
  [{:keys [value-path label-path]}]
  {::low-code4/req-ks       [:form-id :data-path :options :value-path :label-path :inline]
   ::low-code4/opt-ks       [:placeholder]
   ::low-code4/schema       {:type "object" :properties {}}
   ::low-code4/schema-paths [value-path label-path]})

(defn radio-group-simple
  [config]
  (let [props @(rf/subscribe [:metcalf.common.components4/get-block-props config])
        {:keys [value inline options label-path value-path disabled is-hidden show-errors?]} props
        value (or value "")]
    (when-not is-hidden
      [ui-controls/RadioGroupSimple
       {:value    value
        :disabled disabled
        :options  options
        :inline   inline
        :getLabel (ui-controls/obj-path-getter label-path)
        :getValue (ui-controls/obj-path-getter value-path)
        :hasError show-errors?
        :onChange #(rf/dispatch [:metcalf.common.components4/value-changed config %])}])))
