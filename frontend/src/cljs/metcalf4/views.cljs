(ns metcalf4.views
  (:require [clojure.string :as string]))


; For pure views only, no re-frame subs/handlers

(defn user-display
  [user]
  (if (and (string/blank? (:lastName user)) (string/blank? (:firstName user)))
    (if (string/blank? (:email user))
      (:username user)
      (:email user))
    (str (:firstName user) " " (:lastName user))))

#_metcalf3.views/label-template
#_metcalf3.views/validation-state
#_metcalf3.views/dp-term-paths
#_metcalf3.views/masked-text-widget
#_metcalf3.views/InputWidget
#_metcalf3.views/filter-name
#_metcalf3.views/NameInputWidget
#_metcalf3.views/SimpleInputWidget
#_metcalf3.views/filter-table
#_metcalf3.views/InputField
#_metcalf3.views/input-field-with-label
#_metcalf3.views/date-field-with-label
#_metcalf3.views/date-field
#_metcalf3.views/OptionWidget
#_metcalf3.views/SelectWidget
#_metcalf3.views/SelectField
#_metcalf3.views/textarea-widget
#_metcalf3.views/textarea-field
#_metcalf3.views/textarea-field-with-label
#_metcalf3.views/Checkbox
#_metcalf3.views/CheckboxField
#_metcalf3.views/BackButton
#_metcalf3.views/getter
#_metcalf3.views/update-table-width
#_metcalf3.views/KeywordsThemeCell
#_metcalf3.views/TopicCategoryCell
#_metcalf3.views/KeywordsThemeTable
#_metcalf3.views/handle-highlight-new
#_metcalf3.views/modal-dialog-table-modal-edit-form
#_metcalf3.views/modal-dialog-table-modal-add-form
#_metcalf3.views/TableModalEdit
#_metcalf3.views/theme-option-renderer
#_metcalf3.views/modal-dialog-theme-keywords
#_metcalf3.views/TopicCategories
#_metcalf3.views/ThemeKeywords
#_metcalf3.views/ThemeInputField
#_metcalf3.views/ThemeKeywordsExtra
#_metcalf3.views/TaxonKeywordsExtra
#_metcalf3.views/->float
#_metcalf3.views/CoordInputWidget
#_metcalf3.views/CoordInputField
#_metcalf3.views/CoordField
#_metcalf3.views/GeographicCoverage
#_metcalf3.views/breadcrumb-renderer
#_metcalf3.views/nasa-list-renderer
#_metcalf3.views/other-term?
#_metcalf3.views/NasaListSelectField
#_metcalf3.views/Tooltip
#_metcalf3.views/ElasticsearchSelectField
#_metcalf3.views/PersonListWidget
#_metcalf3.views/ApiTreeWidget
#_metcalf3.views/ApiTermTreeField
#_metcalf3.views/TermOrOtherForm
#_metcalf3.views/UnitTermOrOtherForm
#_metcalf3.views/PersonListField
#_metcalf3.views/PersonForm
#_metcalf3.views/modal-dialog-parametername
#_metcalf3.views/modal-dialog-parameterunit
#_metcalf3.views/modal-dialog-parameterinstrument
#_metcalf3.views/modal-dialog-parameterplatform
#_metcalf3.views/modal-dialog-person
#_metcalf3.views/DataParameterRowEdit
#_metcalf3.views/DataParametersTable
#_metcalf3.views/upload!
#_metcalf3.views/handle-file
#_metcalf3.views/FileDrop
#_metcalf3.views/delete-attachment!
#_metcalf3.views/UploadData
#_metcalf3.views/save!
#_metcalf3.views/handle-submit-click
#_metcalf3.views/IMASLodge
#_metcalf3.views/Lodge
#_metcalf3.views/AddressField
#_metcalf3.views/OrganisationPickerWidget
#_metcalf3.views/PersonPickerWidget
#_metcalf3.views/SelectRoleWidget
#_metcalf3.views/PersonInputField
#_metcalf3.views/OrganisationInputField
#_metcalf3.views/ResponsiblePartyField
#_metcalf3.views/FieldError
#_metcalf3.views/ManyFieldError
#_metcalf3.views/PageErrors
#_metcalf3.views/help
#_metcalf3.views/help
#_metcalf3.views/navbar
#_metcalf3.views/PageView404
#_metcalf3.views/PageViewError
#_metcalf3.views/CreditField
#_metcalf3.views/delete-contact!
#_metcalf3.views/parties-list
#_metcalf3.views/default-selected-group
#_metcalf3.views/Who
#_metcalf3.views/MethodOrOtherForm
#_metcalf3.views/Methods
#_metcalf3.views/UseLimitationsFieldEdit
#_metcalf3.views/UseLimitations
#_metcalf3.views/SupplementalInformationRowEdit
#_metcalf3.views/IMASSupplementalInformation
#_metcalf3.views/SupplementalInformation
#_metcalf3.views/ResourceConstraints
#_metcalf3.views/SupportingResourceFieldEdit
#_metcalf3.views/IMASSupportingResource
#_metcalf3.views/SupportingResource
#_metcalf3.views/DataSourceRowEdit
#_metcalf3.views/DataSources
#_metcalf3.views/progress-bar
#_metcalf3.views/handle-archive-click
#_metcalf3.views/edit-tabs
#_metcalf3.views/PageViewEdit
#_metcalf3.views/FormErrors
#_metcalf3.views/NewDocumentForm
#_metcalf3.views/modal-dialog-dashboard-create-modal
#_metcalf3.views/NewDocumentButton
#_metcalf3.views/clone-doc
#_metcalf3.views/DocumentTeaser
#_metcalf3.views/PageViewDashboard
#_metcalf3.views/LegacyIECompatibility
#_metcalf3.views/modal-dialog-alert
#_metcalf3.views/modal-dialog-confirm
#_metcalf3.views/ModalStack
#_metcalf3.views/AppRoot