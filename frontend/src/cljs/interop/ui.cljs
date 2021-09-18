(ns interop.ui
  (:require ["/ui/components/BoxMap/BoxMap" :as BoxMap]
            ["/ui/components/DateField/DateField" :as DateField]
            ["/ui/components/ErrorSidebar/ErrorSidebar" :as ErrorSidebar]
            ["/ui/components/ExpandingControl/ExpandingControl" :as ExpandingControl]
            ["/ui/components/FormGroup/FormGroup" :as FormGroup]
            ["/ui/components/InputField/InputField" :as InputField]
            ["/ui/components/SelectField/SelectField" :as SelectField]
            ["/ui/components/SelectionList/SelectionList" :as SelectionList]
            ["/ui/components/TextareaField/TextareaField" :as TextareaField]
            ["/ui/components/YesNoRadioGroup/YesNoRadioGroup" :as YesNoRadioGroup]

            [cljs.spec.alpha :as s]))

(assert BoxMap/BoxMap)
(assert DateField/DateField)
(assert ErrorSidebar/ErrorSidebar)
(assert ExpandingControl/ExpandingControl)
(assert FormGroup/FormGroup)
(assert InputField/InputField)
(assert SelectField/SelectField)
(assert SelectField/AsyncSelectField)
(assert SelectionList/SelectionList)
(assert TextareaField/TextareaField)
(assert YesNoRadioGroup/YesNoRadioGroup)

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
  [{:keys []}]
  [:> DateField/DateField
   {}])

(defn ErrorSidebar
  [{:keys []}]
  [:> ErrorSidebar/ErrorSidebar
   {}])

(defn ExpandingControl
  [{:keys []}]
  [:> ExpandingControl/ExpandingControl
   {}])

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
  [{:keys [value placeholder disabled hasError onChange]}]
  [:> InputField/InputField
   {:value       value
    :placeholder placeholder
    :disabled    disabled
    :hasError    hasError
    :onChange    onChange}])

(defn SelectField
  [{:keys []}]
  [:> SelectField/SelectField
   {}])

(defn AsyncSelectField
  [{:keys []}]
  [:> SelectField/AsyncSelectField
   {}])

(defn SelectionList
  [{:keys []}]
  [:> SelectionList/SelectionList
   {}])

(defn TextareaField
  [{:keys []}]
  [:> TextareaField/TextareaField
   {}])

(defn YesNoRadioGroup
  [{:keys []}]
  [:> YesNoRadioGroup/YesNoRadioGroup
   {}])