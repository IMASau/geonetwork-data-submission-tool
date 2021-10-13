(ns interop.ui
  (:require ["/ui/components/BoxMap/BoxMap" :as BoxMap]
            ["/ui/components/DateField/DateField" :as DateField]
            ["/ui/components/ErrorSidebar/ErrorSidebar" :as ErrorSidebar]
            ["/ui/components/ExpandingControl/ExpandingControl" :as ExpandingControl]
            ["/ui/components/FormGroup/FormGroup" :as FormGroup]
            ["/ui/components/InputField/InputField" :as InputField]
            ["/ui/components/SelectField/SelectField" :as SelectField]
            ["/ui/components/SelectionList/SelectionList" :as SelectionList]
            ["/ui/components/ListItem/ListItem" :as ListItem]
            ["/ui/components/TextareaField/TextareaField" :as TextareaField]
            ["/ui/components/YesNoRadioGroup/YesNoRadioGroup" :as YesNoRadioGroup]
            ["/ui/components/CheckboxField/CheckboxField" :as CheckboxField]
            ["/ui/components/NumericInputField/NumericInputField" :as NumericInputField]
            ["/ui/components/EditDialog/EditDialog" :as EditDialog]
            ["/ui/components/utils" :as ui-utils]
            [cljs.spec.alpha :as s]
            [goog.object :as gobj]
            [reagent.core :as r]))

(assert BoxMap/BoxMap)
(assert DateField/DateField)
(assert ErrorSidebar/ErrorSidebar)
(assert ExpandingControl/ExpandingControl)
(assert FormGroup/FormGroup)
(assert InputField/InputField)
(assert SelectField/SelectValueField)
(assert SelectField/SimpleSelectField)
(assert SelectField/AsyncSimpleSelectField)
(assert SelectionList/SelectionList)
(assert ListItem/SimpleListItem)
(assert ListItem/TableListItem)
(assert ListItem/BreadcrumbListItem)
(assert TextareaField/TextareaField)
(assert YesNoRadioGroup/YesNoRadioGroup)
(assert CheckboxField/CheckboxField)
(assert NumericInputField/NumericInputField)
(assert EditDialog/EditDialog)

(s/def ::northBoundLatitude number?)
(s/def ::westBoundLongitude number?)
(s/def ::southBoundLatitude number?)
(s/def ::eastBoundLongitude number?)
(s/def ::element (s/keys :req-un [::northBoundLatitude ::westBoundLongitude ::southBoundLatitude ::eastBoundLongitude]))
(s/def ::elements (s/coll-of ::element))

(defn has-key? [s] #(contains? (set (map name (keys %))) s))
(defn has-keys? [ss] (apply every-pred (map has-key? ss)))
(defn js->map [o ks]
  (assert (every? #(gobj/containsKey o %) ks)
          (str "Missing expected key" (pr-str {:obj o :ks ks})))
  (when o (zipmap ks (map #(gobj/get o %) ks))))

(defn setup-blueprint []
  (ui-utils/setupBlueprint))

(defn box-map
  [{:keys [elements map-width tick-id on-change]}]
  (s/assert fn? on-change)
  [:> BoxMap/BoxMap
   {:elements (filter #(s/valid? ::element %) elements)
    :mapWidth (s/assert pos? map-width)
    :tickId   (s/assert number? tick-id)
    :onChange (fn [geojson] (on-change (js->clj geojson :keywordize-keys true)))}])

(defn DateField
  [{:keys [value disabled onChange hasError minDate maxDate]}]
  (s/assert inst? minDate)
  (s/assert inst? maxDate)
  [:> DateField/DateField
   {:value    value
    :disabled disabled
    :onChange onChange
    :hasError hasError
    :minDate  minDate
    :maxDate  maxDate}])

(defn ErrorSidebar
  [{:keys []}]
  [:> ErrorSidebar/ErrorSidebar
   {}])

(defn ExpandingControl
  [{:keys [label required]} & children]
  (s/assert string? label)
  (s/assert (s/nilable boolean?) required)
  (into [:> ExpandingControl/ExpandingControl
         {:label    label
          :required required}]
        children))

(defn FormGroup
  [{:keys [label required disabled hasError helperText toolTip]} & children]
  (into [:> FormGroup/FormGroup
         {:label      label
          :required   required
          :disabled   disabled
          :hasError   hasError
          :helperText helperText
          :toolTip    (r/as-element toolTip)}]
        children))

(defn InlineFormGroup
  [{:keys [label required disabled hasError helperText toolTip]} & children]
  (into [:> FormGroup/InlineFormGroup
         {:label      label
          :required   required
          :disabled   disabled
          :hasError   hasError
          :helperText helperText
          :toolTip    (r/as-element toolTip)}]
        children))

(defn InputField
  [{:keys [value placeholder maxLength disabled hasError onChange]}]
  [:> InputField/InputField
   {:value       value
    :placeholder placeholder
    :maxLength   maxLength
    :disabled    disabled
    :hasError    hasError
    :onChange    onChange}])

(defn SelectValueField
  "Simple HTML select field to select a string value"
  [{:keys [value options label-path value-path placeholder disabled hasError onChange]}]
  (s/assert (s/nilable string?) value)
  (s/assert string? label-path)
  (s/assert string? value-path)
  (s/assert (s/coll-of (has-keys? [label-path value-path])) options)
  [:> SelectField/SelectValueField
   {:value       value
    :options     options
    :getValue    #(gobj/get % value-path "No value")
    :getLabel    #(gobj/get % label-path "No label")
    :placeholder placeholder
    :disabled    disabled
    :hasError    hasError
    :onChange    onChange}])

(defn SimpleSelectField
  [{:keys [value options placeholder disabled hasError onChange value-path label-path]}]
  (s/assert (s/nilable map?) value)
  (s/assert (s/coll-of map?) options)
  (s/assert string? value-path)
  (s/assert string? label-path)
  (s/assert (s/nilable string?) placeholder)
  (s/assert (s/nilable boolean?) disabled)
  (s/assert (s/nilable boolean?) hasError)
  (s/assert fn? onChange)
  [:> SelectField/SimpleSelectField
   {:value       value
    :options     options
    :placeholder placeholder
    :disabled    disabled
    :hasError    hasError
    :getValue    #(gobj/get % value-path "No value")
    :getLabel    #(gobj/get % label-path "No label")
    :onChange    (fn [o] (onChange (js->map o [value-path label-path])))}])

(defn BreadcrumbSelectField
  [{:keys [value options placeholder disabled hasError onChange value-path label-path breadcrumbKey]}]
  (s/assert (s/nilable map?) value)
  (s/assert (s/coll-of map?) options)
  (s/assert (s/nilable string?) placeholder)
  (s/assert string? value-path)
  (s/assert string? label-path)
  (s/assert string? breadcrumbKey)
  (s/assert (s/nilable boolean?) disabled)
  (s/assert (s/nilable boolean?) hasError)
  (s/assert fn? onChange)
  [:> SelectField/BreadcrumbSelectField
   {:value         value
    :options       options
    :placeholder   placeholder
    :disabled      disabled
    :hasError      hasError
    :getValue      #(gobj/get % value-path "No value")
    :getLabel      #(gobj/get % label-path "No label")
    :getBreadcrumb #(gobj/get % breadcrumbKey "No breadcrumb")
    :onChange      (fn [o] (onChange (js->map o [value-path label-path])))}])

(defn AsyncBreadcrumbSelectField
  [{:keys [value loadOptions placeholder disabled hasError onChange value-path label-path breadcrumbKey]}]
  (s/assert (s/nilable map?) value)
  (s/assert fn? loadOptions)
  (s/assert (s/nilable string?) placeholder)
  (s/assert string? value-path)
  (s/assert string? label-path)
  (s/assert string? breadcrumbKey)
  (s/assert (s/nilable boolean?) disabled)
  (s/assert (s/nilable boolean?) hasError)
  (s/assert fn? onChange)
  [:> SelectField/AsyncBreadcrumbSelectField
   {:value         value
    :loadOptions   loadOptions
    :placeholder   placeholder
    :disabled      disabled
    :hasError      hasError
    :getValue      #(gobj/get % value-path "No value")
    :getLabel      #(gobj/get % label-path "No label")
    :getBreadcrumb #(gobj/get % breadcrumbKey "No breadcrumb")
    :onChange      (fn [o] (onChange (js->map o [value-path label-path breadcrumbKey])))}])

(defn TableSelectField
  [{:keys [value options placeholder disabled hasError onChange label-path value-path columns]}]
  (s/assert (s/nilable map?) value)
  (s/assert (s/coll-of map?) options)
  (s/assert (s/nilable string?) placeholder)
  (s/assert string? value-path)
  (s/assert string? label-path)
  (s/assert (s/coll-of (s/keys :req-un [::labelKey ::flex])) columns)
  (s/assert (s/coll-of (has-key? value-path)) options)
  (s/assert (s/coll-of (has-keys? (map :label-path columns)) :distinct true) options)
  (s/assert (s/nilable boolean?) disabled)
  (s/assert (s/nilable boolean?) hasError)
  (s/assert fn? onChange)
  [:> SelectField/TableSelectField
   {:value       value
    :options     options
    :placeholder placeholder
    :disabled    disabled
    :hasError    hasError
    :getValue    #(gobj/get % value-path "No value")
    :getLabel    #(gobj/get % label-path "No label")
    :columns     (for [{:keys [flex label-path]} columns]
                   {:flex     flex
                    :getLabel #(gobj/get % label-path "No label")})
    :onChange    (fn [o] (onChange (js->map o [value-path label-path])))}])

(defn AsyncTableSelectField
  [{:keys [value loadOptions placeholder disabled hasError onChange label-path value-path columns]}]
  (s/assert (s/nilable map?) value)
  (s/assert fn? loadOptions)
  (s/assert (s/nilable string?) placeholder)
  (s/assert string? value-path)
  (s/assert string? label-path)
  (s/assert (s/coll-of (s/keys :req-un [::labelKey ::flex])) columns)
  (s/assert (s/nilable boolean?) disabled)
  (s/assert (s/nilable boolean?) hasError)
  (s/assert fn? onChange)
  [:> SelectField/AsyncTableSelectField
   {:value       value
    :loadOptions loadOptions
    :placeholder placeholder
    :disabled    disabled
    :hasError    hasError
    :getValue    #(gobj/get % value-path "No value")
    :getLabel    #(gobj/get % label-path "No label")
    :columns     (for [{:keys [flex label-path]} columns]
                   {:flex     flex
                    :getLabel #(gobj/get % label-path "No label")})
    :onChange    (fn [o] (onChange (js->map o [value-path label-path])))}])

(defn AsyncSimpleSelectField
  [{:keys [value loadOptions placeholder disabled hasError onChange value-path label-path]}]
  (s/assert (s/nilable map?) value)
  (s/assert string? value-path)
  (s/assert string? label-path)
  (s/assert fn? loadOptions)
  (s/assert (s/nilable string?) placeholder)
  (s/assert (s/nilable boolean?) disabled)
  (s/assert (s/nilable boolean?) hasError)
  (s/assert fn? onChange)
  [:> SelectField/AsyncSimpleSelectField
   {:value       value
    :loadOptions loadOptions
    :placeholder placeholder
    :disabled    disabled
    :hasError    hasError
    :getValue    #(gobj/get % value-path "No value")
    :getLabel    #(gobj/get % label-path "No label")
    :onChange    (fn [o] (onChange (js->map o [value-path label-path])))}])

(defn SimpleSelectionList
  [{:keys [items onReorder onItemClick onRemoveClick label-path value-path added-path]}]
  (s/assert fn? onReorder)
  (s/assert (s/nilable fn?) onItemClick)
  (s/assert fn? onRemoveClick)
  (s/assert string? label-path)
  (s/assert string? value-path)
  (s/assert (s/nilable string?) added-path)
  (s/assert (s/coll-of (has-keys? #{#_labelPath value-path}) :distinct true) items)
  [:> SelectionList/SimpleSelectionList
   {:items         items
    :onReorder     onReorder
    :onItemClick   onItemClick
    :onRemoveClick onRemoveClick
    :getValue      #(gobj/get % value-path "No value")
    :getLabel      #(gobj/get % label-path "No label")
    :getAdded      (if added-path #(gobj/get % added-path) (constantly false))}])

(defn BreadcrumbSelectionList
  [{:keys [items onReorder onItemClick onRemoveClick breadcrumbKey label-path value-path added-path]}]
  (s/assert fn? onReorder)
  (s/assert (s/nilable fn?) onItemClick)
  (s/assert fn? onRemoveClick)
  (s/assert string? breadcrumbKey)
  (s/assert string? label-path)
  (s/assert string? value-path)
  (s/assert (s/nilable string?) added-path)
  (s/assert (s/coll-of (has-keys? #{#_labelPath value-path breadcrumbKey}) :distinct true) items)
  [:> SelectionList/BreadcrumbSelectionList
   {:items         items
    :onReorder     onReorder
    :onItemClick   onItemClick
    :onRemoveClick onRemoveClick
    :getValue      #(gobj/get % value-path "No value")
    :getLabel      #(gobj/get % label-path "No label")
    :getBreadcrumb #(gobj/get % breadcrumbKey "No breadcrumb")
    :getAdded      (if added-path #(gobj/get % added-path) (constantly false))}])

(defn TableSelectionList
  [{:keys [items onReorder onItemClick onRemoveClick value-path added-path columns]}]
  (s/assert fn? onReorder)
  (s/assert (s/nilable fn?) onItemClick)
  (s/assert fn? onRemoveClick)
  (s/assert string? value-path)
  (s/assert (s/nilable string?) added-path)
  (s/assert (s/coll-of (s/keys :req-un [::labelKey ::flex] :opt-un [::columnHeader])) columns)
  (s/assert (s/coll-of (has-key? value-path)) items)
  #_(s/assert (s/coll-of (has-keys? (map :label-path columns)) :distinct true) items)
  [:> SelectionList/TableSelectionList
   {:items         items
    :onReorder     onReorder
    :onItemClick   onItemClick
    :onRemoveClick onRemoveClick
    :getValue      #(gobj/get % value-path "No value")
    :getAdded      (if added-path #(gobj/get % added-path) (constantly false))
    :columns       (for [{:keys [flex label-path columnHeader]} columns]
                     {:flex         flex
                      :getLabel     #(gobj/get % label-path "No label")
                      :columnHeader (or columnHeader "None")})}])

(defn TextareaField
  [{:keys [value placeholder maxLength rows disabled hasError onChange]}]
  (s/assert string? value)
  (s/assert (s/nilable string?) placeholder)
  (s/assert (s/nilable nat-int?) maxLength)
  (s/assert (s/nilable pos-int?) rows)
  (s/assert (s/nilable boolean?) disabled)
  (s/assert (s/nilable boolean?) hasError)
  (s/assert fn? onChange)
  [:> TextareaField/TextareaField
   {:value       value
    :placeholder placeholder
    :maxLength   maxLength
    :rows        rows
    :disabled    disabled
    :hasError    hasError
    :onChange    onChange}])

(defn YesNoRadioGroup
  [{:keys [value label disabled hasError onChange]}]
  (s/assert (s/nilable boolean?) value)
  (s/assert string? label)
  (s/assert (s/nilable boolean?) disabled)
  (s/assert (s/nilable boolean?) hasError)
  (s/assert fn? onChange)
  [:> YesNoRadioGroup/YesNoRadioGroup
   {:value    value
    :label    label
    :disabled disabled
    :hasError hasError
    :onChange onChange}])

(defn CheckboxField
  [{:keys [checked disabled hasError onChange]}]
  (s/assert (s/nilable boolean?) checked)
  (s/assert (s/nilable boolean?) disabled)
  (s/assert (s/nilable boolean?) hasError)
  (s/assert fn? onChange)
  [:> CheckboxField/CheckboxField
   {:checked  checked
    :disabled disabled
    :hasError hasError
    :onChange onChange}])

(defn NumericInputField
  [{:keys [value placeholder disabled hasError hasButtons onChange]}]
  (s/assert (s/nilable number?) value)
  (s/assert (s/nilable string?) placeholder)
  (s/assert (s/nilable boolean?) disabled)
  (s/assert (s/nilable boolean?) hasError)
  (s/assert (s/nilable boolean?) hasButtons)
  (s/assert fn? onChange)
  [:> NumericInputField/NumericInputField
   {:value       value
    :placeholder placeholder
    :disabled    disabled
    :hasError    hasError
    :hasButtons  hasButtons
    :onChange    onChange}])

(defn EditDialog
  [{:keys [isOpen title onClose onClear onSave canSave]} & children]
  (s/assert (s/nilable boolean?) isOpen)
  (s/assert string? title)
  (s/assert fn? onClose)
  (s/assert fn? onClear)
  (s/assert fn? onSave)
  (s/assert boolean? canSave)
  (into [:> EditDialog/EditDialog
         {:isOpen  (boolean isOpen)
          :title   title
          :onClose onClose
          :onClear onClear
          :onSave  onSave
          :canSave canSave}]
        children))