(ns interop.ui
  (:require ["/ui/components/BoxMap/BoxMap" :as BoxMap]
            ["/ui/components/CheckboxField/CheckboxField" :as CheckboxField]
            ["/ui/components/DateField/DateField" :as DateField]
            ["/ui/components/EditDialog/EditDialog" :as EditDialog]
            ["/ui/components/ErrorSidebar/ErrorSidebar" :as ErrorSidebar]
            ["/ui/components/ExpandingControl/ExpandingControl" :as ExpandingControl]
            ["/ui/components/FormGroup/FormGroup" :as FormGroup]
            ["/ui/components/InputField/InputField" :as InputField]
            ["/ui/components/ListItem/ListItem" :as ListItem]
            ["/ui/components/NumericInputField/NumericInputField" :as NumericInputField]
            ["/ui/components/SelectField/SelectField" :as SelectField]
            ["/ui/components/SelectionList/SelectionList" :as SelectionList]
            ["/ui/components/TextareaField/TextareaField" :as TextareaField]
            ["/ui/components/utils" :as ui-utils]
            ["/ui/components/YesNoRadioGroup/YesNoRadioGroup" :as YesNoRadioGroup]
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
(s/def ::obj-path (s/coll-of string? :min-count 1))

(defn get-obj-path
  ([path] #(get-obj-path % path))
  ([o path]
   (let [path (if (string? path) [path] path)]
     (apply gobj/getValueByKeys o path))))

(defn get-option-data [o] (js->clj o))

(defn setup-blueprint []
  (ui-utils/setupBlueprint))

(defn get-geojson-data [o] (js->clj o :keywordize-keys true))
(defn boxes->elements
  [boxes]
  (for [box boxes]
    {:northBoundLatitude (get-in box ["northBoundLatitude"])
     :southBoundLatitude (get-in box ["southBoundLatitude"])
     :eastBoundLongitude (get-in box ["eastBoundLongitude"])
     :westBoundLongitude (get-in box ["westBoundLongitude"])}))

(defn valid-element?
  [{:keys [northBoundLatitude southBoundLatitude eastBoundLongitude westBoundLongitude]}]
  (and northBoundLatitude southBoundLatitude eastBoundLongitude westBoundLongitude))

(def BoxMap (r/adapt-react-class BoxMap/BoxMap))
(def DateField (r/adapt-react-class DateField/DateField))
(def ErrorSidebar (r/adapt-react-class ErrorSidebar/ErrorSidebar))
(def ExpandingControl (r/adapt-react-class ExpandingControl/ExpandingControl))
(def FormGroup (r/adapt-react-class FormGroup/FormGroup))
(def InlineFormGroup (r/adapt-react-class FormGroup/InlineFormGroup))
(def InputField (r/adapt-react-class InputField/InputField))
(def SelectValueField (r/adapt-react-class SelectField/SelectValueField))
(def SimpleSelectField (r/adapt-react-class SelectField/SimpleSelectField))
(def BreadcrumbSelectField (r/adapt-react-class SelectField/BreadcrumbSelectField))
(def AsyncBreadcrumbSelectField (r/adapt-react-class SelectField/AsyncBreadcrumbSelectField))
(def TableSelectField (r/adapt-react-class SelectField/TableSelectField))
(def AsyncTableSelectField (r/adapt-react-class SelectField/AsyncTableSelectField))
(def AsyncSimpleSelectField (r/adapt-react-class SelectField/AsyncSimpleSelectField))
(def SimpleSelectionList (r/adapt-react-class SelectionList/SimpleSelectionList))
(def SelectionList (r/adapt-react-class SelectionList/SelectionList))
(def BreadcrumbSelectionList (r/adapt-react-class SelectionList/BreadcrumbSelectionList))
(def TableSelectionList (r/adapt-react-class SelectionList/TableSelectionList))
(def TextareaField (r/adapt-react-class TextareaField/TextareaField))
(def YesNoRadioGroup (r/adapt-react-class YesNoRadioGroup/YesNoRadioGroup))
(def CheckboxField (r/adapt-react-class CheckboxField/CheckboxField))
(def NumericInputField (r/adapt-react-class NumericInputField/NumericInputField))
(def EditDialog (r/adapt-react-class EditDialog/EditDialog))
