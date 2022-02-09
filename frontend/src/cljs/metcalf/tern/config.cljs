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
(rf/reg-event-fx ::components4/value-changed handlers4/value-changed-handler)
(rf/reg-event-fx ::components4/input-blur handlers4/input-blur-handler)
(rf/reg-event-fx ::components4/value-list-add-with-defaults-click-handler handlers4/value-list-add-with-defaults-click-handler2)
(rf/reg-event-fx ::handlers4/-save-current-document-error handlers4/-save-current-document-error)
(rf/reg-event-fx ::handlers4/-save-current-document-success handlers4/-save-current-document-success)
(rf/reg-event-fx :app/-archive-current-document-error handlers3/-archive-current-document-error)
(rf/reg-event-fx :app/-archive-current-document-success handlers3/-archive-current-document-success)
(rf/reg-event-fx :app/-clone-document-error handlers3/-clone-document-error)
(rf/reg-event-fx :app/-clone-document-success handlers3/-clone-document-success)
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
(rf/reg-event-fx :metcalf.common.actions4/-upload-single-attachment handlers4/-upload-single-attachment)
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
(rf/reg-sub :subs/get-edit-tab-props :<- [:subs/get-page-props] :<- [::subs4/get-form-state [:form]] :<- [::tern-subs/get-edit-tabs] tern-subs/get-edit-tab-props)
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
      {"requiredField"        rules4/required-field
       "requiredWhenYes"      rules4/required-when-yes
       "spatialUnits"         rules4/spatial-resolution-units
       "requiredAllNone"      rules4/required-all-or-nothing
       "maxLength"            rules4/max-length
       "mergeNameParts"       rules4/merge-names
       "validOrcid"           rules4/valid-ordid-uri
       "geographyRequired"    rules4/geography-required
       "numericOrder"         rules4/numeric-order
       "positive"             rules4/force-positive
       "dateOrder"            rules4/date-order
       "dateBeforeToday"      rules4/date-before-today
       "endPosition"          rules4/end-position
       "maintFreq"            rules4/maint-freq
       "verticalRequired"     rules4/vertical-required
       "maxKeywords"          rules4/tern-max-keywords})
(set! low-code4/component-registry
      {
       'm4/async-simple-item-option-picker     {:view #'components4/async-simple-item-option-picker :init components4/async-simple-item-option-picker-settings}
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
       'm4/select-option-simple                {:view #'components4/select-option-simple :init components4/select-option-simple-settings}
       'm4/select-option-breadcrumb            {:view #'components4/select-option-breadcrumb :init components4/select-option-breadcrumb-settings}
       'm4/select-option-columns               {:view #'components4/select-option-columns :init components4/select-option-columns-settings}
       'm4/select-value                        {:view #'components4/select-value :init components4/select-value-settings}
       ;'m4/simple-list-option-picker           {:view #'components4/simple-list-option-picker :init components4/simple-list-option-picker-settings}
       'm4/selection-list-template             {:view #'components4/selection-list-template :init components4/selection-list-template-settings}
       'm4/selection-list-simple               {:view #'components4/selection-list-simple :init components4/selection-list-simple-settings}
       'm4/selection-list-values               {:view #'components4/selection-list-values :init components4/selection-list-values-settings}
       ;'m4/table-list-option-picker            {:view #'components4/table-list-option-picker :init components4/table-list-option-picker-settings}
       'm4/selection-list-columns              {:view #'components4/selection-list-columns3 :init components4/selection-list-columns3-settings}
       'm4/textarea-field                      {:view #'components4/textarea-field :init components4/textarea-field-settings}
       'm4/when-data                           {:view #'components4/when-data :init components4/when-data-settings}
       'm4/get-data                            {:view #'components4/get-data :init components4/get-data-settings}
       'm4/yes-no-field                        {:view #'components4/yes-no-field :init components4/yes-no-field-settings}
       'm4/simple-list                         {:view #'components4/simple-list :init components4/simple-list-settings}
       ;'m4/record-add-button                   {:view #'components4/record-add-button :init components4/record-add-button-settings}
       'm4/text-add-button                     {:view #'components4/text-add-button :init components4/text-add-button-settings}
       'm4/upload-files                        {:view #'components4/upload-files :init components4/upload-files-settings}
       'm4/upload-thumbnail                    {:view #'components4/upload-thumbnail :init components4/upload-thumbnail-settings}
       })

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
       :toolTip   "Specify the source of the platform descriptions in citation format, if available."}
      [m4/textarea-field
       {:form-id     ?form-id
        :data-path   [?data-path "source"]
        :placeholder "E.g. Creator (Publication year).  Title.  Version.  Publisher.  Resource type.  Identifier.  "}]]]

    :instrument/user-defined-entry-form
    [:div

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "label"]
       :label     "Name/Label"
       :toolTip   "TODO"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "label"]}]]

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "description"]
       :label     "Description /Definition"
       :toolTip   "TODO"}
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
       :toolTip    "This is optional. You can a serial number of the instrument if it is available."}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "serial"]}]]]

    :unit/user-defined-entry-form
    [:div

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "label"]
       :label     "Name/Label"
       :toolTip   "TODO"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "label"]}]]

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "description"]
       :label     "Description /Definition"
       :toolTip   "TODO"}
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
      {:form-id    ?form-id
       :data-path  [?data-path "symbol"]
       :label      "Symbol"
       :helperText "Optional"}
      [m4/textarea-field
       {:form-id   ?form-id
        :data-path [?data-path "symbol"]}]]

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "source"]
       :label     "Source"}
      [m4/textarea-field
       {:form-id     ?form-id
        :data-path   [?data-path "source"]
        :placeholder "E.g. Creator (Publication year).  Title.  Version.  Publisher.  Resource type.  Identifier.  "}]]

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "unit"]
       :label     "Unit of measure"
       :toolTip   "Select a Unit of Measure (UoM) from the list. If the required UoM is not found within the list, you can click the 'Add' button to define a new units of measurement. The entry will be reviewed prior to publishing."}

      [:div.bp3-control-group
       [:div.bp3-fill
        [m4/async-select-option-simple
         {:form-id    ?form-id
          :data-path  [?data-path "unit"]
          :uri        "/api/qudtunits"
          :label-path ["label"]
          :value-path ["uri"]
          :added-path ["isUserDefined"]}]]
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
     #_[m4/page-errors
        {:form-id    [:form]
         :data-path  []
         :data-paths [["identificationInfo" "title"]
                      ["identificationInfo" "dateCreation"]
                      ["identificationInfo" "topicCategory"]
                      ["identificationInfo" "status"]
                      ["identificationInfo" "maintenanceAndUpdateFrequency"]
                      ["identificationInfo" "version"]]}]

     [:h2 "1. Data Identification"]

     [m4/form-group
      {:form-id    [:form]
       :data-path  ["identificationInfo" "title"]
       :label      "Title"
       :helperText "Clear and concise description of the content of the resource including What, Where, (How), When e.g. Fractional Cover for Australia 2014 ongoing"
       :toolTip    "Enter the title of the dataset. Title should be short and informative."}
      [m4/input-field
       {:form-id     [:form]
        :data-path   ["identificationInfo" "title"]
        :placeholder "Provide a descriptive title for the data set including the subject of study, the study location and time period. Example: TERN OzFlux Arcturus Emerald Tower Site 2014-ongoing"}]]

     [m4/form-group
      {:form-id  [:form]
       :data-path ["parentMetadata"]
       :label    "Parent Metadata"
       :toolTip  "Check 'Yes' if there is a Parent Metadata Record associated with the dataset"}
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
       :data-path ["identificationInfo" "topicCategory"]
       :label     "Topic Category"
       :toolTip   "Please select a topic category relevant to the dataset (multiple selection is allowed)."}
      [m4/select-value
       {:form-id     [:form]
        :data-path   ["identificationInfo" "topicCategory"]
        :placeholder "Start typing to filter list..."
        :label-path  ["label"]
        :value-path  ["value"]
        :options     [{"value" "farming" "label" "farming"}
                      {"value" "biota" "label" "biota"}
                      {"value" "boundaries" "label" "boundaries"}
                      {"value" "climatology/meteorology/atmosphere" "label" "climatology/meteorology/atmosphere"}
                      {"value" "economy" "label" "economy"}
                      {"value" "environment" "label" "environment"}
                      {"value" "geoscientificInformation" "label" "geoscientificInformation"}
                      {"value" "health" "label" "health"}
                      {"value" "imageryBaseMapsEarthCover" "label" "imageryBaseMapsEarthCover"}
                      {"value" "intelligenceMilitary" "label" "intelligenceMilitary"}
                      {"value" "inlandWaters" "label" "inlandWaters"}
                      {"value" "location" "label" "location"}
                      {"value" "oceans" "label" "oceans"}
                      {"value" "planningCadastre" "label" "planningCadastre"}
                      {"value" "society" "label" "society"}
                      {"value" "structure" "label" "structure"}
                      {"value" "transportation" "label" "transportation"}
                      {"value" "utilitiesCommunication" "label" "utilitiesCommunication"}
                      {"value" "extraTerrestrial" "label" "extraTerrestrial"}
                      {"value" "disaster" "label" "disaster"}]}]]

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
          :label-path ["label"]
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
         :toolTip    "Please input the version number of the collection."
         :required   true}
        [m4/input-field
         {:form-id   [:form]
          :data-path ["identificationInfo" "version"]}]]]

      [:div

       ;; FIXME: Should this be use api for options?
       [m4/form-group
        {:form-id   [:form]
         :data-path ["identificationInfo" "maintenanceAndUpdateFrequency"]
         :label     "Maintenance/Update Freq"
         :toolTip   "Please select the update frequency of the dataset."}
        [m4/select-value
         {:form-id    [:form]
          :data-path  ["identificationInfo" "maintenanceAndUpdateFrequency"]
          :value-path ["value"]
          :label-path ["label"]
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
       :label     "Date the resource was created"
       :toolTip   "Select the creation date of the record."}
      [m4/date-field2
       {:form-id   [:form]
        :data-path ["identificationInfo" "dateCreation"]
        :required  true}]]

     [m4/yes-no-field
      {:form-id   [:form]
       :data-path ["identificationInfo" "datePublicationFlag"]
       :label     "Has the data been published before?"
       :toolTip   "If the record has been published before, then please indicate as Yes and enter a published date. Or else leave as default value."}]

     ;; FIXME: I think this should be formatted as YYYY or YYYY-MM (according to the commented template)
     [m4/inline-form-group
      {:form-id   [:form]
       :data-path ["identificationInfo" "datePublication"]
       :label     "Previous Publication Date"}
      [m4/date-field2
       {:form-id   [:form]
        :data-path ["identificationInfo" "datePublication"]
        :required  true}]]]

    :what
    [:div
     #_[m4/page-errors
        {:form-id    [:form]
         :data-path  []
         :data-paths [["identificationInfo" "abstract"]
                      ["identificationInfo" "purpose"]]}]
     [:h2 "2. What"]
     [:p "TODO: Lorem ipsum..."]
     [m4/form-group
      {:form-id    [:form]
       :data-path  ["identificationInfo" "abstract"]
       :label      "Abstract"
       :helperText "Describe the content of the resource; e.g. what information was collected, how was it collected"
       :toolTip    "A summary describing the dataset, e.g., “What, When, Where and How” in relation to the dataset."
       :required   true}
      [m4/textarea-field
       {:form-id   [:form]
        :data-path ["identificationInfo" "abstract"]
        :maxLength 2500}]]
     [m4/form-group
      {:form-id    [:form]
       :data-path  ["identificationInfo" "purpose"]
       :label      "Purpose"
       :helperText "Brief statement about the purpose of the study"
       :toolTip    "Provide the purpose of the dataset."}
      [m4/textarea-field
       {:form-id     [:form]
        :data-path   ["identificationInfo" "purpose"]
        :placeholder "Provide a brief summary of the purpose for collecting the data including the potential use."
        :maxLength   1000}]]

     [m4/form-group
      {:label      "Descriptive keywords"
       :toolTip    "TODO"
       :helperText "Vocabulary terms that describe the general science categories, general location, organizations, projects, platforms, instruments associated with the resource."}]

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
        :toolTip   "Select the keywords representing fields of research . You may select up to 12 keywords."}
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

     [m4/expanding-control {:label "Platforms" :required true}
      ;; TODO: also need a user-added option
      [m4/form-group
       {:label   "Select a platform for the data measurement"
        :toolTip "Select the platform(s) that hosts the sensors that generates the dataset from the list. If the required platform is not in the list, you can click the '+ Add' button to add your platform. The entry will be reviewed prior to publishing."}

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

     [m4/expanding-control {:label "Instruments" :required true}
      ;; TODO: also need a user-added option
      [m4/form-group
       {:label   "Select the instrument used for the platform"
        :toolTip "Select the instruments(s) or sensor(s) used to create the dataset from the list. If the required instrument is not in the list, you can click the '+ Add' button to add your instrument. The entry will be reviewed prior to publishing."}

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

     [m4/expanding-control {:label "Parameters" :required true}

      ;; TODO: also need a user-added option
      [m4/form-group
       {:label   "Select the name of the measured parameter, e.g. vegetation height"
        :toolTip "Select a parameter (observed variable) from the predefined list. If the required parameter is not found within the list, you can click the '+ Add' button to define a new parameter. The entry will be reviewed prior to publishing."}

       [:div.bp3-control-group
        [:div.bp3-fill
         [m4/async-list-option-picker
          {:form-id    [:form]
           :data-path  ["identificationInfo" "keywordsParameters" "keywords"]
           :uri        "/api/ternparameters"
           :label-path ["label"]
           :value-path ["uri"]}]]
        [m4/list-add-button
         {:form-id            [:form]
          :data-path          ["identificationInfo" "keywordsParameters" "keywords"]
          :button-text        "Add"
          :value-path         ["uri"]
          :random-uuid-value? true
          :item-defaults      {"userAddedCategory" "parameter"}
          :added-path         ["isUserDefined"]}]]
       [:div.SelectionListItemColoured
        [m4/selection-list-columns
         {:form-id            [:form]
          :data-path          ["identificationInfo" "keywordsParameters" "keywords"]
          :value-path         ["uri"]
          :random-uuid-value? true
          :select-snapshot?   true
          :added-path         ["isUserDefined"]
          :columns            [{:columnHeader "Name" :label-path ["label"] :flex 2}
                               {:columnHeader "Units" :label-path ["unit" "label"] :flex 3}]}]]
       [m4/list-edit-dialog
        {:form-id     [:form]
         :data-path   ["identificationInfo" "keywordsParameters" "keywords"]
         :title       "Parameter"
         :template-id :parameter/user-defined-entry-form}]]]

     [m4/expanding-control {:label "Temporal Resolution" :required true}
      [m4/form-group
       {:label   "Select a Temporal Resolution range"
        :toolTip "Temporal resolution specifies the targeted time period between each value in the data set. Select a Temporal resolution range from the predefined list. Only one item can be selected."}
       [m4/async-select-option-simple
        {:form-id    [:form]
         :data-path  ["identificationInfo" "keywordsTemporal" "keywords"]
         :uri        "/api/samplingfrequency"
         :label-path ["label"]
         :value-path ["uri"]}]]]

     [m4/expanding-control {:label "Horizontal Resolution" :required true}
      [m4/form-group
       {:label   "Select a Horizontal Resolution range"
        :toolTip "Select a Horizontal resolution range from the predefined list. Only one item can be selected."}
       [m4/async-select-option-simple
        {:form-id    [:form]
         :data-path  ["identificationInfo" "keywordsHorizontal" "keywords"]
         :uri        "/api/horizontalresolution"
         :label-path ["label"]
         :value-path ["uri"]}]]]

     [m4/expanding-control {:label "Vertical Resolution (Optional)" :required false}
      [m4/form-group
       {:label   "Select a Vertical Resolution range"
        :toolTip "Select a Vertical resolution range from the predefined list. Only one item can be selected."}
       [m4/async-select-option-simple
        {:form-id    [:form]
         :data-path  ["identificationInfo" "keywordsVertical" "keywords"]
         :uri        "/api/verticalresolution"
         :label-path ["label"]
         :value-path ["uri"]}]]]

     [m4/expanding-control {:label "Australian Plant Name Index (Optional)" :required false}
      [m4/form-group
       {:label   "Select Plant Name Indexes keywords"
        :toolTip "Select the plant names from the APNI list. You may select up to 12 names."}
       [m4/async-list-option-picker-breadcrumb
        {:form-id         [:form]
         :data-path       ["identificationInfo" "keywordsFlora" "keywords"]
         :uri             "/api/ausplantnames"              ; TODO: testing required; currently nothing in index
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
        :toolTip "Select animal species from the Australian Faunal Directory. You may select up to 12 keywords."}
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
       {:label   "Additional theme keywords can be added for review and approval process"
        :toolTip "You may define additional keywords if they are not available in the lists above.  The keywords will be reviewed prior to publishing."}
       [m4/text-add-button
        {:form-id     [:form]
         :data-path   ["identificationInfo" "keywordsAdditional" "keywords"]
         :button-text "Add"}]
       [:div.SelectionListItemColoured
        [m4/selection-list-values
         {:form-id   [:form]
          :data-path ["identificationInfo" "keywordsAdditional" "keywords"]}]]]]]

    :when
    [:div
     #_[m4/page-errors
        {:form-id    [:form]
         :data-path  []
         :data-paths [["identificationInfo" "beginPosition"]
                      ["identificationInfo" "endPosition"]]}]

     [:h2 "3. When"]
     [:p "Lorem ipsum..."]

     [:div
      {:style {:display               "grid"
               :grid-column-gap       "1em"
               :grid-template-columns "repeat(auto-fill, minmax(10em, 1fr))"}}
      [m4/form-group
       {:form-id   [:form]
        :data-path ["identificationInfo" "beginPosition"]
        :label     "Start date"
        :toolTip   "Describes the date of the first data point in the dataset."
        :required  true}
       [m4/date-field2
        {:form-id   [:form]
         :data-path ["identificationInfo" "beginPosition"]}]]
      [m4/form-group
       {:form-id   [:form]
        :data-path ["identificationInfo" "endPosition"]
        :label     "End date"
        :toolTip   "Describes the date of the last data point in the data set."
        :required  true}
       [m4/date-field2
        {:form-id   [:form]
         :data-path ["identificationInfo" "endPosition"]}]]]]

    :where
    [:div
     #_[m4/page-errors
        {:form-id    [:form]
         :data-path  []
         :data-paths [["identificationInfo" "geographicElement" "boxes"]
                      ["identificationInfo" "verticalElement" "minimumValue"]
                      ["identificationInfo" "verticalElement" "maximumValue"]]}]
     [:h2 "4. Where"]
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
          :placeholder "A descriptive reference for the coverage. May include a project code. Example: Geelong (Site: G145), VIC, Australia"}]]

       [:p
        "Please input in decimal degrees in coordinate reference system WGS84."
        "Geoscience Australia see "
        [:a {:href   "https://geodesyapps.ga.gov.au/grid-to-geographic"
             :target "_blank"}
         "Grid to Geographic converter"]]

       [m4/form-group
        {:label    "Limits"
         :toolTip  "Select the data's spatial extent on the map and the coordinates will be automatically populated."
         :required true}
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

       [:div {:style {:display               "grid"
                      :grid-column-gap       "1em"
                      :grid-template-columns "1fr 1fr"}}
        [m4/form-group
         {:label    "Coordinate Reference System"
          :required true
          :toolTip  "Select a Coordinate Reference System."}
         [m4/async-select-option-simple
          {:form-id     [:form]
           :data-path   ["referenceSystemInfo" "crsCode"]
           :label-path  ["description"]
           :value-path  ["code"]
           :placeholder "Select from list"
           :uri         "/api/horizontalcrs"}]]

        [m4/form-group
         {:form-id [:form]
          :label   "Date of dynamic datum"
          :toolTip "TODO"}
         [m4/form-group
          {:form-id   [:form]
           :data-path ["referenceSystemInfo" "DateOfDynamicDatum"]}
          [m4/date-field2
           {:form-id   [:form]
            :data-path ["referenceSystemInfo" "DateOfDynamicDatum"]}]]]]

       [:p [:label "Vertical extent (optional)"]]
       [:p "The vertical extent is optional.  If you choose to enter details then the following fields are mandatory"]

       [m4/inline-form-group
        {:label    "Vertical Coordinate Reference System"
         :required true
         :toolTip  "Select the Vertical Coordinate System."}
        [m4/async-select-option-simple
         {:form-id     [:form]
          :data-path   ["identificationInfo" "verticalElement" "coordinateReferenceSystem"]
          :uri         "/api/verticalcrs"
          :label-path  ["description"]
          :value-path  ["code"]
          :placeholder "Select from list"}]]

       [m4/inline-form-group
        {:label    "Minimum"
         :required true
         :toolTip  "Input the minimum extent in meters."}
        [m4/numeric-input-field
         {:form-id   [:form]
          :data-path ["identificationInfo" "verticalElement" "minimumValue"]
          :unit      "meters"}]]

       [m4/inline-form-group
        {:label    "Maximum"
         :required true
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
           :required true
           :toolTip  "Indicate the attribute used is either a Denominator Scale, Vertical, Horizontal, or Angular distance."}
          [m4/select-value
           {:form-id     [:form]
            :data-path   ["identificationInfo" "SpatialResolution" "ResolutionAttribute"]
            :placeholder "Start typing to filter list..."
            :label-path  ["label"]
            :value-path  ["value"]
            :options     [{"value" "None" "label" "None"}
                          {"value" "Denominator scale" "label" "Denominator scale"}
                          {"value" "Vertical" "label" "Vertical"}
                          {"value" "Horizontal" "label" "Horizontal"}
                          {"value" "Angular distance" "label" "Angular distance"}]}]]

         [m4/form-group
          {:label    "Value"
           :required true}
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
     This is a mandatory section and requires the person/organisation who is/are responsible for the dataset
     and the point of contact/s for the dataset.
     It can be a person or an organisation.
     You can assign more than one person or organisation to the sections.
     The person or organisation can be added at any point of time but must be completed prior to lodgement
     "]

     [m4/expanding-control {:label "Responsible for the creation of dataset" :required true :defaultOpen true}

      [:div.tern-collapsible-group
       [:p
        "Please assign a person and/or an organisation as responsible for the creation of the dataset. "
        "More than one person or an organisation can be included as well."]

       [m4/list-add-button
        {:form-id            [:form]
         :data-path          ["identificationInfo" "citedResponsibleParty"]
         :button-text        "Add person"
         :value-path         ["uri"]
         :random-uuid-value? true
         :added-path         ["isUserDefined"]
         ;; FIXME: Add userAddedCategory to item defaults?
         :item-defaults      {"partyType" "person"}}]

       [m4/list-add-button
        {:form-id            [:form]
         :data-path          ["identificationInfo" "citedResponsibleParty"]
         :button-text        "Add organisation"
         :value-path         ["uri"]
         :random-uuid-value? true
         :added-path         ["isUserDefined"]
         ;; FIXME: Add userAddedCategory to item defaults?
         :item-defaults      {"partyType" "organisation"}}]

       [:div.SelectionListItemColoured
        [m4/selection-list-template
         {:form-id     [:form]
          :data-path   ["identificationInfo" "citedResponsibleParty"]
          :template-id :party/list-item
          :value-path  ["uri"]
          :added-path  ["isUserDefined"]}]]

       [m4/typed-list-edit-dialog
        {:form-id   [:form]
         :data-path ["identificationInfo" "citedResponsibleParty"]
         :type-path ["partyType"]
         :templates {"person"
                     {:title       "Person"
                      :template-id :party-person/user-defined-entry-form
                      :field-paths #{["role"] ["contact"] ["organisation"]}}
                     "organisation"
                     {:title       "Organisation"
                      :template-id :party-organisation/user-defined-entry-form
                      :field-paths #{["role"] ["organisation"]}}}}]]]

     [m4/expanding-control {:label "Point of contact for dataset" :required true :defaultOpen true}

      [:div.tern-collapsible-group
       [:p
        "Please assign a persona and/or an organisation as the point of contact.  More than one person or organisation can be included."]

       [m4/list-add-button
        {:form-id            [:form]
         :data-path          ["identificationInfo" "pointOfContact"]
         :button-text        "Add person"
         :value-path         ["uri"]
         :random-uuid-value? true
         :added-path         ["isUserDefined"]
         ;; FIXME: Add userAddedCategory to item defaults?
         :item-defaults      {"partyType" "person"}}]

       [m4/list-add-button
        {:form-id            [:form]
         :data-path          ["identificationInfo" "pointOfContact"]
         :button-text        "Add organisation"
         :value-path         ["uri"]
         :random-uuid-value? true
         :added-path         ["isUserDefined"]
         ;; FIXME: Add userAddedCategory to item defaults?
         :item-defaults      {"partyType" "organisation"}}]

       [m4/selection-list-template
        {:form-id     [:form]
         :data-path   ["identificationInfo" "pointOfContact"]
         :template-id :party/list-item
         :value-path  ["uri"]
         :added-path  ["isUserDefined"]}]

       [m4/typed-list-edit-dialog
        {:form-id   [:form]
         :data-path ["identificationInfo" "pointOfContact"]
         :type-path ["partyType"]
         :templates {"person"
                     {:title       "Person"
                      :template-id :party-person/user-defined-entry-form
                      :field-paths #{["role"] ["contact"] ["organisation"]}}
                     "organisation"
                     {:title       "Organisation"
                      :template-id :party-organisation/user-defined-entry-form
                      :field-paths #{["role"] ["organisation"]}}}}]]]

     ;[m4/list-add-button
     ; {:form-id    [:form]
     ;  :data-path  ["identificationInfo" "PointOfContactForDataset"]
     ;  :text       "Add"
     ;  :value-path ["uri"]
     ;  :added-path ["isUserDefined"]}]
     ;
     ;[m4/selection-list-columns
     ; {:form-id    [:form]
     ;  :data-path  ["identificationInfo" "PointOfContactForDataset"]
     ;  :label-path ["contact" "label"]
     ;  :value-path ["uri"]
     ;  :random-uuid-value? true
     ;  :columns    [{:columnHeader "contact" :label-path ["contact" "label"] :flex 2}
     ;               {:columnHeader "role" :label-path ["role" "label"] :flex 3}]
     ;  :added-path ["isUserDefined"]}]
     ;
     ;[m4/list-edit-dialog
     ; {:form-id     [:form]
     ;  :data-path   ["identificationInfo" "PointOfContactForDataset"]
     ;  :title       "Responsible for creating the data"
     ;  :template-id :person/user-defined-entry-form}]

     #_[m3/Who
        {:credit-path [:form :fields :identificationInfo :credit]}]]

    :party/list-item
    [:div

     [m4/when-data {:form-id   [:form]
                    :data-path [?data-path "partyType"]
                    :pred      #{"person"}}
      [:div
       [m4/get-data {:form-id ?form-id :data-path [?data-path "contact" "given_name"]}] " "
       [m4/get-data {:form-id ?form-id :data-path [?data-path "contact" "surname"]}] " / "
       [m4/get-data {:form-id ?form-id :data-path [?data-path "role" "Identifier"]}]]]

     [m4/when-data {:form-id   [:form]
                    :data-path [?data-path "partyType"]
                    :pred      #{"organisation"}}
      [:div
       [m4/get-data {:form-id ?form-id :data-path [?data-path "organisation" "name"]}] " / "
       [m4/get-data {:form-id ?form-id :data-path [?data-path "role" "Identifier"]}]]]]

    :party-person/user-defined-entry-form
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
                     {"UUID"        "6511df52-a5ff-42da-8788-34dcad38ccc8"
                      "Identifier"  "pointOfContact"
                      "Description" "Party who can be contacted for acquiring knowledge about or acquisition of the resource"}
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
        :label-path ["Description"]
        :value-path ["Identifier"]}]]

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "contact"]
       :label     "Contact"
       :toolTip   "Select the primary contact of the dataset."}
      [m4/async-simple-item-option-picker
       {:form-id     ?form-id
        :data-path   [?data-path "contact"]
        :uri         "/api/ternpeople"
        :label-path  ["name"]
        :value-path  ["uri"]
        :placeholder "Search for contact details"}]]

     [:p "If contact is not available, please enter the contact details below."]

     [:div {:style {:display               "grid"
                    :grid-column-gap       "1em"
                    :grid-template-columns "1fr 1fr"}}

      [m4/form-group
       {:form-id   ?form-id
        :data-path [?data-path "contact" "given_name"]
        :label     "Given name"}
       [m4/input-field
        {:form-id   ?form-id
         :data-path [?data-path "contact" "given_name"]}]]

      [m4/form-group
       {:form-id   ?form-id
        :data-path [?data-path "contact" "surname"]
        :label     "Surname"}
       [m4/input-field
        {:form-id   ?form-id
         :data-path [?data-path "contact" "surname"]}]]]

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "contact" "email"]
       :label     "Email address"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "contact" "email"]}]]

     [m4/form-group
      {:form-id     ?form-id
       :data-path   [?data-path "contact" "orcid"]
       :label       "ORCID ID"
       :placeholder "XXXX-XXXX-XXXX-XXXX"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "contact" "orcid"]}]]

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
          :label-path ["name"]
          :value-path ["uri"]}]]
       [m4/item-dialog-button
        {:form-id            ?form-id
         :data-path          [?data-path "organisation"]
         :value-path         ["uri"]
         :random-uuid-value? true
         :added-path         ["isUserDefined"]}]]

      [m4/edit-dialog
       {:form-id     ?form-id
        :data-path   [?data-path "organisation"]
        :title       "Organisation"
        :template-id :person-organisation/user-defined-entry-form}]]]

    ; NOTE: organisation with role (not associated with a person)
    :party-organisation/user-defined-entry-form
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
                     {"UUID"        "6511df52-a5ff-42da-8788-34dcad38ccc8"
                      "Identifier"  "pointOfContact"
                      "Description" "Party who can be contacted for acquiring knowledge about or acquisition of the resource"}
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
        :label-path ["Description"]
        :value-path ["Identifier"]}]]

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "organisation"]
       :label     "Contact"
       :toolTip   "Select an organisation from the list or add a new organisation if  it is not available  in the list."}
      [m4/async-simple-item-option-picker
       {:form-id     ?form-id
        :data-path   [?data-path "organisation"]
        :uri         "/api/ternorgs"
        :label-path  ["name"]
        :value-path  ["uri"]
        :placeholder "Search for organisation details"}]]

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "organisation" "name"]
       :label     "Organisation Name"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "organisation" "name"]}]]

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "organisation" "full_address_line"]
       :label     "Campus/Sitename"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "organisation" "full_address_line"]}]]

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "organisation" "street_address"]
       :label     "Building"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "organisation" "street_address"]}]]

     [:div {:style {:display               "grid"
                    :grid-column-gap       "1em"
                    :grid-template-columns "1fr 1fr"}}

      [m4/form-group
       {:form-id   ?form-id
        :data-path [?data-path "organisation" "address_locality"]
        :label     "City"}
       [m4/input-field
        {:form-id   ?form-id
         :data-path [?data-path "organisation" "address_locality"]}]]

      [m4/form-group
       {:form-id   ?form-id
        :data-path [?data-path "organisation" "address_region"]
        :label     "State"}
       [m4/input-field
        {:form-id   ?form-id
         :data-path [?data-path "organisation" "address_region"]}]]]

     [:div {:style {:display               "grid"
                    :grid-column-gap       "1em"
                    :grid-template-columns "1fr 1fr"}}

      [m4/form-group
       {:form-id   ?form-id
        :data-path [?data-path "organisation" "postcode"]
        :label     "Postal Code"}
       [m4/input-field
        {:form-id   ?form-id
         :data-path [?data-path "organisation" "postcode"]}]]

      [m4/form-group
       {:form-id   ?form-id
        :data-path [?data-path "organisation" "country"]
        :label     "Country"}
       [m4/input-field
        {:form-id   ?form-id
         :data-path [?data-path "organisation" "country"]}]]]

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "organisation" "email"]
       :label     "Email address"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "organisation" "email"]}]]

     [:div {:style {:display               "grid"
                    :grid-column-gap       "1em"
                    :grid-template-columns "1fr 1fr"}}

      [m4/form-group
       {:form-id   ?form-id
        :data-path [?data-path "contact" "phone"]
        :label     "Phone"}
       [m4/input-field
        {:form-id   ?form-id
         :data-path [?data-path "contact" "phone"]}]]

      [m4/form-group
       {:form-id   ?form-id
        :data-path [?data-path "contact" "fax"]
        :label     "Fax"}
       [m4/input-field
        {:form-id   ?form-id
         :data-path [?data-path "contact" "fax"]}]]]]

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
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "full_address_line"]}]]

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "street_address"]
       :label     "Building"}
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
        :data-path [?data-path "email"]}]]

     [:div {:style {:display               "grid"
                    :grid-column-gap       "1em"
                    :grid-template-columns "1fr 1fr"}}

      [m4/form-group
       {:form-id   ?form-id
        :data-path [?data-path "phone"]
        :label     "Phone"}
       [m4/input-field
        {:form-id   ?form-id
         :data-path [?data-path "phone"]}]]

      [m4/form-group
       {:form-id   ?form-id
        :data-path [?data-path "fax"]
        :label     "Fax"}
       [m4/input-field
        {:form-id   ?form-id
         :data-path [?data-path "fax"]}]]]]

    :how
    [:div
     #_[m4/page-errors
        {:form-id    [:form]
         :data-path  []
         :data-paths [["resourceLineage" "processStep"]
                      ["dataQualityInfo" "methods"]
                      ["dataQualityInfo" "results"]]}]
     [:h2 "6. How"]

     [:p "This section is optional.  You can add method/s used for the collection of the data and provide the Data Quality description and the associated results"]

     [m4/expanding-control {:label "Data creation procedure details (Optional)"}

      [m4/form-group
       {:form-id    [:form]
        :data-path  ["resourceLineage" "statement"]
        :label      "Provide a brief summary of the source of the data and related collection and/or processing methods."
        :required   true
        :toolTip    "TODO"
        :helperText "e.g. Data was collected at the site using the meethod described in XXX Manual, refer to URL..."}
       [m4/textarea-field
        {:form-id   [:form]
         :data-path ["resourceLineage" "statement"]}]]

      [m4/form-group
       {:label    "Method documentation"
        :toolTip  "The method of production of the dataset. Provide the title and url of the method documentation."}
       [m4/selection-list-columns
        {:form-id            [:form]
         :data-path          ["resourceLineage" "onlineMethods"]
         :value-path         ["uri"]
         :random-uuid-value? true
         :select-snapshot?   true
         :added-path         ["isUserDefined"]
         :columns            [{:columnHeader "Title" :label-path ["title"] :flex 1}
                              {:columnHeader "URL" :label-path ["url"] :flex 1}]}]

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
         :title       "Method Document"
         :template-id :method-doc/user-defined-entry-form}]]]

     [m4/expanding-control {:label "Data creation procedure steps (Optional)"}

      ;; How6: Name
      #_[m4/form-group
       {:form-id    [:form]
        :data-path  ["resourceLineage" "processStatement"]
        :label      "Name"
        :toolTip    "TODO"
        :helperText "Provide the name of the method or procedure"}
       [m4/textarea-field
        {:form-id     [:form]
         :data-path   ["resourceLineage" "processStatement"]
         :placeholder "Provide the name of the method or procedure"}]]

      ;; How7: Description
      #_[m4/form-group
       {:form-id    [:form]
        :data-path  ["resourceLineage" "summary"]
        :label      "Description"
        :toolTip    "TODO"
        :helperText "Provide a brief description of the method"}
       [m4/textarea-field
        {:form-id     [:form]
         :data-path   ["resourceLineage" "summary"]
         :placeholder "Provide a brief summary of a single method or procedure"}]]

      ;; How7b: list-add free-text entries
      [m4/form-group
       {:label   "If the need arises please add steps taken for the Data creation procedure to support the brief provided above."
        :toolTip "Specify the steps of the procedure."}
       [m4/text-add-button
        {:form-id     [:form]
         :data-path   ["resourceLineage" "steps"]
         :button-text "Add"}]
       [m4/selection-list-values
        {:form-id   [:form]
         :data-path ["resourceLineage" "steps"]}]]]]

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

    :quality
    [:div
     #_[m4/page-errors
        {:form-id    [:form]
         :data-path  []
         :data-paths [["dataQualityInfo" "methodSummary"]
                      ["dataQualityInfo" "results"]]}]
     [:h2 "7. Data Quality"]
     [:i "This section is optional"]
     [m4/form-group
      {:form-id   [:form]
       :data-path ["dataQualityInfo" "methodSummary"]
       :label     "Provide a summary of the scope of the Data Quality Assessment"}
      [m4/textarea-field
       {:form-id     [:form]
        :data-path   ["dataQualityInfo" "methodSummary"]
        :maxLength   1000
        :placeholder "The data quality was assessed by ..."}]]

     [m4/form-group
      {:label    "Online data quality report"
       :toolTip  "Data quality report refers to a textual description of the quality control of the dataset. Provide the title and URL of the report, if available."
       :required true}
      [m4/selection-list-columns
       {:form-id            [:form]
        :data-path          ["dataQualityInfo" "onlineMethods"]
        :value-path         ["uri"]
        :random-uuid-value? true
        :select-snapshot?   true
        :added-path         ["isUserDefined"]
        :columns            [{:columnHeader "Title" :label-path ["title"] :flex 1}
                             {:columnHeader "URL" :label-path ["url"] :flex 1}]}]

      [m4/list-add-button
       {:form-id            [:form]
        :data-path          ["dataQualityInfo" "onlineMethods"]
        :button-text        "Add"
        :value-path         ["uri"]
        :random-uuid-value? true
        ;; :item-defaults {"userAddedCategory" "onlineMethods"}
        :added-path         ["isUserDefined"]}]

      [m4/list-edit-dialog
       {:form-id     [:form]
        :data-path   ["dataQualityInfo" "onlineMethods"]
        :value-path  ["uri"]
        :added-path  ["isUserDefined"]
        :title       "Online Quality Report"
        :template-id :quality/user-defined-entry-form}]]

     [m4/form-group
      {:form-id   [:form]
       :data-path ["dataQualityInfo" "results"]
       :label     "Provide a statement regarding the Data Quality Assessment outcome"}
      [m4/textarea-field
       {:form-id     [:form]
        :data-path   ["dataQualityInfo" "results"]
        :maxLength   1000
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
     #_[m4/page-errors
        {:form-id    [:form]
         :data-path  []
         :data-paths [["identificationInfo" "environment"]
                      ["identificationInfo" "supplemental"]
                      ["identificationInfo" "resourceSpecificUsage"]
                      ["identificationInfo" "credit"]
                      ["identificationInfo" "customCitation"]]}]
     [:h2 "8: About Dataset"]
     [:i "This section allows you to provide information of the dataset collection, and will inform the consumer with the legal obligations, limitations of use and any other relevant details such as resources and publications."]
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
       :label     "Other constraints"}
      [m4/simple-list
       {:form-id [:form]
        :data-path ["identificationInfo" "otherConstraints"]
        :template-id :about/other-constraints-list-entry}]]
     [m4/form-group
      {:label   "Any other constraints"
       :toolTip "Enter any additional constraints as required and click to add"}
      [m4/text-add-button
       {:form-id     [:form]
        :data-path   ["identificationInfo" "additionalConstraints" "constraints"]
        :button-text "Add"}]
      [m4/selection-list-values
       {:form-id   [:form]
        :data-path ["identificationInfo" "additionalConstraints" "constraints"]}]]

     [m4/form-group
      {:label   "Security Classification"
       :toolTip "Please select a relevant security classification for the dataset."}
      [m4/select-option-simple
       {:form-id    [:form]
        :data-path  ["identificationInfo" "securityClassification"]
        :label-path ["label"]
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
        :toolTip    "Description of the dataset in the producer's processing environment, including items such as the software, the computer operating system, file name, format, language and the dataset size."
        :helperText "Software, computer operating system, file name, or dataset size"}
       [m4/textarea-field
        {:form-id     [:form]
         :data-path   ["identificationInfo" "environment"]
         :placeholder "Information about the source and software to process the resource"
         :maxLength   1000}]]]

     [m4/expanding-control {:label "Associated Documentation (Optional)"}

      [m4/form-group
       {:label    "Publication"
        :toolTip  "Please provide the title and URL of the publications describing the dataset."}
       [m4/selection-list-columns
        {:form-id            [:form]
         :data-path          ["identificationInfo" "additionalPublications"]
         :value-path         ["uri"]
         :random-uuid-value? true
         :select-snapshot?   true
         :added-path         ["isUserDefined"]
         :columns            [{:columnHeader "Title" :label-path ["title"] :flex 1}
                              {:columnHeader "URL" :label-path ["url"] :flex 1}]}]

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
        :toolTip    "Miscellaneous information about the dataset, not captured elsewhere.  This is an optional field."
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
        :toolTip    "Please provide a brief description with regards to the usage of the resource and/or the resource series usage."
        :helperText "What can this resource be used for environmental research?"}
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
        :toolTip    "Please include statements to acknowledge various types of support that led to the creation of the dataset."
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
        :toolTip    "The system will generate a standard citation based on the metadata provided. Here you may indicate a specific citation for this dataset."
        :helperText "The format of the standard citation is provided at https://ternaus.atlassian.net/wiki/spaces/TERNSup/pages/1223163969/How+is+the+citation+constructed+from+the+metadata  For a non-standard citation, provide the details below."}
       [m4/textarea-field
        {:form-id   [:form]
         :data-path ["identificationInfo" "customCitation"]
         :maxLength 1000}]]]]

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
     [:i
      [m4/get-data
       {:form-id ?form-id
        :data-path ?data-path}]]]

    :upload
    [:div
     #_[m4/page-errors {:form-id [:form] :data-paths []}]
     [:h2 "9. Data Sources"]
     [m4/upload-files
      {:form-id     [:form]
       :data-path   ["attachments"]
       :value-path  ["id"]
       :placeholder [:div
                     [:h3 "Drop file here or click here to upload"]
                     [:span.help-block "Maximum file size 100 MB"]]}]

     [:h3 "Thumbnail"]

     [m4/form-group
      {:form-id   [:form]
       :data-path ["identificationInfo" "thumbnail" "title"]
       :label     "Title"
       :toolTip   "Provide the title of the file uploaded."}
      [m4/input-field
       {:form-id   [:form]
        :data-path ["identificationInfo" "thumbnail" "title"]}]]
     [m4/upload-thumbnail
      {:form-id     [:form]
       :data-path   ["identificationInfo" "thumbnail" "file"]
       :value-path  ["id"]
       :placeholder [:div
                     [:h3 "Drag and drop thumbnail here"]
                     [:span.help-block "Maximum size 100 x 100px"]]}]

     [:h2 "Data Services"]
     [m4/form-group
      {:label    "Distributions"}
      [m4/selection-list-columns
       {:form-id            [:form]
        :data-path          ["dataSources"]
        :value-path         ["uri"]
        :random-uuid-value? true
        :select-snapshot?   true
        :added-path         ["isUserDefined"]
        :columns            [{:columnHeader "Protocol" :label-path ["transferOptions" "protocol"] :flex 1}
                             {:columnHeader "Server" :label-path ["transferOptions" "linkage"] :flex 1}
                             {:columnHeader "Name" :label-path ["transferOptions" "name"] :flex 1}]}]

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

    :data-sources/user-defined-entry-form
    [:div
     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "transferOptions" "description"]
       :label     "Description"}
      [m4/textarea-field
       {:form-id   ?form-id
        :data-path [?data-path "transferOptions" "description"]}]]

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "transferOptions" "protocol"]
       :label     "Protocol"}
      [m4/select-value
       {:form-id    ?form-id
        :data-path  [?data-path "transferOptions" "protocol"]
        :label-path ["label"]
        :value-path ["uri"]
        :options    [{"label" "WFS" "value" "WFS"}
                     {"label" "WMS" "value" "WMS"}
                     {"label" "WCS" "value" "WCS"}
                     {"label" "OpenDAP" "value" "OpenDAP"}]}]]

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "transferOptions" "linkage"]
       :label     "URL"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "transferOptions" "linkage"]}]]

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "transferOptions" "name"]
       :label     "Layer Name"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "transferOptions" "name"]}]]

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path]
       :label     "Distributor"
       :toolTip   "TERN is selected by default. You may select a different distributor for the dataset, if necessary."}
      [m4/async-select-option-simple
       {:form-id     ?form-id
        :data-path   [?data-path "distributor"]
        :uri         "/api/ternorgs"
        :label-path  ["name"]
        :value-path  ["uri"]
        :placeholder "Search for organisation details"}]]]

    :lodge
    [:div
     #_[m4/page-errors {:form-id [:form] :data-paths []}]
     [:h2 "10: Lodge Metadata Draft"]
     #_[m3/Lodge
        {:note-for-data-manager-path [:form :fields :noteForDataManager]
         :agreed-to-terms-path       [:form :fields :agreedToTerms]
         :doi-requested-path         [:form :fields :doiRequested]
         :current-doi-path           [:form :fields :identificationInfo :doi]}]]})

(set! low-code4/template-registry
      (merge edit-templates
             {::components4/create-document-modal-form
              components4/create-document-modal-template}))
