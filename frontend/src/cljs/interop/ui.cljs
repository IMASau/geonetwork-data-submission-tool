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

(s/def ::northBoundLatitude number?)
(s/def ::westBoundLongitude number?)
(s/def ::southBoundLatitude number?)
(s/def ::eastBoundLongitude number?)
(s/def ::element (s/keys :req-un [::northBoundLatitude ::westBoundLongitude ::southBoundLatitude ::eastBoundLongitude]))
(s/def ::elements (s/coll-of ::element))

(defn has-key? [s] #(contains? (set (map name (keys %))) s))
(defn has-keys? [ss] (apply every-pred (map has-key? ss)))
(defn js->map [o ks] (when o (zipmap ks (map #(gobj/get o %) ks))))

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
          :toolTip    toolTip}]
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
  [{:keys [value options labelKey valueKey placeholder disabled hasError onChange]}]
  (s/assert (s/nilable string?) value)
  (s/assert string? labelKey)
  (s/assert string? valueKey)
  (s/assert (s/coll-of (has-keys? [labelKey valueKey])) options)
  [:> SelectField/SelectValueField
   {:value       value
    :options     options
    :getValue    #(gobj/get % valueKey "No value")
    :getLabel    #(gobj/get % labelKey "No label")
    :placeholder placeholder
    :disabled    disabled
    :hasError    hasError
    :onChange    onChange}])

(defn SimpleSelectField
  [{:keys [value options placeholder disabled hasError onChange valueKey labelKey]}]
  (s/assert (s/nilable map?) value)
  (s/assert (s/coll-of map?) options)
  (s/assert (s/nilable string?) valueKey)
  (s/assert (s/nilable string?) labelKey)
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
    :getValue    #(gobj/get % valueKey "No value")
    :getLabel    #(gobj/get % labelKey "No label")
    :onChange    (fn [o] (onChange (js->map o [valueKey labelKey])))}])

(defn BreadcrumbSelectField
  [{:keys [value options placeholder disabled hasError onChange valueKey labelKey breadcrumbKey]}]
  (s/assert (s/nilable map?) value)
  (s/assert (s/coll-of map?) options)
  (s/assert (s/nilable string?) placeholder)
  (s/assert (s/nilable string?) valueKey)
  (s/assert (s/nilable string?) labelKey)
  (s/assert (s/nilable string?) breadcrumbKey)
  (s/assert (s/nilable boolean?) disabled)
  (s/assert (s/nilable boolean?) hasError)
  (s/assert fn? onChange)
  [:> SelectField/BreadcrumbSelectField
   {:value         value
    :options       options
    :placeholder   placeholder
    :disabled      disabled
    :hasError      hasError
    :getValue      #(gobj/get % valueKey "No value")
    :getLabel      #(gobj/get % labelKey "No label")
    :getBreadcrumb #(gobj/get % breadcrumbKey "No breadcrumb")
    :onChange      (fn [o] (onChange (js->map o [valueKey labelKey])))}])


(defn TableSelectField
  [{:keys [value options placeholder disabled hasError onChange labelKey valueKey columns]}]
  (s/assert (s/nilable map?) value)
  (s/assert (s/coll-of map?) options)
  (s/assert (s/nilable string?) placeholder)
  (s/assert (s/nilable string?) valueKey)
  (s/assert (s/nilable string?) labelKey)
  (s/assert (s/coll-of (s/keys :req-un [::labelKey ::flex])) columns)
  (s/assert (s/coll-of (has-key? valueKey)) options)
  (s/assert (s/coll-of (has-keys? (map :labelKey columns)) :distinct true) options)
  (s/assert (s/nilable boolean?) disabled)
  (s/assert (s/nilable boolean?) hasError)
  (s/assert fn? onChange)
  [:> SelectField/TableSelectField
   {:value         value
    :options       options
    :placeholder   placeholder
    :disabled      disabled
    :hasError      hasError
    :getValue      #(gobj/get % valueKey "No value")
    :getLabel      #(gobj/get % labelKey "No label")
    :columns       (for [{:keys [flex labelKey]} columns]
                     {:flex     flex
                      :getLabel #(gobj/get % labelKey "No label")})
    :onChange      (fn [o] (onChange (js->map o [valueKey labelKey])))}])

(defn AsyncSimpleSelectField
  [{:keys [value loadOptions placeholder disabled hasError onChange valueKey labelKey]}]
  (s/assert (s/nilable map?) value)
  (s/assert (s/nilable string?) valueKey)
  (s/assert (s/nilable string?) labelKey)
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
    :getValue    #(gobj/get % valueKey "No value")
    :getLabel    #(gobj/get % labelKey "No label")
    :onChange    (fn [o] (onChange (js->map o [valueKey labelKey])))}])

(defn SimpleSelectionList
  [{:keys [items onReorder onRemoveClick labelKey valueKey]}]
  (s/assert fn? onReorder)
  (s/assert fn? onRemoveClick)
  (s/assert string? labelKey)
  (s/assert string? valueKey)
  (s/assert (s/coll-of (has-keys? #{labelKey valueKey}) :distinct true) items)
  [:> SelectionList/SimpleSelectionList
   {:items         items
    :onReorder     onReorder
    :onRemoveClick onRemoveClick
    :getValue      #(gobj/get % valueKey "No value")
    :getLabel      #(gobj/get % labelKey "No label")}])

(defn BreadcrumbSelectionList
  [{:keys [items onReorder onRemoveClick breadcrumbKey labelKey valueKey]}]
  (s/assert fn? onReorder)
  (s/assert fn? onRemoveClick)
  (s/assert string? breadcrumbKey)
  (s/assert string? labelKey)
  (s/assert string? valueKey)
  (s/assert (s/coll-of (has-keys? #{labelKey valueKey breadcrumbKey}) :distinct true) items)
  [:> SelectionList/BreadcrumbSelectionList
   {:items         items
    :onReorder     onReorder
    :onRemoveClick onRemoveClick
    :getValue      #(gobj/get % valueKey "No value")
    :getLabel      #(gobj/get % labelKey "No label")
    :getBreadcrumb #(gobj/get % breadcrumbKey "No breadcrumb")}])

(defn TableSelectionList
  [{:keys [items onReorder onRemoveClick valueKey columns]}]
  (s/assert fn? onReorder)
  (s/assert fn? onRemoveClick)
  (s/assert string? valueKey)
  (s/assert (s/coll-of (s/keys :req-un [::labelKey ::flex])) columns)
  (s/assert (s/coll-of (has-key? valueKey)) items)
  (s/assert (s/coll-of (has-keys? (map :labelKey columns)) :distinct true) items)
  [:> SelectionList/TableSelectionList
   {:items         items
    :onReorder     onReorder
    :onRemoveClick onRemoveClick
    :getValue      #(gobj/get % valueKey "No value")
    :columns       (for [{:keys [flex labelKey]} columns]
                     {:flex     flex
                      :getLabel #(gobj/get % labelKey "No label")})}])

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