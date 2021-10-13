(ns metcalf4.components
  (:require [cljs.spec.alpha :as s]
            [clojure.string :as string]
            [interop.date :as date]
            [interop.ui :as ui]
            [metcalf3.widget.boxmap :as boxmap]
            [metcalf4.blocks :as blocks]
            [metcalf4.subs :as common-subs]
            [metcalf4.utils :as utils4]
            [re-frame.core :as rf]
            [metcalf4.schema :as schema]
            [metcalf4.low-code :as low-code]))

(defn str-value
  [data]
  (binding [*print-level* 3
            *print-length* 5]
    (pr-str data)))

(defn console-value
  [data]
  (if goog/DEBUG
    data
    (str-value data)))

(defn report-config-error
  [msg data]
  (js/console.error msg (console-value data)))

(defn report-config-warn
  [msg data]
  (js/console.warn msg (console-value data)))

(defn massage-config
  [settings config]
  (let [{:keys [opt-ks req-ks]} settings
        all-ks (distinct (concat opt-ks req-ks))
        missing-ks (remove (set (keys config)) req-ks)]
    (doseq [k missing-ks]
      (report-config-error (str "Missing required key (" (pr-str k) ") in config") {:config config :settings settings}))
    (-> config
        (utils4/if-contains-update :data-path utils4/massage-data-path)
        (select-keys all-ks))))

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

(def form-group-settings
  {:req-ks [:label]
   :opt-ks [:form-id :data-path :placeholder :helperText :toolTip]})

(defn form-group
  [config & children]
  (let [config (massage-config form-group-settings config)
        props @(rf/subscribe [::get-block-props config])
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

(def inline-form-group-settings
  {:req-ks [:form-id :data-path :label]
   :opt-ks [:placeholder :helperText :toolTip]})

(defn inline-form-group
  [config & children]
  (let [config (massage-config inline-form-group-settings config)
        props @(rf/subscribe [::get-block-props config])
        {:keys [label helperText toolTip required disabled show-errors errors]} props
        hasError (when (and show-errors (seq errors)) true)]

    (into [ui/InlineFormGroup
           {:label      label
            :required   required
            :disabled   disabled
            :hasError   hasError
            :helperText (if hasError (string/join ". " errors) helperText)
            :toolTip    toolTip}]
          children)))

(def list-edit-dialog-settings
  {:req-ks [:form-id :data-path :title :template-id]
   :opt-ks []})

(defn list-edit-dialog
  "Popup dialog if item is selected"
  [config]
  (let [config (massage-config list-edit-dialog-settings config)
        props @(rf/subscribe [::get-block-props config])
        {:keys [form-id data-path selected title template-id show-errors errors]} props
        hasError (when (and show-errors (seq errors)) true)
        item-data-path (conj data-path selected)]
    [ui/EditDialog
     {:isOpen  (boolean selected)
      :title   title
      :onClose #(rf/dispatch [::list-edit-dialog-close config])
      :onClear #(rf/dispatch [::list-edit-dialog-cancel config])
      :onSave  #(rf/dispatch [::list-edit-dialog-save config])
      :canSave (not hasError)}
     (low-code/render-template
       {:template-id template-id
        :variables   {'?form-id   form-id
                      '?data-path item-data-path}})]))

(def item-edit-dialog-settings
  {:req-ks [:form-id :data-path :title :template-id]
   :opt-ks []})

(defn item-edit-dialog
  "Popup dialog if item is selected"
  [config]
  (let [config (massage-config item-edit-dialog-settings config)
        props @(rf/subscribe [::get-block-props config])
        {:keys [form-id data-path isOpen title template-id show-errors errors]} props
        hasError (when (and show-errors (seq errors)) true)]
    [ui/EditDialog
     {:isOpen  isOpen
      :title   title
      :onClose #(rf/dispatch [::item-edit-dialog-close config])
      :onClear #(rf/dispatch [::item-edit-dialog-cancel config])
      :onSave  #(rf/dispatch [::item-edit-dialog-save config])
      :canSave (not hasError)}
     (low-code/render-template
       {:template-id template-id
        :variables   {'?form-id   form-id
                      '?data-path data-path}})]))

(def input-field-settings
  {:req-ks [:form-id :data-path]
   :opt-ks [:placeholder :maxLength]})

(defn input-field
  [config]
  (let [config (massage-config input-field-settings config)
        props @(rf/subscribe [::get-block-props config])
        {:keys [placeholder maxLength value disabled show-errors errors]} props
        hasError (when (and show-errors (seq errors)) true)]
    (schema/assert-compatible-schema
      {:schema1 @(rf/subscribe [::get-data-schema config])
       :schema2 {:type "string"}})
    [ui/InputField
     {:value       (or value "")                            ; TODO: should be guaranteed by sub
      :placeholder placeholder
      :maxLength   maxLength
      :disabled    disabled
      :hasError    hasError
      :onChange    #(rf/dispatch [::value-changed config %])}]))

(defn input-field-with-label
  [config]
  [form-group config
   [input-field config]])

(def numeric-input-field-settings
  {:req-ks [:form-id :data-path]
   :opt-ks [:placeholder :hasButtons]})

(defn numeric-input-field
  [config]
  (let [config (massage-config numeric-input-field-settings config)
        props @(rf/subscribe [::get-block-props config])
        {:keys [placeholder hasButtons value disabled show-errors errors]} props
        hasError (when (and show-errors (seq errors)) true)]
    (schema/assert-compatible-schema
      {:schema1 @(rf/subscribe [::get-data-schema config])
       :schema2 {:type "number"}})

    [ui/NumericInputField
     {:value       value
      :placeholder placeholder
      :disabled    disabled
      :hasError    hasError
      :hasButtons  hasButtons
      :onChange    #(rf/dispatch [::value-changed config %])}]))

(defn numeric-input-field-with-label
  [config]
  [form-group config
   [numeric-input-field config]])

(def textarea-field-settings
  {:req-ks [:form-id :data-path]
   :opt-ks [:placeholder :rows :maxLength]})

(defn textarea-field
  [config]
  (let [config (massage-config textarea-field-settings config)
        props @(rf/subscribe [::get-block-props config])
        {:keys [placeholder rows maxLength value disabled show-errors errors]} props
        hasError (when (and show-errors (seq errors)) true)]

    (schema/assert-compatible-schema
      {:schema1 @(rf/subscribe [::get-data-schema config])
       :schema2 {:type "string"}})

    [ui/TextareaField
     {:value       (or value "")                            ; TODO: should be guaranteed by sub
      :placeholder placeholder
      :disabled    disabled
      :hasError    hasError
      :maxLength   maxLength
      :rows        rows
      :onChange    #(rf/dispatch [::value-changed config %])}]))

(def checkbox-field-settings
  {:req-ks [:form-id :data-path]
   :opt-ks []})

(defn checkbox-field
  [config]
  (let [config (massage-config checkbox-field-settings config)
        props @(rf/subscribe [::get-block-props config])
        {:keys [value disabled show-errors errors]} props
        hasError (when (and show-errors (seq errors)) true)]

    (schema/assert-compatible-schema
      {:schema1 @(rf/subscribe [::get-data-schema config])
       :schema2 {:type "boolean"}})

    [ui/CheckboxField
     {:checked  (or value false)                            ; TODO: should be guaranteed by sub
      :disabled disabled
      :hasError hasError
      :onChange #(rf/dispatch [::value-changed config %])}]))

(defn checkbox-field-with-label
  [config]
  [:div.checkbox-field-with-label
   [form-group config
    [checkbox-field config]]])

(defn textarea-field-with-label
  [config]
  [form-group config
   [textarea-field config]])

(def date-field-settings
  {:req-ks [:form-id :data-path]
   :opt-ks [:minDate :maxDate]})

(defn date-field
  [config]
  (let [config (massage-config date-field-settings config)
        props @(rf/subscribe [::get-block-props config])
        {:keys [minDate maxDate value disabled errors show-errors]} props
        hasError (when (and show-errors (seq errors)) true)]

    (schema/assert-compatible-schema
      {:schema1 @(rf/subscribe [::get-data-schema config])
       :schema2 {:type "string"}})

    [ui/DateField
     {:value    (date/from-value value)
      :disabled disabled
      :hasError hasError
      :minDate  (date/from-value minDate)
      :maxDate  (date/from-value maxDate)
      :onChange #(rf/dispatch [::value-changed config (date/to-value %)])}]))

(defn date-field-with-label
  [config]
  [form-group config
   [date-field config]])

(defn portal-link
  []
  (let [{:keys [site]} @(rf/subscribe [:subs/get-derived-path [:context]])
        {:keys [portal_title portal_url]} site]
    (if portal_url
      [:a {:href portal_url :target "_blank"} [:span.portal-title portal_title]]
      [:span.portal-title portal_title])))

(def note-for-data-manager-settings
  {:req-ks [:form-id :data-path]
   :opt-ks []})

(defn note-for-data-manager
  [config]
  (let [config (massage-config note-for-data-manager-settings config)
        {:keys [document]} @(rf/subscribe [:subs/get-derived-path [:context]])
        value @(rf/subscribe [::get-block-data config])]
    [:div
     {:style {:padding-top    5
              :padding-bottom 5}}
     (if (= "Draft" (:status document))
       [textarea-field-with-label config]
       (when-not (string/blank? value)
         [:div
          [:strong "Note for the data manager:"]
          [:p value]]))]))

(defn handle-submit-click
  []
  (rf/dispatch [:handlers/lodge-click]))

(defn lodge-button
  []
  (let [page @(rf/subscribe [:subs/get-page-props])
        ;; FIXME need an m4 saving? value.
        saving (:metcalf3.handlers/saving? page)
        {:keys [document urls]} @(rf/subscribe [:subs/get-derived-path [:context]])
        {:keys [errors]} @(rf/subscribe [:subs/get-derived-path [:progress]])
        {:keys [disabled]} @(rf/subscribe [:subs/get-derived-path [:form]])
        has-errors? (and errors (> errors 0))
        archived? (= (:status document) "Archived")
        submitted? (= (:status document) "Submitted")]
    (when-not (or archived? submitted?)
      [:button.btn.btn-primary.btn-lg
       {:disabled (or has-errors? saving disabled)
        :on-click handle-submit-click}
       (when saving
         [:img
          {:src (str (:STATIC_URL urls)
                     "metcalf3/img/saving.gif")}])
       "Lodge data"])))

(defn lodge-status-info
  []
  (let [page @(rf/subscribe [:subs/get-page-props])
        ;; FIXME need an m4 saving? value.
        saving (:metcalf3.handlers/saving? page)
        {:keys [document]} @(rf/subscribe [:subs/get-derived-path [:context]])
        {:keys [errors]} @(rf/subscribe [:subs/get-derived-path [:progress]])
        is-are (if (> errors 1) "are" "is")
        plural (when (> errors 1) "s")
        has-errors? (and errors (> errors 0))]
    (if has-errors?
      [:span.text-danger [:b "Unable to lodge: "]
       "There " is-are " " [:span errors " error" plural
                            " which must be corrected first."]]
      [:span.text-success
       [:b
        (cond
          saving "Submitting..."
          (= (:status document) "Draft") "Ready to lodge"
          (= (:status document) "Submitted") "Your record has been submitted."
          :else (:status document))]])))

(def xml-export-link-settings
  {:req-ks [:form-id :data-path :label]
   :opt-ks []})

(defn xml-export-link
  [config]
  (let [config (massage-config xml-export-link-settings config)
        {:keys [label]} @(rf/subscribe [::get-block-props config])
        {:keys [document]} @(rf/subscribe [:subs/get-derived-path [:context]])
        dirty @(rf/subscribe [:subs/get-form-dirty])]
    (let [download-props {:href     (str (:export_url document) "?download")
                          :on-click #(when dirty
                                       (.preventDefault %)
                                       (rf/dispatch [:handlers/open-modal
                                                     {:type    :alert
                                                      :message "Please save changes before exporting."}]))}]

      [:a download-props label])))

(defn mailto-data-manager-link
  []
  (let [{:keys [site]} @(rf/subscribe [:subs/get-derived-path [:context]])
        {:keys [email]} site]
    [:a {:href (str "mailto:" email)} email]))

(def simple-select-option-settings
  {:req-ks [:form-id :data-path :options]
   :opt-ks [:placeholder]})

(defn simple-select-option
  [config]
  (let [config (massage-config simple-select-option-settings config)
        props @(rf/subscribe [::get-block-props config])
        value @(rf/subscribe [::get-block-data config])
        {:keys [placeholder options disabled errors show-errors]} props
        hasError (when (and show-errors (seq errors)) true)]

    (schema/assert-compatible-schema
      {:schema1 @(rf/subscribe [::get-data-schema config])
       :schema2 {:type "object" :properties {}}})

    [ui/SimpleSelectField
     {:value       value
      :options     options
      :placeholder placeholder
      :disabled    disabled
      :hasError    (seq hasError)
      :onChange    #(rf/dispatch [::option-change config %])}]))

(def table-select-option-settings
  {:req-ks [:form-id :data-path :options :labelKey :valueKey :columns]
   :opt-ks [:placeholder]})

(defn table-select-option
  [config]
  (let [config (massage-config table-select-option-settings config)
        props @(rf/subscribe [::get-block-props config])
        value @(rf/subscribe [::get-block-data config])
        {:keys [placeholder options disabled errors show-errors labelKey valueKey columns]} props
        hasError (when (and show-errors (seq errors)) true)]

    (schema/assert-compatible-schema
      {:schema1 @(rf/subscribe [::get-data-schema config])
       :schema2 {:type "object" :properties {}}})

    [ui/TableSelectField
     {:value       value
      :options     options
      :labelKey    labelKey
      :valueKey    valueKey
      :columns     columns
      :placeholder placeholder
      :disabled    disabled
      :hasError    (seq hasError)
      :onChange    #(rf/dispatch [::option-change config %])}]))

(def breadcrumb-select-option-settings
  {:req-ks [:form-id :data-path :options :labelKey :valueKey :breadcrumbKey]
   :opt-ks [:placeholder]})

(defn breadcrumb-select-option
  [config]
  (let [config (massage-config breadcrumb-select-option-settings config)
        props @(rf/subscribe [::get-block-props config])
        value @(rf/subscribe [::get-block-data config])
        {:keys [placeholder options disabled errors show-errors labelKey valueKey breadcrumbKey]} props
        hasError (when (and show-errors (seq errors)) true)]

    (schema/assert-compatible-schema
      {:schema1 @(rf/subscribe [::get-data-schema config])
       :schema2 {:type "object" :properties {}}})

    [ui/TableSelectField
     {:value         value
      :options       options
      :labelKey      labelKey
      :valueKey      valueKey
      :breadcrumbKey breadcrumbKey
      :placeholder   placeholder
      :disabled      disabled
      :hasError      (seq hasError)
      :onChange      #(rf/dispatch [::option-change config %])}]))

(defmulti select-option :kind)
(defmethod select-option :default [config] (simple-select-option config))
(defmethod select-option :breadcrumb [config] (breadcrumb-select-option config))
(defmethod select-option :table [config] (table-select-option config))

(defn select-option-with-label
  [config]
  [form-group config
   [select-option config]])

(def list-add-button-settings
  {:req-ks [:form-id :data-path :valueKey :addedKey]
   :opt-ks []})

; NOTE: Experimental
(defn item-add-button
  "Add user defined item as value"
  [config]
  (let [config (massage-config list-add-button-settings config)
        props @(rf/subscribe [::get-block-props config])
        {:keys [valueKey addedKey]} props]

    (s/assert string? valueKey)
    (s/assert string? addedKey)

    (schema/assert-compatible-schema
      {:schema1 @(rf/subscribe [::get-data-schema config])
       :schema2 (utils4/schema-object-with-keys [valueKey addedKey])})

    [:button.bp3-button.bp3-intent-primary
     {:onClick #(rf/dispatch [::item-add-with-defaults-click-handler config])}
     "Add"]))

(defn list-add-button
  "Add user defined item to list"
  [config]
  (let [config (massage-config list-add-button-settings config)
        props @(rf/subscribe [::get-block-props config])
        {:keys [valueKey addedKey]} props]

    (s/assert string? valueKey)
    (s/assert string? addedKey)

    (schema/assert-compatible-schema
      {:schema1 @(rf/subscribe [::get-data-schema config])
       :schema2 {:type "array" :items (utils4/schema-object-with-keys [valueKey addedKey])}})

    [:button.bp3-button.bp3-intent-primary
     {:onClick #(rf/dispatch [::list-add-with-defaults-click-handler config])}
     "Add"]))

(defn simple-select-option-with-label
  [config]
  [form-group config
   [simple-select-option config]])

(def async-simple-select-option-settings
  {:req-ks [:form-id :data-path :uri :valueKey :labelKey]
   :opt-ks []})

(defn async-simple-select-option
  [config]
  (let [config (massage-config async-simple-select-option-settings config)
        props @(rf/subscribe [::get-block-props config])
        value @(rf/subscribe [::get-block-data config])
        {:keys [placeholder uri valueKey labelKey disabled errors show-errors]} props
        hasError (when (and show-errors (seq errors)) true)]

    (schema/assert-compatible-schema
      {:schema1 @(rf/subscribe [::get-data-schema config])
       :schema2 {:type "object" :properties {}}})

    [ui/AsyncSimpleSelectField
     {:value       value
      :loadOptions #(utils4/fetch-post {:uri uri :body {:query %}})
      :valueKey    valueKey
      :labelKey    labelKey
      :placeholder placeholder
      :disabled    disabled
      :hasError    (seq hasError)
      :onChange    #(rf/dispatch [::option-change config %])}]))

(defn async-simple-select-option-with-label
  [config]
  [form-group config
   [async-simple-select-option config]])

(def async-breadcrumb-select-option-settings
  {:req-ks [:form-id :data-path :uri :valueKey :labelKey :breadcrumbKey]
   :opt-ks []})

(defn async-breadcrumb-select-option
  [config]
  (let [config (massage-config async-breadcrumb-select-option-settings config)
        props @(rf/subscribe [::get-block-props config])
        value @(rf/subscribe [::get-block-data config])
        {:keys [placeholder uri disabled errors show-errors valueKey labelKey breadcrumbKey]} props
        hasError (when (and show-errors (seq errors)) true)]

    (schema/assert-compatible-schema
      {:schema1 @(rf/subscribe [::get-data-schema config])
       :schema2 {:type "object" :properties {}}})

    [ui/AsyncBreadcrumbSelectField
     {:value         value
      :loadOptions   #(utils4/fetch-post {:uri uri :body {:query %}})
      :valueKey      valueKey
      :labelKey      labelKey
      :breadcrumbKey breadcrumbKey
      :placeholder   placeholder
      :disabled      disabled
      :hasError      (seq hasError)
      :onChange      #(rf/dispatch [::option-change config %])}]))

(defn async-breadcrumb-select-option-with-label
  [config]
  [form-group config
   [async-breadcrumb-select-option config]])

(def async-table-select-option-settings
  {:req-ks [:form-id :data-path :uri :valueKey :labelKey :columns]
   :opt-ks []})

(defn async-table-select-option
  [config]
  (let [config (massage-config async-table-select-option-settings config)
        props @(rf/subscribe [::get-block-props config])
        value @(rf/subscribe [::get-block-data config])
        {:keys [placeholder uri disabled errors show-errors valueKey labelKey columns]} props
        hasError (when (and show-errors (seq errors)) true)]

    (schema/assert-compatible-schema
      {:schema1 @(rf/subscribe [::get-data-schema config])
       :schema2 {:type "object" :properties {}}})

    [ui/AsyncTableSelectField
     {:value       value
      :loadOptions #(utils4/fetch-post {:uri uri :body {:query %}})
      :valueKey    valueKey
      :labelKey    labelKey
      :columns     columns
      :placeholder placeholder
      :disabled    disabled
      :hasError    (seq hasError)
      :onChange    #(rf/dispatch [::option-change config %])}]))

(defn async-table-select-option-with-label
  [config]
  [form-group config
   [async-table-select-option config]])

(defmulti async-select-option :kind)
(defmethod async-select-option :default [config] (async-simple-select-option config))
(defmethod async-select-option :breadcrumb [config] (async-breadcrumb-select-option config))
(defmethod async-select-option :table [config] (async-table-select-option config))

(defn async-select-option-with-label
  [config]
  [form-group config
   [async-select-option config]])

(def select-value-settings
  {:req-ks [:form-id :data-path :options :labelKey :valueKey]
   :opt-ks []})

(defn select-value
  [config]
  (let [config (massage-config select-value-settings config)
        props @(rf/subscribe [::get-block-props config])
        {:keys [value options labelKey valueKey disabled errors show-errors]} props
        hasError (when (and show-errors (seq errors)) true)
        value (or value "")]

    (s/assert schema/schema-value-type? @(rf/subscribe [::get-data-schema config]))

    [ui/SelectValueField
     {:value    value
      :disabled disabled
      :options  options
      :labelKey labelKey
      :valueKey valueKey
      :hasError hasError
      :onChange #(rf/dispatch [::value-changed config %])}]))

(defn select-value-with-label
  [config]
  [form-group config
   [select-value config]])

(def yes-no-field-settings
  {:req-ks [:form-id :data-path :label]
   :opt-ks []})

(defn yes-no-field
  [config]
  (let [config (massage-config yes-no-field-settings config)
        props @(rf/subscribe [::get-yes-no-field-props config])
        {:keys [label value disabled errors show-errors]} props
        hasError (when (and show-errors (seq errors)) true)]

    (schema/assert-compatible-schema
      {:schema1 @(rf/subscribe [::get-data-schema config])
       :schema2 {:type "boolean"}})

    [ui/YesNoRadioGroup
     {:value    value
      :label    label
      :disabled disabled
      :hasError hasError
      :onChange #(rf/dispatch [::value-changed config %])}]))

; FIXME: Is :label for form group or yes/no field?
(def yes-no-field-with-label-settings
  {:req-ks [:form-id :data-path :label]
   :opt-ks []})

(defn yes-no-field-with-label
  [config]
  (let [config (massage-config yes-no-field-with-label-settings config)
        props @(rf/subscribe [::get-yes-no-field-with-label-props config])
        {:keys [label value errors show-errors]} props
        hasError (when (and show-errors (seq errors)) true)]

    (schema/assert-compatible-schema
      {:schema1 @(rf/subscribe [::get-data-schema config])
       :schema2 {:type "boolean"}})

    [form-group config
     [ui/YesNoRadioGroup
      {:value    value
       :label    label
       :disabled false
       :hasError (seq hasError)
       :onChange #(rf/dispatch [::yes-no-field-with-label-value-changed config %])}]]))

(def simple-selection-list-settings
  {:req-ks [:form-id :data-path :labelKey :valueKey]
   :opt-ks [:addedKey]})

(defn simple-selection-list
  [config]
  (let [config (massage-config simple-selection-list-settings config)
        props @(rf/subscribe [::get-block-props config])
        items @(rf/subscribe [::get-block-data config])
        {:keys [key disabled labelKey valueKey addedKey]} props]

    (s/assert string? valueKey)
    (s/assert string? labelKey)
    (s/assert (s/nilable string?) addedKey)

    (schema/assert-compatible-schema
      {:schema1 @(rf/subscribe [::get-data-schema config])
       :schema2 {:type "array" :items (utils4/schema-object-with-keys (remove nil? [valueKey labelKey addedKey]))}})

    [ui/SimpleSelectionList
     {:key           key
      :items         items
      :labelKey      labelKey
      :valueKey      valueKey
      :addedKey      addedKey
      :disabled      disabled
      :onReorder     (fn [src-idx dst-idx] (rf/dispatch [::selection-list-reorder props src-idx dst-idx]))
      :onItemClick   (fn [idx] (rf/dispatch [::selection-list-item-click props idx]))
      :onRemoveClick (fn [idx] (rf/dispatch [::selection-list-remove-click props idx]))}]))

(def breadcrumb-selection-list-settings
  {:req-ks [:form-id :data-path :labelKey :valueKey :breadcrumbKey]
   :opt-ks [:addedKey]})

(defn breadcrumb-selection-list
  [config]
  (let [config (massage-config breadcrumb-selection-list-settings config)
        props @(rf/subscribe [::get-block-props config])
        items @(rf/subscribe [::get-block-data config])
        {:keys [key disabled labelKey valueKey addedKey breadcrumbKey]} props]

    (s/assert string? valueKey)
    (s/assert string? labelKey)
    (s/assert string? breadcrumbKey)
    (s/assert (s/nilable string?) addedKey)

    (schema/assert-compatible-schema
      {:schema1 @(rf/subscribe [::get-data-schema config])
       :schema2 {:type "array" :items (utils4/schema-object-with-keys (remove nil? [labelKey valueKey breadcrumbKey addedKey]))}})

    [ui/BreadcrumbSelectionList
     {:key           key
      :items         items
      :disabled      disabled
      :breadcrumbKey breadcrumbKey
      :labelKey      labelKey
      :valueKey      valueKey
      :addedKey      addedKey
      :onReorder     (fn [src-idx dst-idx] (rf/dispatch [::selection-list-reorder props src-idx dst-idx]))
      :onItemClick   (fn [idx] (rf/dispatch [::selection-list-item-click props idx]))
      :onRemoveClick (fn [idx] (rf/dispatch [::selection-list-remove-click props idx]))}]))

(def table-selection-list-settings
  {:req-ks [:form-id :data-path :columns :valueKey]
   :opt-ks [:addedKey]})

(defn table-selection-list
  [config]
  (let [config (massage-config table-selection-list-settings config)
        props @(rf/subscribe [::get-block-props config])
        {:keys [key disabled columns valueKey addedKey]} props
        items @(rf/subscribe [::get-block-data config])]

    (s/assert string? valueKey)
    (s/assert (s/nilable string?) addedKey)

    (schema/assert-compatible-schema
      {:schema1 @(rf/subscribe [::get-data-schema config])
       :schema2 {:type  "array"
                 :items (utils4/schema-object-with-keys
                          (into (remove nil? [valueKey addedKey]) (map :labelKey columns)))}})

    [ui/TableSelectionList
     {:key           key
      :items         items
      :disabled      disabled
      :columns       columns
      :valueKey      valueKey
      :addedKey      addedKey
      :onReorder     (fn [src-idx dst-idx] (rf/dispatch [::selection-list-reorder config src-idx dst-idx]))
      :onItemClick   (fn [idx] (rf/dispatch [::selection-list-item-click config idx]))
      :onRemoveClick (fn [idx] (rf/dispatch [::selection-list-remove-click config idx]))}]))

(def simple-list-option-picker-settings
  {:req-ks [:form-id :data-path :options :valueKey :labelKey]
   :opt-ks [:placeholder]})

(defn simple-list-option-picker
  [config]
  (let [config (massage-config simple-list-option-picker-settings config)
        props @(rf/subscribe [::get-block-props config])
        {:keys [placeholder options disabled errors show-errors valueKey labelKey]} props
        hasError (when (and show-errors (seq errors)) true)]

    (schema/assert-compatible-schema
      {:schema1 @(rf/subscribe [::get-data-schema config])
       :schema2 {:type "array" :items {:type "object" :properties {}}}})

    [ui/SimpleSelectField
     {:value       nil
      :options     options
      :placeholder placeholder
      :disabled    disabled
      :hasError    (seq hasError)
      :labelKey    labelKey
      :valueKey    valueKey
      :onChange    #(rf/dispatch [::list-option-picker-change config %])}]))

(def breadcrumb-list-option-picker-settings
  {:req-ks [:form-id :data-path :options :valueKey :labelKey :breadcrumbKey]
   :opt-ks [:placeholder]})

(defn breadcrumb-list-option-picker
  [config]
  (let [config (massage-config breadcrumb-list-option-picker-settings config)
        props @(rf/subscribe [::get-block-props config])
        {:keys [placeholder options disabled errors show-errors valueKey labelKey breadcrumbKey]} props
        hasError (when (and show-errors (seq errors)) true)]

    (schema/assert-compatible-schema
      {:schema1 @(rf/subscribe [::get-data-schema config])
       :schema2 {:type "array" :items {:type "object" :properties {}}}})

    [ui/BreadcrumbSelectField
     {:value         nil
      :options       options
      :placeholder   placeholder
      :disabled      disabled
      :hasError      hasError
      :labelKey      labelKey
      :valueKey      valueKey
      :breadcrumbKey breadcrumbKey
      :onChange      #(rf/dispatch [::list-option-picker-change config %])}]))

(def table-list-option-picker-settings
  {:req-ks [:form-id :data-path :options :valueKey :labelKey :columns]
   :opt-ks [:placeholder]})

(defn table-list-option-picker
  [config]
  (let [config (massage-config table-list-option-picker-settings config)
        props @(rf/subscribe [::get-block-props config])
        {:keys [placeholder options disabled errors show-errors valueKey labelKey columns]} props
        hasError (when (and show-errors (seq errors)) true)]

    (schema/assert-compatible-schema
      {:schema1 @(rf/subscribe [::get-data-schema config])
       :schema2 {:type "array" :items {:type "object" :properties {}}}})

    [ui/TableSelectField
     {:value       nil
      :options     options
      :placeholder placeholder
      :disabled    disabled
      :hasError    hasError
      :labelKey    labelKey
      :valueKey    valueKey
      :columns     columns
      :onChange    #(rf/dispatch [::list-option-picker-change config %])}]))

(def async-simple-list-option-picker-settings
  {:req-ks [:form-id :data-path :uri :valueKey :labelKey]
   :opt-ks [:placeholder]})

(defn async-simple-list-option-picker
  [config]
  (let [config (massage-config async-simple-list-option-picker-settings config)
        props @(rf/subscribe [::get-block-props config])
        {:keys [placeholder uri disabled errors show-errors valueKey labelKey]} props
        hasError (when (and show-errors (seq errors)) true)]

    (schema/assert-compatible-schema
      {:schema1 @(rf/subscribe [::get-data-schema config])
       :schema2 {:type "array" :items {:type "object" :properties {}}}})

    [ui/AsyncSimpleSelectField
     {:value       nil
      :valueKey    valueKey
      :labelKey    labelKey
      :placeholder placeholder
      :disabled    disabled
      :hasError    hasError
      :loadOptions #(utils4/fetch-post {:uri uri :body {:query %}})
      :onChange    #(rf/dispatch [::list-option-picker-change config %])}]))

(def async-breadcrumb-list-option-picker-settings
  {:req-ks [:form-id :data-path :uri :valueKey :labelKey :breadcrumbKey]
   :opt-ks [:placeholder]})

(defn async-breadcrumb-list-option-picker
  [config]
  (let [config (massage-config async-breadcrumb-list-option-picker-settings config)
        props @(rf/subscribe [::get-block-props config])
        {:keys [placeholder uri disabled errors show-errors valueKey labelKey breadcrumbKey]} props
        hasError (when (and show-errors (seq errors)) true)]

    (schema/assert-compatible-schema
      {:schema1 @(rf/subscribe [::get-data-schema config])
       :schema2 {:type "array" :items {:type "object" :properties {}}}})

    [ui/AsyncBreadcrumbSelectField
     {:value         nil
      :placeholder   placeholder
      :disabled      disabled
      :hasError      hasError
      :labelKey      labelKey
      :valueKey      valueKey
      :breadcrumbKey breadcrumbKey
      :loadOptions   #(utils4/fetch-post {:uri uri :body {:query %}})
      :onChange      #(rf/dispatch [::list-option-picker-change config %])}]))

(def async-table-list-option-picker-settings
  {:req-ks [:form-id :data-path :uri :valueKey :labelKey :columns]
   :opt-ks [:placeholder]})

(defn async-table-list-option-picker
  [config]
  (let [config (massage-config async-table-list-option-picker-settings config)
        props @(rf/subscribe [::get-block-props config])
        {:keys [placeholder uri disabled errors show-errors valueKey labelKey columns]} props
        hasError (when (and show-errors (seq errors)) true)]

    (schema/assert-compatible-schema
      {:schema1 @(rf/subscribe [::get-data-schema config])
       :schema2 {:type "array" :items {:type "object" :properties {}}}})

    [ui/AsyncTableSelectField
     {:value       nil
      :placeholder placeholder
      :disabled    disabled
      :hasError    hasError
      :labelKey    labelKey
      :valueKey    valueKey
      :columns     columns
      :loadOptions #(utils4/fetch-post {:uri uri :body {:query %}})
      :onChange    #(rf/dispatch [::list-option-picker-change config %])}]))

(defmulti async-list-picker :kind)
(defmethod async-list-picker :default [config] (async-simple-list-option-picker config))
(defmethod async-list-picker :breadcrumb [config] (async-breadcrumb-list-option-picker config))
(defmethod async-list-picker :table [config] (async-table-list-option-picker config))

(def expanding-control-settings
  {:req-ks [:label]
   :opt-ks [:form-id :data-path :required]})

(defn expanding-control
  [config & children]
  (let [config (massage-config expanding-control-settings config)
        props @(rf/subscribe [::get-block-props config])
        {:keys [label required errors show-errors]} props
        hasError (when (and show-errors (seq errors)) true)]
    (s/assert string? label)
    (into [ui/ExpandingControl
           {:label    label
            :required required}]
          children)))


(defn coord-field
  [path]
  [:div.CoordField
   [:div.row
    [:div.col-sm-6.col-sm-offset-3.col-lg-4.col-lg-offset-2
     [:div.n-block
      [numeric-input-field-with-label
       {:form-id   [:form]
        :data-path (conj path "northBoundLatitude")
        :required  true}]]]]
   [:div.row
    [:div.col-sm-6.col-lg-4
     [:div.w-block
      [numeric-input-field-with-label
       {:form-id   [:form]
        :data-path (conj path "westBoundLongitude")
        :required  true}]]]
    [:div.col-sm-6.col-lg-4
     [:div.e-block
      [numeric-input-field-with-label
       {:form-id   [:form]
        :data-path (conj path "eastBoundLongitude")}]]]]
   [:div.row
    [:div.col-sm-6.col-sm-offset-3.col-lg-4.col-lg-offset-2
     [:div.s-block
      [numeric-input-field-with-label
       {:form-id   [:form]
        :data-path (conj path "southBoundLatitude")}]]]]])

(def boxmap-field-settings
  {:req-ks [:form-id :data-path]
   :opt-ks []})

(defn boxmap-field
  [config]
  (letfn [(boxes->elements
            [boxes]
            (for [box boxes]
              {:northBoundLatitude (get-in box ["northBoundLatitude"])
               :southBoundLatitude (get-in box ["southBoundLatitude"])
               :eastBoundLongitude (get-in box ["eastBoundLongitude"])
               :westBoundLongitude (get-in box ["westBoundLongitude"])}))]
    (let [config (massage-config boxmap-field-settings config)
          props @(rf/subscribe [::get-block-props config])
          data @(rf/subscribe [::get-block-data config])
          elements (boxes->elements data)
          {:keys [disabled is-hidden]} props]
      (when-not is-hidden
        [boxmap/box-map2-fill
         {:elements  elements
          :disabled  (not disabled)
          :tick-id   @(rf/subscribe [:subs/get-form-tick])
          :on-change #(rf/dispatch [::boxes-changed config %])}]))))

(def coordinates-modal-field-settings
  {:req-ks [:form-id :data-path]
   :opt-ks [:help]})

(defn coordinates-modal-field
  [config]
  (let [config (massage-config coordinates-modal-field-settings config)
        props @(rf/subscribe [::get-block-props config])
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
                                        :on-save      #(rf/dispatch [:handlers/close-modal])}])))
            (delete-fn [idx] (rf/dispatch [::boxmap-coordinates-list-delete config idx]))
            (try-delete-fn [idx] (rf/dispatch [::boxmap-coordinates-click-confirm-delete #(delete-fn idx)]))
            (open-edit-fn [indexed-data-path] (when-not disabled
                                                (rf/dispatch [::boxmap-coordinates-open-edit-modal
                                                              {:ctx         (assoc config :data-path indexed-data-path)
                                                               :coord-field coord-field
                                                               :on-delete   #(try-delete-fn (last indexed-data-path))
                                                               :on-save     #(rf/dispatch [:handlers/close-modal])
                                                               :on-cancel   #(rf/dispatch [:handlers/close-modal])}])))]
      (when-not is-hidden
        [:div.TableInlineEdit
         (when help [:p.help-block help])
         (if (pos? (count data))
           [:table.table {:class (when-not (or disabled (empty? data)) "table-hover")}
            [:thead
             (-> [:tr]
                 (into (for [th ths] [:th th]))
                 (conj [:th.xcell " "]))]
            (if (not-empty data)
              (into [:tbody]
                    (for [[idx field] (map-indexed vector data)]
                      (let [data-path (conj data-path idx)
                            form-state @(rf/subscribe [::common-subs/get-form-state (:form-id config)])
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
