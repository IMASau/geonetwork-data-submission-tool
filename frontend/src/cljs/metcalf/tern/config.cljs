(ns ^:dev/always metcalf.tern.config
  (:require [cljs.spec.alpha :as s]
            [clojure.string :as string]
            [interop.ui-controls :as ui-controls]
            [metcalf.common.components4 :as components4]
            [metcalf.common.fx3 :as fx3]
            [metcalf.common.handlers3 :as handlers3]
            [metcalf.common.handlers4 :as handlers4]
            [metcalf.common.ins4 :as ins4]
            [metcalf.common.low-code4 :as low-code4]
            [metcalf.common.rules4 :as rules4]
            [metcalf.common.subs3 :as subs3]
            [metcalf.common.subs4 :as subs4]
            [metcalf.common.utils4 :as utils4]
            [metcalf.tern.db :as tern-db]
            [metcalf.tern.handlers :as tern-handlers]
            [metcalf.tern.subs :as tern-subs]
            [re-frame.core :as rf]))

#_(rf/reg-event-fx :app/upload-data-confirm-upload-click-add-attachment handlers3/add-attachment)
(rf/reg-event-fx ::components4/add-record handlers4/add-record-handler)
(rf/reg-event-fx ::components4/boxes-changed handlers4/boxes-changed)
(rf/reg-event-fx ::components4/create-document-modal-clear-click handlers4/create-document-modal-clear-click)
(rf/reg-event-fx ::components4/create-document-modal-close-click handlers4/create-document-modal-close-click)
(rf/reg-event-fx ::components4/create-document-modal-save-click handlers4/create-document-modal-save-click)
(rf/reg-event-fx ::components4/contributors-modal-close-click handlers4/contributors-modal-close-click)
(rf/reg-event-fx ::components4/contributors-modal-clear-click handlers4/contributors-modal-clear-click)
(rf/reg-event-fx ::components4/contributors-modal-save-click handlers4/contributors-modal-save-click)
(rf/reg-event-fx ::components4/edit-dialog-cancel handlers4/edit-dialog-cancel-handler)
(rf/reg-event-fx ::components4/edit-dialog-close handlers4/edit-dialog-close-handler)
(rf/reg-event-fx ::components4/edit-dialog-save handlers4/edit-dialog-save-handler)
(rf/reg-event-fx ::components4/item-add-button-click handlers4/item-add-with-defaults-click-handler2)
(rf/reg-event-fx ::components4/item-dialog-button-add-click handlers4/item-add-with-defaults-click-handler2)
(rf/reg-event-fx ::components4/item-dialog-button-edit-click handlers4/item-edit-click-handler)
(rf/reg-event-fx ::components4/item-edit-with-defaults-click-handler handlers4/item-edit-click-handler)
(rf/reg-event-fx ::components4/item-option-picker-change handlers4/item-option-picker-change)
(rf/reg-event-fx ::components4/list-add-with-defaults-click-handler3 handlers4/list-add-with-defaults-click-handler3)
(rf/reg-event-fx ::components4/list-edit-dialog-cancel handlers4/list-edit-dialog-cancel-handler)
(rf/reg-event-fx ::components4/list-edit-dialog-close handlers4/list-edit-dialog-cancel-handler)
(rf/reg-event-fx ::components4/list-edit-dialog-save handlers4/list-edit-dialog-save-handler)
(rf/reg-event-fx ::components4/list-option-picker-change handlers4/list-option-picker-change)
(rf/reg-event-fx ::components4/option-change handlers4/option-change-handler)
(rf/reg-event-fx ::components4/selection-list-item-click handlers4/selection-list-item-click3)
(rf/reg-event-fx ::components4/selection-list-remove-click handlers4/selection-list-remove-click)
(rf/reg-event-fx ::components4/selection-list-reorder handlers4/selection-list-reorder)
(rf/reg-event-fx ::components4/selection-list-values-item-click handlers4/selection-list-values-item-click)
(rf/reg-event-fx ::components4/selection-list-values-remove-click handlers4/selection-list-remove-click)
(rf/reg-event-fx ::components4/selection-list-values-reorder handlers4/selection-list-reorder)
(rf/reg-event-fx ::components4/text-value-add-click-handler handlers4/text-value-add-click-handler)
(rf/reg-event-fx ::components4/upload-file-drop handlers4/upload-file-drop)
(rf/reg-event-fx ::components4/upload-files-drop handlers4/upload-files-drop)
(rf/reg-event-fx ::components4/document-attachment-upload-success handlers4/document-attachment-upload-success)
(rf/reg-event-fx ::components4/value-changed handlers4/value-changed-handler)
(rf/reg-event-fx ::components4/input-blur handlers4/input-blur-handler)
(rf/reg-event-fx ::components4/value-list-add-with-defaults-click-handler handlers4/value-list-add-with-defaults-click-handler2)
(rf/reg-event-fx ::handlers4/-save-current-document-error handlers4/-save-current-document-error)
(rf/reg-event-fx ::handlers4/-save-current-document-success handlers4/-save-current-document-success)
(rf/reg-event-fx :app/-archive-current-document-error handlers3/-archive-current-document-error)
(rf/reg-event-fx :app/-archive-current-document-success handlers3/-archive-current-document-success)
(rf/reg-event-fx :app/-clone-document-error handlers3/-clone-document-error)
(rf/reg-event-fx :app/-clone-document-success handlers3/-clone-document-success)
(rf/reg-event-fx :app/lodge-button-click handlers3/lodge-click)
(rf/reg-event-fx :app/-lodge-click-error handlers3/lodge-error)
(rf/reg-event-fx :app/-lodge-click-success handlers3/lodge-save-success)
(rf/reg-event-fx :app/-lodge-save-error handlers3/lodge-error)
(rf/reg-event-fx :app/-lodge-save-success handlers3/-lodge-save-success)
(rf/reg-event-fx :app/-transite-doc-click-confirm handlers3/-transite-doc-click-confirm)
(rf/reg-event-fx :app/-transite-doc-confirm-error handlers3/-transite-doc-confirm-error)
(rf/reg-event-fx :app/-transite-doc-confirm-success handlers3/-transite-doc-confirm-success)
(rf/reg-event-fx :app/clone-doc-confirm handlers3/clone-document)
(rf/reg-event-fx :app/contributors-modal-share-click handlers4/contributors-modal-share-click)
(rf/reg-event-fx :app/contributors-modal-unshare-click handlers4/contributors-modal-unshare-click)
(rf/reg-event-fx :app/dashboard-create-click handlers3/dashboard-create-click)
(rf/reg-event-fx :app/dashboard-show-all-click handlers3/show-all-documents)
(rf/reg-event-fx :app/dashboard-toggle-status-filter handlers3/toggle-status-filter)
(rf/reg-event-fx :app/delete-attachment-click handlers3/delete-attachment-click)
(rf/reg-event-fx :app/delete-attachment-confirm handlers3/del-value)
(rf/reg-event-fx :app/document-teaser-archive-click (handlers3/transite-doc-click "archive"))
(rf/reg-event-fx :app/document-teaser-clone-click handlers3/document-teaser-clone-click)
(rf/reg-event-fx :app/document-teaser-delete-archived-click (handlers3/transite-doc-click "delete_archived"))
(rf/reg-event-fx :app/document-teaser-restore-click (handlers3/transite-doc-click "restore"))
(rf/reg-event-fx :app/document-teaser-share-click handlers4/document-teaser-share-click)
(rf/reg-event-fx :app/edit-tabs-pick-click handlers3/set-tab)
(rf/reg-event-fx :app/handle-page-view-edit-archive-click handlers3/handle-page-view-edit-archive-click)
(rf/reg-event-fx :app/modal-dialog-alert-dismiss handlers4/modal-dialog-alert-dismiss)
(rf/reg-event-fx :app/modal-dialog-alert-save handlers4/modal-dialog-alert-save)
(rf/reg-event-fx :app/modal-dialog-confirm-cancel handlers3/close-and-cancel)
(rf/reg-event-fx :app/modal-dialog-confirm-dismiss handlers3/close-and-cancel)
(rf/reg-event-fx :app/modal-dialog-confirm-save handlers3/close-and-confirm)
(rf/reg-event-fx :app/page-view-edit-archive-click-confirm handlers3/archive-current-document)
(rf/reg-event-fx :app/PageViewEdit-save-button-click handlers4/save-current-document)
(rf/reg-event-fx :app/upload-data-file-upload-failed handlers3/upload-data-file-upload-failed)
(rf/reg-event-fx :app/upload-max-filesize-exceeded handlers3/upload-max-filesize-exceeded)
(rf/reg-event-fx :metcalf.common.actions4/-create-document handlers4/-create-document-handler)
(rf/reg-event-fx :metcalf.common.actions4/-get-document-data-action handlers4/-get-document-data-action)
(rf/reg-event-fx :metcalf.common.actions4/-upload-attachment handlers4/-upload-attachment)
(rf/reg-event-fx :metcalf.common.actions4/-get-document-attachment-data handlers4/-get-document-attachment-data)
(rf/reg-event-fx :metcalf.common.actions4/-upload-single-attachment handlers4/-upload-single-attachment)
(rf/reg-event-fx :metcalf.common.components4/thumbnail-remove-selection handlers4/thumbnail-remove-selection)
(rf/reg-event-fx :metcalf.common.components4/coordinates-modal-field-close-modal handlers4/coordinates-modal-field-close-modal)
(rf/reg-event-fx :metcalf.common.handlers4/-contributors-modal-share-resolve handlers4/-contributors-modal-share-resolve)
(rf/reg-event-fx :metcalf.common.handlers4/-contributors-modal-unshare-resolve handlers4/-contributors-modal-unshare-resolve)
(rf/reg-event-fx :metcalf.tern.core/init-db tern-handlers/init-db)
(rf/reg-fx ::fx3/post fx3/post)
(rf/reg-fx ::fx3/post-json-data fx3/post-json-data)
(rf/reg-fx ::fx3/set-location-href fx3/set-location-href)
(rf/reg-fx ::low-code4/init! low-code4/init!)
(rf/reg-fx :app/get-json-fx (utils4/promise-fx utils4/get-json))
(rf/reg-fx :app/post-data-fx (utils4/promise-fx utils4/post-json))
(rf/reg-fx :app/post-multipart-form (utils4/promise-fx utils4/post-multipart-form))
(rf/reg-fx :ui/setup-blueprint ui-controls/setup-blueprint)
(rf/reg-sub ::components4/can-dialog-cancel? subs4/can-dialog-cancel-sub)
(rf/reg-sub ::components4/create-document-modal-can-save? subs4/create-document-modal-can-save?)
(rf/reg-sub ::components4/get-block-data subs4/form-state-signal subs4/get-block-data-sub)
(rf/reg-sub ::components4/get-block-props subs4/form-state-signal subs4/get-block-props-sub)
(rf/reg-sub ::components4/get-list-edit-can-save-sub subs4/form-state-signal subs4/get-list-edit-can-save-sub)
(rf/reg-sub ::components4/get-page-errors-props subs4/form-state-signal subs4/get-page-errors-props-sub)
(rf/reg-sub ::components4/get-yes-no-field-props subs4/form-state-signal subs4/get-block-props-sub)
(rf/reg-sub ::components4/has-block-errors? subs4/form-state-signal subs4/has-block-errors?)
(rf/reg-sub ::components4/has-selected-block-errors? subs4/form-state-signal subs4/has-selected-block-errors?)
(rf/reg-sub ::components4/is-item-added? subs4/form-state-signal subs4/is-item-added?)
(rf/reg-sub ::low-code4/get-data-schema subs4/get-data-schema-sub)
(rf/reg-sub ::subs4/get-form-state subs4/get-form-state)
(rf/reg-sub ::tern-subs/get-edit-tabs tern-subs/get-edit-tabs)
(rf/reg-sub :app/contributors-modal-props subs4/contributors-modal-props)
(rf/reg-sub :app/get-dashboard-props subs3/get-dashboard-props)
(rf/reg-sub :app/get-progress-bar-props :<- [::subs4/get-form-state [:form]] subs3/get-progress-props)
(rf/reg-sub :subs/get-app-root-modal-props subs4/get-modal-props)
(rf/reg-sub :subs/get-app-root-page-name subs4/get-page-name)
(rf/reg-sub :subs/get-attachments subs3/get-attachments)
(rf/reg-sub :subs/get-context subs3/get-context)
(rf/reg-sub :subs/get-edit-tab-props :<- [:subs/get-page-props] :<- [::subs4/get-form-state [:form]] :<- [::tern-subs/get-edit-tabs] tern-subs/get-edit-tab-props2)
(rf/reg-sub :subs/get-form subs3/get-form)
(rf/reg-sub :subs/get-form-dirty subs4/get-form-dirty?)
(rf/reg-sub :subs/get-form-disabled? subs3/get-form-disabled?)
(rf/reg-sub :subs/get-page-props subs3/get-page-props)
(rf/reg-sub :subs/get-progress subs3/get-progress)
(rf/reg-sub :subs/get-upload-form subs3/get-upload-form)
(ins4/reg-global-singleton ins4/form-ticker)
(ins4/reg-global-singleton ins4/breadcrumbs)
(ins4/reg-global-singleton (ins4/slow-handler 100))
(when goog/DEBUG (ins4/reg-global-singleton ins4/db-diff))
(when goog/DEBUG (ins4/reg-global-singleton (ins4/check-and-throw ::tern-db/db)))
(set! rules4/rule-registry
      {"requiredField"                  rules4/required-field
       "requiredWhenYes"                rules4/required-when-yes
       "spatialUnits"                   rules4/spatial-resolution-units
       "requiredAllNone"                rules4/required-all-or-nothing
       "requiredIfValue"                rules4/required-if-value
       "maxLength"                      rules4/max-length
       "mergeNameParts"                 rules4/merge-names
       "validURL"                       rules4/valid-url
       "validOrcid"                     rules4/valid-ordid-uri
       "geographyRequired"              rules4/geography-required
       "numericOrder"                   rules4/numeric-order
       "positive"                       rules4/force-positive
       "dateOrder"                      rules4/date-order
       "dateBeforeToday"                rules4/date-before-today
       "endPosition"                    rules4/end-position
       "maintFreq"                      rules4/maint-freq
       "verticalRequired"               rules4/vertical-required
       "protocolLayer"                  rules4/data-source-required-layer
       "defaultDistributor"             rules4/default-distributor
       "defaultClassification"          rules4/default-classification
       "defaultRole"                    rules4/default-role
       "maxKeywords"                    rules4/tern-max-keywords
       "authorRequired"                 rules4/author-required
       "contactOrganisationUserDefined" rules4/tern-contact-organisation-user-defined
       "contactNotForOrgs"              rules4/tern-contact-unless-org
       "generateCitation"               rules4/generate-citation
       "uploadTitleFromName"            rules4/uploads-title-from-name
       "duplicateParameters"            rules4/tern-duplicate-parameters
       "parameterUnitUserDefined"       rules4/tern-parameter-unit-user-defined
       "validContributors"              rules4/valid-contributors
       "orgDerivedName"                 rules4/tern-derive-org-name
       "requiredFieldInChildren"        rules4/required-field-in-children})
(set! low-code4/component-registry
      {'m4/async-simple-item-option-picker     {:view #'components4/async-simple-item-option-picker :init components4/async-simple-item-option-picker-settings}
       'm4/async-list-option-picker            {:view #'components4/async-list-option-picker :init components4/async-list-option-picker-settings}
       'm4/async-list-option-picker-breadcrumb {:view #'components4/async-list-option-picker-breadcrumb :init components4/async-list-option-picker-breadcrumb-settings}
       'm4/async-list-option-picker-columns    {:view #'components4/async-list-option-picker-columns :init components4/async-list-option-picker-columns-settings}
       'm4/async-select-option-simple          {:view #'components4/async-select-option-simple :init components4/async-select-option-simple-settings}
       'm4/async-select-option-breadcrumb      {:view #'components4/async-select-option-breadcrumb :init components4/async-select-option-breadcrumb-settings}
       'm4/async-select-option-columns         {:view #'components4/async-select-option-columns :init components4/async-select-option-columns-settings}
       ;'m4/async-select-value                  {:view #'components4/async-select-value :init components4/async-select-value-settings}
       'm4/boxmap-field                        {:view #'components4/boxmap-field :init components4/boxmap-field-settings}
       ;'m4/breadcrumb-list-option-picker       {:view #'components4/breadcrumb-list-option-picker :init components4/breadcrumb-list-option-picker-settings}
       'm4/selection-list-breadcrumb           {:view #'components4/selection-list-breadcrumb :init components4/selection-list-breadcrumb-settings}
       'm4/checkbox-field                      {:view #'components4/checkbox-field :init components4/checkbox-field-settings}
       'm4/date-field2                         {:view #'components4/date-field2 :init components4/date-field2-settings}
       'm4/expanding-control                   {:view #'components4/expanding-control :init components4/expanding-control-settings}
       'm4/form-group                          {:view #'components4/form-group :init components4/form-group-settings}
       'm4/inline-form-group                   {:view #'components4/inline-form-group :init components4/inline-form-group-settings}
       'm4/input-field                         {:view #'components4/input-field :init components4/input-field-settings}
       'm4/item-add-button                     {:view #'components4/item-add-button :init components4/item-add-button-settings}
       'm4/item-dialog-button                  {:view #'components4/item-dialog-button :init components4/item-dialog-button-settings}
       'm4/edit-dialog                         {:view #'components4/edit-dialog :init components4/edit-dialog-settings}
       'm4/list-add-button                     {:view #'components4/list-add-button3 :init components4/list-add-button3-settings}
       'm4/value-list-add-button               {:view #'components4/value-list-add-button :init components4/value-list-add-button-settings}
       'm4/list-edit-dialog                    {:view #'components4/list-edit-dialog :init components4/list-edit-dialog-settings}
       'm4/typed-list-edit-dialog              {:view #'components4/typed-list-edit-dialog :init components4/typed-list-edit-dialog-settings}
       'm4/numeric-input-field                 {:view #'components4/numeric-input-field :init components4/numeric-input-field-settings}
       'm4/page-errors                         {:view #'components4/page-errors :init components4/page-errors-settings}
       'm4/submit-status                       {:view #'components4/tern-document-status-display}
       'm4/select-option-simple                {:view #'components4/select-option-simple :init components4/select-option-simple-settings}
       'm4/select-option-breadcrumb            {:view #'components4/select-option-breadcrumb :init components4/select-option-breadcrumb-settings}
       'm4/select-option-columns               {:view #'components4/select-option-columns :init components4/select-option-columns-settings}
       'm4/select-value                        {:view #'components4/select-value :init components4/select-value-settings}
       'm4/simple-list-option-picker           {:view #'components4/simple-list-option-picker :init components4/simple-list-option-picker-settings}
       'm4/selection-list-template             {:view #'components4/selection-list-template :init components4/selection-list-template-settings}
       'm4/selection-list-simple               {:view #'components4/selection-list-simple :init components4/selection-list-simple-settings}
       'm4/selection-list-values               {:view #'components4/selection-list-values :init components4/selection-list-values-settings}
       ;'m4/table-list-option-picker            {:view #'components4/table-list-option-picker :init components4/table-list-option-picker-settings}
       'm4/selection-list-columns              {:view #'components4/selection-list-columns3 :init components4/selection-list-columns3-settings}
       'm4/textarea-field                      {:view #'components4/textarea-field :init components4/textarea-field-settings}
       'm4/when-data                           {:view #'components4/when-data :init components4/when-data-settings}
       'm4/get-data                            {:view #'components4/get-data :init components4/get-data-settings}
       'm4/dangerous-data                      {:view #'components4/dangerous-data :init components4/dangerous-data-settings}
       'm4/yes-no-field                        {:view #'components4/yes-no-field :init components4/yes-no-field-settings}
       'm4/simple-list                         {:view #'components4/simple-list :init components4/simple-list-settings}
       ;'m4/record-add-button                   {:view #'components4/record-add-button :init components4/record-add-button-settings}
       'm4/text-add-button                     {:view #'components4/text-add-button :init components4/text-add-button-settings}
       'm4/upload-files                        {:view #'components4/tern-upload-files :init components4/tern-upload-files-settings}
       'm4/upload-thumbnail                    {:view #'components4/upload-thumbnail :init components4/upload-thumbnail-settings}
       'm4/terms-conditions                    {:view #'components4/terms-and-conditions}
       'm4/lodge-button                        {:view #'components4/lodge-button :init components4/lodge-button-settings}
       'm4/xml-export-link                     {:view #'components4/xml-export-link :init components4/xml-export-link-settings}
       'm4/when-errors                         {:view #'components4/when-errors :init components4/when-errors-settings}})

; Specs intended for use with when-data :pred
(s/def :m4/empty-list? empty?)
(s/def :m4/not-set? (s/or :n nil? :s (s/and string? string/blank?)))

(def edit-templates
  "Default ui templates for edit-page/"
  '{:platform/user-defined-entry-form
    [:div

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "label"]
       :label     "Name/Label"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "label"]}]]

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "description"]
       :label     "Description / Definition"}
      [m4/textarea-field
       {:form-id   ?form-id
        :data-path [?data-path "description"]}]]

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "source"]
       :label     "Source"
       :toolTip   "Specify the source of the platform, if available, in citation format. Eg Creators (Publication year), Title, Version, Publisher, Resource type, Identifer."}
      [m4/textarea-field
       {:form-id     ?form-id
        :data-path   [?data-path "source"]
        :placeholder "E.g. Creator (Publication year).  Title.  Version.  Publisher.  Resource type.  Identifier.  "}]]]

    :instrument/user-defined-entry-form
    [:div

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "label"]
       :label     "Name/Label"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "label"]}]]

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "description"]
       :label     "Description /Definition"}
      [m4/textarea-field
       {:form-id   ?form-id
        :data-path [?data-path "description"]}]]

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "source"]
       :label     "Source"
       :toolTip   "Specify the source of the instrument information in citation format, if available."}
      [m4/textarea-field
       {:form-id     ?form-id
        :data-path   [?data-path "source"]
        :placeholder "E.g. Creator (Publication year).  Title.  Version.  Publisher.  Resource type.  Identifier.  "}]]

     [m4/inline-form-group
      {:form-id    ?form-id
       :data-path  [?data-path "serial"]
       :label      "Serial Number"
       :helperText "Optional"
       :toolTip    "This is optional. You can add a serial number of the instrument if it is available."}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "serial"]}]]]

    :unit/user-defined-entry-form
    [:div

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "label"]
       :label     "Name/Label"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "label"]}]]

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "description"]
       :label     "Description /Definition"}
      [m4/textarea-field
       {:form-id   ?form-id
        :data-path [?data-path "description"]}]]]

    :parameter/user-defined-entry-form
    [:div

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "label"]
       :label     "Name/Label"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "label"]}]]

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "description"]
       :label     "Description /Definition"}
      [m4/textarea-field
       {:form-id   ?form-id
        :data-path [?data-path "description"]}]]

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "source"]
       :label     "Source"}
      [m4/textarea-field
       {:form-id     ?form-id
        :data-path   [?data-path "source"]
        :placeholder "E.g. Creator (Publication year).  Title.  Version.  Publisher.  Resource type.  Identifier.  "}]]]

    :parameter-unit/user-defined-entry-form
    [:div
     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "parameter"]
       :label     "Parameter"
       :toolTip   "Select Parameters (observable properties) in the dataset from the predefined list. If the required parameter is not available, you can click the ‘Add’ button to define a new parameter. All new entries will be reviewed prior to publication."}

      [:div.bp3-control-group
       [:div.bp3-fill
        [m4/async-select-option-simple
         {:form-id    ?form-id
          :data-path  [?data-path "parameter"]
          :uri        "/api/ternparameters"
          :label-path ["label"]
          :value-path ["uri"]}]]

       [m4/item-dialog-button
        {:form-id            ?form-id
         :data-path          [?data-path "parameter"]
         :label-path         ["label"]
         :value-path         ["uri"]
         :random-uuid-value? true
         :item-defaults      {"userAddedCategory" "parameter"}
         :added-path         ["isUserDefined"]}]]

      [m4/edit-dialog
       {:form-id     ?form-id
        :data-path   [?data-path "parameter"]
        :title       "Parameter"
        :template-id :parameter/user-defined-entry-form}]]

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "unit"]
       :label     "Unit of measure"
       :toolTip   "Select a Unit of Measure (UoM) related to the selected parameter from the list. If the required UoM is not found within the list, you can click the ‘Add’ button to define a new unit of measure."}

      [:div.bp3-control-group
       [:div.bp3-fill
        [m4/async-select-option-simple
         {:form-id    ?form-id
          :data-path  [?data-path "unit"]
          :uri        "/api/qudtunits"
          :label-path ["label"]
          :value-path ["uri"]}]]

       [m4/item-dialog-button
        {:form-id            ?form-id
         :data-path          [?data-path "unit"]
         :label-path         ["label"]
         :value-path         ["uri"]
         :random-uuid-value? true
         :item-defaults      {"userAddedCategory" "unit"}
         :added-path         ["isUserDefined"]}]]

      [m4/edit-dialog
       {:form-id     ?form-id
        :data-path   [?data-path "unit"]
        :title       "Unit"
        :template-id :unit/user-defined-entry-form}]]]

    :data-identification
    [:div

     [:h2 "1. Data Identification"]

     [m4/form-group
      {:form-id    [:form]
       :data-path  ["identificationInfo" "title"]
       :label      "Title"
       :helperText "Clear and concise description of the content of the resource including What, Where, (How), When e.g. Fractional Cover for Australia 2014 ongoing"
       :toolTip    "Enter the title of the dataset. The title should be short and informative."}
      [m4/input-field
       {:form-id     [:form]
        :data-path   ["identificationInfo" "title"]
        :placeholder "Provide a descriptive title for the data set including the subject of study, the study location and time period. Example: TERN OzFlux Arcturus Emerald Tower Site 2014-ongoing"}]]

     [m4/form-group
      {:form-id   [:form]
       :data-path ["parentMetadata"]
       :label     "Parent Metadata"
       :toolTip   "Check 'Yes' if there is a parent metadata record associated with the dataset."}
      [m4/yes-no-field
       {:form-id   [:form]
        :data-path ["parentMetadata" "parentMetadataFlag"]
        :label     "Does this record have a parent dataset?"}]
      [m4/async-select-option-breadcrumb
       {:form-id         [:form]
        :data-path       ["parentMetadata" "record"]
        :uri             "/api/terngeonetwork"
        :label-path      ["label"]
        :value-path      ["uri"]
        :breadcrumb-path ["uuid"]
        :placeholder     "Start typing to filter list..."}]]

     [m4/form-group
      {:form-id   [:form]
       :data-path ["identificationInfo" "topicCategories"]
       :label     "Topic Categories"
       :toolTip   "Select high-level thematic classification of the dataset."}
      [m4/simple-list-option-picker
       {:form-id     [:form]
        :data-path   ["identificationInfo" "topicCategories"]
        :placeholder "Start typing to filter list..."
        :label-path  ["value"]
        :value-path  ["value"]
        :options     [{"value" "biota" "label" "Biota" "uri" "46cb9d9a-08d5-4d65-ae01-794881169317"}
                      {"value" "boundaries" "label" "Boundaries" "uri" "13f26c70-a6cc-4c14-b823-487e05046324"}
                      {"value" "climatologyMeteorologyAtmosphere" "label" "Climatology/meteorology/atmosphere" "uri" "f4d1f2fb-3f2c-43c0-8196-0f12a32c0192"}
                      {"value" "disaster" "label" "Disaster" "uri" "36e178f2-ff28-461b-9a6f-117f9faed747"}
                      {"value" "economy" "label" "Economy" "uri" "19baf9c1-46e5-4522-872a-f4f10da3f530"}
                      {"value" "elevation" "label" "Elevation" "uri" "58b94857-6977-474a-be0e-6c9856bfebdc"}
                      {"value" "environment" "label" "Environment" "uri" "615c77e6-aec0-43d8-994c-9ff9421c3c96"}
                      {"value" "extraTerrestrial" "label" "Extra Terrestrial" "uri" "ccd13796-b99c-459d-a604-a8cc89674746"}
                      {"value" "farming" "label" "Farming" "uri" "58c9c399-cf08-4f74-82f8-23506cf3c472"}
                      {"value" "geoscientificInformation" "label" "Geoscientific Information" "uri" "943e577c-2097-4314-85ae-609f5e749886"}
                      {"value" "health" "label" "Health" "uri" "2f2d5de2-6def-4243-b407-b68bdef4d18d"}
                      {"value" "imageryBaseMapsEarthCover" "label" "Imagery Base Maps Earth Cover" "uri" "cacc1969-e445-4f5c-b63f-ae15357eca63"}
                      {"value" "inlandWaters" "label" "Inland Waters" "uri" "d83db4fd-ab8b-4b3d-b3ba-1150f6365a70"}
                      {"value" "intelligenceMilitary" "label" "Intelligence Military" "uri" "c6e74ebf-8b85-43c1-98f0-ad384e7d9926"}
                      {"value" "location" "label" "Location" "uri" "71107532-2a3c-4ac3-baae-147192cfb369"}
                      {"value" "oceans" "label" "Oceans" "uri" "7c3024e9-3838-4db0-a3b3-e4005f6a6572"}
                      {"value" "planningCadastre" "label" "Planning Cadastre" "uri" "012cae3c-0aa6-4cb7-a182-36b6cb60b83b"}
                      {"value" "society" "label" "Society" "uri" "a7c4b461-a912-4bcd-83de-b5b53a60b92e"}
                      {"value" "structure" "label" "Structure" "uri" "abb85428-f6f9-4647-9591-b28392fa3f12"}
                      {"value" "transportation" "label" "Transportation" "uri" "cfc0ef86-a8e1-4c06-b450-dac04c116d86"}
                      {"value" "utilitiesCommunication" "label" "Utilities Communication" "uri" "d090879c-f1cc-4929-b8a1-723c57eba3c1"}]}]
      [:div.SelectionListItemColoured
       [m4/selection-list-simple
        {:form-id    [:form]
         :data-path  ["identificationInfo" "topicCategories"]
         :label-path ["label"]
         :value-path ["value"]}]]]

     [:div {:style {:display               "grid"
                    :grid-column-gap       "1em"
                    :grid-template-columns "1fr 0.5fr 1fr"}}

      [:div

       ;; FIXME: Should this be use api for options?
       [m4/form-group
        {:form-id   [:form]
         :data-path ["identificationInfo" "status"]
         :label     "Status of Data"
         :toolTip   "Please indicate the status of the dataset."}
        [m4/select-value
         {:form-id    [:form]
          :data-path  ["identificationInfo" "status"]
          :value-path ["value"]
          :label-path ["value"]
          :options    [{"value" "accepted" "label" "Accepted"}
                       {"value" "completed" "label" "Completed"}
                       {"value" "deprecated" "label" "Deprecated"}
                       {"value" "final" "label" "Final"}
                       {"value" "historicalArchive" "label" "Historical Archive"}
                       {"value" "notAccepted" "label" "Not Accepted"}
                       {"value" "obsolete" "label" "Obsolete"}
                       {"value" "onGoing" "label" "Ongoing"}
                       {"value" "pending" "label" "Pending"}
                       {"value" "planned" "label" "Planned"}
                       {"value" "proposed" "label" "Proposed"}
                       {"value" "required" "label" "Required"}
                       {"value" "retired" "label" "Retired"}
                       {"value" "superseded" "label" "Superseded"}
                       {"value" "tentative" "label" "Tentative"}
                       {"value" "underDevelopment" "label" "Under Development"}
                       {"value" "valid" "label" "Valid"}
                       {"value" "withdrawn" "label" "Withdrawn"}]}]]]
      [:div
       [m4/form-group
        {:form-id    [:form]
         :data-path  ["identificationInfo" "version"]
         :label      "Version"
         :helperText "Version number of the resource"
         :toolTip    "Please input the version number of the collection, by default the version number starts with 1.0"}
        [m4/input-field
         {:form-id   [:form]
          :data-path ["identificationInfo" "version"]}]]]

      [:div

       ;; FIXME: Should this be use api for options?
       [m4/form-group
        {:form-id   [:form]
         :data-path ["identificationInfo" "maintenanceAndUpdateFrequency"]
         :label     "Maintenance/Update Freq"
         :toolTip   "Select the frequency of data maintained or updated"}
        [m4/select-value
         {:form-id    [:form]
          :data-path  ["identificationInfo" "maintenanceAndUpdateFrequency"]
          :value-path ["value"]
          :label-path ["value"]
          :options    [{"value" "continual" "label" "Continually"}
                       {"value" "daily" "label" "Daily"}
                       {"value" "weekly" "label" "Weekly"}
                       {"value" "fortnightly" "label" "Fortnightly"}
                       {"value" "monthly" "label" "Monthly"}
                       {"value" "quarterly" "label" "Quarterly"}
                       {"value" "biannually" "label" "Twice each year"}
                       {"value" "annually" "label" "Annually"}
                       {"value" "asNeeded" "label" "As required"}
                       {"value" "irregular" "label" "Irregular"}
                       {"value" "notPlanned" "label" "None planned"}
                       {"value" "unknown" "label" "Unknown"}
                       {"value" "periodic" "label" "Periodic"}
                       {"value" "semimonthly" "label" "Twice a month"}
                       {"value" "biennially" "label" "Every 2 years"}]}]]]]

     [m4/inline-form-group
      {:form-id   [:form]
       :data-path ["identificationInfo" "dateCreation"]
       :label     "Date the dataset was created"
       :toolTip   "For raw data this is the date when the data were collected. For derived data this is the date when the data were processed."}
      [m4/date-field2
       {:form-id   [:form]
        :data-path ["identificationInfo" "dateCreation"]}]]

     [m4/inline-form-group
      {:form-id   [:form]
       :data-path ["identificationInfo" "datePublicationFlag"]
       :label     "Has the data been published before?"
       :toolTip   "If the record has been published before, then select Yes and enter a published date below. Or else leave as default value as No"}
      [m4/yes-no-field
       {:form-id   [:form]
        :data-path ["identificationInfo" "datePublicationFlag"]
        :label     ""}]]

     ;; FIXME: I think this should be formatted as YYYY or YYYY-MM (according to the commented template)
     [m4/inline-form-group
      {:form-id   [:form]
       :data-path ["identificationInfo" "datePublication"]
       :label     "Previous Publication Date"}
      [m4/date-field2
       {:form-id   [:form]
        :data-path ["identificationInfo" "datePublication"]}]]]

    :what
    [:div
     [:h2 "2. What"]
     [:p "In this tab you will provide more contextual information about the data you are publishing."]
     [m4/form-group
      {:form-id    [:form]
       :data-path  ["identificationInfo" "abstract"]
       :label      "Abstract"
       :helperText "Describe the content of the resource; e.g. what information was collected, how was it collected"
       :toolTip    "Brief summary description about key aspects, attributes of the data, and contextual information about the dataset you are publishing in a clear, concise and human readable manner"}
      [m4/textarea-field
       {:form-id   [:form]
        :data-path ["identificationInfo" "abstract"]
        :maxLength 6000}]]
     [m4/form-group
      {:form-id    [:form]
       :data-path  ["identificationInfo" "purpose"]
       :label      "Purpose"
       :helperText "Brief statement about the purpose of the study"
       :toolTip    "Summary of intentions for which the dataset was created."}
      [m4/textarea-field
       {:form-id     [:form]
        :data-path   ["identificationInfo" "purpose"]
        :placeholder "Provide a brief summary of the purpose for collecting the data including the potential use."
        :maxLength   1000}]]

     [m4/form-group
      {:label      "Descriptive keywords"
       :helperText "Vocabulary terms related to the dataset that describes the science categories, Field of research, Platforms, Instruments, Resolutions and Species information."}]

     [m4/expanding-control
      {:label    "GCMD Science keywords"
       :required true}

      [m4/form-group
       {:form-id   [:form]
        :data-path ["identificationInfo" "keywordsTheme" "keywords"]
        :label     "Select research theme keywords - maximum of 12 allowed"
        :toolTip   "Select the GCMD Science keywords representing the dataset . You may select up to 12 keywords."}
       [m4/async-list-option-picker-breadcrumb
        {:form-id         [:form]
         :data-path       ["identificationInfo" "keywordsTheme" "keywords"]
         :uri             "/api/sciencekeyword"
         :label-path      ["label"]
         :value-path      ["uri"]
         :breadcrumb-path ["breadcrumb"]}]
       [:div.SelectionListItemColoured
        [m4/selection-list-breadcrumb
         {:form-id         [:form]
          :data-path       ["identificationInfo" "keywordsTheme" "keywords"]
          :label-path      ["label"]
          :value-path      ["uri"]
          :breadcrumb-path ["breadcrumb"]}]]]]

     [m4/expanding-control {:label "ANZSRC Fields keywords" :required true}
      [m4/form-group
       {:form-id   [:form]
        :data-path ["identificationInfo" "keywordsThemeAnzsrc" "keywords"]
        :label     "Select research theme keywords - maximum of 12 allowed"
        :toolTip   "Select Fields of Research keywords from the list. You can select upto 12 keywords."}
       [m4/async-list-option-picker-breadcrumb
        {:form-id         [:form]
         :data-path       ["identificationInfo" "keywordsThemeAnzsrc" "keywords"]
         :uri             "/api/anzsrckeyword"
         :label-path      ["label"]
         :value-path      ["uri"]
         :breadcrumb-path ["breadcrumb"]}]
       [:div.SelectionListItemColoured
        [m4/selection-list-breadcrumb
         {:form-id         [:form]
          :data-path       ["identificationInfo" "keywordsThemeAnzsrc" "keywords"]
          :label-path      ["label"]
          :value-path      ["uri"]
          :breadcrumb-path ["breadcrumb"]}]]]]

     [m4/expanding-control {:label "Parameters" :required true}

      [m4/form-group
       {:label     "Select a measured parameter, e.g. vegetation height"
        :toolTip   "Select Parameters (observable properties) in the dataset from the predefined list. If the required parameter is not available, you can click the ‘Add’ button to define a new parameter. All new entries will be reviewed prior to publication."
        :form-id   [:form]
        :data-path ["identificationInfo" "keywordsParametersUnits" "keywords"]}

       [m4/list-add-button
        {:form-id            [:form]
         :data-path          ["identificationInfo" "keywordsParametersUnits" "keywords"]
         :button-text        "Add Parameter"
         :value-path         ["uri"]
         :random-uuid-value? true}]

       [m4/form-group
        {:form-id      [:form]
         :data-path    ["identificationInfo" "keywordsParametersUnits" "keywords"]
         :show-errors? true}
        [:div.SelectionListItemColoured
         [m4/selection-list-columns
          {:form-id            [:form]
           :data-path          ["identificationInfo" "keywordsParametersUnits" "keywords"]
           :value-path         ["uri"]
           :random-uuid-value? true
           :select-snapshot?   true
           :select-mode        :all-items
           :added-path         ["isUserDefined"]
           :columns            [{:columnHeader "Name" :label-path ["parameter" "label"] :flex 2}
                                {:columnHeader "Units" :label-path ["unit" "label"] :flex 3}]}]]]
       [m4/list-edit-dialog
        {:form-id     [:form]
         :data-path   ["identificationInfo" "keywordsParametersUnits" "keywords"]
         :title       "Parameter"
         :template-id :parameter-unit/user-defined-entry-form
         :field-paths [["parameter" "label"] ["unit"]]}]]]

     [m4/expanding-control {:label "Temporal Resolution" :required true}
      [m4/form-group
       {:label   "Select a Temporal Resolution range"
        :toolTip "Temporal resolution specifies the time interval between data points. Select a resolution from the drop-down menu."}
       [m4/async-select-option-simple
        {:form-id    [:form]
         :data-path  ["identificationInfo" "keywordsTemporal" "keywords"]
         :uri        "/api/samplingfrequency"
         :label-path ["label"]
         :value-path ["uri"]}]]]

     [m4/expanding-control {:label "Horizontal Resolution" :required true}
      [m4/form-group
       {:label   "Select a Horizontal (Spatial) Resolution range"
        :toolTip "Horizontal resolution is a horizontal extent of the dataset. Select a resolution from the drop-down menu."}
       [m4/async-select-option-simple
        {:form-id    [:form]
         :data-path  ["identificationInfo" "keywordsHorizontal" "keywords"]
         :uri        "/api/horizontalresolution"
         :label-path ["label"]
         :value-path ["uri"]}]]]

     [m4/expanding-control {:label "Platforms (Optional)"}
      [m4/form-group
       {:label   "Select a platform for the data measurement"
        :toolTip "Select platform(s) that hosts other entities to generate the dataset from the list. If the required platform is not in the list, you can click the ‘Add’ button to add your platform. All new entries will be reviewed prior to publication."}

       [:div.bp3-control-group
        [:div.bp3-fill
         [m4/async-list-option-picker
          {:form-id    [:form]
           :data-path  ["identificationInfo" "keywordsPlatform" "keywords"]
           :uri        "/api/ternplatforms"
           :label-path ["label"]
           :value-path ["uri"]}]]
        [m4/list-add-button
         {:form-id            [:form]
          :data-path          ["identificationInfo" "keywordsPlatform" "keywords"]
          :button-text        "Add"
          :value-path         ["uri"]
          :random-uuid-value? true
          :item-defaults      {"userAddedCategory" "platform"}
          :added-path         ["isUserDefined"]}]]

       [:div.SelectionListItemColoured
        [m4/selection-list-simple
         {:form-id    [:form]
          :data-path  ["identificationInfo" "keywordsPlatform" "keywords"]
          :label-path ["label"]
          :value-path ["uri"]
          :added-path ["isUserDefined"]}]]

       [m4/list-edit-dialog
        {:form-id     [:form]
         :data-path   ["identificationInfo" "keywordsPlatform" "keywords"]
         :title       "Platform"
         :template-id :platform/user-defined-entry-form}]]]

     [m4/expanding-control {:label "Instruments (Optional)"}
      [m4/form-group
       {:label   "Select the instrument used for the platform"
        :toolTip "Select the instrument(s) or sensor(s) used in data collection.  If the required instrument is not in the list, you can click the ‘Add’ button to add your instrument. All new entries will be reviewed prior to publication."}

       [:div.bp3-control-group
        [:div.bp3-fill
         [m4/async-list-option-picker
          {:form-id    [:form]
           :data-path  ["identificationInfo" "keywordsInstrument" "keywords"]
           :uri        "/api/terninstruments"
           :label-path ["label"]
           :value-path ["uri"]}]]
        [m4/list-add-button
         {:form-id            [:form]
          :data-path          ["identificationInfo" "keywordsInstrument" "keywords"]
          :button-text        "Add"
          :value-path         ["uri"]
          :random-uuid-value? true
          :item-defaults      {"userAddedCategory" "instrument"}
          :added-path         ["isUserDefined"]}]]
       [:div.SelectionListItemColoured
        [m4/selection-list-columns
         {:form-id            [:form]
          :data-path          ["identificationInfo" "keywordsInstrument" "keywords"]
          :value-path         ["uri"]
          :random-uuid-value? true
          :select-snapshot?   true
          :added-path         ["isUserDefined"]
          :columns            [{:columnHeader "Instrument" :label-path ["label"] :flex 2}
                               {:columnHeader "Serial no." :label-path ["serial"] :flex 3}]}]]
       [m4/list-edit-dialog
        {:form-id     [:form]
         :data-path   ["identificationInfo" "keywordsInstrument" "keywords"]
         :title       "Instrument"
         :template-id :instrument/user-defined-entry-form}]]]

     [m4/expanding-control {:label "Vertical Resolution (Optional)" :required false}
      [m4/form-group
       {:label   "Select a Vertical Resolution range"
        :toolTip "This field is optional. A Vertical resolution is a vertical extent of the dataset. Select a resolution from the drop-down menu."}
       [m4/async-select-option-simple
        {:form-id    [:form]
         :data-path  ["identificationInfo" "keywordsVertical" "keywords"]
         :uri        "/api/verticalresolution"
         :label-path ["label"]
         :value-path ["uri"]}]]]

     [m4/expanding-control {:label "Australian Plant Name Index (Optional)" :required false}
      [m4/form-group
       {:label   "Select Plant Name Indexes keywords"
        :toolTip "Select Plant names from the APNI list. You may select upto 12 names."}
       [m4/async-list-option-picker-breadcrumb
        {:form-id         [:form]
         :data-path       ["identificationInfo" "keywordsFlora" "keywords"]
         :uri             "/api/ausplantnames"
         :label-path      ["label"]
         :value-path      ["uri"]
         :breadcrumb-path ["breadcrumb"]}]
       [:div.SelectionListItemColoured
        [m4/selection-list-breadcrumb
         {:form-id         [:form]
          :data-path       ["identificationInfo" "keywordsFlora" "keywords"]
          :label-path      ["label"]
          :value-path      ["uri"]
          :breadcrumb-path ["breadcrumb"]}]]]]

     [m4/expanding-control {:label "Australian Faunal Directory (Optional)" :required false}
      [m4/form-group
       {:label   "Select Australian Faunal Directory keywords"
        :toolTip "Select Animal species from the Australian Faunal Directory. You can select up to 12 names."}
       [m4/async-list-option-picker-breadcrumb
        {:form-id         [:form]
         :data-path       ["identificationInfo" "keywordsFauna" "keywords"]
         :uri             "/api/ausfaunalnames"
         :label-path      ["label"]
         :value-path      ["uri"]
         :breadcrumb-path ["breadcrumb"]}]
       [:div.SelectionListItemColoured
        [m4/selection-list-breadcrumb
         {:form-id         [:form]
          :data-path       ["identificationInfo" "keywordsFauna" "keywords"]
          :label-path      ["label"]
          :value-path      ["uri"]
          :breadcrumb-path ["breadcrumb"]}]]]]

     [m4/expanding-control {:label "Additional Keywords (Optional)" :required false}
      [m4/form-group
       {:label   "Additional theme keywords"
        :toolTip "You can add any additional keywords if they are not available in the lists above. All entries will be reviewed prior to publication."}
       [m4/text-add-button
        {:form-id     [:form]
         :data-path   ["identificationInfo" "keywordsAdditional" "keywords"]
         :button-text "Add"}]
       [:div.SelectionListItemColoured.Inverted
        [m4/selection-list-values
         {:form-id   [:form]
          :data-path ["identificationInfo" "keywordsAdditional" "keywords"]}]]]]]

    :when
    [:div

     [:h2 "3. When"]
     [:p "Provide temporal extent of the dataset."]

     [:div
      {:style {:display               "grid"
               :grid-column-gap       "1em"
               :grid-template-columns "repeat(auto-fill, minmax(10em, 1fr))"}}
      [m4/form-group
       {:form-id   [:form]
        :data-path ["identificationInfo" "beginPosition"]
        :label     "Start date"
        :toolTip   "This field is required. Start date in the dataset."}
       [m4/date-field2
        {:form-id   [:form]
         :data-path ["identificationInfo" "beginPosition"]}]]
      [m4/form-group
       {:form-id   [:form]
        :data-path ["identificationInfo" "endPosition"]
        :label     "End date"
        :toolTip   "This field is optional. The End date in the dataset, leave blank if it is continuous dataset."}
       [m4/date-field2
        {:form-id   [:form]
         :data-path ["identificationInfo" "endPosition"]}]]]]

    :where
    [:div
     [:h2 "4. Where"]
     [:p "Spatial representation and properties related to the dataset."]
     [:div.row
      [:div.col-sm-5
       ;; FIXME add toggle for satellite imagery.
       [m4/boxmap-field
        {:form-id    [:form]
         :data-path  ["identificationInfo" "geographicElement" "boxes"]
         :value-path ["uri"]
         :added-path ["isUserDefined"]}]]
      [:div.col-sm-7

       [m4/form-group
        {:form-id   [:form]
         :data-path ["identificationInfo" "geographicElement" "siteDescription"]
         :label     "Provide a site description (optional)"
         :toolTip   "Please provide a short description pertaining to the site of the dataset. This field is optional."}
        [m4/textarea-field
         {:form-id     [:form]
          :data-path   ["identificationInfo" "geographicElement" "siteDescription"]
          :placeholder "A descriptive reference for the coverage. May include a project code. Example: Geelong (Site: G145), VIC, Australia"
          :maxLength   500}]]

       [:p
        "Please input in decimal degrees in coordinate reference system WGS84."
        "Geoscience Australia see "
        [:a {:href   "https://geodesyapps.ga.gov.au/grid-to-geographic"
             :target "_blank"}
         "Grid to Geographic converter"]]

       [m4/form-group
        {:label     "Limits"
         :form-id   [:form]
         :data-path ["identificationInfo" "geographicElement" "boxes"]
         :required  true
         :toolTip   "Select the data's spatial extent on the map and the coordinates will be automatically populated."}
        [m4/selection-list-columns
         {:form-id            [:form]
          :data-path          ["identificationInfo" "geographicElement" "boxes"]
          :value-path         ["uri"]
          :random-uuid-value? true
          :select-snapshot?   true
          :added-path         ["isUserDefined"]
          :columns            [{:columnHeader "North" :label-path ["northBoundLatitude"] :flex 1}
                               {:columnHeader "East" :label-path ["eastBoundLongitude"] :flex 1}
                               {:columnHeader "South" :label-path ["southBoundLatitude"] :flex 1}
                               {:columnHeader "West" :label-path ["westBoundLongitude"] :flex 1}]}]

        [m4/list-add-button
         {:form-id            [:form]
          :data-path          ["identificationInfo" "geographicElement" "boxes"]
          :button-text        "Add"
          :value-path         ["uri"]
          :random-uuid-value? true
          :added-path         ["isUserDefined"]}]

        [m4/list-edit-dialog
         {:form-id     [:form]
          :data-path   ["identificationInfo" "geographicElement" "boxes"]
          :value-path  ["uri"]
          :added-path  ["isUserDefined"]
          :title       "Bounding box"
          :template-id :box/user-defined-entry-form}]]

       [m4/inline-form-group
        {:label     "Coordinate Reference System"
         :form-id   [:form]
         :data-path ["referenceSystemInfo" "crsCode"]
         :toolTip   "This field is required. Select a Coordinate Reference System related to the dataset."}
        [m4/async-select-option-simple
         {:form-id     [:form]
          :data-path   ["referenceSystemInfo" "crsCode"]
          :label-path  ["label"]
          :value-path  ["code"]
          :placeholder "Select from list"
          :uri         "/api/horizontalcrs"}]]

       [:p [:label "Vertical extent (optional)"]]
       [:p "The vertical extent is optional.  If you choose to enter details then the following fields are mandatory"]

       [m4/inline-form-group
        {:label    "Vertical Coordinate Reference System"
         :toolTip  "This field is Optional. If a Vertical Coordinate System is selected for the related to the dataset, then Minimum and Maximum are required."}
        [m4/async-select-option-simple
         {:form-id     [:form]
          :data-path   ["identificationInfo" "verticalElement" "coordinateReferenceSystem"]
          :uri         "/api/verticalcrs"
          :label-path  ["label"]
          :value-path  ["code"]
          :placeholder "Select from list"}]]

       [m4/inline-form-group
        {:label    "Minimum"
         :toolTip  "Input the minimum extent in meters."}
        [m4/numeric-input-field
         {:form-id   [:form]
          :data-path ["identificationInfo" "verticalElement" "minimumValue"]
          :unit      "meters"}]]

       [m4/inline-form-group
        {:label    "Maximum"
         :toolTip  "Input the maximum extent in meters."}
        [m4/numeric-input-field
         {:form-id   [:form]
          :data-path ["identificationInfo" "verticalElement" "maximumValue"]
          :unit      "meters"}]]

       [m4/expanding-control {:label "Spatial resolution (optional)"}
        [:div {:style {:display               "grid"
                       :grid-column-gap       "1em"
                       :grid-template-columns "auto auto"}}

         [m4/form-group
          {:label    "Resolution attribute"
           :toolTip  "Select an attribute used from the drop down list."}
          [m4/select-value
           {:form-id     [:form]
            :data-path   ["identificationInfo" "SpatialResolution" "ResolutionAttribute"]
            :placeholder "Start typing to filter list..."
            :label-path  ["label"]
            :value-path  ["value"]
            :options     [{"value" "Equivalent scale" "label" "Equivalent scale"}
                          {"value" "Distance" "label" "Distance"}
                          {"value" "Vertical" "label" "Vertical"}
                          {"value" "Angular distance" "label" "Angular distance"}]}]]

         [m4/form-group
          {:label    "Value"}
          [m4/numeric-input-field
           {:form-id   [:form]
            :data-path ["identificationInfo" "SpatialResolution" "ResolutionAttributeValue"]
            :unit      [m4/get-data
                        {:form-id   [:form]
                         :data-path ["identificationInfo" "SpatialResolution" "ResolutionAttributeUnits"]}]}]]]]]]]

    :box/user-defined-entry-form
    [:div

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "northBoundLatitude"]
       :label     "North"}
      [m4/numeric-input-field
       {:form-id   ?form-id
        :data-path [?data-path "northBoundLatitude"]}]]

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "eastBoundLongitude"]
       :label     "East"}
      [m4/numeric-input-field
       {:form-id   ?form-id
        :data-path [?data-path "eastBoundLongitude"]}]]

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "southBoundLatitude"]
       :label     "South"}
      [m4/numeric-input-field
       {:form-id   ?form-id
        :data-path [?data-path "southBoundLatitude"]}]]

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "westBoundLongitude"]
       :label     "West"}
      [m4/numeric-input-field
       {:form-id   ?form-id
        :data-path [?data-path "westBoundLongitude"]}]]]

    :who
    [:div
     [:h2 "5. Who"]

     [:p "
     This is a mandatory section and requires the person/organisation who is/are responsible for the dataset creation
     and the point of contact/s for the dataset.
     It can be a person or an organisation.
     You can assign more than one person or organisation.
     "]

     [m4/expanding-control {:label "Responsible for the creation of dataset" :required true :defaultOpen true}

      [:div.tern-collapsible-group
       [:p
        "Add people and/or organisations responsible for the dataset creation. Names of those assigned with Author and Co-authors will appear in the Citation statement."]

       [m4/list-add-button
        {:form-id            [:form]
         :data-path          ["identificationInfo" "citedResponsibleParty"]
         :button-text        "Add person"
         :value-path         ["uri"]
         :random-uuid-value? true
         :added-path         ["isUserDefined"]
         :item-defaults      {"partyType" "person"}}]

       [m4/list-add-button
        {:form-id            [:form]
         :data-path          ["identificationInfo" "citedResponsibleParty"]
         :button-text        "Add organisation"
         :value-path         ["uri"]
         :random-uuid-value? true
         :added-path         ["isUserDefined"]
         :item-defaults      {"partyType" "organisation"}}]

       [m4/form-group
        {:form-id    [:form]
         :data-path ["identificationInfo" "citedResponsibleParty"]}
        [:div.SelectionListItemColoured
         [m4/selection-list-template
          {:form-id     [:form]
           :data-path   ["identificationInfo" "citedResponsibleParty"]
           :template-id :party/list-item
           :select-mode :all-items
           :value-path  ["uri"]
           :added-path  ["isUserDefined"]}]]]

       [m4/typed-list-edit-dialog
        {:form-id   [:form]
         :data-path ["identificationInfo" "citedResponsibleParty"]
         :type-path ["partyType"]
         :templates {"person"
                     {:title       "Person"
                      :template-id :party-person-responsible/user-defined-entry-form
                      :field-paths #{["role"] ["contact"] ["organisation"]}}
                     "organisation"
                     {:title       "Organisation"
                      :template-id :party-organisation-responsible/user-defined-entry-form
                      :field-paths #{["role"] ["organisation"]}}}}]]]

     [m4/expanding-control {:label "Point of contact for dataset" :required true :defaultOpen true}

      [:div.tern-collapsible-group
       [:p
        "Add a Person and/or an Organisation as the contact for the dataset."]

       [m4/list-add-button
        {:form-id            [:form]
         :data-path          ["identificationInfo" "pointOfContact"]
         :button-text        "Add person"
         :value-path         ["uri"]
         :random-uuid-value? true
         :added-path         ["isUserDefined"]
         :item-defaults      {"partyType" "person"}}]

       [m4/list-add-button
        {:form-id            [:form]
         :data-path          ["identificationInfo" "pointOfContact"]
         :button-text        "Add organisation"
         :value-path         ["uri"]
         :random-uuid-value? true
         :added-path         ["isUserDefined"]
         :item-defaults      {"partyType" "organisation"}}]

       [m4/form-group
        {:form-id   [:form]
         :data-path ["identificationInfo" "pointOfContact"]}
        [:div.SelectionListItemColoured
         [m4/selection-list-template
          {:form-id     [:form]
           :data-path   ["identificationInfo" "pointOfContact"]
           :template-id :party/list-item
           :select-mode :all-items
           :value-path  ["uri"]
           :added-path  ["isUserDefined"]}]]]

       [m4/typed-list-edit-dialog
        {:form-id   [:form]
         :data-path ["identificationInfo" "pointOfContact"]
         :type-path ["partyType"]
         :templates {"person"
                     {:title       "Person"
                      :template-id :party-person-poc/user-defined-entry-form
                      :field-paths #{["role"] ["contact"] ["organisation"]}}
                     "organisation"
                     {:title       "Organisation"
                      :template-id :party-organisation-poc/user-defined-entry-form
                      :field-paths #{["role"] ["organisation"]}}}}]]]]

    :party/list-item
    [:div

     [m4/when-data {:form-id   [:form]
                    :data-path [?data-path "partyType"]
                    :pred      #{"person"}}
      [:div
       [m4/get-data {:form-id ?form-id :data-path [?data-path "contact" "given_name"]}] " "
       [m4/get-data {:form-id ?form-id :data-path [?data-path "contact" "surname"]}] " / "
       [:span {:style {:text-transform "capitalize"}}
        [m4/get-data {:form-id ?form-id :data-path [?data-path "role" "Identifier"]}]]]]

     [m4/when-data {:form-id   [:form]
                    :data-path [?data-path "partyType"]
                    :pred      #{"organisation"}}
      [:div
       [m4/get-data {:form-id ?form-id :data-path [?data-path "organisation" "name"]}] " / "
       [:span {:style {:text-transform "capitalize"}}
        [m4/get-data {:form-id ?form-id :data-path [?data-path "role" "Identifier"]}]]]]]

    :party-person-responsible/user-defined-entry-form
    [:div

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "role"]
       :label     "Role"
       :toolTip   "Select the role of any individuals that contributed to the creation of the dataset."}
      [m4/select-option-simple
       {:form-id    ?form-id
        :data-path  [?data-path "role"]
        :options    [{"UUID"        "a37cc120-9920-4495-9a2f-698e225b5902"
                      "Identifier"  "author"
                      "Description" "Party who authored the resource"}
                     {"UUID"        "cc22ca92-a323-42fa-8e01-1503f0edf6b9"
                      "Identifier"  "coAuthor"
                      "Description" "Party who jointly authors the resource"}
                     {"UUID"        "a2d57717-48fb-4675-95dd-4be8f9d585d6"
                      "Identifier"  "collaborator"
                      "Description" "Party who assists with the generation of the resource other than the principal investigator"}
                     {"UUID"        "b91ddbe5-584e-46ff-a242-1c7c67b836e3"
                      "Identifier"  "contributor"
                      "Description" "Party contributing to the resource"}
                     {"UUID"        "3373d310-f065-4ece-a61b-9bb04bd1df27"
                      "Identifier"  "custodian"
                      "Description" "Party that accepts accountability and responsibility for the resource and ensures appropriate care and maintenance of the resource"}
                     {"UUID"        "abd843f7-9d47-4a69-b9bc-3544202488fe"
                      "Identifier"  "distributor"
                      "Description" "Party who distributes the resource"}
                     {"UUID"        "370e8b34-d7ce-42fc-904f-05e263789389"
                      "Identifier"  "editor"
                      "Description" "Party who reviewed or modified the resource to improve the content"}
                     {"UUID"        "06213565-8aff-4c98-9ae3-4dd1023a2cdc"
                      "Identifier"  "funder"
                      "Description" "Party providing monetary support for the resource"}
                     {"UUID"        "2961f936-74cf-4192-95dc-959e8dae7189"
                      "Identifier"  "mediator"
                      "Description" "A class of entity that mediates access to the resource and for whom the resource is intended or useful"}
                     {"UUID"        "6cd5bbc6-463d-4850-9ad4-2353cb9451f5"
                      "Identifier"  "originator"
                      "Description" "Party who created the resource"}
                     {"UUID"        "0e75b54c-0cff-4753-a66a-c359f604689d"
                      "Identifier"  "owner"
                      "Description" "Party that owns the resource"}
                     {"UUID"        "6b20a462-bc67-46c3-bdcb-b558f0127fe2"
                      "Identifier"  "principalInvestigator"
                      "Description" "Key party responsible for gathering information and conducting research"}
                     {"UUID"        "c3429513-50aa-4288-b919-cdeb816815a7"
                      "Identifier"  "processor"
                      "Description" "Party who has processed the data in a manner such that the resource has been modified"}
                     {"UUID"        "1359d456-c428-49f1-8c8e-c46ebff53a10"
                      "Identifier"  "publisher"
                      "Description" "Party who published the resource"}
                     {"UUID"        "b25e217a-ed48-4d10-831e-298975f6cedf"
                      "Identifier"  "resourceProvider"
                      "Description" "Party that supplies the resource"}
                     {"UUID"        "028232f0-36c8-4ff6-aef4-ec0c424b7887"
                      "Identifier"  "rightsHolder"
                      "Description" "Party owning or managing rights over the resource"}
                     {"UUID"        "8211c24f-e1be-4a2d-962e-856304fa53de"
                      "Identifier"  "sponsor"
                      "Description" "Party who speaks for the resource"}
                     {"UUID"        "a9199aa5-26e2-4951-af7b-3132118d7569"
                      "Identifier"  "stakeholder"
                      "Description" "Party who has an interest in the resource or the use of the resource"}
                     {"UUID"        "4122989f-f824-4d4a-8a29-10bd3541c17e"
                      "Identifier"  "user"
                      "Description" "Party who uses the resource"}]
        :label-path ["Identifier"]
        :value-path ["Identifier"]}]]

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "contact"]
       :label     "Select Person"
       :toolTip   "Specify the name of the person associated with the selected role."}

      [:div.bp3-control-group
       [:div.bp3-fill
        [m4/async-select-option-simple
         {:form-id     ?form-id
          :data-path   [?data-path "contact"]
          :uri         "/api/ternpeople"
          :label-path  ["name"]
          :value-path  ["uri"]
          :placeholder "Search for contact details"}]]
       [m4/item-dialog-button
        {:form-id            ?form-id
         :data-path          [?data-path "contact"]
         :value-path         ["uri"]
         :random-uuid-value? true
         :item-defaults      {"userAddedCategory" "person"}
         :added-path         ["isUserDefined"]}]]

      [m4/edit-dialog
       {:form-id     ?form-id
        :data-path   [?data-path "contact"]
        :title       "Contact"
        :template-id :person-contact/user-defined-entry-form}]]

     [:div {:style {:display               "grid"
                    :grid-column-gap       "1em"
                    :grid-template-columns "1fr 1fr"}}

      [m4/form-group
       {:label "Given name"}
       [m4/input-field
        {:form-id   ?form-id
         :data-path [?data-path "contact" "given_name"]
         :disabled  true}]]

      [m4/form-group
       {:label "Surname"}
       [m4/input-field
        {:form-id   ?form-id
         :data-path [?data-path "contact" "surname"]
         :disabled  true}]]]

     [m4/form-group
      {:label "Email address"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "contact" "email"]
        :disabled  true}]]

     [m4/form-group
      {:label "ORCID ID"}
      [m4/input-field
       {:form-id     ?form-id
        :data-path   [?data-path "contact" "orcid"]
        :disabled    true}]]

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "organisation"]
       :label     "Select associated Organisation"
       :toolTip   "Select the organisation associated with the person. You can add an organisation using the “Add” button if the required organisation is not listed."}

      [:div.bp3-control-group
       [:div.bp3-fill
        [m4/async-select-option-simple
         {:form-id    ?form-id
          :data-path  [?data-path "organisation"]
          :uri        "/api/ternorgs"
          :label-path ["display_name"]
          :value-path ["uri"]}]]
       [m4/item-dialog-button
        {:form-id            ?form-id
         :data-path          [?data-path "organisation"]
         :value-path         ["uri"]
         :random-uuid-value? true
         :item-defaults      {"userAddedCategory" "organization"}
         :added-path         ["isUserDefined"]}]]

      [m4/edit-dialog
       {:form-id     ?form-id
        :data-path   [?data-path "organisation"]
        :title       "Organisation"
        :template-id :person-organisation/user-defined-entry-form}]]]

    :party-person-poc/user-defined-entry-form
    [:div

     ;; Only one "option" here, so leave it as the default:
     #_[m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "role"]
       :label     "Role"
       :toolTip   "Select the role of any individuals that contributed to the creation of the dataset."}
      [m4/select-option-simple
       {:form-id    ?form-id
        :data-path  [?data-path "role"]
        :options    [{"UUID"        "6511df52-a5ff-42da-8788-34dcad38ccc8"
                      "Identifier"  "pointOfContact"
                      "Description" "Party who can be contacted for acquiring knowledge about or acquisition of the resource"}]
        :label-path ["Identifier"]
        :value-path ["Identifier"]}]]

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "contact"]
       :label     "Select Person"
       :toolTip   "Specify the name of the person associated with the selected role."}

      [:div.bp3-control-group
       [:div.bp3-fill
        [m4/async-select-option-simple
         {:form-id     ?form-id
          :data-path   [?data-path "contact"]
          :uri         "/api/ternpeople"
          :label-path  ["name"]
          :value-path  ["uri"]
          :placeholder "Search for contact details"}]]
       [m4/item-dialog-button
        {:form-id            ?form-id
         :data-path          [?data-path "contact"]
         :value-path         ["uri"]
         :random-uuid-value? true
         :item-defaults      {"userAddedCategory" "person"}
         :added-path         ["isUserDefined"]}]]

      [m4/edit-dialog
       {:form-id     ?form-id
        :data-path   [?data-path "contact"]
        :title       "Contact"
        :template-id :person-contact/user-defined-entry-form}]]

     [:div {:style {:display               "grid"
                    :grid-column-gap       "1em"
                    :grid-template-columns "1fr 1fr"}}

      [m4/form-group
       {:label "Given name"}
       [m4/input-field
        {:form-id   ?form-id
         :data-path [?data-path "contact" "given_name"]
         :disabled  true}]]

      [m4/form-group
       {:label "Surname"}
       [m4/input-field
        {:form-id   ?form-id
         :data-path [?data-path "contact" "surname"]
         :disabled  true}]]]

     [m4/form-group
      {:label "Email address"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "contact" "email"]
        :disabled  true}]]

     [m4/form-group
      {:label "ORCID ID"}
      [m4/input-field
       {:form-id     ?form-id
        :data-path   [?data-path "contact" "orcid"]
        :disabled    true}]]

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "organisation"]
       :label     "Select associated Organisation"
       :toolTip   "Select the organisation associated with the person. You can add an organisation using the “Add” button if the required organisation is not listed."}

      [:div.bp3-control-group
       [:div.bp3-fill
        [m4/async-select-option-simple
         {:form-id    ?form-id
          :data-path  [?data-path "organisation"]
          :uri        "/api/ternorgs"
          :label-path ["display_name"]
          :value-path ["uri"]}]]
       [m4/item-dialog-button
        {:form-id            ?form-id
         :data-path          [?data-path "organisation"]
         :value-path         ["uri"]
         :random-uuid-value? true
         :item-defaults      {"userAddedCategory" "organization"}
         :added-path         ["isUserDefined"]}]]

      [m4/edit-dialog
       {:form-id     ?form-id
        :data-path   [?data-path "organisation"]
        :title       "Organisation"
        :template-id :person-organisation/user-defined-entry-form}]]]

                                        ; NOTE: organisation with role (not associated with a person)
    :party-organisation-responsible/user-defined-entry-form
    [:div

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "role"]
       :label     "Role"
       :toolTip   "Select the role of any individuals that contributed to the creation of the dataset."}
      [m4/select-option-simple
       {:form-id    ?form-id
        :data-path  [?data-path "role"]
        :options    [{"UUID"        "a37cc120-9920-4495-9a2f-698e225b5902"
                      "Identifier"  "author"
                      "Description" "Party who authored the resource"}
                     {"UUID"        "cc22ca92-a323-42fa-8e01-1503f0edf6b9"
                      "Identifier"  "coAuthor"
                      "Description" "Party who jointly authors the resource"}
                     {"UUID"        "a2d57717-48fb-4675-95dd-4be8f9d585d6"
                      "Identifier"  "collaborator"
                      "Description" "Party who assists with the generation of the resource other than the principal investigator"}
                     {"UUID"        "b91ddbe5-584e-46ff-a242-1c7c67b836e3"
                      "Identifier"  "contributor"
                      "Description" "Party contributing to the resource"}
                     {"UUID"        "3373d310-f065-4ece-a61b-9bb04bd1df27"
                      "Identifier"  "custodian"
                      "Description" "Party that accepts accountability and responsibility for the resource and ensures appropriate care and maintenance of the resource"}
                     {"UUID"        "abd843f7-9d47-4a69-b9bc-3544202488fe"
                      "Identifier"  "distributor"
                      "Description" "Party who distributes the resource"}
                     {"UUID"        "370e8b34-d7ce-42fc-904f-05e263789389"
                      "Identifier"  "editor"
                      "Description" "Party who reviewed or modified the resource to improve the content"}
                     {"UUID"        "06213565-8aff-4c98-9ae3-4dd1023a2cdc"
                      "Identifier"  "funder"
                      "Description" "Party providing monetary support for the resource"}
                     {"UUID"        "2961f936-74cf-4192-95dc-959e8dae7189"
                      "Identifier"  "mediator"
                      "Description" "A class of entity that mediates access to the resource and for whom the resource is intended or useful"}
                     {"UUID"        "6cd5bbc6-463d-4850-9ad4-2353cb9451f5"
                      "Identifier"  "originator"
                      "Description" "Party who created the resource"}
                     {"UUID"        "0e75b54c-0cff-4753-a66a-c359f604689d"
                      "Identifier"  "owner"
                      "Description" "Party that owns the resource"}
                     {"UUID"        "6b20a462-bc67-46c3-bdcb-b558f0127fe2"
                      "Identifier"  "principalInvestigator"
                      "Description" "Key party responsible for gathering information and conducting research"}
                     {"UUID"        "c3429513-50aa-4288-b919-cdeb816815a7"
                      "Identifier"  "processor"
                      "Description" "Party who has processed the data in a manner such that the resource has been modified"}
                     {"UUID"        "1359d456-c428-49f1-8c8e-c46ebff53a10"
                      "Identifier"  "publisher"
                      "Description" "Party who published the resource"}
                     {"UUID"        "b25e217a-ed48-4d10-831e-298975f6cedf"
                      "Identifier"  "resourceProvider"
                      "Description" "Party that supplies the resource"}
                     {"UUID"        "028232f0-36c8-4ff6-aef4-ec0c424b7887"
                      "Identifier"  "rightsHolder"
                      "Description" "Party owning or managing rights over the resource"}
                     {"UUID"        "8211c24f-e1be-4a2d-962e-856304fa53de"
                      "Identifier"  "sponsor"
                      "Description" "Party who speaks for the resource"}
                     {"UUID"        "a9199aa5-26e2-4951-af7b-3132118d7569"
                      "Identifier"  "stakeholder"
                      "Description" "Party who has an interest in the resource or the use of the resource"}
                     {"UUID"        "4122989f-f824-4d4a-8a29-10bd3541c17e"
                      "Identifier"  "user"
                      "Description" "Party who uses the resource"}]
        :label-path ["Identifier"]
        :value-path ["Identifier"]}]]

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "organisation"]
       :label     "Select associated Organisation"
       :toolTip   "Select the organisation associated with the primary contact. You can add an organisation if a required organisation is not listed."}

      [:div.bp3-control-group
       [:div.bp3-fill
        [m4/async-select-option-simple
         {:form-id    ?form-id
          :data-path  [?data-path "organisation"]
          :uri        "/api/ternorgs"
          :label-path ["display_name"]
          :value-path ["uri"]}]]
       [m4/item-dialog-button
        {:form-id            ?form-id
         :data-path          [?data-path "organisation"]
         :value-path         ["uri"]
         :random-uuid-value? true
         :item-defaults      {"userAddedCategory" "organization"}
         :added-path         ["isUserDefined"]}]]

      [m4/edit-dialog
       {:form-id     ?form-id
        :data-path   [?data-path "organisation"]
        :title       "Organisation"
        :template-id :person-organisation/user-defined-entry-form}]]

     [m4/form-group
      {:label "Organisation Name"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "organisation" "name"]
        :disabled  true}]]

     [m4/form-group
      {:label "Campus/Sitename"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "organisation" "full_address_line"]
        :disabled  true}]]

     [m4/form-group
      {:label "Street name"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "organisation" "street_address"]
        :disabled  true}]]

     [:div {:style {:display               "grid"
                    :grid-column-gap       "1em"
                    :grid-template-columns "1fr 1fr"}}

      [m4/form-group
       {:label "City"}
       [m4/input-field
        {:form-id   ?form-id
         :data-path [?data-path "organisation" "address_locality"]
         :disabled  true}]]

      [m4/form-group
       {:label "State"}
       [m4/input-field
        {:form-id   ?form-id
         :data-path [?data-path "organisation" "address_region"]
         :disabled  true}]]]

     [:div {:style {:display               "grid"
                    :grid-column-gap       "1em"
                    :grid-template-columns "1fr 1fr"}}

      [m4/form-group
       {:label "Postal Code"}
       [m4/input-field
        {:form-id   ?form-id
         :data-path [?data-path "organisation" "postcode"]
         :disabled  true}]]

      [m4/form-group
       {:label     "Country"}
       [m4/input-field
        {:form-id   ?form-id
         :data-path [?data-path "organisation" "country"]
         :disabled  true}]]]

     [m4/form-group
      {:label "Email address"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "organisation" "email"]
        :disabled  true}]]]

    :party-organisation-poc/user-defined-entry-form
    [:div

     ;; Only one "option" here, so leave it as the default:
     #_[m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "role"]
       :label     "Role"
       :toolTip   "Select the role of any individuals that contributed to the creation of the dataset."}
      [m4/select-option-simple
       {:form-id    ?form-id
        :data-path  [?data-path "role"]
        :options    [{"UUID"        "6511df52-a5ff-42da-8788-34dcad38ccc8"
                      "Identifier"  "pointOfContact"
                      "Description" "Party who can be contacted for acquiring knowledge about or acquisition of the resource"}]
        :label-path ["Identifier"]
        :value-path ["Identifier"]}]]

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "organisation"]
       :label     "Select associated Organisation"
       :toolTip   "Select the organisation associated with the primary contact. You can add an organisation if a required organisation is not listed."}

      [:div.bp3-control-group
       [:div.bp3-fill
        [m4/async-select-option-simple
         {:form-id    ?form-id
          :data-path  [?data-path "organisation"]
          :uri        "/api/ternorgs"
          :label-path ["display_name"]
          :value-path ["uri"]}]]
       [m4/item-dialog-button
        {:form-id            ?form-id
         :data-path          [?data-path "organisation"]
         :value-path         ["uri"]
         :random-uuid-value? true
         :item-defaults      {"userAddedCategory" "organization"}
         :added-path         ["isUserDefined"]}]]

      [m4/edit-dialog
       {:form-id     ?form-id
        :data-path   [?data-path "organisation"]
        :title       "Organisation"
        :template-id :person-organisation/user-defined-entry-form}]]

     [m4/form-group
      {:label "Organisation Name"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "organisation" "name"]
        :disabled  true}]]

     [m4/form-group
      {:label "Campus/Sitename"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "organisation" "full_address_line"]
        :disabled  true}]]

     [m4/form-group
      {:label "Street name"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "organisation" "street_address"]
        :disabled  true}]]

     [:div {:style {:display               "grid"
                    :grid-column-gap       "1em"
                    :grid-template-columns "1fr 1fr"}}

      [m4/form-group
       {:label "City"}
       [m4/input-field
        {:form-id   ?form-id
         :data-path [?data-path "organisation" "address_locality"]
         :disabled  true}]]

      [m4/form-group
       {:label "State"}
       [m4/input-field
        {:form-id   ?form-id
         :data-path [?data-path "organisation" "address_region"]
         :disabled  true}]]]

     [:div {:style {:display               "grid"
                    :grid-column-gap       "1em"
                    :grid-template-columns "1fr 1fr"}}

      [m4/form-group
       {:label "Postal Code"}
       [m4/input-field
        {:form-id   ?form-id
         :data-path [?data-path "organisation" "postcode"]
         :disabled  true}]]

      [m4/form-group
       {:label     "Country"}
       [m4/input-field
        {:form-id   ?form-id
         :data-path [?data-path "organisation" "country"]
         :disabled  true}]]]

     [m4/form-group
      {:label "Email address"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "organisation" "email"]
        :disabled  true}]]]

                                        ; NOTE: person organisation (no role)
    :person-organisation/user-defined-entry-form
    [:div

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "name"]
       :label     "Organisation Name"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "name"]}]]

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "full_address_line"]
       :label     "Campus/Sitename"}
      [:i.bp3-text-muted "Site name will be generated based on provided address information:"]
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "full_address_line"]
        :disabled  true}]]

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "street_address"]
       :label     "Street name"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "street_address"]}]]

     [:div {:style {:display               "grid"
                    :grid-column-gap       "1em"
                    :grid-template-columns "1fr 1fr"}}

      [m4/form-group
       {:form-id   ?form-id
        :data-path [?data-path "address_locality"]
        :label     "City"}
       [m4/input-field
        {:form-id   ?form-id
         :data-path [?data-path "address_locality"]}]]

      [m4/form-group
       {:form-id   ?form-id
        :data-path [?data-path "address_region"]
        :label     "State"}
       [m4/input-field
        {:form-id   ?form-id
         :data-path [?data-path "address_region"]}]]]

     [:div {:style {:display               "grid"
                    :grid-column-gap       "1em"
                    :grid-template-columns "1fr 1fr"}}

      [m4/form-group
       {:form-id   ?form-id
        :data-path [?data-path "postcode"]
        :label     "Postal Code"}
       [m4/input-field
        {:form-id   ?form-id
         :data-path [?data-path "postcode"]}]]

      [m4/form-group
       {:form-id   ?form-id
        :data-path [?data-path "country"]
        :label     "Country"}
       [m4/input-field
        {:form-id   ?form-id
         :data-path [?data-path "country"]}]]]

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "email"]
       :label     "Email address"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "email"]}]]]

    :person-contact/user-defined-entry-form
    [:div

     [:div {:style {:display               "grid"
                    :grid-column-gap       "1em"
                    :grid-template-columns "1fr 1fr"}}

      [m4/form-group
       {:form-id   ?form-id
        :data-path [?data-path "given_name"]
        :label     "Given name"}
       [m4/input-field
        {:form-id   ?form-id
         :data-path [?data-path "given_name"]}]]

      [m4/form-group
       {:form-id   ?form-id
        :data-path [?data-path "surname"]
        :label     "Surname"}
       [m4/input-field
        {:form-id   ?form-id
         :data-path [?data-path "surname"]}]]]

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "email"]
       :label     "Email address"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "email"]}]]

     [m4/form-group
      {:form-id     ?form-id
       :data-path   [?data-path "orcid"]
       :label       "ORCID ID"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "orcid"]
        :placeholder "XXXX-XXXX-XXXX-XXXX"}]]]

    :how
    [:div
     [:h2 "6. How"]

     [:p "This section is optional. You can provide a description of method(s) used for the collection of the data and/or Add online documentation of the methods and detailed procedure steps."]

     [m4/expanding-control {:label "Data creation procedure details (Optional)"}

      [m4/form-group
       {:form-id    [:form]
        :data-path  ["resourceLineage" "statement"]
        :label      "Provide a brief summary of the source of the data and related collection and/or processing methods."
        :helperText "e.g. Data was collected at the site using the meethod described in XXX Manual, refer to URL..."}
       [m4/textarea-field
        {:form-id   [:form]
         :data-path ["resourceLineage" "statement"]}]]

      [m4/form-group
       {:label   "Method documentation"
        :toolTip "Provide links to the online method documentations, including peer-review papers describing the method."}
       [:div.SelectionListItemColoured.Inverted
        [m4/selection-list-columns
         {:form-id            [:form]
          :data-path          ["resourceLineage" "onlineMethods"]
          :value-path         ["uri"]
          :random-uuid-value? true
          :select-snapshot?   true
          :added-path         ["isUserDefined"]
          :columns            [{:columnHeader "Title" :label-path ["title"] :flex 1}
                               {:columnHeader "URL" :label-path ["url"] :flex 1}]}]]

       [m4/list-add-button
        {:form-id            [:form]
         :data-path          ["resourceLineage" "onlineMethods"]
         :button-text        "Add"
         :value-path         ["uri"]
         :random-uuid-value? true
         :item-defaults      {"userAddedCategory" "onlineMethods"}
         :added-path         ["isUserDefined"]}]

       [m4/list-edit-dialog
        {:form-id     [:form]
         :data-path   ["resourceLineage" "onlineMethods"]
         :value-path  ["uri"]
         :added-path  ["isUserDefined"]
         :title       "Method Documentation"
         :template-id :method-doc/user-defined-entry-form}]]]

     [m4/expanding-control {:label "Data creation procedure steps (Optional)"}

      ;; How7b: list-add free-text entries
      [m4/form-group
       {:label   "If the need arises please add steps taken for the Data creation procedure to support the summary provided above."
        :toolTip "Specify the steps of the procedure."}
       [m4/text-add-button
        {:form-id     [:form]
         :data-path   ["resourceLineage" "steps"]
         :button-text "Add"}]
       [:div.SelectionListItemColoured.Inverted
        [m4/selection-list-values
         {:form-id   [:form]
          :data-path ["resourceLineage" "steps"]}]]]
      [m4/list-edit-dialog
       {:form-id     [:form]
        :data-path   ["resourceLineage" "steps"]
        :title       "Data creation step"
        :template-id :data-creation-step/user-defined-entry-form}]]]

    :method-doc/user-defined-entry-form
    [:div

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "title"]
       :label     "Title"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "title"]}]]

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "url"]
       :label     "URL"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "url"]}]]]
    
    :data-creation-step/user-defined-entry-form
    [:div
     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path ?data-path
       :label     "Step"}
      [m4/textarea-field
       {:form-id   ?form-id
        :data-path ?data-path}]]]

    :quality
    [:div
     [:h2 "7. Data Quality"]
     [:p "Provide details about the scope of the data quality assessment, reports on data quality and any data quality assessment outcome."]

     [m4/form-group
      {:form-id   [:form]
       :data-path ["dataQualityInfo" "methodSummary"]
       :label     "Provide a summary of the scope of the Data Quality Assessment"}
      [m4/textarea-field
       {:form-id     [:form]
        :data-path   ["dataQualityInfo" "methodSummary"]
        :maxLength   6000
        :placeholder "The data quality was assessed by ..."}]]

     [m4/form-group
      {:label    "Online data quality report"
       :toolTip  "Data quality report refers to a textual description of the quality control of the dataset. Provide the title and URL of the report, if available."}

      [:div {:style {:display               "grid"
                     :grid-column-gap       "1em"
                     :grid-template-columns "auto auto"}}

       [m4/form-group
        {:label "Title"
         :form-id [:form]
         :data-path ["dataQualityInfo" "onlineMethods" "title"]}
        [m4/input-field
         {:form-id [:form]
          :data-path ["dataQualityInfo" "onlineMethods" "title"]}]]

       [m4/form-group
        {:label "URL"
         :form-id [:form]
         :data-path ["dataQualityInfo" "onlineMethods" "url"]}
        [m4/input-field
         {:form-id [:form]
          :data-path ["dataQualityInfo" "onlineMethods" "url"]}]]]]

     [m4/form-group
      {:form-id   [:form]
       :data-path ["dataQualityInfo" "results"]
       :label     "Provide a statement regarding the Data Quality Assessment outcome"}
      [m4/textarea-field
       {:form-id     [:form]
        :data-path   ["dataQualityInfo" "results"]
        :maxLength   6000
        :placeholder "A statement regarding the data quality assessment results. Examples: RMSE relative to reference data set; horizontal or vertical positional accuracy; etc."}]]]

    :quality/user-defined-entry-form
    [:div
     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "title"]
       :label     "Title"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "title"]}]]

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "url"]
       :label     "URL"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "url"]}]]]

    :about
    [:div
     [:h2 "8: About Dataset"]
     [:p "This section allows you to provide information of the dataset collection, and will inform the consumer with the legal obligations, limitations of use and any other relevant details such as resources and publications."]

     [:h3 "Limitation/Constraints"]
     [m4/form-group
      {:form-id   [:form]
       :data-path ["identificationInfo" "useLimitation"]
       :label     "Use limitations"}
      [:div
       [:i [m4/get-data {:form-id [:form] :data-path ["identificationInfo" "useLimitation"]}]]]]
     [m4/form-group
      {;:form-id   [:form]
                                        ;:data-path ["identificationInfo"]
       :label "Other constraints"}
      [m4/simple-list
       {:form-id     [:form]
        :data-path   ["identificationInfo" "otherConstraints"]
        :template-id :about/other-constraints-list-entry}]]
     [m4/form-group
      {:label   "Any other constraints"
       :toolTip "Enter any additional constraints as required and click the Add button."}
      [m4/text-add-button
       {:form-id     [:form]
        :data-path   ["identificationInfo" "additionalConstraints" "constraints"]
        :button-text "Add"}]
      [:div.SelectionListItemColoured.Inverted
       [m4/selection-list-values
        {:form-id   [:form]
         :data-path ["identificationInfo" "additionalConstraints" "constraints"]}]]
      [m4/list-edit-dialog
       {:form-id [:form]
        :data-path ["identificationInfo" "additionalConstraints" "constraints"]
        :title "Other Constraints"
        :template-id :about/other-constraints-edit-entry}]]

     [m4/form-group
      {:label   "Security Classification"
       :toolTip "Please select a relevant security classification for the dataset."}
      [m4/select-option-simple
       {:form-id    [:form]
        :data-path  ["identificationInfo" "securityClassification"]
        :label-path ["value"]
        :value-path ["value"]
        :options    [{"label" "Unclassified" "value" "unclassified"}
                     {"label" "Restricted" "value" "restricted"}
                     {"label" "Confidential" "value" "confidential"}
                     {"label" "Secret" "value" "secret"}
                     {"label" "Top-Secret" "value" "topSecret"}
                     {"label" "SBU" "value" "SBU"}
                     {"label" "For Official Use Only" "value" "forOfficialUseOnly"}
                     {"label" "Protected" "value" "protected"}
                     {"label" "Limited Distribution" "value" "limitedDistribution"}]}]]

     [m4/expanding-control {:label "Environment Description (Optional)"}
      [m4/form-group
       {:form-id    [:form]
        :data-path  ["identificationInfo" "environment"]
        :label      "Environmental description"
        :toolTip    "Description of the dataset in the producer’s processing environment, including items such as the software, the computer operating system, file name, format, language and the dataset size."
        :helperText "Software, computer operating system, file name, or dataset size"}
       [m4/textarea-field
        {:form-id     [:form]
         :data-path   ["identificationInfo" "environment"]
         :placeholder "Information about the source and software to process the resource"
         :maxLength   6000}]]]

     [m4/expanding-control {:label "Associated Documentation (Optional)"}

      [m4/form-group
       {:label   "Publication"
        :toolTip "Associated resources related to the dataset such as projects and documents"}
       [:div.SelectionListItemColoured.Inverted
        [m4/selection-list-columns
         {:form-id            [:form]
          :data-path          ["identificationInfo" "additionalPublications"]
          :value-path         ["uri"]
          :random-uuid-value? true
          :select-snapshot?   true
          :added-path         ["isUserDefined"]
          :columns            [{:columnHeader "Title" :label-path ["title"] :flex 1}
                               {:columnHeader "URL" :label-path ["url"] :flex 1}]}]]

       [m4/list-add-button
        {:form-id            [:form]
         :data-path          ["identificationInfo" "additionalPublications"]
         :button-text        "Add"
         :value-path         ["uri"]
         :random-uuid-value? true
         :item-defaults      {"userAddedCategory" "additionalPublications"}
         :added-path         ["isUserDefined"]}]

       [m4/list-edit-dialog
        {:form-id     [:form]
         :data-path   ["identificationInfo" "additionalPublications"]
         :value-path  ["uri"]
         :added-path  ["isUserDefined"]
         :title       "Online Publication"
         :template-id :about/user-defined-entry}]]

      [m4/form-group
       {:form-id    [:form]
        :data-path  ["identificationInfo" "supplemental"]
        :label      "Supplemental Information"
        :toolTip    "Miscellaneous information about the dataset, not captured elsewhere. This is an optional field."
        :helperText "Any supplemental information needed to interpret the resource"}
       [m4/textarea-field
        {:form-id     [:form]
         :data-path   ["identificationInfo" "supplemental"]
         :placeholder "Information about how to interpret the resource, example: Pixel value indicates the number of days since reference date 1970-01-01"
         :maxLength   1000}]]]

     [m4/expanding-control {:label "Resource specific usage (Optional)"}
      [m4/form-group
       {:form-id    [:form]
        :data-path  ["identificationInfo" "resourceSpecificUsage"]
        :label      "Resource specific usage"
        :toolTip    "Information about use of the datasets in specific application(s) by different users."
        :helperText "Provide a brief overview on how the dataset can be used"}
       [m4/textarea-field
        {:form-id     [:form]
         :data-path   ["identificationInfo" "resourceSpecificUsage"]
         :placeholder "Resource specific usage..."
         :maxLength   1000}]]]

     [m4/expanding-control {:label "Acknowledgment (Optional)"}
      [m4/form-group
       {:form-id    [:form]
        :data-path  ["identificationInfo" "credit"]
        :label      "Acknowledgment"
        :toolTip    "Recognition of those who contributed to the dataset. Do not include URLs here."
        :helperText "Write a sentence acknowledging sponsors, data providers or funding organisations"}
       [m4/textarea-field
        {:form-id     [:form]
         :data-path   ["identificationInfo" "credit"]
         :placeholder "The project was funded by xxx and yyy"
         :maxLength   1000}]]]

     [m4/expanding-control {:label "Citation (Optional)"}
      [m4/form-group
       {:form-id    [:form]
        :data-path  ["identificationInfo" "customCitation"]
        :label      "Specific citation"
        :toolTip    "Other information required to complete the citation that is not
recorded in the standard citation."
        :helperText "e.g., “The data is owned by the Queensland Government.”"}
       [m4/textarea-field
        {:form-id   [:form]
         :data-path ["identificationInfo" "customCitation"]
         :maxLength 1000}]]]]

    :about/other-constraints-edit-entry
    [:div
     [m4/form-group
      {:form-id ?form-id
       :data-path ?data-path
       :label "Edit"}
      [m4/input-field
       {:form-id ?form-id
        :data-path ?data-path}]]]

    :about/user-defined-entry
    [:div
     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "title"]
       :label     "Title"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "title"]}]]

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "url"]
       :label     "URL"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "url"]}]]]

    :about/other-constraints-list-entry
    [:p {:key ?data-path}
     [:i [m4/dangerous-data
          {:form-id   ?form-id
           :data-path ?data-path}]]]

    :upload
    [:div
     [:h2 "9. Data Sources"]
     [:p "Add dataset files and services associated with the metadata."]
     [m4/upload-files
      {:form-id     [:form]
       :data-path   ["attachments"]
       :value-path  ["id"]
       :row-template :upload-files/file-row-template
       :placeholder [:div
                     [:h3 "Drop file here or click here to upload"]
                     [:span.help-block "Maximum file size 100 MB"]]}]
     [m4/list-edit-dialog
      {:form-id     [:form]
       :data-path   ["attachments"]
       :value-path  ["id"]
       :title       "File Details"
       :template-id :upload-files/edit-details-template}]

     [:h3 "Thumbnail"]

     [m4/form-group
      {:form-id   [:form]
       :data-path ["identificationInfo" "thumbnail" "title"]
       :label     "Title"
       :toolTip   "Provide the title of the Thumbnail uploaded."}
      [m4/input-field
       {:form-id   [:form]
        :data-path ["identificationInfo" "thumbnail" "title"]}]]
     [m4/form-group
      {:form-id   [:form]
       :data-path ["identificationInfo" "thumbnail" "file"]}
      [m4/upload-thumbnail
       {:form-id     [:form]
        :data-path   ["identificationInfo" "thumbnail" "file"]
        :value-path  ["id"]
        :placeholder [:div
                      [:h3 "Drag and drop thumbnail here"]
                      [:span.help-block "Maximum size 1000 x 1000px"]]}]]
     [:p "A small image that exemplifies the dataset."]

     [:h2 "Data Services"]
     [m4/form-group
      {:label   "Distributions"
       :toolTip "Information about distributed data details."}
      [:div.SelectionListItemColoured.Inverted
       [m4/selection-list-columns
        {:form-id            [:form]
         :data-path          ["dataSources"]
         :value-path         ["uri"]
         :random-uuid-value? true
         :select-snapshot?   true
         :added-path         ["isUserDefined"]
         :columns            [{:columnHeader "Protocol" :label-path ["transferOptions" "protocol" "label"] :flex 1}
                              {:columnHeader "Server" :label-path ["transferOptions" "linkage"] :flex 1}
                              {:columnHeader "Name" :label-path ["transferOptions" "name"] :flex 1}]}]]

      [m4/list-add-button
       {:form-id            [:form]
        :data-path          ["dataSources"]
        :button-text        "Add"
        :value-path         ["uri"]
        :random-uuid-value? true
        :added-path         ["isUserDefined"]}]

      [m4/list-edit-dialog
       {:form-id     [:form]
        :data-path   ["dataSources"]
        :value-path  ["uri"]
        :added-path  ["isUserDefined"]
        :title       "Data Distribution"
        :template-id :data-sources/user-defined-entry-form}]]]

    :upload-files/file-row-template
    [:div {:style {:display :flex}}
     [:span {:style {:flex 1}}
      [m4/when-errors
       {:form-id ?form-id
        :data-path [?data-path "title"]
        :show false}
       [m4/get-data
        {:form-id ?form-id
         :data-path [?data-path "title"]}]]
      [m4/when-errors
       {:form-id ?form-id
        :data-path [?data-path "title"]
        :show true}
       [:span {:style {:color "red"}} "A title is required"]]]
     [:span {:style {:flex 1}}
      [m4/get-data
       {:form-id ?form-id
        :data-path [?data-path "name"]}]]]

    :upload-files/edit-details-template
    [:div
     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "title"]
       :label     "Title"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "title"]}]]
     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "name"]
       :label     "Filename"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "name"]
        :disabled  true}]]]

    :data-sources/user-defined-entry-form
    [:div
     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "transferOptions" "name"]
       :label     "Title"
       :toolTip   "Title of the data source"}
      [m4/textarea-field
       {:form-id   ?form-id
        :data-path [?data-path "transferOptions" "name"]}]]

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "transferOptions" "protocol"]
       :label     "Protocol"}
      [m4/select-option-simple
       {:form-id    ?form-id
        :data-path  [?data-path "transferOptions" "protocol"]
        :label-path ["label"]
        :value-path ["value"]
        :options    [{"label" "HTTP" "value" "WWW:DOWNLOAD-1.0-http--download"}
                     {"label" "OGC Web Coverage Service (WCS)" "value" "OGC:WCS-1.1.0-http-get-capabilities"}
                     {"label" "OGC Web Map Service (WMS)" "value" "OGC:WMS-1.3.0-http-get-map"}
                     {"label" "OGC Web Feature Service (WFS)" "value" "OGC:WFS-1.1.0-http-get-capabilities"}
                     {"label" "OPeNDAP" "value" "WWW:LINK-1.0-http--opendap"}
                     {"label" "FTP" "value" "FTP"}
                     {"label" "Other/unknown" "value" "WWW:DOWNLOAD-1.0-http--download"}]}]]

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "transferOptions" "linkage"]
       :label     "URL Address"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "transferOptions" "linkage"]}]]

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "transferOptions" "description"]
       :label     "Layer Name"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "transferOptions" "description"]}]]

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path]
       :label     "Distributor's Organisation"
       :toolTip   "TERN is selected by default. You can select another organisation as a distributor for the dataset"}
      [m4/async-select-option-simple
       {:form-id     ?form-id
        :data-path   [?data-path "distributor"]
        :uri         "/api/ternorgs"
        :label-path  ["display_name"]
        :value-path  ["uri"]
        :placeholder "Will use TERN - UQ Long Pocket if not specified"}]]]

    :lodge
    [:div
     [:h2 "10: Lodge Metadata Draft"]
     [:p "Information about distribution and options to load data."]

     [m4/when-errors
      {:form-id   [:form]
       :data-path []
       :show      true}

      ;; TODO: CSS for <b> styling?
      [:b
       {:style {:color "#e36a51"}}
       "Please note the following:"]
      [:ul
       [:li
        "You will not be able to lodge this form as it is "
        ;; TODO: CSS for <b> styling?
        [:b
         {:style {:color "#e36a51"}}
         "incomplete"]
        "."]
       [:li
        "The tabs with the "
        ;; TODO: CSS for span styling?
        [:span
         {:style {:color "#e36a51"}}
         "Red Asterisk"]
        " indicate that "
        ;; TODO: CSS for span styling?
        [:span
         {:style {:font-style "italic"
                  :font-weight "bold"}}
         "Mandatory fields are incomplete"]
        "."]
       [:li "Ensure all required fields are complete."]
       [:li
        "Please contact "
        [:a {:href "mailto:esupport@tern.org.au"} "esupport@tern.org.au"]
        " if you encounter any difficulties."]]]

     [m4/when-errors
      {:form-id   [:form]
       :data-path []
       :show      false}

      [m4/form-group
       {:form-id   [:form]
        :data-path ["identificationInfo" "generatedCitation"]
        :label     "Generated Citation"}
       [m4/textarea-field
        {:form-id     [:form]
         :data-path   ["identificationInfo" "generatedCitation"]
         :disabled    true
         :placeholder "(Authors, co-authors)[Year Published](Title). Version {number}. Terrestrial Ecosystem Research Network (TERN), dataset {DOI}"}]]

      [:p "If you have any difficulties with the lodgement process or form entry requirements, please email: "
       [:a {:href "mailto:esupport@tern.org.au"} "esupport@tern.org.au"]]

      [m4/submit-status]

      [:p "The Data Manager will be notified of your submission and will be in contact"
       " if any further information is required. Once approved, your data will be archived for discovery in the "
       [:b "TERN Data Portal."]]
      [m4/form-group
       {:form-id   [:form]
        :data-path ["noteForDataManager"]
        :label     "You can include a note for the Data Manager (Optional)"}
       [m4/textarea-field
        {:form-id   [:form]
         :data-path ["noteForDataManager"]}]]

      [m4/inline-form-group
       {:form-id   [:form]
        :data-path ["identificationInfo" "doiFlag"]
        :label     "Is there an existing DOI for this dataset?"
                                        ;:toolTip   "Select 'Yes' if a DOI has already been minted for this dataset."
        }
       [m4/yes-no-field
        {:form-id   [:form]
         :data-path ["identificationInfo" "doiFlag"]
         :label     ""}]]
      [m4/input-field
       {:form-id         [:form]
        :data-path       ["identificationInfo" "doi"]
        :placeholder     "Please enter existing DOI here"}]

      [m4/form-group
       {:form-id [:form]
        :label   "Please tick the relevant boxes as required"}

       [m4/checkbox-field
        {:form-id   [:form]
         :data-path ["identificationInfo" "doiRequested"]
         :label     "Please mint a DOI for this submission"}]

       [m4/checkbox-field
        {:form-id   [:form]
         :data-path ["agreedToTerms"]
         :label     "I have read and agree with the terms and conditions"}]]

      [m4/terms-conditions]

      [m4/lodge-button
       {:form-id   [:form]
        :data-path ["agreedToTerms"]}]

      [:hr]
      [:b "Want to keep a personal copy of your metadata record?"]
      [:p
       [m4/xml-export-link
        {:label "Click here"}]
       " to generate an XML version of your metadata submission. The file generated includes all of the details you have provided under the tabs, but not files you have uploaded."]
      [:p "Please note: this XML file is not the recommended way to share your metadata. We want you to submit your data via 'lodging' the information. This permits multi-user access via the portal in a more friendly format."]]]})

(set! low-code4/template-registry
      (merge edit-templates
             {::components4/create-document-modal-form
              components4/create-document-modal-template
              ::components4/contributors-modal-form
              components4/contributors-modal-template}))
