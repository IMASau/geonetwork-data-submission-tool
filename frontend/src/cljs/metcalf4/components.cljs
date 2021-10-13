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

(defn massage-config
  [{:keys [opt-ks req-ks]} config]
  (let [all-ks (distinct (concat opt-ks req-ks))
        config1 (utils4/get-ctx config)
        config2 (select-keys config all-ks)
        extra-ks (remove (set all-ks) (keys config))
        missing-ks (remove (set (keys config)) req-ks)]
    (s/assert empty? missing-ks)
    (s/assert empty? extra-ks)
    (merge config1 config2)))

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
  (let [config (massage-config config {:req-ks [:label] :opt-ks [:placeholder :helperText :toolTip]})
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

(defn inline-form-group
  [config & children]
  (let [config (massage-config config {:req-ks [:label] :opt-ks [:placeholder :helperText :toolTip]})
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

(defn list-edit-dialog
  "Popup dialog if item is selected"
  [config]
  (let [config (massage-config config {:req-ks [:title :template-id] :opt-ks []})
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

(defn input-field
  [config]
  (let [config (massage-config config {:req-ks [] :opt-ks [:placeholder :maxLength]})
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

(defn numeric-input-field
  [config]
  (let [config (massage-config config {:req-ks [] :opt-ks [:placeholder :hasButtons]})
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

(defn textarea-field
  [config]
  (let [config (massage-config config {:req-ks [] :opt-ks [:placeholder :rows :maxLength]})
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

(defn checkbox-field
  [config]
  (let [config (massage-config config {:req-ks [] :opt-ks []})
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

(defn date-field
  [config]
  (let [config (massage-config config {:req-ks [] :opt-ks [:minDate :maxDate]})
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

(defn note-for-data-manager
  [config]
  (let [config (massage-config config {:req-ks [] :opt-ks []})
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

(defn xml-export-link
  [config]
  (let [config (massage-config config {:req-ks [:label] :opt-ks []})
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

(defn simple-select-option
  [config]
  (let [config (massage-config config {:req-ks [:options] :opt-ks [:placeholder]})
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

(defn table-select-option
  [config]
  (let [config (massage-config config {:req-ks [:options :labelKey :valueKey :columns] :opt-ks [:placeholder]})
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

(defn breadcrumb-select-option
  [config]
  (let [config (massage-config config {:req-ks [:options :labelKey :valueKey :breadcrumbKey] :opt-ks [:placeholder]})
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

(defn list-add-button
  "Add user defined item to list"
  [config]
  (let [config (massage-config config {:req-ks [:valueKey :addedKey] :opt-ks []})
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

(defn async-simple-select-option
  [config]
  (let [config (massage-config config {:req-ks [:uri :valueKey :labelKey] :opt-ks []})
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

(defn async-breadcrumb-select-option
  [config]
  (let [config (massage-config config {:req-ks [:uri :valueKey :labelKey :breadcrumbKey] :opt-ks []})
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

(defn async-table-select-option
  [config]
  (let [config (massage-config config {:req-ks [:uri :valueKey :labelKey :columns] :opt-ks []})
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

(defn select-value
  [config]
  (let [config (massage-config config {:req-ks [:options :labelKey :valueKey] :opt-ks []})
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

(defn yes-no-field
  [config]
  (let [config (massage-config config {:req-ks [:label] :opt-ks []})
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
(defn yes-no-field-with-label
  [config]
  (let [config (massage-config config {:req-ks [:label] :opt-ks []})
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

(defn simple-selection-list
  [config]
  (let [config (massage-config config {:req-ks [:labelKey :valueKey] :opt-ks [:addedKey]})
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

(defn breadcrumb-selection-list
  [config]
  (let [config (massage-config config {:req-ks [:labelKey :valueKey :breadcrumbKey] :opt-ks [:addedKey]})
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

(defn table-selection-list
  [config]
  (let [config (massage-config config {:req-ks [:columns :valueKey] :opt-ks [:addedKey]})
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

(defn simple-list-option-picker
  [config]
  (let [ctx (utils4/get-ctx config)
        config-keys [:options :placeholder :valueKey :labelKey]
        logic @(rf/subscribe [::get-block-props ctx])
        onChange #(rf/dispatch [::list-option-picker-change ctx %])
        props (merge ctx logic (select-keys config config-keys))
        {:keys [placeholder options disabled errors show-errors valueKey labelKey]} props
        hasError (when (and show-errors (seq errors)) true)]

    (schema/assert-compatible-schema
      {:schema1 @(rf/subscribe [::get-data-schema ctx])
       :schema2 {:type "array" :items {:type "object" :properties {}}}})

    [ui/SimpleSelectField
     {:value       nil
      :options     options
      :placeholder placeholder
      :disabled    disabled
      :hasError    (seq hasError)
      :onChange    onChange
      :labelKey    labelKey
      :valueKey    valueKey}]))

(defn breadcrumb-list-option-picker
  [config]
  (let [ctx (utils4/get-ctx config)
        config-keys [:options :placeholder :valueKey :labelKey :breadcrumbKey]
        logic @(rf/subscribe [::get-block-props ctx])
        onChange #(rf/dispatch [::list-option-picker-change ctx %])
        props (merge ctx logic (select-keys config config-keys))
        {:keys [placeholder options disabled errors show-errors valueKey labelKey breadcrumbKey]} props
        hasError (when (and show-errors (seq errors)) true)]

    (schema/assert-compatible-schema
      {:schema1 @(rf/subscribe [::get-data-schema ctx])
       :schema2 {:type "array" :items {:type "object" :properties {}}}})

    [ui/BreadcrumbSelectField
     {:value         nil
      :options       options
      :placeholder   placeholder
      :disabled      disabled
      :hasError      (seq hasError)
      :onChange      onChange
      :labelKey      labelKey
      :valueKey      valueKey
      :breadcrumbKey breadcrumbKey}]))

(defn table-list-option-picker
  [config]
  (let [ctx (utils4/get-ctx config)
        config-keys [:options :placeholder :valueKey :labelKey :columns]
        logic @(rf/subscribe [::get-block-props ctx])
        onChange #(rf/dispatch [::list-option-picker-change ctx %])
        props (merge ctx logic (select-keys config config-keys))
        {:keys [placeholder options disabled errors show-errors valueKey labelKey columns]} props
        hasError (when (and show-errors (seq errors)) true)]

    (schema/assert-compatible-schema
      {:schema1 @(rf/subscribe [::get-data-schema ctx])
       :schema2 {:type "array" :items {:type "object" :properties {}}}})

    [ui/TableSelectField
     {:value       nil
      :options     options
      :placeholder placeholder
      :disabled    disabled
      :hasError    (seq hasError)
      :onChange    onChange
      :labelKey    labelKey
      :valueKey    valueKey
      :columns     columns}]))

(defn async-simple-list-option-picker
  [config]
  (let [ctx (utils4/get-ctx config)
        config-keys [:uri :placeholder :valueKey :labelKey]
        logic @(rf/subscribe [::get-block-props ctx])
        onChange #(rf/dispatch [::list-option-picker-change ctx %])
        props (merge ctx logic (select-keys config config-keys))
        {:keys [placeholder uri disabled errors show-errors valueKey labelKey]} props
        hasError (when (and show-errors (seq errors)) true)]

    (schema/assert-compatible-schema
      {:schema1 @(rf/subscribe [::get-data-schema ctx])
       :schema2 {:type "array" :items {:type "object" :properties {}}}})

    [ui/AsyncSimpleSelectField
     {:value       nil
      :loadOptions #(utils4/fetch-post {:uri uri :body {:query %}})
      :valueKey    valueKey
      :labelKey    labelKey
      :placeholder placeholder
      :disabled    disabled
      :hasError    (seq hasError)
      :onChange    onChange}]))

(defn async-breadcrumb-list-option-picker
  [config]
  (let [ctx (utils4/get-ctx config)
        config-keys [:uri :placeholder :valueKey :labelKey :breadcrumbKey]
        logic @(rf/subscribe [::get-block-props ctx])
        onChange #(rf/dispatch [::list-option-picker-change ctx %])
        props (merge ctx logic (select-keys config config-keys))
        {:keys [placeholder uri disabled errors show-errors valueKey labelKey breadcrumbKey]} props
        hasError (when (and show-errors (seq errors)) true)]

    (schema/assert-compatible-schema
      {:schema1 @(rf/subscribe [::get-data-schema ctx])
       :schema2 {:type "array" :items {:type "object" :properties {}}}})

    [ui/AsyncBreadcrumbSelectField
     {:value         nil
      :loadOptions   #(utils4/fetch-post {:uri uri :body {:query %}})
      :placeholder   placeholder
      :disabled      disabled
      :hasError      (seq hasError)
      :onChange      onChange
      :labelKey      labelKey
      :valueKey      valueKey
      :breadcrumbKey breadcrumbKey}]))

(defn async-table-list-option-picker
  [config]
  (let [ctx (utils4/get-ctx config)
        config-keys [:uri :placeholder :valueKey :labelKey :columns]
        logic @(rf/subscribe [::get-block-props ctx])
        onChange #(rf/dispatch [::list-option-picker-change ctx %])
        props (merge ctx logic (select-keys config config-keys))
        {:keys [placeholder uri disabled errors show-errors valueKey labelKey columns]} props
        hasError (when (and show-errors (seq errors)) true)]

    (schema/assert-compatible-schema
      {:schema1 @(rf/subscribe [::get-data-schema ctx])
       :schema2 {:type "array" :items {:type "object" :properties {}}}})

    [ui/AsyncTableSelectField
     {:value       nil
      :loadOptions #(utils4/fetch-post {:uri uri :body {:query %}})
      :placeholder placeholder
      :disabled    disabled
      :hasError    (seq hasError)
      :onChange    onChange
      :labelKey    labelKey
      :valueKey    valueKey
      :columns     columns}]))

(defmulti async-list-picker :kind)
(defmethod async-list-picker :default [config] (async-simple-list-option-picker config))
(defmethod async-list-picker :breadcrumb [config] (async-breadcrumb-list-option-picker config))
(defmethod async-list-picker :table [config] (async-table-list-option-picker config))

(defn expanding-control
  [config & children]
  (let [config-keys [:label :required]
        ctx (utils4/get-ctx config)
        logic @(rf/subscribe [::get-block-props ctx])
        props (merge ctx logic (select-keys config config-keys))
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

(defn boxmap-field
  [config]
  (letfn [(boxes->elements
            [boxes]
            (for [box boxes]
              {:northBoundLatitude (get-in box ["northBoundLatitude"])
               :southBoundLatitude (get-in box ["southBoundLatitude"])
               :eastBoundLongitude (get-in box ["eastBoundLongitude"])
               :westBoundLongitude (get-in box ["westBoundLongitude"])}))]
    (let [ctx (utils4/get-ctx config)
          config-keys [:options :placeholder]
          logic @(rf/subscribe [::get-block-props ctx])
          data @(rf/subscribe [::get-block-data ctx])
          props (merge ctx logic (select-keys config config-keys))
          elements (boxes->elements data)
          {:keys [disabled is-hidden]} props]
      (when-not is-hidden
        [boxmap/box-map2-fill
         {:elements  elements
          :disabled  (not disabled)
          :tick-id   @(rf/subscribe [:subs/get-form-tick])
          :on-change #(rf/dispatch [::boxes-changed ctx %])}]))))

(defn coordinates-modal-field
  [config]
  (let [pretty-print (fn [x] (if (nil? x) "--" (if (number? x) (.toFixed x 3) (pr-str x))))
        ctx (utils4/get-ctx config)
        config-keys [:options :placeholder]
        logic @(rf/subscribe [::get-block-props ctx])
        data @(rf/subscribe [::get-block-data ctx])
        props (merge ctx logic (select-keys config config-keys))
        {:keys [is-hidden disabled help]} props
        data-path (:data-path ctx)
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
                                       {:ctx          ctx
                                        :coord-field  coord-field
                                        :initial-data new-item-with-values
                                        :idx          (count data)
                                        :on-close     #(rf/dispatch [::boxmap-coordinates-list-delete ctx %])
                                        :on-save      #(rf/dispatch [:handlers/close-modal])}])))
            (delete-fn [idx] (rf/dispatch [::boxmap-coordinates-list-delete ctx idx]))
            (try-delete-fn [idx] (rf/dispatch [::boxmap-coordinates-click-confirm-delete #(delete-fn idx)]))
            (open-edit-fn [indexed-data-path] (when-not disabled
                                                (rf/dispatch [::boxmap-coordinates-open-edit-modal
                                                              {:ctx         (assoc ctx :data-path indexed-data-path)
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
                            form-state @(rf/subscribe [::common-subs/get-form-state (:form-id ctx)])
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
