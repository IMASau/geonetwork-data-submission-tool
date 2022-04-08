(ns interop.ui-controls
  (:require ["/ui/controls/BoxMap/BoxMap" :as BoxMap]
            ["/ui/controls/CheckboxField/CheckboxField" :as CheckboxField]
            ["/ui/controls/DateField/DateField" :as DateField]
            ["/ui/controls/DateField/DateField2" :as DateField2]
            ["/ui/controls/EditDialog/EditDialog" :as EditDialog]
            ["/ui/controls/ErrorSidebar/ErrorSidebar" :as ErrorSidebar]
            ["/ui/controls/ExpandingControl/ExpandingControl" :as ExpandingControl]
            ["/ui/controls/FormGroup/FormGroup" :as FormGroup]
            ["/ui/controls/InputField/InputField" :as InputField]
            ["/ui/controls/ListItem/ListItem" :as ListItem]
            ["/ui/controls/NumericInputField/NumericInputField" :as NumericInputField]
            ["/ui/controls/SelectField/SelectField" :as SelectField]
            ["/ui/controls/SelectionList/SelectionList" :as SelectionList]
            ["/ui/controls/TextAddField/TextAddField" :as TextAddField]
            ["/ui/controls/RecordAddField/RecordAddField" :as RecordAddField]
            ["/ui/controls/UploadField/UploadField" :as UploadField]
            ["/ui/controls/TextareaField/TextareaField" :as TextareaField]
            ["/ui/controls/utils" :as ui-utils]
            ["/ui/controls/YesNoRadioGroup/YesNoRadioGroup" :as YesNoRadioGroup]
            ["/ui/controls/RadioGroup/RadioGroup" :as RadioGroup]
            ["/ui/controls/DropdownMenu/DropdownMenu" :as DropdownMenu]
            [cljs.spec.alpha :as s]
            [goog.object :as gobj]
            [reagent.core :as r]))

(assert BoxMap/BoxMap)
(assert DateField/DateField)
(assert DateField2/DateField2)
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
(assert TextAddField/TextAddField)
(assert RecordAddField/RecordAddField)
(assert UploadField/UploadField)
(assert RadioGroup/RadioGroup)
(assert RadioGroup/RadioGroupSimple)
(assert DropdownMenu/DropdownMenu)

(s/def ::northBoundLatitude number?)
(s/def ::westBoundLongitude number?)
(s/def ::southBoundLatitude number?)
(s/def ::eastBoundLongitude number?)
(s/def ::element (s/keys :req-un [::northBoundLatitude ::westBoundLongitude ::southBoundLatitude ::eastBoundLongitude]))
(s/def ::elements (s/coll-of ::element))
(s/def ::obj-path (s/coll-of string? :min-count 1))

(defn obj-path-value
  "Returns value at path in js object."
  ([o path]
   (s/assert vector? path)
   (let [path (if (string? path) [path] path)]
     (apply gobj/getValueByKeys o path))))

(defn obj-path-getter
  "Returns a getter for extracting value at path in js object."
  [path] (fn [o] (obj-path-value o path)))

(defn get-option-data
  "Convert js option data from ui control into clj data"
  [o] (js->clj o))

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
(def DateField2 (r/adapt-react-class DateField2/DateField2))
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
(def Modal (r/adapt-react-class EditDialog/Modal))
(def ModalDialog (r/adapt-react-class EditDialog/ModalDialog))
(def TextAddField (r/adapt-react-class TextAddField/TextAddField))
(def RecordAddField (r/adapt-react-class RecordAddField/RecordAddField))
(def Dropzone (r/adapt-react-class UploadField/Dropzone))
(def RadioGroup (r/adapt-react-class RadioGroup/RadioGroup))
(def RadioGroupSimple (r/adapt-react-class RadioGroup/RadioGroupSimple))
(def DropdownMenu (r/adapt-react-class DropdownMenu/DropdownMenu))