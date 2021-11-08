(ns metcalf.common.components4
  (:require [cljs.spec.alpha :as s]
            [clojure.string :as string]
            [interop.date :as date]
            [interop.ui :as ui]
            [metcalf.common.blocks4 :as blocks]
            [metcalf.common.low-code4 :as low-code]
            [metcalf.common.subs4 :as common-subs]
            [metcalf.common.utils4 :as utils4]
            [re-frame.core :as rf]
            [reagent.core :as r]))

(s/def ::obj-path (s/coll-of string? :min-count 1))
(s/def ::value-path string?)

(defn has-error?
  "Given the current form state, and a data path, check if
  the field for that data path has errors."
  [form-state data-path]
  (let [path (blocks/block-path data-path)
        field (get-in form-state path)
        errors (-> field :props :errors)]
    (seq errors)))

(defn page-errors-settings
  [{:keys [data-paths]}]
  {::low-code/req-ks       [:form-id :data-path :data-paths]
   ::low-code/opt-ks       []
   ::low-code/schema-paths data-paths})

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

(defn form-group-settings [_]
  {::low-code/req-ks []
   ::low-code/opt-ks [:label :form-id :data-path :placeholder :helperText :toolTip]})

(defn form-group
  [config & children]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [label helperText toolTip required disabled is-hidden show-errors errors]} props
        ; NOTE: treating label is a special case, if defined in config it overrides logic
        label (get config :label label)
        hasError (when (and show-errors (seq errors)) true)]
    (when-not is-hidden
      (into [ui/FormGroup
             {:label      label
              :required   required
              :disabled   disabled
              :hasError   hasError
              :helperText (if hasError (string/join ". " errors) helperText)
              :toolTip    (r/as-element toolTip)}]
            children))))

(defn inline-form-group-settings [_]
  {::low-code/req-ks [:label]
   ::low-code/opt-ks [:form-id :data-path :placeholder :helperText :toolTip]})

(defn inline-form-group
  [config & children]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [label helperText toolTip required disabled is-hidden show-errors errors]} props
        ; NOTE: treating label is a special case, if defined in config it overrides logic
        label (get config :label label)
        hasError (when (and show-errors (seq errors)) true)]
    (when-not is-hidden
      (into [ui/InlineFormGroup
             {:label      label
              :required   required
              :disabled   disabled
              :hasError   hasError
              :helperText (if hasError (string/join ". " errors) helperText)
              :toolTip    (r/as-element toolTip)}]
            children))))

(defn list-edit-dialog-settings [_]
  {::low-code/req-ks [:form-id :data-path :title :template-id]
   ::low-code/opt-ks []})

(defn list-edit-dialog
  "Popup dialog if item is selected"
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
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

(defn typed-list-edit-dialog-settings
  [{:keys [data-path type-path]}]
  {::low-code/req-ks       [:form-id :data-path :type-path :templates]
   ::low-code/opt-ks       []
   ::low-code/schema       {:type "array" :items {:type "object"}}
   ::low-code/schema-paths [data-path type-path]})

(defn typed-list-edit-dialog
  "Popup dialog if item is selected.  Use type to decide which template to use."
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [form-id data-path type-path selected templates show-errors errors]} props
        hasError (when (and show-errors (seq errors)) true)
        item-data-path (conj data-path selected)
        value @(rf/subscribe [::get-block-data config])
        item-type (get-in value (into [selected] type-path))
        {:keys [title template-id]} (get templates item-type)]
    [ui/EditDialog
     {:isOpen  (boolean selected)
      :title   (or title "")
      :onClose #(rf/dispatch [::list-edit-dialog-close config])
      :onClear #(rf/dispatch [::list-edit-dialog-cancel config])
      :onSave  #(rf/dispatch [::list-edit-dialog-save config])
      :canSave (not hasError)}

     (low-code/render-template
       {:template-id template-id
        :variables   {'?form-id   form-id
                      '?data-path item-data-path
                      '?item-type item-type}})]))

(defn item-edit-dialog-settings [_]
  {::low-code/req-ks [:form-id :data-path :title :template-id]
   ::low-code/opt-ks []})

(defn item-edit-dialog
  "Popup dialog if item is selected"
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
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

(defn input-field-settings [_]
  {::low-code/req-ks [:form-id :data-path]
   ::low-code/opt-ks [:placeholder :maxLength]
   ::low-code/schema {:type "string"}})

(defn input-field
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [placeholder maxLength value disabled is-hidden show-errors errors]} props
        hasError (when (and show-errors (seq errors)) true)]
    (when-not is-hidden
      [ui/InputField
       {:value       (or value "")                          ; TODO: should be guaranteed by sub
        :placeholder placeholder
        :maxLength   maxLength
        :disabled    disabled
        :hasError    hasError
        :onChange    #(rf/dispatch [::value-changed config %])}])))

(defn when-data-settings [_]
  {::low-code/req-ks [:form-id :data-path :pred]
   ::low-code/opt-ks []})

(s/def ::empty-list? empty?)
(s/def ::not-set? (s/or :n nil? :s (s/and string? string/blank?)))

(defn when-data
  [config & children]
  (let [{:keys [pred is-hidden]} @(rf/subscribe [::get-block-props config])
        value @(rf/subscribe [::get-block-data config])]
    (when-not is-hidden
      (when (s/valid? pred value)
        (into [:div] children)))))

(defn get-data-settings [_]
  {::low-code/req-ks [:form-id :data-path]
   ::low-code/opt-ks []})

(defn get-data
  [config]
  (str @(rf/subscribe [::get-block-data config])))

(defn input-field-with-label
  [config]
  [form-group config
   [input-field config]])

(defn numeric-input-field-settings [_]
  {::low-code/req-ks [:form-id :data-path]
   ::low-code/opt-ks [:placeholder :hasButtons :unit]
   ::low-code/schema {:type "number"}})

(defn numeric-input-field
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [placeholder hasButtons value disabled is-hidden show-errors errors unit]} props
        hasError (when (and show-errors (seq errors)) true)]
    (when-not is-hidden
      ; TODO: move styling to css
      [:div {:style {:display "flex" :flex-direction "row" :align-items "center"}}
       [ui/NumericInputField
        {:value       value
         :placeholder placeholder
         :disabled    disabled
         :hasError    hasError
         :hasButtons  hasButtons
         :onChange    #(rf/dispatch [::value-changed config %])}]
       [:div {:style {:padding-left "0.5em"}} unit]])))

(defn numeric-input-field-with-label
  [config]
  [form-group config
   [numeric-input-field config]])

(defn textarea-field-settings [_]
  {::low-code/req-ks [:form-id :data-path]
   ::low-code/opt-ks [:placeholder :rows :maxLength]
   ::low-code/schema {:type "string"}})

(defn textarea-field
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [placeholder rows maxLength value disabled is-hidden show-errors errors]} props
        hasError (when (and show-errors (seq errors)) true)]
    (when-not is-hidden
      [ui/TextareaField
       {:value       (or value "")                          ; TODO: should be guaranteed by sub
        :placeholder placeholder
        :disabled    disabled
        :hasError    hasError
        :maxLength   maxLength
        :rows        rows
        :onChange    #(rf/dispatch [::value-changed config %])}])))

(defn checkbox-field-settings [_]
  {::low-code/req-ks [:form-id :data-path]
   ::low-code/opt-ks [:label]
   ::low-code/schema {:type "boolean"}})

(defn checkbox-field
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [label value disabled show-errors errors]} props
        hasError (when (and show-errors (seq errors)) true)
        ; NOTE: treating label is a special case, if defined in config it overrides logic
        label (get config :label label)]
    [ui/CheckboxField
     {:label    label
      :checked  (or value false)                            ; TODO: should be guaranteed by sub?
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

(defn date-field-settings [_]
  {::low-code/req-ks [:form-id :data-path]
   ::low-code/opt-ks [:minDate :maxDate]
   ::low-code/schema {:type "string"}})

(defn date-field
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [minDate maxDate value disabled is-hidden errors show-errors]} props
        hasError (when (and show-errors (seq errors)) true)]
    (when-not is-hidden
      [ui/DateField
       {:value    (date/from-value value)
        :disabled disabled
        :hasError hasError
        :minDate  (date/from-value minDate)
        :maxDate  (date/from-value maxDate)
        :onChange #(rf/dispatch [::value-changed config (date/to-value %)])}])))

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

(defn note-for-data-manager-settings [_]
  {::low-code/req-ks [:form-id :data-path]
   ::low-code/opt-ks []})

(defn note-for-data-manager
  [config]
  (let [{:keys [document]} @(rf/subscribe [:subs/get-derived-path [:context]])
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
        :on-click #(rf/dispatch [::lodge-button-click])}
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

(defn xml-export-link-settings [_]
  {::low-code/req-ks [:label]
   ::low-code/opt-ks [:form-id :data-path]})

(defn xml-export-link
  [config]
  (let [{:keys [label]} @(rf/subscribe [::get-block-props config])
        {:keys [document]} @(rf/subscribe [:subs/get-derived-path [:context]])
        dirty @(rf/subscribe [:subs/get-form-dirty])
        download-props {:href     (str (:export_url document) "?download")
                        :on-click #(when dirty
                                     (js/alert "Please save changes before exporting."))}]
    [:a download-props label]))

(defn mailto-data-manager-link
  []
  (let [{:keys [site]} @(rf/subscribe [:subs/get-derived-path [:context]])
        {:keys [email]} site]
    [:a {:href (str "mailto:" email)} email]))

(defn simple-select-option-settings
  [{:keys [value-path label-path added-path]}]
  {::low-code/req-ks       [:form-id :data-path :options :value-path :label-path]
   ::low-code/opt-ks       [:placeholder :added-path]
   ::low-code/schema       {:type "object" :properties {}}
   ::low-code/schema-paths [value-path label-path added-path]})

(defn simple-select-option
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        value @(rf/subscribe [::get-block-data config])
        {:keys [placeholder options disabled is-hidden errors show-errors value-path label-path added-path]} props
        hasError (when (and show-errors (seq errors)) true)]
    (when-not is-hidden
      [ui/SimpleSelectField
       {:value       value
        :options     options
        :placeholder placeholder
        :disabled    disabled
        :getValue    (ui/get-obj-path value-path)
        :getLabel    (ui/get-obj-path label-path)
        :getAdded    (when added-path (ui/get-obj-path added-path))
        :hasError    hasError
        :onChange    #(rf/dispatch [::option-change config (ui/get-option-data %)])}])))

(defn table-select-option-settings
  [{:keys [label-path value-path added-path columns]}]
  {::low-code/req-ks       [:form-id :data-path :options :label-path :value-path :columns]
   ::low-code/opt-ks       [:placeholder :added-path]
   ::low-code/schema       {:type "object" :properties {}}
   ::low-code/schema-paths (into [label-path value-path added-path]
                                 (map :label-path columns))})

(defn table-select-option
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        value @(rf/subscribe [::get-block-data config])
        {:keys [placeholder options disabled is-hidden errors show-errors label-path value-path added-path columns]} props
        hasError (when (and show-errors (seq errors)) true)]
    (when-not is-hidden
      [ui/TableSelectField
       {:value       value
        :options     options
        :placeholder placeholder
        :disabled    disabled
        :hasError    hasError
        :getLabel    (ui/get-obj-path label-path)
        :getValue    (ui/get-obj-path value-path)
        :getAdded    (when added-path (ui/get-obj-path added-path))
        :columns     (for [{:keys [flex label-path]} columns]
                       {:flex     flex
                        :getLabel (ui/get-obj-path label-path)})
        :onChange    #(rf/dispatch [::option-change config (ui/get-option-data %)])}])))

(defn breadcrumb-select-option-settings
  [{:keys [label-path value-path breadcrumb-path added-path]}]
  {::low-code/req-ks       [:form-id :data-path :options :label-path :value-path :breadcrumb-path]
   ::low-code/opt-ks       [:placeholder :added-path]
   ::low-code/schema       {:type "object" :properties {}}
   ::low-code/schema-paths [label-path value-path added-path breadcrumb-path]})

(defn breadcrumb-select-option
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        value @(rf/subscribe [::get-block-data config])
        {:keys [placeholder options disabled is-hidden errors show-errors label-path value-path breadcrumb-path added-path]} props
        hasError (when (and show-errors (seq errors)) true)]
    (when-not is-hidden
      [ui/BreadcrumbSelectField
       {:value         value
        :options       options
        :placeholder   placeholder
        :disabled      disabled
        :hasError      hasError
        :getLabel      (ui/get-obj-path label-path)
        :getValue      (ui/get-obj-path value-path)
        :getBreadcrumb (ui/get-obj-path breadcrumb-path)
        :getAdded      (when added-path (ui/get-obj-path added-path))
        :onChange      #(rf/dispatch [::option-change config (ui/get-option-data %)])}])))

(defmulti select-option-settings :kind)
(defmethod select-option-settings :default [config] (simple-select-option-settings config))
(defmethod select-option-settings :breadcrumb [config] (breadcrumb-select-option-settings config))
(defmethod select-option-settings :table [config] (table-select-option-settings config))

(defmulti select-option :kind)
(defmethod select-option :default [config] (simple-select-option config))
(defmethod select-option :breadcrumb [config] (breadcrumb-select-option config))
(defmethod select-option :table [config] (table-select-option config))

(defn select-option-with-label
  [config]
  [form-group config
   [select-option config]])

(defn item-add-button-settings [_]
  {::low-code/req-ks [:form-id :data-path :value-path :added-path]
   ::low-code/opt-ks []
   ::low-code/schema {:type "object"}})

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
        :onClick  #(rf/dispatch [::item-add-with-defaults-click-handler config])}
       "Add"])))

(defn list-add-button-settings [_]
  {::low-code/req-ks [:form-id :data-path :value-path :added-path :button-text]
   ::low-code/opt-ks [:item-defaults]
   ::low-code/schema {:type "array"}})

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

(defn text-add-button-settings [_]
  {::low-code/req-ks [:form-id :data-path :button-text]
   ::low-code/opt-ks []
   ::low-code/schema {:type "array"}})

(defn text-add-button
  "Add user defined item to list"
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [disabled is-hidden button-text]} props]
    (when-not is-hidden
      [ui/TextAddField
       {:buttonText (r/as-element button-text)
        :disabled   disabled
        :onAddClick #(rf/dispatch [::text-value-add-click-handler config %])}])))

(defn async-select-value-settings
  [{:keys [value-path label-path]}]
  {::low-code/req-ks       [:form-id :data-path :uri :value-path :label-path :results-path]
   ::low-code/opt-ks       [:placeholder]
   ::low-code/schema       {:type "object" :properties {}}
   ::low-code/schema-paths [value-path label-path]})

(defn async-select-value
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [placeholder value value-path label-path disabled is-hidden errors show-errors]} props
        hasError (when (and show-errors (seq errors)) true)]
    (when-not is-hidden
      [ui/AsyncSimpleSelectField
       {:value       value
        :placeholder placeholder
        :disabled    disabled
        :hasError    hasError
        :loadOptions (partial utils4/load-options config)
        :getValue    (ui/get-obj-path value-path)
        :getLabel    (ui/get-obj-path label-path)
        :onChange    #(rf/dispatch [::value-changed config %])}])))

(defn async-simple-select-option-settings
  [{:keys [value-path label-path]}]
  {::low-code/req-ks       [:form-id :data-path :uri :value-path :label-path]
   ::low-code/opt-ks       [:placeholder :added-path]
   ::low-code/schema       {:type "object" :properties {}}
   ::low-code/schema-paths [value-path label-path]})

(defn async-simple-select-option
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        value @(rf/subscribe [::get-block-data config])
        {:keys [placeholder value-path label-path added-path disabled is-hidden errors show-errors]} props
        hasError (when (and show-errors (seq errors)) true)]
    (when-not is-hidden
      [ui/AsyncSimpleSelectField
       {:value       value
        :placeholder placeholder
        :disabled    disabled
        :hasError    hasError
        :loadOptions (partial utils4/load-options config)
        :getValue    (ui/get-obj-path value-path)
        :getLabel    (ui/get-obj-path label-path)
        :getAdded    (when added-path (ui/get-obj-path added-path))
        :onChange    #(rf/dispatch [::option-change config (ui/get-option-data %)])}])))

(defn async-breadcrumb-select-option-settings
  [{:keys [value-path label-path breadcrumb-path]}]
  {::low-code/req-ks       [:form-id :data-path :uri :value-path :label-path :breadcrumb-path]
   ::low-code/opt-ks       [:placeholder :added-path]
   ::low-code/schema       {:type "object" :properties {}}
   ::low-code/schema-paths [value-path label-path breadcrumb-path]})

(defn async-breadcrumb-select-option
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        value @(rf/subscribe [::get-block-data config])
        {:keys [placeholder disabled is-hidden errors show-errors value-path label-path breadcrumb-path added-path]} props
        hasError (when (and show-errors (seq errors)) true)]
    (when-not is-hidden
      [ui/AsyncBreadcrumbSelectField
       {:value         value
        :loadOptions   (partial utils4/load-options config)
        :getValue      (ui/get-obj-path value-path)
        :getLabel      (ui/get-obj-path label-path)
        :getAdded      (when added-path (ui/get-obj-path added-path))
        :getBreadcrumb (ui/get-obj-path breadcrumb-path)
        :placeholder   placeholder
        :disabled      disabled
        :hasError      hasError
        :onChange      #(rf/dispatch [::option-change config (ui/get-option-data %)])}])))

(defn async-table-select-option-settings [_]
  {::low-code/req-ks [:form-id :data-path :uri :value-path :label-path :columns]
   ::low-code/opt-ks [:placeholder :added-path]
   ::low-code/schema {:type "object" :properties {}}})

(defn async-table-select-option
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        value @(rf/subscribe [::get-block-data config])
        {:keys [placeholder disabled is-hidden errors show-errors value-path label-path added-path columns]} props
        hasError (when (and show-errors (seq errors)) true)]
    (when-not is-hidden
      [ui/AsyncTableSelectField
       {:value       value
        :placeholder placeholder
        :disabled    disabled
        :hasError    hasError
        :loadOptions (partial utils4/load-options config)
        :getValue    (ui/get-obj-path value-path)
        :getLabel    (ui/get-obj-path label-path)
        :getAdded    (when added-path (ui/get-obj-path added-path))
        :columns     (for [{:keys [flex label-path]} columns]
                       {:flex     flex
                        :getLabel (ui/get-obj-path label-path)})
        :onChange    #(rf/dispatch [::option-change config (ui/get-option-data %)])}])))

(defmulti async-select-option-settings :kind)
(defmethod async-select-option-settings :default [config] (async-simple-select-option-settings config))
(defmethod async-select-option-settings :breadcrumb [config] (async-breadcrumb-select-option-settings config))
(defmethod async-select-option-settings :table [config] (async-table-select-option-settings config))

(defmulti async-select-option :kind)
(defmethod async-select-option :default [config] (async-simple-select-option config))
(defmethod async-select-option :breadcrumb [config] (async-breadcrumb-select-option config))
(defmethod async-select-option :table [config] (async-table-select-option config))

(defn select-value-settings [_]
  {::low-code/req-ks [:form-id :data-path :options :label-path :value-path]
   ::low-code/opt-ks []})

(defn select-value
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [value options label-path value-path disabled is-hidden errors show-errors]} props
        hasError (when (and show-errors (seq errors)) true)
        value (or value "")]
    (when-not is-hidden
      [ui/SelectValueField
       {:value    value
        :disabled disabled
        :options  options
        :getLabel (ui/get-obj-path label-path)
        :getValue (ui/get-obj-path value-path)
        :hasError hasError
        :onChange #(rf/dispatch [::value-changed config %])}])))

(defn select-value-with-label
  [config]
  [form-group config
   [select-value config]])

(defn yes-no-field-settings [_]
  {::low-code/req-ks [:form-id :data-path :label]
   ::low-code/opt-ks []
   ::low-code/schema {:type "boolean"}})

(defn yes-no-field
  [config]
  (let [props @(rf/subscribe [::get-yes-no-field-props config])
        {:keys [label value disabled is-hidden errors show-errors]} props
        hasError (when (and show-errors (seq errors)) true)]
    (when-not is-hidden
      [ui/YesNoRadioGroup
       {:value    value
        :label    label
        :disabled disabled
        :hasError hasError
        :onChange #(rf/dispatch [::value-changed config %])}])))

(defn simple-selection-list-settings
  [{:keys [label-path value-path added-path]}]
  {::low-code/req-ks       [:form-id :data-path :label-path :value-path]
   ::low-code/opt-ks       [:added-path]
   ::low-code/schema       {:type "array" :items {:type "object"}}
   ::low-code/schema-paths [label-path value-path added-path]})

(defn simple-selection-list
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        items @(rf/subscribe [::get-block-data config])
        {:keys [key disabled is-hidden label-path value-path added-path]} props]
    (when-not is-hidden
      [ui/SimpleSelectionList
       {:key           key
        :items         (or items [])
        :disabled      disabled
        :getLabel      (ui/get-obj-path label-path)
        :getValue      (ui/get-obj-path value-path)
        :getAdded      (when added-path (ui/get-obj-path added-path))
        :onReorder     (fn [src-idx dst-idx] (rf/dispatch [::selection-list-reorder props src-idx dst-idx]))
        :onItemClick   (fn [idx] (rf/dispatch [::selection-list-item-click props idx]))
        :onRemoveClick (fn [idx] (rf/dispatch [::selection-list-remove-click props idx]))}])))

(defn value-selection-list-settings
  [_]
  {::low-code/req-ks [:form-id :data-path]
   ::low-code/opt-ks []
   ::low-code/schema {:type "array" :items {:type "string"}}})

(defn value-selection-list
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        labels @(rf/subscribe [::get-block-data config])
        {:keys [key disabled is-hidden]} props
        items (map (fn [label] {:value (gensym) :label label}) labels)]
    (when-not is-hidden
      [ui/SimpleSelectionList
       {:key           key
        :items         items
        :disabled      disabled
        :getLabel      (ui/get-obj-path ["label"])
        :getValue      (ui/get-obj-path ["value"])
        :onReorder     (fn [src-idx dst-idx] (rf/dispatch [::value-selection-list-reorder props src-idx dst-idx]))
        :onRemoveClick (fn [idx] (rf/dispatch [::value-selection-list-remove-click props idx]))}])))

(defn selection-list-settings
  [{:keys [value-path added-path]}]
  {::low-code/req-ks       [:form-id :data-path :value-path :template-id]
   ::low-code/opt-ks       [:added-path]
   ::low-code/schema       {:type "array" :items {:type "object"}}
   ::low-code/schema-paths [value-path added-path]})

(defn selection-list
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        items @(rf/subscribe [::get-block-data config])
        {:keys [form-id data-path key disabled is-hidden template-id value-path added-path]} props]
    (when-not is-hidden
      [ui/SelectionList
       {:key           key
        :items         (or items [])
        :disabled      disabled
        :renderItem    (fn [args]
                         (let [index (ui/get-obj-path args ["index"])]
                           (r/as-element
                             (low-code/render-template
                               {:template-id template-id
                                :variables   {'?form-id   form-id
                                              '?data-path (conj data-path index)}}))))
        :getValue      (ui/get-obj-path value-path)
        :getAdded      (when added-path (ui/get-obj-path added-path))
        :onReorder     (fn [src-idx dst-idx] (rf/dispatch [::selection-list-reorder props src-idx dst-idx]))
        :onItemClick   (fn [idx] (rf/dispatch [::selection-list-item-click props idx]))
        :onRemoveClick (fn [idx] (rf/dispatch [::selection-list-remove-click props idx]))}])))

(defn simple-list-settings
  [_]
  {::low-code/req-ks       [:form-id :data-path :template-id]
   ::low-code/opt-ks       []
   ::low-code/schema       {:type "array" :items {:type "object"}}
   ::low-code/schema-paths []})

(defn simple-list
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        items @(rf/subscribe [::get-block-data config])
        {:keys [form-id data-path is-hidden template-id]} props]
    (when-not is-hidden
      [:<>
       (for [index (range (count items))]
         (low-code/render-template
           {:template-id template-id
            :variables   {'?form-id   form-id
                          '?data-path (conj data-path index)}}))])))

(defn breadcrumb-selection-list-settings
  [{:keys [label-path value-path breadcrumb-path added-path]}]
  {::low-code/req-ks       [:form-id :data-path :label-path :value-path :breadcrumb-path]
   ::low-code/opt-ks       [:added-path]
   ::low-code/schema       {:type "array" :items {:type "object"}}
   ::low-code/schema-paths [label-path value-path breadcrumb-path added-path]})

(defn breadcrumb-selection-list
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        items @(rf/subscribe [::get-block-data config])
        {:keys [key disabled is-hidden label-path value-path added-path breadcrumb-path]} props]
    (when-not is-hidden
      [ui/BreadcrumbSelectionList
       {:key           key
        :items         (or items [])
        :disabled      disabled
        :getBreadcrumb (ui/get-obj-path breadcrumb-path)
        :getLabel      (ui/get-obj-path label-path)
        :getValue      (ui/get-obj-path value-path)
        :getAdded      (when added-path (ui/get-obj-path added-path))
        :onReorder     (fn [src-idx dst-idx] (rf/dispatch [::selection-list-reorder props src-idx dst-idx]))
        :onItemClick   (fn [idx] (rf/dispatch [::selection-list-item-click props idx]))
        :onRemoveClick (fn [idx] (rf/dispatch [::selection-list-remove-click props idx]))}])))

(defn table-selection-list-settings
  [{:keys [value-path columns added-path]}]
  {::low-code/req-ks       [:form-id :data-path :columns :value-path]
   ::low-code/opt-ks       [:added-path]
   ::low-code/schema       {:type "array" :items {:type "object"}}
   ::low-code/schema-paths (into [value-path added-path] (map :label-path columns))})

(defn table-selection-list
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [key disabled is-hidden columns value-path added-path]} props
        items @(rf/subscribe [::get-block-data config])]
    (when-not is-hidden
      [ui/TableSelectionList
       {:key           key
        :items         (or items [])
        :disabled      disabled
        :columns       (for [{:keys [flex label-path columnHeader]} columns]
                         {:flex         flex
                          :getLabel     (ui/get-obj-path label-path)
                          :columnHeader (or columnHeader "None")})
        :getValue      (ui/get-obj-path value-path)
        :getAdded      (when added-path (ui/get-obj-path added-path))
        :onReorder     (fn [src-idx dst-idx] (rf/dispatch [::selection-list-reorder config src-idx dst-idx]))
        :onItemClick   (fn [idx] (rf/dispatch [::selection-list-item-click config idx]))
        :onRemoveClick (fn [idx] (rf/dispatch [::selection-list-remove-click config idx]))}])))

(defn simple-list-option-picker-settings [_]
  {::low-code/req-ks [:form-id :data-path :options :value-path :label-path]
   ::low-code/opt-ks [:placeholder]
   ::low-code/schema {:type "array" :items {:type "object"}}})

(defn simple-list-option-picker
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [placeholder options disabled is-hidden errors show-errors value-path label-path]} props
        hasError (when (and show-errors (seq errors)) true)]
    (when-not is-hidden
      [ui/SimpleSelectField
       {:value       nil
        :options     options
        :placeholder placeholder
        :disabled    disabled
        :hasError    hasError
        :getLabel    (ui/get-obj-path label-path)
        :getValue    (ui/get-obj-path value-path)
        :onChange    #(rf/dispatch [::list-option-picker-change config (ui/get-option-data %)])}])))

(defn breadcrumb-list-option-picker-settings [_]
  {::low-code/req-ks [:form-id :data-path :options :value-path :label-path :breadcrumb-path]
   ::low-code/opt-ks [:placeholder]
   ::low-code/schema {:type "array" :items {:type "object"}}})

(defn breadcrumb-list-option-picker
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [placeholder options disabled is-hidden errors show-errors value-path label-path breadcrumb-path]} props
        hasError (when (and show-errors (seq errors)) true)]
    (when-not is-hidden
      [ui/BreadcrumbSelectField
       {:value         nil
        :options       options
        :placeholder   placeholder
        :disabled      disabled
        :hasError      hasError
        :getValue      (ui/get-obj-path value-path)
        :getLabel      (ui/get-obj-path label-path)
        :getBreadcrumb (ui/get-obj-path breadcrumb-path)
        :onChange      #(rf/dispatch [::list-option-picker-change config (ui/get-option-data %)])}])))

(defn table-list-option-picker-settings [_]
  {::low-code/req-ks [:form-id :data-path :options :value-path :label-path :columns]
   ::low-code/opt-ks [:placeholder]
   ::low-code/schema {:type "array" :items {:type "object"}}})

(defn table-list-option-picker
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [placeholder options disabled is-hidden errors show-errors value-path label-path columns]} props
        hasError (when (and show-errors (seq errors)) true)]
    (when-not is-hidden
      [ui/TableSelectField
       {:value       nil
        :options     options
        :placeholder placeholder
        :disabled    disabled
        :hasError    hasError
        :getLabel    (ui/get-obj-path label-path)
        :getValue    (ui/get-obj-path value-path)
        :columns     (for [{:keys [flex label-path]} columns]
                       {:flex     flex
                        :getLabel (ui/get-obj-path label-path)})
        :onChange    #(rf/dispatch [::list-option-picker-change config (ui/get-option-data %)])}])))

(defn async-simple-list-option-picker-settings [_]
  {::low-code/req-ks [:form-id :data-path :uri :value-path :label-path]
   ::low-code/opt-ks [:placeholder]
   ::low-code/schema {:type "array" :items {:type "object"}}})

(defn async-simple-list-option-picker
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [placeholder is-hidden disabled errors show-errors value-path label-path]} props
        hasError (when (and show-errors (seq errors)) true)]
    (when-not is-hidden
      [ui/AsyncSimpleSelectField
       {:value       nil
        :placeholder placeholder
        :disabled    disabled
        :hasError    hasError
        :getValue    (ui/get-obj-path value-path)
        :getLabel    (ui/get-obj-path label-path)
        :loadOptions (partial utils4/load-options config)
        :onChange    #(rf/dispatch [::list-option-picker-change config (ui/get-option-data %)])}])))

(defn async-simple-item-option-picker-settings [_]
  {::low-code/req-ks [:form-id :data-path :uri :value-path :label-path]
   ::low-code/opt-ks [:placeholder]
   ::low-code/schema {:type "array" :items {:type "object"}}})

(defn async-simple-item-option-picker
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [placeholder disabled is-hidden errors show-errors value-path label-path]} props
        hasError (when (and show-errors (seq errors)) true)]
    (when-not is-hidden
      [ui/AsyncSimpleSelectField
       {:value       nil
        :placeholder placeholder
        :disabled    disabled
        :hasError    hasError
        :getValue    (ui/get-obj-path value-path)
        :getLabel    (ui/get-obj-path label-path)
        :loadOptions (partial utils4/load-options config)
        :onChange    #(rf/dispatch [::item-option-picker-change config (ui/get-option-data %)])}])))

(defn async-breadcrumb-list-option-picker-settings [_]
  {::low-code/req-ks [:form-id :data-path :uri :value-path :label-path :breadcrumb-path]
   ::low-code/opt-ks [:placeholder]
   ::low-code/schema {:type "array" :items {:type "object"}}})

(defn async-breadcrumb-list-option-picker
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [placeholder disabled is-hidden errors show-errors value-path label-path breadcrumb-path]} props
        hasError (when (and show-errors (seq errors)) true)]
    (when-not is-hidden
      [ui/AsyncBreadcrumbSelectField
       {:value         nil
        :placeholder   placeholder
        :disabled      disabled
        :hasError      hasError
        :getValue      (ui/get-obj-path value-path)
        :getLabel      (ui/get-obj-path label-path)
        :getBreadcrumb (ui/get-obj-path breadcrumb-path)
        :loadOptions   (partial utils4/load-options config)
        :onChange      #(rf/dispatch [::list-option-picker-change config (ui/get-option-data %)])}])))

(defn async-table-list-option-picker-settings [_]
  {::low-code/req-ks [:form-id :data-path :uri :value-path :label-path :columns]
   ::low-code/opt-ks [:placeholder]
   ::low-code/schema {:type "array" :items {:type "object"}}})

(defn async-table-list-option-picker
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [placeholder disabled is-hidden errors show-errors value-path label-path columns]} props
        hasError (when (and show-errors (seq errors)) true)]
    (when-not is-hidden
      [ui/AsyncTableSelectField
       {:value       nil
        :placeholder placeholder
        :disabled    disabled
        :hasError    hasError
        :getLabel    (ui/get-obj-path label-path)
        :getValue    (ui/get-obj-path value-path)
        :columns     (for [{:keys [flex label-path]} columns]
                       {:flex     flex
                        :getLabel (ui/get-obj-path label-path)})
        :loadOptions (partial utils4/load-options config)
        :onChange    #(rf/dispatch [::list-option-picker-change config (ui/get-option-data %)])}])))

(defmulti async-list-picker-settings :kind)
(defmethod async-list-picker-settings :default [config] (async-simple-list-option-picker-settings config))
(defmethod async-list-picker-settings :breadcrumb [config] (async-breadcrumb-list-option-picker-settings config))
(defmethod async-list-picker-settings :table [config] (async-table-list-option-picker-settings config))

(defmulti async-list-picker :kind)
(defmethod async-list-picker :default [config] (async-simple-list-option-picker config))
(defmethod async-list-picker :breadcrumb [config] (async-breadcrumb-list-option-picker config))
(defmethod async-list-picker :table [config] (async-table-list-option-picker config))


(defmulti async-item-picker-settings :kind)
(defmethod async-item-picker-settings :default [config] (async-simple-item-option-picker-settings config))
;(defmethod async-item-picker-settings :breadcrumb [config] (async-breadcrumb-item-option-picker-settings config))
;(defmethod async-item-picker-settings :table [config] (async-table-item-option-picker-settings config))

(defmulti async-item-picker :kind)
(defmethod async-item-picker :default [config] (async-simple-item-option-picker config))
;(defmethod async-item-picker :breadcrumb [config] (async-breadcrumb-item-option-picker config))
;(defmethod async-item-picker :table [config] (async-table-item-option-picker config))

(defn expanding-control-settings [_]
  {::low-code/req-ks [:label]
   ::low-code/opt-ks [:form-id :data-path :required]})

(defn expanding-control
  [config & children]
  (let [props @(rf/subscribe [::get-block-props config])
        {:keys [label required]} props]
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

(defn boxmap-field-settings [_]
  {::low-code/req-ks [:form-id :data-path]
   ::low-code/opt-ks []})

(defn boxmap-field
  [config]
  (let [props @(rf/subscribe [::get-block-props config])
        data @(rf/subscribe [::get-block-data config])
        elements (ui/boxes->elements data)
        {:keys [disabled is-hidden key]} props]
    (when-not is-hidden
      [ui/BoxMap
       {:elements (filter ui/valid-element? elements)
        :disabled (not disabled)
        :tickId   key
        :onChange #(rf/dispatch [::boxes-changed config (ui/get-geojson-data %)])}])))

(defn coordinates-modal-field-settings [_]
  {::low-code/req-ks [:form-id :data-path]
   ::low-code/opt-ks [:help]})

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
                                                               :on-cancel   #(rf/dispatch [::coordinates-modal-field-close-modal])}])))]
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
  [ui/EditDialog
   {:isOpen  true
    :title   "Create a new record"
    :onClose #(rf/dispatch [::create-document-modal-close-click])
    :onClear #(rf/dispatch [::create-document-modal-clear-click])
    :onSave  #(rf/dispatch [::create-document-modal-save-click])
    :canSave @(rf/subscribe [::create-document-modal-can-save?])}
   [low-code/render-template
    {:template-id ::create-document-modal-form
     :variables   '{?form-id [:create_form]}}]])
