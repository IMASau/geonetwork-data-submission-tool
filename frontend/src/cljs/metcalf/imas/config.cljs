(ns ^:dev/always metcalf.imas.config
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
            [metcalf.imas.components :as imas-components]
            [metcalf.imas.handlers :as imas-handlers]
            [metcalf.imas.subs :as imas-subs]
            [re-frame.core :as rf]))

#_(rf/reg-event-fx :app/upload-data-confirm-upload-click-add-attachment handlers3/add-attachment)
(rf/reg-event-fx ::components4/boxes-changed handlers4/boxes-changed)
(rf/reg-event-fx ::components4/boxmap-coordinates-click-confirm-delete handlers4/boxmap-coordinates-click-confirm-delete)
(rf/reg-event-fx ::components4/boxmap-coordinates-list-delete handlers4/boxmap-coordinates-list-delete)
(rf/reg-event-fx ::components4/boxmap-coordinates-open-add-modal handlers4/boxmap-coordinates-open-add-modal)
(rf/reg-event-fx ::components4/boxmap-coordinates-open-edit-modal handlers4/boxmap-coordinates-open-edit-modal)
(rf/reg-event-fx ::components4/create-document-modal-clear-click handlers4/create-document-modal-clear-click)
(rf/reg-event-fx ::components4/create-document-modal-close-click handlers4/create-document-modal-close-click)
(rf/reg-event-fx ::components4/create-document-modal-save-click handlers4/create-document-modal-save-click)
(rf/reg-event-fx ::components4/item-add-button-click handlers4/item-add-with-defaults-click-handler2)
(rf/reg-event-fx ::components4/item-dialog-button-add-click handlers4/item-add-with-defaults-click-handler2)
(rf/reg-event-fx ::components4/item-edit-with-defaults-click-handler handlers4/item-edit-click-handler)
(rf/reg-event-fx ::components4/item-dialog-button-edit-click handlers4/item-edit-click-handler)
(rf/reg-event-fx ::components4/edit-dialog-cancel handlers4/edit-dialog-cancel-handler)
(rf/reg-event-fx ::components4/edit-dialog-close handlers4/edit-dialog-close-handler)
(rf/reg-event-fx ::components4/edit-dialog-save handlers4/edit-dialog-save-handler)
(rf/reg-event-fx ::components4/item-option-picker-change handlers4/item-option-picker-change)
(rf/reg-event-fx ::components4/list-add-with-defaults-click-handler3 handlers4/list-add-with-defaults-click-handler3)
(rf/reg-event-fx ::components4/value-list-add-with-defaults-click-handler handlers4/value-list-add-with-defaults-click-handler2)
(rf/reg-event-fx ::components4/list-edit-dialog-cancel handlers4/list-edit-dialog-cancel-handler)
(rf/reg-event-fx ::components4/list-edit-dialog-close handlers4/list-edit-dialog-cancel-handler)
(rf/reg-event-fx ::components4/list-edit-dialog-save handlers4/list-edit-dialog-save-handler)
(rf/reg-event-fx ::components4/list-option-picker-change handlers4/list-option-picker-change)
(rf/reg-event-fx ::components4/option-change handlers4/option-change-handler)
(rf/reg-event-fx ::components4/add-record handlers4/add-record-handler)
(rf/reg-event-fx ::components4/selection-list-item-click handlers4/selection-list-item-click3)
(rf/reg-event-fx ::components4/selection-list-remove-click handlers4/selection-list-remove-click)
(rf/reg-event-fx ::components4/selection-list-reorder handlers4/selection-list-reorder)
(rf/reg-event-fx ::components4/text-value-add-click-handler handlers4/text-value-add-click-handler)
(rf/reg-event-fx ::components4/value-changed handlers4/value-changed-handler)
(rf/reg-event-fx ::components4/input-blur handlers4/input-blur-handler)
(rf/reg-event-fx ::components4/selection-list-values-remove-click handlers4/selection-list-remove-click)
(rf/reg-event-fx ::components4/selection-list-values-reorder handlers4/selection-list-reorder)
(rf/reg-event-fx ::components4/selection-list-values-item-click handlers4/selection-list-values-item-click)
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
(rf/reg-event-fx :app/PageViewEdit-save-button-click handlers4/save-current-document)
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
(rf/reg-event-fx :app/upload-data-file-upload-failed handlers3/upload-data-file-upload-failed)
(rf/reg-event-fx :app/upload-max-filesize-exceeded handlers3/upload-max-filesize-exceeded)
(rf/reg-event-fx :metcalf.common.actions4/-create-document handlers4/-create-document-handler)
(rf/reg-event-fx :metcalf.common.actions4/-get-document-data-action handlers4/-get-document-data-action)
(rf/reg-event-fx :metcalf.common.components4/coordinates-modal-field-close-modal handlers4/coordinates-modal-field-close-modal)
(rf/reg-event-fx ::imas-components/lodge-button-click handlers3/lodge-click)
(rf/reg-event-fx :metcalf.common.handlers4/-contributors-modal-share-resolve handlers4/-contributors-modal-share-resolve)
(rf/reg-event-fx :metcalf.common.handlers4/-contributors-modal-unshare-resolve handlers4/-contributors-modal-unshare-resolve)
(rf/reg-event-fx :metcalf.imas.core/init-db imas-handlers/init-db)
(rf/reg-event-fx ::components4/upload-files-drop handlers4/upload-files-drop)
(rf/reg-event-fx :metcalf.common.actions4/-upload-attachment handlers4/-upload-attachment)
(rf/reg-fx ::fx3/post fx3/post)
(rf/reg-fx ::fx3/post-json-data fx3/post-json-data)
(rf/reg-fx ::fx3/set-location-href fx3/set-location-href)
(rf/reg-fx ::low-code4/init! low-code4/init!)
(rf/reg-fx :app/get-json-fx (utils4/promise-fx utils4/get-json))
(rf/reg-fx :app/post-data-fx (utils4/promise-fx utils4/post-json))
(rf/reg-fx :app/post-multipart-form (utils4/promise-fx utils4/post-multipart-form))
(rf/reg-fx :ui/setup-blueprint ui-controls/setup-blueprint)
(rf/reg-sub ::components4/create-document-modal-can-save? subs4/create-document-modal-can-save?)
(rf/reg-sub ::components4/get-block-data subs4/form-state-signal subs4/get-block-data-sub)
(rf/reg-sub ::components4/get-block-props subs4/form-state-signal subs4/get-block-props-sub)
(rf/reg-sub ::components4/is-item-added? subs4/form-state-signal subs4/is-item-added?)
(rf/reg-sub ::components4/get-list-edit-can-save-sub subs4/form-state-signal subs4/get-list-edit-can-save-sub)
(rf/reg-sub ::components4/has-block-errors? subs4/form-state-signal subs4/has-block-errors?)
(rf/reg-sub ::components4/has-selected-block-errors? subs4/form-state-signal subs4/has-selected-block-errors?)
(rf/reg-sub ::components4/can-dialog-cancel? subs4/can-dialog-cancel-sub)
(rf/reg-sub ::components4/get-yes-no-field-props subs4/form-state-signal subs4/get-block-props-sub)
(rf/reg-sub ::components4/get-page-errors-props subs4/form-state-signal subs4/get-page-errors-props-sub)
(rf/reg-sub ::low-code4/get-data-schema subs4/get-data-schema-sub)
(rf/reg-sub ::subs4/get-form-state subs4/get-form-state)
(rf/reg-sub :app/contributors-modal-props subs4/contributors-modal-props)
(rf/reg-sub :app/get-dashboard-props subs3/get-dashboard-props)
(rf/reg-sub :app/get-progress-bar-props :<- [::subs4/get-form-state [:form]] subs3/get-progress-props)
(rf/reg-sub :subs/get-app-root-modal-props subs4/get-modal-props)
(rf/reg-sub :subs/get-app-root-page-name subs4/get-page-name)
(rf/reg-sub :subs/get-attachments subs3/get-attachments)
(rf/reg-sub :subs/get-context subs3/get-context)
(rf/reg-sub :subs/get-edit-tab-props :<- [:subs/get-page-props] :<- [::subs4/get-form-state [:form]] imas-subs/get-edit-tab-props)
(rf/reg-sub :subs/get-form subs3/get-form)
(rf/reg-sub :subs/get-form-dirty subs4/get-form-dirty?)
(rf/reg-sub :subs/get-form-disabled? subs3/get-form-disabled?)
(rf/reg-sub :subs/get-page-props subs3/get-page-props)
(rf/reg-sub :subs/get-progress subs3/get-progress)
(rf/reg-sub :subs/get-upload-form subs3/get-upload-form)
(ins4/reg-global-singleton ins4/form-ticker)
(ins4/reg-global-singleton ins4/breadcrumbs)
(ins4/reg-global-singleton (ins4/slow-handler 100))
;(when goog/DEBUG (ins4/reg-global-singleton ins4/db-diff))
;(when goog/DEBUG (ins4/reg-global-singleton (ins4/check-and-throw ::tern-db/db)))
(set! rules4/rule-registry
      {"requiredField"           rules4/required-field
       "requiredWhenYes"         rules4/required-when-yes
       "otherConstraintsLogic"   rules4/other-constraints-logic
       "maxLength"               rules4/max-length
       "maxKeywords"             rules4/tern-max-keywords
       "stringConcat"            rules4/string-concat
       "geographyRequired"       rules4/imas-geography-required
       "imasVerticalRequired"    rules4/imas-vertical-required
       "imasTransferOptionLayer" rules4/imas-transfer-option-layer
       "licenseOther"            rules4/license-other
       "numericOrder"            rules4/numeric-order
       "dateOrder"               rules4/date-order
       "endPosition"             rules4/end-position
       "positive"                rules4/force-positive
       "maintFreq"               rules4/maint-freq
       "firstCommaLast"          rules4/first-comma-last
       "validOrcid"              rules4/valid-ordid-uri})

; Specs intended for use with when-data :pred
(s/def :m4/empty-list? empty?)
(s/def :m4/not-set? (s/or :n nil? :s (s/and string? string/blank?)))

(set! low-code4/component-registry
      {
       ;'m3/UploadData                     {:view #'views3/UploadData}
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
       ;'m4/date-field                          {:view #'components4/date-field :init components4/date-field-settings}
       'm4/date-field2                         {:view #'components4/date-field2 :init components4/date-field2-settings}
       'm4/coordinates-modal-field             {:view #'components4/coordinates-modal-field :init components4/coordinates-modal-field-settings}
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
       'm4/lodge-button                        {:view #'imas-components/lodge-button}
       'm4/lodge-status-info                   {:view #'imas-components/lodge-status-info}
       'm4/mailto-data-manager-link            {:view #'imas-components/mailto-data-manager-link}
       'm4/note-for-data-manager               {:view #'imas-components/note-for-data-manager :init imas-components/note-for-data-manager-settings}
       'm4/numeric-input-field                 {:view #'components4/numeric-input-field :init components4/numeric-input-field-settings}
       'm4/page-errors                         {:view #'components4/page-errors :init components4/page-errors-settings}
       'm4/portal-link                         {:view #'imas-components/portal-link}
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
       'm4/yes-no-field                        {:view #'components4/yes-no-field :init components4/yes-no-field-settings}
       'm4/xml-export-link                     {:view #'components4/xml-export-link :init components4/xml-export-link-settings}
       'm4/async-simple-item-option-picker     {:view #'components4/async-simple-item-option-picker :init components4/async-simple-item-option-picker-settings}
       ;'m4/record-add-button                   {:view #'components4/record-add-button :init components4/record-add-button-settings}
       'm4/text-add-button                     {:view #'components4/text-add-button :init components4/text-add-button-settings}
       'm4/upload-files                        {:view #'components4/imas-upload-files :init components4/imas-upload-files-settings}
       ;'m4/simple-list                         {:view #'components4/simple-list :init components4/simple-list-settings}
       })

(def edit-templates
  '{:data-identification
    [:div
     ; [m4/page-errors
     ;  {:form-id    [:form]
     ;   :data-path  []
     ;   :data-paths [["identificationInfo" "title"]
     ;                ["identificationInfo" "dateCreation"]
     ;                ["identificationInfo" "topicCategory"]
     ;                ["identificationInfo" "status"]
     ;                ["identificationInfo" "maintenanceAndUpdateFrequency"]]}]
     [:h2 "1. Data Identification"]
     [m4/form-group
      {:form-id    [:form]
       :data-path  ["identificationInfo" "title"]
       :helperText "Clear and concise description of the content of the resource"}
      [m4/input-field
       {:form-id   [:form]
        :data-path ["identificationInfo" "title"]}]]
     [m4/form-group
      {:form-id   [:form]
       :data-path ["identificationInfo" "dateCreation"]
       :label     "Date of record creation"}
      [m4/date-field2
       {:form-id   [:form]
        :data-path ["identificationInfo" "dateCreation"]}]]
     [m4/form-group
      {:form-id   [:form]
       :data-path ["identificationInfo" "topicCategories"]
       :label     "Topic Categories"}
      [m4/simple-list-option-picker
       {:form-id     [:form]
        :data-path   ["identificationInfo" "topicCategories"]
        :value-path  ["value"]
        :label-path  ["label"]
        :placeholder "Please select"
        :options     [{"value" "biota"
                       "label" "biota"}
                      {"value" "climatologyMeteorologyAtmosphere"
                       "label" "climatology/meteorology/atmosphere"}
                      {"value" "oceans"
                       "label" "oceans"}
                      {"value" "geoscientificInformation"
                       "label" "geoscientificInformation"}
                      {"value" "inlandWater"
                       "label" "inlandWater"}]}]
      [m4/selection-list-simple
       {:form-id    [:form]
        :data-path  ["identificationInfo" "topicCategories"]
        :value-path ["value"]
        :label-path ["label"]}]]

     [m4/form-group
      {:form-id   [:form]
       :data-path ["identificationInfo" "status"]
       :label     "Status"}
      [m4/select-value
       {:form-id    [:form]
        :data-path  ["identificationInfo" "status"]
        :value-path ["value"]
        :label-path ["label"]
        :options    [{"value" "onGoing" "label" "ongoing"}
                     {"value" "planned" "label" "planned"}
                     {"value" "completed" "label" "completed"}]}]]
     [m4/form-group
      {:form-id   [:form]
       :data-path ["identificationInfo" "maintenanceAndUpdateFrequency"]}
      [m4/select-value
       {:form-id    [:form]
        :data-path  ["identificationInfo" "maintenanceAndUpdateFrequency"]
        :value-path ["value"]
        :label-path ["label"]
        :options    [{"value" "continually" "label" "Continually"}
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
                     {"value" "biennially" "label" "Every 2 years"}]}]]]

    :what
    [:div
     ; [m4/page-errors
     ;  {:form-id    [:form]
     ;   :data-path  []
     ;   :data-paths [["identificationInfo" "abstract"]]}]
     [:h2 "2. What"]
     [m4/form-group
      {:form-id    [:form]
       :data-path  ["identificationInfo" "abstract"]
       :helperText "Describe the content of the resource; e.g. what information was collected, how was it collected, brief statement about the purpose of the study"}
      [m4/textarea-field
       {:form-id     [:form]
        :data-path   ["identificationInfo" "abstract"]
        :placeholder nil
        :rows        3}]]

     [m4/form-group
      {:form-id   [:form]
       :data-path ["identificationInfo" "keywordsTheme" "keywords"]
       :label     "Research theme keywords"}

      [:div "Select up to 12 research theme keywords describing your data"]

      [m4/selection-list-breadcrumb
       {:form-id         [:form]
        :data-path       ["identificationInfo" "keywordsTheme" "keywords"]
        :label-path      ["label"]
        :value-path      ["uri"]
        :breadcrumb-path ["breadcrumb"]}]

      [m4/async-list-option-picker-breadcrumb
       {:form-id         [:form]
        :data-path       ["identificationInfo" "keywordsTheme" "keywords"]
        :uri             "/api/keywords_with_breadcrumb_info"
        :placeholder     "Search for keywords"
        :label-path      ["label"]
        :value-path      ["uri"]
        :breadcrumb-path ["breadcrumb"]}]]

     [m4/form-group
      {:form-id   [:form]
       :data-path ["identificationInfo" "keywordsGeoExtent" "keywords"]
       :label     "Geographic Extent keywords"}
      [m4/selection-list-breadcrumb
       {:form-id         [:form]
        :data-path       ["identificationInfo" "keywordsGeoExtent" "keywords"]
        :label-path      ["label"]
        :value-path      ["uri"]
        :breadcrumb-path ["breadcrumb"]}]
      [m4/async-list-option-picker-breadcrumb
       {:form-id         [:form]
        :data-path       ["identificationInfo" "keywordsGeoExtent" "keywords"]
        :uri             "/api/geoextents_with_breadcrumb_info"
        :placeholder     "Search for keywords"
        :label-path      ["label"]
        :value-path      ["uri"]
        :breadcrumb-path ["breadcrumb"]}]]

     [m4/form-group
      {:form-id   [:form]
       :data-path ["identificationInfo" "keywordsThemeExtra" "keywords"]
       :label     "Additional theme keywords"}
      [:div "Enter your own additional theme keywords as required and click + to add"]
      [m4/selection-list-values
       {:form-id   [:form]
        :data-path ["identificationInfo" "keywordsThemeExtra" "keywords"]}]
      [m4/text-add-button
       {:form-id     [:form]
        :data-path   ["identificationInfo" "keywordsThemeExtra" "keywords"]
        :button-text "Add"}]]

     [m4/form-group
      {:form-id   [:form]
       :data-path ["identificationInfo" "keywordsTaxonExtra" "keywords"]
       :label     "Taxon keywords"}
      [:div "Add any taxon names describing your data and click + to add"]
      [m4/selection-list-values
       {:form-id   [:form]
        :data-path ["identificationInfo" "keywordsTaxonExtra" "keywords"]}]
      [m4/text-add-button
       {:form-id     [:form]
        :data-path   ["identificationInfo" "keywordsTaxonExtra" "keywords"]
        :button-text "Add"}]]]

    :when
    [:div
     ; [m4/page-errors
     ;  {:form-id    [:form]
     ;   :data-path  []
     ;   :data-paths [["identificationInfo" "beginPosition"]
     ;                ["identificationInfo" "endPosition"]
     ;                ["identificationInfo" "samplingFrequency"]]}]
     [:h2 "3. When was the data acquired?"]
     [m4/form-group
      {:form-id   [:form]
       :data-path ["identificationInfo" "extents" "beginPosition"]
       :label     "Start date"}
      [m4/date-field2
       {:form-id   [:form]
        :data-path ["identificationInfo" "extents" "beginPosition"]}]]
     [m4/form-group
      {:form-id   [:form]
       :data-path ["identificationInfo" "extents" "endPosition"]
       :label     "End date"}
      [m4/date-field2
       {:form-id   [:form]
        :data-path ["identificationInfo" "extents" "endPosition"]}]]]

    :where
    [:div
     ; [m4/page-errors
     ;  {:form-id    [:form]
     ;   :data-path  []
     ;   :data-paths [["identificationInfo" "geographicElement" "boxes"]
     ;                ["identificationInfo" "verticalElement" "minimumValue"]
     ;                ["identificationInfo" "verticalElement" "maximumValue"]]}]
     [:h2 "4. Where"]
     [:h3 "Geographic Coverage"]
     [m4/checkbox-field
      {:form-id   [:form]
       :data-path ["identificationInfo" "extents" "hasGeographicCoverage"]
       :label     "Does data have a geographic coverage?"}]
     [:div.row
      [:div.col-sm-6
       ;; FIXME add toggle for satellite imagery.
       [m4/boxmap-field
        {:form-id    [:form]
         :data-path  ["identificationInfo" "extents" "geographicElement"]
         :value-path ["uri"]
         :added-path ["isUserDefined"]}]]
      [:div.col-sm-6

       [m4/when-data
        {:form-id   [:form]
         :data-path ["identificationInfo" "extents" "geographicElement"]
         :pred      :m4/empty-list?}
        [:p "Specify the location(s) of this study."]]

       [:div.SelectionTableStyle
        [m4/selection-list-columns
         {:form-id             [:form]
          :data-path           ["identificationInfo" "extents" "geographicElement"]
          :value-path          ["uri"]
          :added-path          ["isUserDefined"]
          :columns             [{:columnHeader "North" :label-path ["northBoundLatitude"] :flex 1}
                                {:columnHeader "East" :label-path ["eastBoundLongitude"] :flex 1}
                                {:columnHeader "South" :label-path ["southBoundLatitude"] :flex 1}
                                {:columnHeader "West" :label-path ["westBoundLongitude"] :flex 1}]
          :placeholder-record? true
          :select-snapshot?    true
          :random-uuid-value?  true}]
        [m4/list-add-button
         {:form-id            [:form]
          :data-path          ["identificationInfo" "extents" "geographicElement"]
          :button-text        "Add location"
          :value-path         ["uri"]
          :random-uuid-value? true
          :added-path         ["isUserDefined"]}]]

       [m4/list-edit-dialog
        {:form-id     [:form]
         :data-path   ["identificationInfo" "extents" "geographicElement"]
         :title       "Bounding box"
         :template-id :box/user-defined-entry-form}]]]
     [:h3 "Vertical Coverage"]
     [m4/checkbox-field
      {:form-id   [:form]
       :data-path ["identificationInfo" "extents" "hasVerticalExtent"]
       :label     "Does data have a vertical coverage?"}]
     [m4/form-group
      {:form-id   [:form]
       :data-path ["identificationInfo" "extents" "verticalElement" "verticalCRS"]
       :label     "Vertical type"}
      [m4/select-option-simple
       {:form-id     [:form]
        :data-path   ["identificationInfo" "extents" "verticalElement" "verticalCRS"]
        :value-path  ["identifier"]
        :label-path  ["label"]
        :placeholder "Please select"
        :options     [{"label"      "Depth (distance below mean sea level)"
                       "name"       "MSL depth"
                       "identifier" "EPSG::5715"}
                      {"label"      "Altitude (height above mean sea level)"
                       "name"       "MSL height"
                       "identifier" "EPSG::5714"}]}]]
     [m4/form-group
      {:form-id    [:form]
       :data-path  ["identificationInfo" "extents" "verticalElement" "minimumValue"]
       :label      "Minimum"
       :helperText "Shallowest depth / lowest altitude"}
      [m4/numeric-input-field
       {:form-id   [:form]
        :data-path ["identificationInfo" "extents" "verticalElement" "minimumValue"]
        :class     "wauto"}]]
     [m4/form-group
      {:form-id    [:form]
       :data-path  ["identificationInfo" "extents" "verticalElement" "maximumValue"]
       :label      "Maximum"
       :helperText "Deepest depth / highest altitude"}
      [m4/numeric-input-field
       {:form-id   [:form]
        :data-path ["identificationInfo" "extents" "verticalElement" "maximumValue"]
        :class     "wauto"}]]]

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

    :how
    [:div
     ; [m4/page-errors
     ;  {:form-id    [:form]
     ;   :data-path  []
     ;   :data-paths [["resourceLineage" "lineage"]]}]
     ; [:h2 "5: How"]
     [m4/form-group
      {:form-id    [:form]
       :data-path  ["resourceLineage" "statement"]
       :label      "Methodological information"
       :helperText "Provide a brief statement of the methods used for collection of the
                     data, can include information regarding sampling equipment (collection hardware),
                     procedures, and precision/resolution of data collected."}
      [m4/textarea-field
       {:form-id   [:form]
        :data-path ["resourceLineage" "statement"]
        :rows      6}]]]

    :who
    [:div
     ; [m4/page-errors
     ;  {:form-id    [:form]
     ;   :data-path  []
     ;   :data-paths [["identificationInfo" "citedResponsibleParty"]
     ;                ["identificationInfo" "pointOfContact"]]}]
     [:h2 "6. Who"]
     [m4/form-group
      {:form-id   [:form]
       :data-path ["pointOfContact"]
       :label     "Point of contact for dataset"}

      [:div.SelectionTableStyle
       [m4/selection-list-columns
        {:form-id             [:form]
         :data-path           ["pointOfContact"]
         :columns             [{:flex 1 :label-path ["contact" "name"] :columnHeader "Contact name"}
                               {:flex 2 :label-path ["organisation" "name"] :columnHeader "Organisation"}
                               {:flex 1 :label-path ["role"] :columnHeader "Role"}]
         :placeholder-record? true
         :value-path          ["uri"]
         :added-path          ["isUserDefined"]
         :select-snapshot?    true
         :random-uuid-value?  true}]]

      [m4/list-add-button
       {:form-id            [:form]
        :data-path          ["pointOfContact"]
        :button-text        "Add person"
        :value-path         ["uri"]
        :random-uuid-value? true
        :added-path         ["isUserDefined"]}]

      [m4/list-edit-dialog
       {:form-id     [:form]
        :data-path   ["pointOfContact"]
        :title       "Person"
        :template-id :party-person/user-defined-entry-form}]]

     [m4/form-group
      {:form-id   [:form]
       :data-path ["identificationInfo" "citedResponsibleParty"]
       :label     "Responsible parties for creating dataset"}

      [:div.SelectionTableStyle

       [m4/selection-list-columns
        {:form-id             [:form]
         :data-path           ["identificationInfo" "citedResponsibleParty"]
         :columns             [{:flex 1 :label-path ["contact" "name"] :columnHeader "Contact name"}
                               {:flex 2 :label-path ["organisation" "name"] :columnHeader "Organisation"}
                               {:flex 1 :label-path ["role"] :columnHeader "Role"}]
         :placeholder-record? true
         :value-path          ["uri"]
         :added-path          ["isUserDefined"]
         :select-snapshot?    true
         :random-uuid-value?  true}]]

      [m4/list-add-button
       {:form-id            [:form]
        :data-path          ["identificationInfo" "citedResponsibleParty"]
        :button-text        "Add person"
        :value-path         ["uri"]
        :random-uuid-value? true
        :added-path         ["isUserDefined"]}]

      [m4/list-edit-dialog
       {:form-id     [:form]
        :data-path   ["identificationInfo" "citedResponsibleParty"]
        :title       "Person"
        :template-id :party-person/user-defined-entry-form}]]

     [:h3 "Other credits"]
     [:div "Acknowledge the contribution of any funding schemes or organisations."]
     [m4/selection-list-values
      {:form-id   [:form]
       :data-path ["identificationInfo" "credits"]}]

     [m4/value-list-add-button
      {:form-id     [:form]
       :data-path   ["identificationInfo" "credits"]
       :button-text "Add credit"}]

     [m4/list-edit-dialog
      {:form-id     [:form]
       :data-path   ["identificationInfo" "credits"]
       :title       "Credit"
       :template-id :credit/edit-form}]]

    :credit/edit-form
    [:div
     [m4/form-group
      {:form-id   ?form-id
       :data-path ?data-path
       :label     "Credit"}
      [m4/textarea-field
       {:form-id   ?form-id
        :data-path ?data-path
        :rows      5}]]]

    :party-person/user-defined-entry-form
    [:div

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "role"]
       :label     "Role"}
      [m4/select-value
       {:form-id    ?form-id
        :data-path  [?data-path "role"]
        :label-path ["label"]
        :value-path ["value"]
        :options    [{"value" "author" "label" "Author"}
                     {"value" "custodian" "label" "Custodian"}
                     {"value" "distributor" "label" "Distributor"}
                     {"value" "originator" "label" "Originator"}
                     {"value" "owner" "label" "Owner"}
                     {"value" "pointOfContact" "label" "Point of contact"}
                     {"value" "principalInvestigator" "label" "Principal investigator"}
                     {"value" "processor" "label" "Processor"}
                     {"value" "publisher" "label" "Publisher"}
                     {"value" "resourceProvider" "label" "Resource Provider"}
                     {"value" "user" "label" "User"}
                     ;{"value" "coAuthor" "label" "Co-Author"}
                     ;{"value" "collaborator" "label" "Collaborator"}
                     ;{"value" "contributor" "label" "Contributor"}
                     ;{"value" "editor" "label" "Editor"}
                     ;{"value" "funder" "label" "Funder"}
                     ;{"value" "mediator" "label" "Mediator"}
                     ;{"value" "rightsHolder" "label" "Rights Holder"}
                     ;{"value" "sponsor" "label" "Sponsor"}
                     ;{"value" "stakeholder" "label" "Stakeholder"}
                     ]}]]

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "contact" "name"]
       :label     "Contact name"}
      [m4/input-field
       {:form-id     ?form-id
        :data-path   [?data-path "contact" "name"]
        :placeholder "Last name, First name"}]]

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "contact" "orcid2"]
       :label     "ORCID ID"}
      [m4/input-field
       {:form-id     ?form-id
        :data-path   [?data-path "contact" "orcid2"]
        :placeholder "https://orcid.org/XXXX-XXXX-XXXX-XXXX"}]]

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "organisation"]
       :label     "Organisation"}
      [m4/async-simple-item-option-picker
       {:form-id     ?form-id
        :data-path   ?data-path
        :data-mapper [{:get-path ["prefLabel"] :set-path ["organisation" "name"]}
                      {:get-path ["deliveryPoint"] :set-path ["contact" "deliveryPoint"]}
                      {:get-path ["deliveryPoint2"] :set-path ["contact" "deliveryPoint2"]}
                      {:get-path ["city"] :set-path ["contact" "city"]}
                      {:get-path ["administrativeArea"] :set-path ["contact" "administrativeArea"]}
                      {:get-path ["postalCode"] :set-path ["contact" "postalCode"]}
                      {:get-path ["country"] :set-path ["contact" "country"]}]
        :uri         "/api/institution/"
        :label-path  ["prefLabel"]
        :value-path  ["uri"]
        :placeholder "Search for an organisation"}]]

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "organisation" "name"]
       :label     "Organisation Name"}
      [m4/textarea-field
       {:form-id   ?form-id
        :data-path [?data-path "organisation" "name"]
        :rows      2}]]

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "contact" "deliveryPoint"]
       :label     "Postal address"}
      [m4/input-field
       {:form-id     ?form-id
        :data-path   [?data-path "contact" "deliveryPoint"]
        :placeholder "Street address"}]]

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "contact" "deliveryPoint2"]}
      [m4/input-field
       {:form-id     ?form-id
        :data-path   [?data-path "contact" "deliveryPoint2"]
        :placeholder ""}]]

     [m4/inline-form-group
      {}
      [:div {:style {:display               "grid"
                     :grid-column-gap       "1em"
                     :grid-template-columns "1fr 1fr"}}
       [m4/form-group
        {:form-id   ?form-id
         :data-path [?data-path "contact" "city"]}
        [m4/input-field
         {:form-id     ?form-id
          :data-path   [?data-path "contact" "city"]
          :placeholder "City"}]]
       [m4/form-group
        {:form-id   ?form-id
         :data-path [?data-path "contact" "administrativeArea"]}
        [m4/input-field
         {:form-id     ?form-id
          :data-path   [?data-path "contact" "administrativeArea"]
          :placeholder "State / territory"}]]
       [m4/form-group
        {:form-id   ?form-id
         :data-path [?data-path "contact" "postalCode"]}
        [m4/input-field
         {:form-id     ?form-id
          :data-path   [?data-path "contact" "postalCode"]
          :placeholder "Post code"}]]
       [m4/form-group
        {:form-id   ?form-id
         :data-path [?data-path "contact" "country"]}
        [m4/input-field
         {:form-id     ?form-id
          :data-path   [?data-path "contact" "country"]
          :placeholder "Country"}]]]]

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "contact" "email"]
       :label     "Email address"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "contact" "email"]}]]

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "contact" "phone"]
       :label     "Phone"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "contact" "phone"]}]]]

    :about
    [:div
     ; [m4/page-errors
     ;  {:form-id    [:form]
     ;   :data-path  []
     ;   :data-paths [["identificationInfo" "dataParameters"]
     ;                ["identificationInfo" "creativeCommons"]
     ;                ["identificationInfo" "otherConstraints"]
     ;                ["identificationInfo" "useLimitations"]
     ;                ["identificationInfo" "supplementalInformation"]
     ;                ["supportingResources"]
     ;                ["distributionInfo" "distributionFormat" "name"]
     ;                ["distributionInfo" "distributionFormat" "version"]]}]
     [:h2 "7: About Dataset"]
     [:h4 "Data parameters"]
     [m4/form-group
      {:form-id   [:form]
       :data-path ["identificationInfo" "dataParameters"]}
      [:div.SelectionTableStyle

       [m4/selection-list-columns
        {:form-id             [:form]
         :data-path           ["identificationInfo" "dataParameters"]
         :value-path          ["uri"]
         :added-path          ["isUserDefined"]
         :random-uuid-value?  true
         :placeholder-record? true
         :select-snapshot?    true
         :columns             [{:columnHeader "Name" :flex 1 :label-path ["longName_term" "Name"]}
                               {:columnHeader "Units" :flex 1 :label-path ["unit_term" "Name"]}
                               {:columnHeader "Instrument" :flex 1 :label-path ["instrument_term" "Name"]}
                               {:columnHeader "Platform" :flex 1 :label-path ["platform_term" "Name"]}]}]
       [m4/list-add-button
        {:form-id            [:form]
         :data-path          ["identificationInfo" "dataParameters"]
         :button-text        "Add data parameter"
         :value-path         ["uri"]
         :random-uuid-value? true
         :added-path         ["isUserDefined"]}]]]

     [m4/list-edit-dialog
      {:form-id     [:form]
       :data-path   ["identificationInfo" "dataParameters"]
       :title       "Data parameter"
       :template-id :data-parameter/user-defined-entry-form}]

     [:h4 "Resource constraints"]
     [m4/form-group
      {:form-id    [:form]
       :data-path  ["identificationInfo" "creativeCommons"]
       :helperText [:span "Learn more about which license is right for you at "
                    [:a {:href   "https://creativecommons.org/choose/"
                         :target "_blank"}
                     "Creative Commons"]]
       :label      "License"}
      [m4/select-option-simple
       {:form-id    [:form]
        :data-path  ["identificationInfo" "creativeCommons"]
        :value-path ["value"]
        :label-path ["label"]
        :options    [{"label" "Creative Commons Attribution 4.0 International License"
                      "value" "CC-BY"}
                     {"label" "Creative Commons Attribution-NonCommercial 4.0 International License"
                      "value" "CC-BY-NC"}
                     {"label" "Other constraints"
                      "value" "OTHER"
                      "other" true}]}]]

     [m4/form-group
      {:form-id   [:form]
       :data-path ["identificationInfo" "otherConstraints"]
       :label     "Additional license requirements"}
      [m4/input-field
       {:form-id     [:form]
        :data-path   ["identificationInfo" "otherConstraints"]
        :placeholder "Enter additional license requirements"}]]

     [:label "Use limitations"]
     [m4/selection-list-values
      {:form-id   [:form]
       :data-path ["identificationInfo" "useLimitations"]}]
     [m4/text-add-button
      {:form-id     [:form]
       :data-path   ["identificationInfo" "useLimitations"]
       :button-text "Add"}]

     [:h4 "Supplemental information"]
     [m4/form-group
      {:form-id   [:form]
       :data-path ["identificationInfo" "supplementalInformation"]
       :label     "Publications associated with the dataset"}
      [:div.SelectionTableStyle
       [m4/selection-list-values
        {:form-id   [:form]
         :data-path ["identificationInfo" "supplementalInformation"]}]
       [m4/list-add-button
        {:form-id     [:form]
         :data-path   ["identificationInfo" "supplementalInformation"]
         :button-text "Add associated publication"}]]]
     [m4/list-edit-dialog
      {:form-id     [:form]
       :data-path   ["identificationInfo" "supplementalInformation"]
       :title       "Add supporting resource"
       :template-id :supplementalInformation/user-defined-entry-form}]

     [m4/form-group
      {:form-id   [:form]
       :data-path ["identificationInfo" "supportingResources"]
       :label     "Supplemental resources with hyperlinks"}
      [:div.SelectionTableStyle
       [m4/selection-list-columns
        {:form-id          [:form]
         :data-path        ["identificationInfo" "supportingResources"]
         :value-path       ["url"]
         :columns          [{:columnHeader "Title" :label-path ["name"] :flex 1}
                            {:columnHeader "URL" :label-path ["url"] :flex 1}]
         :select-snapshot? true}]
       [m4/list-add-button
        {:form-id     [:form]
         :data-path   ["identificationInfo" "supportingResources"]
         :button-text "Add supplemental resource"}]]]
     [m4/list-edit-dialog
      {:form-id     [:form]
       :data-path   ["identificationInfo" "supportingResources"]
       :title       "Add supporting resource"
       :template-id :supportingResources/user-defined-entry-form}]

     [:h4 "Distribution"]
     [m4/form-group
      {:form-id   [:form]
       :data-path ["distributionInfo" "distributionFormat" "title"]
       :label     "Data file format"}
      [m4/input-field
       {:form-id     [:form]
        :data-path   ["distributionInfo" "distributionFormat" "title"]
        :placeholder "e.g. Microsoft Excel, CSV, NetCDF"}]]
     [m4/form-group
      {:form-id   [:form]
       :data-path ["distributionInfo" "distributionFormat" "edition"]
       :label     "Data file format date/version"}
      [m4/input-field
       {:form-id     [:form]
        :data-path   ["distributionInfo" "distributionFormat" "edition"]
        :placeholder "Date format date or version if applicable"}]]]

    :supplementalInformation/user-defined-entry-form
    [:div
     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "name"]
       :label     "Title"}
      [m4/textarea-field
       {:form-id   ?form-id
        :data-path ?data-path}]]]

    :supportingResources/user-defined-entry-form
    [:div
     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "name"]
       :label     "Title"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "name"]}]]

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "url"]
       :label     "URL"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "url"]}]]]

    ;:test/long-name
    ;[:div "Setting longName."]

    :parameter-name/user-defined-entry-form
    [:div
     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "Name"]}
      [m4/input-field
       {:form-id     ?form-id
        :data-path   [?data-path "Name"]
        :placeholder "Name in dataset (optional)"}]]]

    :parameter-unit/user-defined-entry-form
    [:div
     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "Name"]}
      [m4/input-field
       {:form-id     ?form-id
        :data-path   [?data-path "Name"]
        :placeholder "Name in dataset (optional)"}]]]

    :parameter-instrument/user-defined-entry-form
    [:div
     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "Name"]}
      [m4/input-field
       {:form-id     ?form-id
        :data-path   [?data-path "Name"]
        :placeholder "Name in dataset (optional)"}]]]

    :parameter-platform/user-defined-entry-form
    [:div
     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "Name"]
       :label     "Parameter platform"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "Name"]}]]]

    :data-parameter/user-defined-entry-form
    [:div

     [m4/form-group
      {:form-id    ?form-id
       :data-path  [?data-path "longName_term"]
       :label      "Name"
       :added-path ["isUserDefined"]}
      [:div.bp3-control-group
       [:div.bp3-fill
        [m4/async-select-option-breadcrumb
         {:form-id         ?form-id
          :data-path       [?data-path "longName_term"]
          :uri             "/api/parametername"
          :label-path      ["Name"]
          :value-path      ["URI"]
          :breadcrumb-path ["breadcrumbs"]
          :added-path      ["isUserDefined"]
          :placeholder     "Select..."}]]
       [m4/item-dialog-button
        {:form-id            ?form-id
         :data-path          [?data-path "longName_term"]
         :value-path         ["URI"]
         :random-uuid-value? true
         :added-path         ["isUserDefined"]}]]

      [m4/edit-dialog
       {:form-id     ?form-id
        :data-path   [?data-path "longName_term"]
        :title       "Define a custom parameter name"
        :template-id :parameter-name/user-defined-entry-form}]]

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "name"]}
      [m4/input-field
       {:form-id     ?form-id
        :data-path   [?data-path "name"]
        :placeholder "Name in dataset (optional)"}]]

     [m4/form-group
      {:form-id    ?form-id
       :data-path  [?data-path "unit_term"]
       :label      "Unit"
       :added-path ["isUserDefined"]}
      [:div.bp3-control-group
       [:div.bp3-fill
        [m4/async-select-option-simple
         {:form-id     ?form-id
          :data-path   [?data-path "unit_term"]
          :uri         "/api/parameterunit"
          :label-path  ["Name"]
          :value-path  ["URI"]
          :added-path  ["isUserDefined"]
          :placeholder "Select..."}]]
       [m4/item-dialog-button
        {:form-id            ?form-id
         :data-path          [?data-path "unit_term"]
         :value-path         ["URI"]
         :random-uuid-value? true
         :added-path         ["isUserDefined"]}]]
      [m4/edit-dialog
       {:form-id     ?form-id
        :data-path   [?data-path "unit_term"]
        :title       "Define a custom parameter unit"
        :template-id :parameter-unit/user-defined-entry-form}]]

     [m4/form-group
      {:form-id    ?form-id
       :data-path  [?data-path "instrument_term"]
       :label      "Instrument"
       :added-path ["isUserDefined"]}
      [:div.bp3-control-group
       [:div.bp3-fill
        [m4/async-select-option-breadcrumb
         {:form-id         ?form-id
          :data-path       [?data-path "instrument_term"]
          :uri             "/api/parameterinstrument"
          :label-path      ["Name"]
          :value-path      ["URI"]
          :breadcrumb-path ["breadcrumbs"]
          :added-path      ["isUserDefined"]
          :placeholder     "Select..."}]]
       [m4/item-dialog-button
        {:form-id            ?form-id
         :data-path          [?data-path "instrument_term"]
         :value-path         ["URI"]
         :random-uuid-value? true
         :added-path         ["isUserDefined"]}]]
      [m4/edit-dialog
       {:form-id     ?form-id
        :data-path   [?data-path "instrument_term"]
        :title       "Define a custom parameter instrument"
        :template-id :parameter-instrument/user-defined-entry-form}]]

     [m4/form-group
      {:form-id    ?form-id
       :data-path  [?data-path "platform_term"]
       :label      "Platform"
       :added-path ["isUserDefined"]}
      [:div.bp3-control-group
       [:div.bp3-fill
        [m4/async-select-option-breadcrumb
         {:form-id         ?form-id
          :data-path       [?data-path "platform_term"]
          :uri             "/api/parameterplatform"
          :label-path      ["Name"]
          :value-path      ["URI"]
          :breadcrumb-path ["breadcrumbs"]
          :added-path      ["isUserDefined"]
          :placeholder     "Select..."}]]
       [m4/item-dialog-button
        {:form-id            ?form-id
         :data-path          [?data-path "platform_term"]
         :text               "Browse"
         :value-path         ["URI"]
         :random-uuid-value? true
         :added-path         ["isUserDefined"]}]]
      [m4/edit-dialog
       {:form-id     ?form-id
        :data-path   [?data-path "platform_term"]
        :title       "Define a custom parameter platform"
        :template-id :parameter-platform/user-defined-entry-form}]]]

    :upload
    [:div
     ; [m4/page-errors
     ;  {:form-id    [:form]
     ;   :data-path  []
     ;   :data-paths [["attachments"]]}]
     [:h2 "8: Data sources"]
     ; #_[m3/UploadData
     ;    {:attachments-path [:form :fields :attachments]}]

     [m4/form-group
      {:label     "Upload data"
       :form-id   [:form]
       :data-path ["attachments"]
       :required  true}
      [m4/upload-files
       {:form-id    [:form]
        :data-path  ["attachments"]
        :value-path ["file"]
        :placeholder
        [:div.bp3-non-ideal-state
         [:h4.bp3-heading "Drop file here or click to upload"]
         [:div "Max file size 100 MB"]]}]]

     [m4/form-group
      {:label     "Link to data services"
       :form-id   [:form]
       :data-path ["distributionInfo" "transferOptions"]
       :required  true}
      [:div.SelectionTableStyle
       [m4/selection-list-columns
        {:form-id             [:form]
         :data-path           ["distributionInfo" "transferOptions"]
         :value-path          ["linkage"]
         :placeholder-record? true
         :select-snapshot?    true
         :columns             [{:columnHeader "Protocol" :label-path ["protocol"] :flex 2}
                               {:columnHeader "Server" :label-path ["linkage"] :flex 2}
                               {:columnHeader "Name" :label-path ["name"] :flex 1}]}]]

      [m4/list-add-button
       {:form-id     [:form]
        :data-path   ["distributionInfo" "transferOptions"]
        :button-text "Add data service"
        :value-path  ["linkage"]}]

      [m4/list-edit-dialog
       {:form-id     [:form]
        :data-path   ["distributionInfo" "transferOptions"]
        :value-path  ["linkage"]
        :title       "Data Distribution"
        :template-id :transferOptions/user-defined-entry-form}]]]

    :transferOptions/user-defined-entry-form
    [:div
     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "description"]
       :label     "Title"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "description"]}]]

     [m4/inline-form-group
      {:form-id   [:form]
       :data-path [?data-path "protocol"]
       :label     "Protocol"}
      [m4/select-value
       {:form-id    [:form]
        :data-path  [?data-path "protocol"]
        :value-path ["value"]
        :label-path ["label"]
        :options    [{"value" "OGC:WMS-1.3.0-http-get-map" "label" "OGC Web Map Service (WMS)"}
                     {"value" "OGC:WFS-1.0.0-http-get-capabilities" "label" "OGC Web Feature Service (WFS)"}
                     {"value" "WWW:LINK-1.0-http--downloaddata" "label" "Other/unknown"}]}]]

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "linkage"]
       :label     "URL"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "linkage"]}]]

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "name"]
       :label     "Layer"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "name"]}]]]

    ;:lodge
    ;[:div
    ; [:h2 "9: Lodge Metadata Draft"]
    ; [:div.lodge-section
    ;  [:p "Are you finished? Use this page to lodge your completed metadata record."]
    ;  [:p "Any difficulties?  Please contact " [m4/mailto-data-manager-link]]
    ;  [:p "The Data Manager will be notified of your submission and will be in contact
    ;           if any further information is required. Once approved, your data will be archived
    ;           for discovery in the " [m4/portal-link] "."]
    ;  [:p "How complete is your data?"]
    ;  [m4/note-for-data-manager
    ;   {:form-id   [:form]
    ;    :data-path ["noteForDataManager"]}]
    ;  [m4/lodge-button]
    ;  [m4/lodge-status-info]
    ;  [:div.user-export
    ;   [:p [:strong "Want to keep a personal copy of your metadata record?"]]
    ;   [:p
    ;    [m4/xml-export-link {:label "Click here"}]
    ;    " to generate an XML version of your metadata submission. "
    ;    "The file generated includes all of the details you have provided under the
    ;     tabs, but not files you have uploaded."]
    ;   [:p
    ;    "Please note: this XML file is not the recommended way to share your metadata.
    ;     We want you to submit your data via 'lodging' the information.
    ;     This permits multi-user access via the portal in a more friendly format."]]]]
    })

(set! low-code4/template-registry
      (merge edit-templates
             {::components4/create-document-modal-form
              components4/create-document-modal-template}))
