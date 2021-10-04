(ns metcalf4.components
  (:require [cljs.spec.alpha :as s]
            [clojure.string :as string]
            [interop.date :as date]
            [interop.ui :as ui]
            [metcalf4.blocks :as blocks]
            [metcalf4.subs :as common-subs]
            [metcalf4.utils :as utils4]
            [re-frame.core :as rf]
            [metcalf4.schema :as schema]))

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

    (schema/assert-compatible-schema
      {:schema1 @(rf/subscribe [::get-data-schema ctx])
       :schema2 {:type "string"}})

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

    (schema/assert-compatible-schema
      {:schema1 @(rf/subscribe [::get-data-schema ctx])
       :schema2 {:type "string"}})

    [ui/TextareaField
     {:value       (or value "")                            ; TODO: should be guaranteed by sub
      :placeholder placeholder
      :disabled    disabled
      :hasError    hasError
      :maxLength   maxLength
      :rows        rows
      :onChange    onChange}]))

(defn checkbox-field
  [config]
  (let [config-keys [:placeholder :rows :maxLength]
        ctx (utils4/get-ctx config)
        logic @(rf/subscribe [::get-block-props ctx])
        onChange #(rf/dispatch [::checkbox-field-value-changed ctx %])
        props (merge logic (select-keys config config-keys))
        {:keys [value disabled show-errors errors]} props
        hasError (when (and show-errors (seq errors)) true)]

    (schema/assert-compatible-schema
      {:schema1 @(rf/subscribe [::get-data-schema ctx])
       :schema2 {:type "boolean"}})

    [ui/CheckboxField
     {:checked  (or value false)                            ; TODO: should be guaranteed by sub
      :disabled disabled
      :hasError hasError
      :onChange onChange}]))

(defn checkbox-field-with-label
  [config]
  [form-group config
   [checkbox-field config]])

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

    (schema/assert-compatible-schema
      {:schema1 @(rf/subscribe [::get-data-schema ctx])
       :schema2 {:type "string"}})

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

(defn portal-link
  []
  (let [{:keys [site]} @(rf/subscribe [:subs/get-derived-path [:context]])
        {:keys [portal_title portal_url]} site]
    (if portal_url
      [:a {:href portal_url :target "_blank"} [:span.portal-title portal_title]]
      [:span.portal-title portal_title])))

(defn note-for-data-manager
  [config]
  (let [{:keys [form-id notes-path]} config
        {:keys [document]} @(rf/subscribe [:subs/get-derived-path [:context]])
        notes @(rf/subscribe [:subs/get-derived-path notes-path])]
    ;; TODO show form, or a readonly paragraph if submitted.
    [:div
     {:style {:padding-top    5
              :padding-bottom 5}}
     (if (= "Draft" (:status document))
       [textarea-field-with-label {:form-id   form-id
                                   :data-path notes-path}]
       (when-not (string/blank? (:value notes))
         [:div
          [:strong "Note for the data manager:"]
          [:p (:value notes)]]))]))

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
        submitted? (= (:status document) "Submitted")]
    [:button.btn.btn-primary.btn-lg
     {:disabled (or has-errors? saving disabled submitted?)
      :on-click handle-submit-click}
     (when saving
       [:img
        {:src (str (:STATIC_URL urls)
                   "metcalf3/img/saving.gif")}])
     "Lodge data"]))

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
  (let [{:keys [document]} @(rf/subscribe [:subs/get-derived-path [:context]])
        dirty @(rf/subscribe [:subs/get-form-dirty])]
    (let [download-props {:href     (str (:export_url document) "?download")
                          :on-click #(when dirty
                                       (.preventDefault %)
                                       (rf/dispatch [:handlers/open-modal
                                                     {:type    :alert
                                                      :message "Please save changes before exporting."}]))}]

      [:a download-props (:label config)])))

(defn mailto-data-manager-link
  []
  (let [{:keys [site]} @(rf/subscribe [:subs/get-derived-path [:context]])
        {:keys [email]} site]
    [:a {:href (str "mailto:" email)} email]))

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

    (schema/assert-compatible-schema
      {:schema1 @(rf/subscribe [::get-data-schema ctx])
       :schema2 {:type "object" :properties {}}})

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

    (schema/assert-compatible-schema
      {:schema1 @(rf/subscribe [::get-data-schema ctx])
       :schema2 {:type "object" :properties {}}})

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
        config-keys [:options :labelKey :valueKey]
        logic @(rf/subscribe [::get-block-props ctx])
        onChange #(rf/dispatch [::select-value-changed ctx %])
        props (merge logic (select-keys config config-keys))
        {:keys [value options labelKey valueKey disabled errors show-errors]} props
        hasError (when (and show-errors (seq errors)) true)
        value (or value "")]

    (schema/assert-compatible-schema
      {:schema1 @(rf/subscribe [::get-data-schema ctx])
       :schema2 {:type "object" :properties {labelKey {} valueKey {}}}})

    [ui/SelectValueField
     {:value    value
      :disabled disabled
      :options  options
      :labelKey labelKey
      :valueKey valueKey
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
        onRemoveClick (fn [idx] (rf/dispatch [::selection-list-remove-click ctx idx]))
        onReorder (fn [src-idx dst-idx] (rf/dispatch [::selection-list-reorder ctx src-idx dst-idx]))
        logic @(rf/subscribe [::get-block-props ctx])
        {:keys [key disabled labelKey valueKey]} (merge logic (select-keys config config-keys))
        items @(rf/subscribe [::get-block-data ctx])]
    [ui/SimpleSelectionList
     {:key           key
      :items         items
      :labelKey      labelKey
      :valueKey      valueKey
      :disabled      disabled
      :onReorder     onReorder
      :onRemoveClick onRemoveClick}]))

(defn breadcrumb-selection-list
  [config]
  (let [config-keys [:labelKey :valueKey :breadcrumbKey]
        ctx (utils4/get-ctx config)
        onRemoveClick (fn [idx] (rf/dispatch [::selection-list-remove-click ctx idx]))
        onReorder (fn [src-idx dst-idx] (rf/dispatch [::selection-list-reorder ctx src-idx dst-idx]))
        logic @(rf/subscribe [::get-block-props ctx])
        {:keys [key disabled labelKey valueKey breadcrumbKey]} (merge logic (select-keys config config-keys))
        items @(rf/subscribe [::get-block-data ctx])]
    [ui/BreadcrumbSelectionList
     {:key           key
      :items         items
      :disabled      disabled
      :onReorder     onReorder
      :onRemoveClick onRemoveClick
      :breadcrumbKey breadcrumbKey
      :labelKey      labelKey
      :valueKey      valueKey}]))

(defn table-selection-list
  [config]
  (let [config-keys [:columns :valueKey]
        ctx (utils4/get-ctx config)
        onRemoveClick (fn [idx] (rf/dispatch [::selection-list-remove-click ctx idx]))
        onReorder (fn [src-idx dst-idx] (rf/dispatch [::selection-list-reorder ctx src-idx dst-idx]))
        logic @(rf/subscribe [::get-block-props ctx])
        {:keys [key disabled columns valueKey]} (merge logic (select-keys config config-keys))
        items @(rf/subscribe [::get-block-data ctx])]
    [ui/TableSelectionList
     {:key           key
      :items         items
      :disabled      disabled
      :onReorder     onReorder
      :onRemoveClick onRemoveClick
      :columns       columns
      :valueKey      valueKey}]))

(defn list-option-picker
  [config]
  (let [ctx (utils4/get-ctx config)
        config-keys [:options :placeholder]
        logic @(rf/subscribe [::get-block-props ctx])
        onChange #(rf/dispatch [::list-option-picker-change ctx %])
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

(defn async-list-option-picker
  [config]
  (let [ctx (utils4/get-ctx config)
        config-keys [:uri :placeholder]
        logic @(rf/subscribe [::get-block-props ctx])
        onChange #(rf/dispatch [::list-option-picker-change ctx %])
        props (merge logic (select-keys config config-keys))
        {:keys [placeholder uri disabled errors show-errors]} props
        hasError (when (and show-errors (seq errors)) true)]
    [ui/AsyncSelectOptionField
     {:value       nil
      :loadOptions #(utils4/fetch-post {:uri uri :body {:query %}})
      :placeholder placeholder
      :disabled    disabled
      :hasError    (seq hasError)
      :onChange    onChange}]))

(defn expanding-control
  [config & children]
  (let [config-keys [:label :required]
        ctx (utils4/get-ctx config)
        logic @(rf/subscribe [::get-block-props ctx])
        props (merge logic (select-keys config config-keys))
        {:keys [label required errors show-errors]} props
        hasError (when (and show-errors (seq errors)) true)]
    (s/assert string? label)
    (into [ui/ExpandingControl
           {:label    label
            :required required}]
          children)))
