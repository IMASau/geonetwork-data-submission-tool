(ns interop.ui
  (:require ["/ui/components/BoxMap/BoxMap" :as BoxMap]
            ["/ui/components/DateField/DateField" :as DateField]
            ["/ui/components/ErrorSidebar/ErrorSidebar" :as ErrorSidebar]
            ["/ui/components/ExpandingControl/ExpandingControl" :as ExpandingControl]
            ["/ui/components/FormGroup/FormGroup" :as FormGroup]
            ["/ui/components/InputField/InputField" :as InputField]
            ["/ui/components/SelectValueField/SelectValueField" :as SelectValueField]
            ["/ui/components/SelectOptionField/SelectOptionField" :as SelectOptionField]
            ["/ui/components/AsyncSelectOptionField/AsyncSelectOptionField" :as AsyncSelectOptionField]
            ["/ui/components/SelectionList/SelectionList" :as SelectionList]
            ["/ui/components/TextareaField/TextareaField" :as TextareaField]
            ["/ui/components/YesNoRadioGroup/YesNoRadioGroup" :as YesNoRadioGroup]
            ["/ui/components/CheckboxField/CheckboxField" :as CheckboxField]
            [cljs.spec.alpha :as s]
            [goog.object :as gobj]
            [reagent.core :as r]))

(assert BoxMap/BoxMap)
(assert DateField/DateField)
(assert ErrorSidebar/ErrorSidebar)
(assert ExpandingControl/ExpandingControl)
(assert FormGroup/FormGroup)
(assert InputField/InputField)
(assert SelectValueField/SelectValueField)
(assert SelectOptionField/SelectOptionField)
(assert AsyncSelectOptionField/AsyncSelectOptionField)
(assert SelectionList/SelectionList)
(assert TextareaField/TextareaField)
(assert YesNoRadioGroup/YesNoRadioGroup)
(assert CheckboxField/CheckboxField)

(s/def ::northBoundLatitude number?)
(s/def ::westBoundLongitude number?)
(s/def ::southBoundLatitude number?)
(s/def ::eastBoundLongitude number?)
(s/def ::element (s/keys :req-un [::northBoundLatitude ::westBoundLongitude ::southBoundLatitude ::eastBoundLongitude]))
(s/def ::elements (s/coll-of ::element))

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
  [{:keys [value options placeholder disabled hasError onChange]}]
  (s/assert (s/nilable string?) value)
  (s/assert (s/coll-of (s/keys :req-un [::label ::value])) options)
  [:> SelectValueField/SelectValueField
   {:value       value
    :options     options
    :placeholder placeholder
    :disabled    disabled
    :hasError    hasError
    :onChange    onChange}])

(defn SelectOptionField
  [{:keys [value options placeholder disabled hasError onChange]}]
  (s/assert (s/nilable (s/keys :req-un [::label ::value])) value)
  (s/assert (s/coll-of (s/keys :req-un [::label ::value])) options)
  [:> SelectOptionField/SelectOptionField
   {:value       value
    :options     options
    :placeholder placeholder
    :disabled    disabled
    :hasError    hasError
    :onChange    #(onChange (js->clj % :keywordize-keys true))}])

(defn AsyncSelectOptionField
  [{:keys [value loadOptions placeholder disabled hasError onChange]}]
  (s/assert fn? loadOptions)
  [:> AsyncSelectOptionField/AsyncSelectOptionField
   {:value       value
    :loadOptions loadOptions
    :placeholder placeholder
    :disabled    disabled
    :hasError    hasError
    :onChange    #(onChange (js->clj % :keywordize-keys true))}])

(defn SimpleSelectionList
  [{:keys [items onReorder onRemoveClick labelKey valueKey]}]
  (s/assert fn? onReorder)
  (s/assert fn? onRemoveClick)
  (s/assert string? labelKey)
  (s/assert string? valueKey)
  [:> SelectionList/SelectionList
   {:items         items
    :onReorder     onReorder
    :onRemoveClick onRemoveClick
    :getValue      #(gobj/get % valueKey "No value")
    :itemProps     {:getLabel #(gobj/get % labelKey "No label")
                    :getValue #(gobj/get % valueKey "No value")}
    :renderItem    SelectionList/SimpleListItem}])

(defn BreadcrumbSelectionList
  [{:keys [items onReorder onRemoveClick breadcrumbKey labelKey valueKey]}]
  (s/assert fn? onReorder)
  (s/assert fn? onRemoveClick)
  (s/assert string? breadcrumbKey)
  (s/assert string? labelKey)
  (s/assert string? valueKey)
  [:> SelectionList/SelectionList
   {:items         items
    :onReorder     onReorder
    :onRemoveClick onRemoveClick
    :getValue      #(gobj/get % valueKey "No value")
    :itemProps     {:getBreadcrumb #(gobj/get % breadcrumbKey "No breadcrumb")
                    :getLabel      #(gobj/get % labelKey "No label")
                    :getValue      #(gobj/get % valueKey "No value")}
    :renderItem    SelectionList/BreadcrumbListItem}])

(defn has-key? [s] #(contains? (set (map name (keys %))) s))
(defn has-keys? [ss] (apply every-pred (map has-key? ss)))

(defn TableSelectionList
  [{:keys [items onReorder onRemoveClick valueKey columns]}]
  (s/assert (s/coll-of map?) items)
  (s/assert fn? onReorder)
  (s/assert fn? onRemoveClick)
  (s/assert string? valueKey)
  (s/assert (s/coll-of (s/keys :req-un [::labelKey ::flex])) columns)
  (s/assert (s/coll-of (has-key? valueKey)) items)
  (s/assert (s/coll-of (has-keys? (map :labelKey columns))) items)
  [:> SelectionList/SelectionList
   {:items         items
    :onReorder     onReorder
    :onRemoveClick onRemoveClick
    :getValue      #(gobj/get % valueKey "No value")
    :itemProps     {:columns (for [{:keys [flex labelKey]} columns]
                               {:flex     flex
                                :getLabel #(gobj/get % labelKey "No label")})}
    :renderItem    SelectionList/TableListItem}])

(defn TextareaField
  [{:keys [value placeholder maxLength rows disabled hasError onChange]}]
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
  [:> YesNoRadioGroup/YesNoRadioGroup
   {:value    value
    :label    label
    :disabled disabled
    :hasError hasError
    :onChange onChange}])

(defn CheckboxField
  [{:keys [checked disabled hasError onChange]}]
  [:> CheckboxField/CheckboxField
   {:checked  checked
    :disabled disabled
    :hasError hasError
    :onChange onChange}])
