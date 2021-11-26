(ns ^:dev/always metcalf.imas.config
  (:require [interop.ui :as ui]
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
            [metcalf.common.views3 :as views3]
            [metcalf.imas.handlers :as imas-handlers]
            [metcalf.imas.subs :as imas-subs]
            [re-frame.core :as rf]))

(rf/reg-event-fx ::components4/boxes-changed handlers4/boxes-changed)
(rf/reg-event-fx ::components4/boxmap-coordinates-click-confirm-delete handlers4/boxmap-coordinates-click-confirm-delete)
(rf/reg-event-fx ::components4/boxmap-coordinates-list-delete handlers4/boxmap-coordinates-list-delete)
(rf/reg-event-fx ::components4/boxmap-coordinates-open-add-modal handlers4/boxmap-coordinates-open-add-modal)
(rf/reg-event-fx ::components4/boxmap-coordinates-open-edit-modal handlers4/boxmap-coordinates-open-edit-modal)
(rf/reg-event-fx ::components4/create-document-modal-clear-click handlers3/close-modal)
(rf/reg-event-fx ::components4/create-document-modal-close-click handlers3/close-modal)
(rf/reg-event-fx ::components4/create-document-modal-save-click handlers4/create-document-modal-save-click)
(rf/reg-event-fx ::components4/share-document-modal-clear-click handlers3/close-modal)
(rf/reg-event-fx ::components4/share-document-modal-close-click handlers3/close-modal)
(rf/reg-event-fx ::components4/item-add-with-defaults-click-handler handlers4/item-add-with-defaults-click-handler2)
(rf/reg-event-fx ::components4/item-edit-dialog-cancel handlers4/item-edit-dialog-cancel-handler)
(rf/reg-event-fx ::components4/item-edit-dialog-close handlers4/item-edit-dialog-close-handler)
(rf/reg-event-fx ::components4/item-edit-dialog-save handlers4/item-edit-dialog-save-handler)
(rf/reg-event-fx ::components4/item-option-picker-change handlers4/item-option-picker-change)
(rf/reg-event-fx ::components4/list-add-with-defaults-click-handler handlers4/list-add-with-defaults-click-handler2)
(rf/reg-event-fx ::components4/list-edit-dialog-cancel handlers4/list-edit-dialog-cancel-handler)
(rf/reg-event-fx ::components4/list-edit-dialog-close handlers4/list-edit-dialog-close-handler)
(rf/reg-event-fx ::components4/list-edit-dialog-save handlers4/list-edit-dialog-save-handler)
(rf/reg-event-fx ::components4/list-option-picker-change handlers4/list-option-picker-change)
(rf/reg-event-fx ::components4/option-change handlers4/option-change-handler)
(rf/reg-event-fx ::components4/selection-list-item-click handlers4/selection-list-item-click2)
(rf/reg-event-fx ::components4/selection-list-remove-click handlers4/selection-list-remove-click)
(rf/reg-event-fx ::components4/selection-list-reorder handlers4/selection-list-reorder)
(rf/reg-event-fx ::components4/text-value-add-click-handler handlers4/text-value-add-click-handler)
(rf/reg-event-fx ::components4/value-changed handlers4/value-changed-handler)
(rf/reg-event-fx ::components4/value-selection-list-remove-click handlers4/selection-list-remove-click)
(rf/reg-event-fx ::components4/value-selection-list-reorder handlers4/selection-list-reorder)
(rf/reg-event-fx ::handlers4/-save-current-document-error handlers4/-save-current-document-error)
(rf/reg-event-fx ::handlers4/-save-current-document-success handlers4/-save-current-document-success)
(rf/reg-event-fx :app/-archive-current-document-success handlers3/-archive-current-document-success)
(rf/reg-event-fx :app/-clone-document-error handlers3/-clone-document-error)
(rf/reg-event-fx :app/-clone-document-success handlers3/-clone-document-success)
(rf/reg-event-fx :app/-load-api-options handlers3/-load-api-options)
(rf/reg-event-fx :app/-lodge-click-error handlers3/lodge-error)
(rf/reg-event-fx :app/-lodge-click-success handlers3/lodge-save-success)
(rf/reg-event-fx :app/-lodge-save-error handlers3/lodge-error)
(rf/reg-event-fx :app/-lodge-save-success handlers3/-lodge-save-success)
(rf/reg-event-fx :app/-transite-doc-click-confirm handlers3/-transite-doc-click-confirm)
(rf/reg-event-fx :app/-transite-doc-confirm-error handlers3/-transite-doc-confirm-error)
(rf/reg-event-fx :app/-transite-doc-confirm-success handlers3/-transite-doc-confirm-success)
(rf/reg-event-fx :app/PageViewEdit-save-button-click handlers4/save-current-document)
(rf/reg-event-fx :app/clone-doc-confirm handlers3/clone-document)
(rf/reg-event-fx :app/dashboard-create-click handlers3/dashboard-create-click)
(rf/reg-event-fx :app/document-teaser-share-click handlers4/document-teaser-share-click)
(rf/reg-event-fx :metcalf.common.actions4/-get-document-data-action handlers4/-get-document-data-action)
(rf/reg-event-fx :app/dashboard-show-all-click handlers3/show-all-documents)
(rf/reg-event-fx :app/dashboard-toggle-status-filter handlers3/toggle-status-filter)
(rf/reg-event-fx :app/delete-attachment-click handlers3/open-modal-handler)
(rf/reg-event-fx :app/delete-attachment-confirm handlers3/del-value)
(rf/reg-event-fx :app/document-teaser-archive-click (handlers3/transite-doc-click "archive"))
(rf/reg-event-fx :app/document-teaser-clone-click handlers3/document-teaser-clone-click)
(rf/reg-event-fx :app/document-teaser-delete-archived-click (handlers3/transite-doc-click "delete_archived"))
(rf/reg-event-fx :app/document-teaser-restore-click (handlers3/transite-doc-click "restore"))
(rf/reg-event-fx :app/edit-tabs-pick-click handlers3/set-tab)
(rf/reg-event-fx :app/handle-page-view-edit-archive-click handlers3/handle-page-view-edit-archive-click)
(rf/reg-event-fx :app/modal-dialog-alert-dismiss handlers3/close-modal)
(rf/reg-event-fx :app/modal-dialog-alert-save handlers3/close-modal)
(rf/reg-event-fx :app/modal-dialog-confirm-cancel handlers3/close-and-cancel)
(rf/reg-event-fx :app/modal-dialog-confirm-dismiss handlers3/close-and-cancel)
(rf/reg-event-fx :app/modal-dialog-confirm-save handlers3/close-and-confirm)
(rf/reg-event-fx :app/open-modal handlers3/open-modal-handler)
(rf/reg-event-fx :app/page-view-edit-archive-click-confirm handlers3/archive-current-document)
#_(rf/reg-event-fx :app/upload-data-confirm-upload-click-add-attachment handlers3/add-attachment)
(rf/reg-event-fx :app/upload-data-file-upload-failed handlers3/open-modal-handler)
(rf/reg-event-fx :app/upload-max-filesize-exceeded handlers3/open-modal-handler)
(rf/reg-event-fx :metcalf.imas.core/init-db imas-handlers/init-db)
(rf/reg-event-fx :metcalf.imas.handlers/-init-db-load-api-options handlers3/load-api-options)
(rf/reg-event-fx :metcalf.common.actions4/-create-document handlers4/-create-document-handler)
(rf/reg-event-fx :metcalf.common.components/coordinates-modal-field-close-modal handlers3/close-modal)
(rf/reg-event-fx :metcalf.common.components/lodge-button-click handlers3/lodge-click)
(rf/reg-fx ::fx3/post fx3/post)
(rf/reg-fx ::fx3/post-json-data fx3/post-json-data)
(rf/reg-fx ::fx3/set-location-href fx3/set-location-href)
(rf/reg-fx ::low-code4/init! low-code4/init!)
(rf/reg-fx :app/get-json-fx (utils4/promise-fx utils4/get-json))
(rf/reg-fx :app/post-data-fx (utils4/promise-fx utils4/post-json))
(rf/reg-fx :ui/setup-blueprint ui/setup-blueprint)
(rf/reg-sub ::components4/create-document-modal-can-save? subs4/create-document-modal-can-save?)
(rf/reg-sub :app/contributors-modal-props subs4/contributors-modal-props)
(rf/reg-event-fx :app/contributors-modal-unshare-click handlers4/contributors-modal-unshare-click)
(rf/reg-event-fx :app/contributors-modal-share-click handlers4/contributors-modal-share-click)
(rf/reg-event-fx :metcalf.common.handlers4/-contributors-modal-share-resolve handlers4/-contributors-modal-share-resolve)
(rf/reg-event-fx :metcalf.common.handlers4/-contributors-modal-unshare-resolve handlers4/-contributors-modal-unshare-resolve)
(rf/reg-sub ::components4/get-block-data subs4/form-state-signal subs4/get-block-data-sub)
(rf/reg-sub ::components4/get-block-props subs4/form-state-signal subs4/get-block-props-sub)
(rf/reg-sub ::components4/get-yes-no-field-props subs4/form-state-signal subs4/get-block-props-sub)
(rf/reg-sub ::low-code4/get-data-schema subs4/get-data-schema-sub)
(rf/reg-sub ::subs4/get-form-state subs4/get-form-state)
(rf/reg-sub :app/get-dashboard-props subs3/get-dashboard-props)
(rf/reg-sub :app/get-progress-bar-props :<- [::subs4/get-form-state [:form]] subs3/get-progress-props)
(rf/reg-sub :subs/get-app-root-modal-props subs4/get-modal-props)
(rf/reg-sub :subs/get-app-root-page-name subs4/get-page-name)
(rf/reg-sub :subs/get-attachments subs3/get-attachments)
(rf/reg-sub :subs/get-edit-tab-props :<- [:subs/get-page-props] :<- [::subs4/get-form-state [:form]] imas-subs/get-edit-tab-props)
(rf/reg-sub :subs/get-form-dirty subs4/get-form-dirty?)
(rf/reg-sub :subs/get-page-props subs3/get-page-props)
(rf/reg-sub :subs/get-context subs3/get-context)
(rf/reg-sub :subs/get-progress subs3/get-progress)
(rf/reg-sub :subs/get-form subs3/get-form)
(rf/reg-sub :subs/get-upload-form subs3/get-upload-form)
(rf/reg-sub :subs/get-form-disabled? subs3/get-form-disabled?)
(ins4/reg-global-singleton ins4/form-ticker)
(ins4/reg-global-singleton ins4/breadcrumbs)
(set! rules4/rule-registry
      {"requiredField"        rules4/required-field
       "maxLength"            rules4/max-length
       "geographyRequired"    rules4/geography-required
       "imasVerticalRequired" rules4/imas-vertical-required
       "licenseOther"         rules4/license-other
       "dateOrder"            rules4/date-order
       "endPosition"          rules4/end-position
       "maintFreq"            rules4/maint-freq})
(set! low-code4/component-registry
      {
       ;'m3/UploadData                     {:view views3/UploadData}
       'm4/async-list-picker              {:view components4/async-list-picker :init components4/async-list-picker-settings}
       'm4/async-select-option            {:view components4/async-select-option :init components4/async-select-option-settings}
       'm4/async-select-value             {:view components4/async-select-value :init components4/async-select-value-settings}
       'm4/boxmap-field                   {:view components4/boxmap-field :init components4/boxmap-field-settings}
       'm4/breadcrumb-list-option-picker  {:view components4/breadcrumb-list-option-picker :init components4/breadcrumb-list-option-picker-settings}
       'm4/breadcrumb-selection-list      {:view components4/breadcrumb-selection-list :init components4/breadcrumb-selection-list-settings}
       'm4/checkbox-field                 {:view components4/checkbox-field :init components4/checkbox-field-settings}
       'm4/checkbox-field-with-label      {:view components4/checkbox-field-with-label :init components4/checkbox-field-settings}
       'm4/coordinates-modal-field        {:view components4/coordinates-modal-field :init components4/coordinates-modal-field-settings}
       'm4/date-field-with-label          {:view components4/date-field-with-label :init components4/date-field-settings}
       'm4/form-group                     {:view components4/form-group :init components4/form-group-settings}
       'm4/inline-form-group              {:view components4/inline-form-group :init components4/inline-form-group-settings}
       'm4/input-field                    {:view components4/input-field :init components4/input-field-settings}
       'm4/input-field-with-label         {:view components4/input-field-with-label :init components4/input-field-settings}
       'm4/item-add-button                {:view components4/item-add-button :init components4/item-add-button-settings}
       'm4/item-edit-dialog               {:view components4/item-edit-dialog :init components4/item-edit-dialog-settings}
       'm4/list-add-button                {:view components4/list-add-button :init components4/list-add-button-settings}
       'm4/list-edit-dialog               {:view components4/list-edit-dialog :init components4/list-edit-dialog-settings}
       'm4/typed-list-edit-dialog         {:view components4/typed-list-edit-dialog :init components4/typed-list-edit-dialog-settings}
       'm4/lodge-button                   {:view components4/lodge-button}
       'm4/lodge-status-info              {:view components4/lodge-status-info}
       'm4/mailto-data-manager-link       {:view components4/mailto-data-manager-link}
       'm4/note-for-data-manager          {:view components4/note-for-data-manager :init components4/note-for-data-manager-settings}
       'm4/numeric-input-field            {:view components4/numeric-input-field :init components4/numeric-input-field-settings}
       'm4/numeric-input-field-with-label {:view components4/numeric-input-field-with-label :init components4/numeric-input-field-settings}
       'm4/page-errors                    {:view components4/page-errors :init components4/page-errors-settings}
       'm4/portal-link                    {:view components4/portal-link}
       'm4/select-option                  {:view components4/select-option :init components4/select-option-settings}
       'm4/select-option-with-label       {:view components4/select-option-with-label :init components4/select-option-settings}
       'm4/select-value                   {:view components4/select-value :init components4/select-value-settings}
       'm4/select-value-with-label        {:view components4/select-value-with-label :init components4/select-value-settings}
       'm4/simple-list-option-picker      {:view components4/simple-list-option-picker :init components4/simple-list-option-picker-settings}
       'm4/selection-list                 {:view components4/selection-list :init components4/selection-list-settings}
       'm4/simple-selection-list          {:view components4/simple-selection-list :init components4/simple-selection-list-settings}
       'm4/value-selection-list           {:view components4/value-selection-list :init components4/value-selection-list-settings}
       'm4/table-list-option-picker       {:view components4/table-list-option-picker :init components4/table-list-option-picker-settings}
       'm4/table-selection-list           {:view components4/table-selection-list :init components4/table-selection-list-settings}
       'm4/textarea-field                 {:view components4/textarea-field :init components4/textarea-field-settings}
       'm4/textarea-field-with-label      {:view components4/textarea-field-with-label :init components4/textarea-field-settings}
       'm4/when-data                      {:view components4/when-data :init components4/when-data-settings}
       'm4/get-data                       {:view components4/get-data :init components4/get-data-settings}
       'm4/yes-no-field                   {:view components4/yes-no-field :init components4/yes-no-field-settings}
       'm4/xml-export-link                {:view components4/xml-export-link :init components4/xml-export-link-settings}
       'm4/async-item-picker              {:view components4/async-item-picker :init components4/async-item-picker-settings}
       'm4/text-add-button                {:view components4/text-add-button :init components4/text-add-button-settings}
       'm4/simple-list                    {:view components4/simple-list :init components4/simple-list-settings}
       })

(def edit-templates
  '{:data-identification
    [:div
     [m4/page-errors
      {:form-id    [:form]
       :data-path  []
       :data-paths [["identificationInfo" "title"]
                    ["identificationInfo" "dateCreation"]
                    ["identificationInfo" "topicCategory"]
                    ["identificationInfo" "status"]
                    ["identificationInfo" "maintenanceAndUpdateFrequency"]]}]
     [:h2 "1. Data Identification"]
     [m4/input-field-with-label
      {:form-id    [:form]
       :data-path  ["identificationInfo" "title"]
       :helperText "Clear and concise description of the content of the resource"}]
     [m4/date-field-with-label
      {:form-id   [:form]
       :data-path ["identificationInfo" "dateCreation"]
       :label     "Date of record creation"
       :minDate   "1900-01-01"
       :maxDate   "2100-01-01"}]
     [m4/select-value-with-label
      {:form-id    [:form]
       :data-path  ["identificationInfo" "topicCategory"]
       :label      "Topic categories"
       :value-path ["value"]
       :label-path ["label"]
       :options    [{"value" "biota" "label" "biota"}
                    {"value" "climatology/meteorology/atmosphere" "label" "climatology/meteorology/atmosphere"}
                    {"value" "oceans" "label" "oceans"}
                    {"value" "geoscientificInformation" "label" "geoscientificInformation"}
                    {"value" "inlandWater" "label" "inlandWater"}]}]
     [m4/select-value-with-label
      {:form-id    [:form]
       :data-path  ["identificationInfo" "status"]
       :value-path ["value"]
       :label-path ["label"]
       :options    [{"value" "onGoing" "label" "ongoing"}
                    {"value" "planned" "label" "planned"}
                    {"value" "completed" "label" "completed"}]}]
     [m4/select-value-with-label
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
                    {"value" "biennially" "label" "Every 2 years"}]}]
     [:div.link-right-container [:a.link-right {:href "#what"} "Next"]]]

    :what
    [:div
     [m4/page-errors
      {:form-id    [:form]
       :data-path  []
       :data-paths [["identificationInfo" "abstract"]]}]
     [:h2 "2. What"]
     [m4/textarea-field-with-label
      {:form-id     [:form]
       :data-path   ["identificationInfo" "abstract"]
       :placeholder nil
       :rows        3
       :helperText  "Describe the content of the resource; e.g. what information was collected, how was it collected"}]

     [m4/form-group
      {:form-id   [:form]
       :data-path ["identificationInfo" "keywordsTheme" "keywords"]
       :label     "Research theme keywords!"}

      [:div "Select up to 12 research theme keywords describing your data"]

      [m4/async-list-picker
       {:form-id         [:form]
        :data-path       ["identificationInfo" "keywordsTheme" "keywords"]
        :kind            :breadcrumb
        :uri             "/api/keywords_with_breadcrumb_info"
        :placeholder     "Search for keywords"
        :label-path      ["label"]
        :value-path      ["uri"]
        :breadcrumb-path ["breadcrumb"]}]
      [m4/breadcrumb-selection-list
       {:form-id         [:form]
        :data-path       ["identificationInfo" "keywordsTheme" "keywords"]
        :label-path      ["label"]
        :value-path      ["uri"]
        :breadcrumb-path ["breadcrumb"]}]]

     [m4/form-group
      {:form-id   [:form]
       :data-path ["identificationInfo" "keywordsThemeExtra" "keywords"]
       :label     "Additional theme keywords"}
      [:div "Enter your own additional theme keywords as required and click + to add"]
      [m4/value-selection-list
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
      [m4/value-selection-list
       {:form-id   [:form]
        :data-path ["identificationInfo" "keywordsTaxonExtra" "keywords"]}]
      [m4/text-add-button
       {:form-id     [:form]
        :data-path   ["identificationInfo" "keywordsTaxonExtra" "keywords"]
        :button-text "Add"}]]

     ;; TODO add theme keywords
     [:div.link-right-container [:a.link-right {:href "#when"} "Next"]]]

    :when
    [:div
     [m4/page-errors
      {:form-id    [:form]
       :data-path  []
       :data-paths [["identificationInfo" "beginPosition"]
                    ["identificationInfo" "endPosition"]
                    ["identificationInfo" "samplingFrequency"]]}]
     [:h2 "3. When was the data acquired?"]
     [m4/date-field-with-label
      {:form-id   [:form]
       :data-path ["identificationInfo" "beginPosition"]
       :minDate   "1900-01-01"
       :maxDate   "2100-01-01"}]
     [m4/date-field-with-label
      {:form-id   [:form]
       :data-path ["identificationInfo" "endPosition"]
       :minDate   "1900-01-01"
       :maxDate   "2100-01-01"}]
     [m4/select-value-with-label
      {:form-id    [:form]
       :data-path  ["identificationInfo" "samplingFrequency"]
       :value-path ["value"]
       :label-path ["label"]
       :options    [{"value" "daily" "label" "Daily"}
                    {"value" "weekly" "label" "Weekly"}
                    {"value" "monthly" "label" "Monthly"}
                    {"value" "quarterly" "label" "Quarterly"}
                    {"value" "annually" "label" "Annually"}
                    {"value" "ongoing" "label" "Ongoing"}
                    {"value" "asNeeded" "label" "As required"}
                    {"value" "irregular" "label" "Irregular"}
                    {"value" "none-planned" "label" "None planned"}]}]
     [:div.link-right-container [:a.link-right {:href "#where"} "Next"]]]

    :where
    [:div
     [m4/page-errors
      {:form-id    [:form]
       :data-path  []
       :data-paths [["identificationInfo" "geographicElement" "boxes"]
                    ["identificationInfo" "verticalElement" "minimumValue"]
                    ["identificationInfo" "verticalElement" "maximumValue"]]}]
     [:h2 "4. Where"]
     [:h3 "Geographic Coverage"]
     [m4/checkbox-field
      {:form-id   [:form]
       :data-path ["identificationInfo" "geographicElement" "hasGeographicCoverage"]
       :label     "Does data have a geographic coverage?"}]
     [:div.row
      [:div.col-sm-6
       ;; FIXME add toggle for satellite imagery.
       [m4/boxmap-field
        {:form-id    [:form]
         :data-path  ["identificationInfo" "geographicElement" "boxes"]
         :value-path ["uri"]
         :added-path ["isUserDefined"]}]]
      [:div.col-sm-6

       [m4/when-data
        {:form-id   [:form]
         :data-path ["identificationInfo" "geographicElement" "boxes"]
         :pred      ::components4/empty-list?}
        [:p "Specify the location(s) of this study."]]

       [m4/table-selection-list
        {:form-id    [:form]
         :data-path  ["identificationInfo" "geographicElement" "boxes"]
         :value-path ["uri"]
         :added-path ["isUserDefined"]
         :columns    [{:columnHeader "North" :label-path ["northBoundLatitude"] :flex 1}
                      {:columnHeader "East" :label-path ["southBoundLatitude"] :flex 1}
                      {:columnHeader "South" :label-path ["eastBoundLongitude"] :flex 1}
                      {:columnHeader "West" :label-path ["westBoundLongitude"] :flex 1}]}]

       [m4/list-add-button
        {:form-id     [:form]
         :data-path   ["identificationInfo" "geographicElement" "boxes"]
         :button-text "Add new"
         :value-path  ["uri"]
         :added-path  ["isUserDefined"]}]

       [m4/list-edit-dialog
        {:form-id     [:form]
         :data-path   ["identificationInfo" "geographicElement" "boxes"]
         :title       "Bounding box"
         :template-id :box/user-defined-entry-form}]]]
     [:h3 "Vertical Coverage"]
     [m4/checkbox-field
      {:form-id   [:form]
       :data-path ["identificationInfo" "verticalElement" "hasVerticalExtent"]
       :label     "Does data have a vertical coverage?"}]
     [m4/form-group
      {:form-id   [:form]
       :data-path ["identificationInfo" "verticalElement" "verticalCRS"]}
      [m4/select-option
       {:form-id     [:form]
        :data-path   ["identificationInfo" "verticalElement" "verticalCRS"]
        :value-path  ["value"]
        :label-path  ["label"]
        :placeholder "Please select"
        :options     [{"label" "Depth (distance below mean sea level)" "value" "EPSG::5715"}
                      {"label" "Altitude (height above mean sea level)" "value" "EPSG::5714"}]}]]
     [m4/numeric-input-field-with-label
      {:form-id    [:form]
       :data-path  ["identificationInfo" "verticalElement" "minimumValue"]
       :class      "wauto"
       :helperText "Shallowest depth / lowest altitude"}]
     [m4/numeric-input-field-with-label
      {:form-id    [:form]
       :data-path  ["identificationInfo" "verticalElement" "maximumValue"]
       :class      "wauto"
       :helperText "Deepest depth / highest altitude"}]
     [:div.link-right-container [:a.link-right {:href "#how"} "Next"]]]

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
       :data-path [?data-path "southBoundLatitude"]
       :label     "East"}
      [m4/numeric-input-field
       {:form-id   ?form-id
        :data-path [?data-path "southBoundLatitude"]}]]

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "eastBoundLongitude"]
       :label     "South"}
      [m4/numeric-input-field
       {:form-id   ?form-id
        :data-path [?data-path "eastBoundLongitude"]}]]

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "westBoundLongitude"]
       :label     "West"}
      [m4/numeric-input-field
       {:form-id   ?form-id
        :data-path [?data-path "westBoundLongitude"]}]]]

    :how
    [:div
     [m4/page-errors
      {:form-id    [:form]
       :data-path  []
       :data-paths [["resourceLineage" "lineage"]]}]
     [:h2 "5: How"]
     [:div.lineage-textarea
      [m4/textarea-field-with-label
       {:form-id     [:form]
        :data-path   ["resourceLineage" "lineage"]
        :placeholder nil
        :helperText  "Provide a brief statement of the methods used for collection of the
                         data, can include information regarding sampling equipment (collection hardware),
                         procedures, and precision/resolution of data collected."
        :required    true}]]
     [:div.link-right-container [:a.link-right {:href "#who"} "Next"]]]

    :who
    [:div
     [m4/page-errors
      {:form-id    [:form]
       :data-path  []
       :data-paths [["identificationInfo" "citedResponsibleParty"]
                    ["identificationInfo" "pointOfContact"]]}]
     [:h2 "6. Who"]
     [m4/table-selection-list
      {:form-id    [:form]
       :data-path  ["identificationInfo" "citedResponsibleParty"]
       :value-path ["uri"]
       :added-path ["isUserDefined"]
       :columns    [{:columnHeader "Given name" :label-path ["givenName"] :flex 1}
                    {:columnHeader "Family name" :label-path ["familyName"] :flex 1}]}]
     [:div.bp3-control-group
      [m4/list-add-button
       {:form-id     [:form]
        :data-path   ["identificationInfo" "citedResponsibleParty"]
        :button-text "Add cited responsible party"
        :value-path  ["uri"]
        :added-path  ["isUserDefined"]}]]
     [m4/list-edit-dialog
      {:form-id     [:form]
       :data-path   ["identificationInfo" "citedResponsibleParty"]
       :title       "Responsible for creating the data"
       :template-id :person/user-defined-entry-form}]
     [:hr]
     [m4/table-selection-list
      {:form-id    [:form]
       :data-path  ["identificationInfo" "pointOfContact"]
       :value-path ["uri"]
       :added-path ["isUserDefined"]
       :columns    [{:columnHeader "Given name" :label-path ["givenName"] :flex 1}
                    {:columnHeader "Family name" :label-path ["familyName"] :flex 1}]}]
     [:div.bp3-control-group
      [m4/list-add-button
       {:form-id     [:form]
        :data-path   ["identificationInfo" "pointOfContact"]
        :button-text "Add point of contact"
        :value-path  ["uri"]
        :added-path  ["isUserDefined"]}]]
     [m4/list-edit-dialog
      {:form-id     [:form]
       :data-path   ["identificationInfo" "pointOfContact"]
       :title       "Responsible for creating the data"
       :template-id :person/user-defined-entry-form}]
     [:h3 "Other credits"]
     [:div "Acknowledge the contribution of any funding schemes or organisations."]
     [m4/value-selection-list
      {:form-id   [:form]
       :data-path ["identificationInfo" "credit"]}]
     [m4/text-add-button
      {:form-id     [:form]
       :data-path   ["identificationInfo" "credit"]
       :button-text "Add"}]
     [:hr]
     [:div.link-right-container [:a.link-right {:href "#about"} "Next"]]]

    :person/user-defined-entry-form
    [:div
     [:div {:style {:display               "grid"
                    :grid-column-gap       "1em"
                    :grid-template-columns "1fr 1fr"}}
      [m4/form-group
       {:form-id   ?form-id
        :data-path [?data-path "givenName"]
        :label     "Given name"}
       [m4/input-field
        {:form-id   ?form-id
         :data-path [?data-path "givenName"]}]]
      [m4/form-group
       {:form-id   ?form-id
        :data-path [?data-path "familyName"]
        :label     "Surname"}
       [m4/input-field
        {:form-id   ?form-id
         :data-path [?data-path "familyName"]}]]]

     [m4/form-group
      {:form-id     ?form-id
       :data-path   [?data-path "orcid"]
       :label       "ORCID ID"
       :placeholder "XXXX-XXXX-XXXX-XXXX"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "orcid"]}]]
     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "role"]
       :label     "Role"}
      [m4/async-select-value
       {:form-id      ?form-id
        :data-path    [?data-path "role"]
        :uri          "/api/rolecode.json"
        :results-path ["results"]
        :label-path   ["Identifier"]
        :value-path   ["UUID"]}]]
     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path]
       :label     "Organisation"}
      [m4/async-select-value
       {:form-id     ?form-id
        :data-path   [?data-path]
        :uri         "/api/institution.json"
        :label-path  ["label"]
        :value-path  ["uri"]
        :placeholder "Search for contact details"}]]
     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "address" "deliveryPoint"]
       :label     "Postal address"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "address" "deliveryPoint"]}]]
     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "address" "deliveryPoint2"]
       :label     "Postal address 2"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "address" "deliveryPoint2"]}]]

     [:div {:style {:display               "grid"
                    :grid-column-gap       "1em"
                    :grid-template-columns "1fr 1fr"}}
      [m4/form-group
       {:form-id   ?form-id
        :data-path [?data-path "address" "city"]
        :label     "City"}
       [m4/input-field
        {:form-id   ?form-id
         :data-path [?data-path "address" "city"]}]]
      [m4/form-group
       {:form-id   ?form-id
        :data-path [?data-path "address" "administrativeArea"]
        :label     "State / territory"}
       [m4/input-field
        {:form-id   ?form-id
         :data-path [?data-path "address" "administrativeArea"]}]]
      [m4/form-group
       {:form-id   ?form-id
        :data-path [?data-path "address" "postalCode"]
        :label     "Postal / Zip code"}
       [m4/input-field
        {:form-id   ?form-id
         :data-path [?data-path "address" "postalCode"]}]]
      [m4/form-group
       {:form-id   ?form-id
        :data-path [?data-path "address" "country"]
        :label     "Country"}
       [m4/input-field
        {:form-id   ?form-id
         :data-path [?data-path "address" "country"]}]]]

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "phone"]
       :label     "Phone number"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "phone"]}]]
     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "facsimile"]
       :label     "Fax number"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "facsimile"]}]]
     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "electronicMailAddress"]
       :label     "Email address"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "electronicMailAddress"]}]]]

    :about
    [:div
     [m4/page-errors
      {:form-id    [:form]
       :data-path  []
       :data-paths [["identificationInfo" "dataParameters"]
                    ["identificationInfo" "creativeCommons"]
                    ["identificationInfo" "otherConstraints"]
                    ["identificationInfo" "useLimitations"]
                    ["identificationInfo" "supplementalInformation"]
                    ["supportingResources"]
                    ["distributionInfo" "distributionFormat" "name"]
                    ["distributionInfo" "distributionFormat" "version"]]}]
     [:h2 "7: About Dataset"]
     [:h4 "Data parameters"]
     ; WIP to replace m3/DataParametersTable
     [m4/table-selection-list
      {:form-id    [:form]
       :data-path  ["identificationInfo" "dataParameters"]
       :value-path ["uri"]
       :added-path ["isUserDefined"]
       :columns    [{:columnHeader "Name" :flex 1 :label-path ["longName_term"]}
                    {:columnHeader "Units" :flex 1 :label-path ["unit_term"]}
                    {:columnHeader "Instrument" :flex 1 :label-path ["instrument_term"]}
                    {:columnHeader "Platform" :flex 1 :label-path ["platform_term"]}]}]
     [m4/list-add-button
      {:form-id     [:form]
       :data-path   ["identificationInfo" "dataParameters"]
       :button-text "Add data parameter"
       :value-path  ["uri"]
       :added-path  ["isUserDefined"]}]
     [m4/list-edit-dialog
      {:form-id     [:form]
       :data-path   ["identificationInfo" "dataParameters"]
       :title       "Add parameter"
       :template-id :data-parameter/user-defined-entry-form}]
     [:h4 "Resource constraints"]
     ;; FIXME license selection isn't being included in XML export.
     [m4/select-option-with-label
      {:form-id    [:form]
       :data-path  ["identificationInfo" "creativeCommons"]
       :help       [:span "Learn more about which license is right for you at "
                    [:a {:href   "https://creativecommons.org/choose/"
                         :target "_blank"}
                     "Creative Commons"]]
       :label      "License"
       :required   true
       :kind       "simple"
       :value-path ["value"]
       :label-path ["label"]
       :options    [{"value" "http://creativecommons.org/licenses/by/4.0/" "label" "Creative Commons by Attribution (recommended​)"}
                    {"value" "http://creativecommons.org/licenses/by-nc/4.0/" "label" "Creative Commons, Non-commercial Use only"}
                    {"value" "http://creativecommons.org/licenses/other" "label" "Other constraints"}]}]
     [m4/input-field-with-label
      {:form-id     [:form]
       :data-path   ["identificationInfo" "otherConstraints"]
       :label       "Additional license requirements"       ;; FIXME
       :placeholder "Enter additional license requirements"
       :required    true}]

     [:label "Use limitations"]
     [m4/value-selection-list
      {:form-id   [:form]
       :data-path ["identificationInfo" "useLimitations"]}]
     [m4/text-add-button
      {:form-id     [:form]
       :data-path   ["identificationInfo" "useLimitations"]
       :button-text "Add"}]

     [:hr]

     [:h4 "Supplemental information"]
     [:label "Publications associated with dataset"]
     [m4/value-selection-list
      {:form-id   [:form]
       :data-path ["identificationInfo" "supplementalInformation"]}]
     [m4/text-add-button
      {:form-id     [:form]
       :data-path   ["identificationInfo" "supplementalInformation"]
       :button-text "Add"}]

     [:label "Supporting resources"]
     [m4/table-selection-list
      {:form-id    [:form]
       :data-path  ["supportingResources"]
       :value-path ["url"]
       :added-path ["isUserDefined"]
       :columns    [{:columnHeader "Title" :label-path ["name"] :flex 1}
                    {:columnHeader "URL" :label-path ["url"] :flex 1}]}]
     [m4/list-add-button
      {:form-id     [:form]
       :data-path   ["supportingResources"]
       :button-text [:span [:span.bp3-icon-plus] " Add supporting resource"]
       :value-path  ["url"]
       :added-path  ["isUserDefined"]}]
     [m4/list-edit-dialog
      {:form-id     [:form]
       :data-path   ["supportingResources"]
       :title       "Add supporting resource"
       :template-id :resource/user-defined-entry-form}]
     [:h4 "Distribution"]
     [m4/input-field-with-label
      {:form-id     [:form]
       :data-path   ["distributionInfo" "distributionFormat" "name"]
       :placeholder "e.g. Microsoft Excel, CSV, NetCDF"}]
     [m4/input-field-with-label
      {:form-id     [:form]
       :data-path   ["distributionInfo" "distributionFormat" "version"]
       :placeholder "Date format date or version if applicable"}]
     [:div.link-right-container [:a.link-right {:href "#upload"} "Next"]]]

    :resource/user-defined-entry-form
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

    :test/long-name
    [:div "Setting longName."]

    :data-parameter/user-defined-entry-form
    [:div

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "longName_term"]
       :label     "Name"}
      #_[m4/async-select-option
         {:form-id     ?form-id
          :data-path   [?data-path "longName_term"]
          :uri         "/api/parametername"
          :label-path  ["label"]
          :value-path  ["uri"]
          :added-path  ["isUserDefined"]
          :placeholder "Select..."}]

      [m4/item-add-button
       {:form-id    ?form-id
        :data-path  [?data-path "longName_term"]
        :text       "Browse"
        :value-path ["longName_term"]
        :added-path ["isUserDefined"]}]

      [m4/item-edit-dialog
       {:form-id     ?form-id
        :data-path   [?data-path "longName_term"]
        :title       "LONG NAME"
        :template-id :test/long-name}]

      [m4/input-field
       {:form-id     ?form-id
        :data-path   [?data-path "name"]
        :placeholder "Name in dataset (optional)"}]]


     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "unit_term"]
       :label     "Unit"}
      [m4/async-select-option
       {:form-id     ?form-id
        :data-path   [?data-path "unit_term"]
        :uri         "/api/parameterunit"
        :label-path  ["label"]
        :value-path  ["value"]
        :placeholder "Select..."}]
      [m4/list-add-button
       {:form-id    ?form-id
        :data-path  [?data-path "unit_term"]
        :text       "Browse"
        :value-path ["value"]}]]

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "instrument_term"]
       :label     "Instrument"}
      [m4/async-select-option
       {:form-id     ?form-id
        :data-path   [?data-path "instrument_term"]
        :uri         "/api/parameterinstrument"
        :label-path  ["label"]
        :value-path  ["uri"]
        :added-path  ["isUserDefined"]
        :placeholder "Select..."}]
      [m4/list-add-button
       {:form-id    ?form-id
        :data-path  [?data-path "instrument_term"]
        :text       "Browse"
        :value-path ["uri"]
        :added-path ["isUserDefined"]}]]

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "platform_term"]
       :label     "Platform"}
      [m4/async-select-option
       {:form-id     ?form-id
        :data-path   [?data-path "platform_term"]
        :uri         "/api/parameterplatform"
        :label-path  ["label"]
        :value-path  ["uri"]
        :added-path  ["isUserDefined"]
        :placeholder "Select..."}]
      [m4/list-add-button
       {:form-id    ?form-id
        :data-path  [?data-path "platform_term"]
        :text       "Browse"
        :value-path ["uri"]
        :added-path ["isUserDefined"]}]]]


    :upload
    [:div
     [m4/page-errors
      {:form-id    [:form]
       :data-path  []
       :data-paths [["attachments"]]}]
     [:h2 "8: Upload Data"]
     #_[m3/UploadData
        {:attachments-path [:form :fields :attachments]}]
     [:h2 "Data Services"]
     [m4/table-selection-list
      {:form-id    [:form]
       :data-path  ["dataSources"]
       :value-path ["url"]
       :added-path ["isUserDefined"]
       :columns    [{:columnHeader "Description" :label-path ["description"] :flex 1}
                    {:columnHeader "URL" :label-path ["url"] :flex 1}
                    {:columnHeader "Layer" :label-path ["name"] :flex 1}]}]
     [:div.bp3-control-group
      [m4/list-add-button
       {:form-id     [:form]
        :data-path   ["dataSources"]
        :button-text "Add data service"
        :value-path  ["url"]
        :added-path  ["isUserDefined"]}]]
     [m4/list-edit-dialog
      {:form-id     [:form]
       :data-path   ["dataSources"]
       :title       "Data Service"
       :template-id :data-source/user-defined-entry-form}]
     [:div.link-right-container [:a.link-right {:href "#lodge"} "Next"]]]

    :data-source/user-defined-entry-form
    [:div
     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "description"]
       :label     "Title"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "description"]}]]

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "protocol"]
       :label     "Protocol"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "protocol"]}]]

     [m4/select-value-with-label
      {:form-id    [:form]
       :data-path  [?data-path "protocol"]
       :label      "Protocol"
       :value-path ["value"]
       :label-path ["label"]
       :options    [{"value" "OGC:WMS-1.3.0-http-get-map" "label" "OGC Web Map Service (WMS)"}
                    {"value" "OGC:WFS-1.0.0-http-get-capabilities" "label" "OGC Web Feature Service (WFS)"}
                    {"value" "WWW:LINK-1.0-http--downloaddata" "label" "Other/unknown"}]}]

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "url"]
       :label     "URL"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "url"]}]]

     [m4/inline-form-group
      {:form-id   ?form-id
       :data-path [?data-path "name"]
       :label     "Layer"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "name"]}]]]

    :lodge
    [:div
     [:h2 "9: Lodge Metadata Draft"]
     [:div.lodge-section
      [:p "Are you finished? Use this page to lodge your completed metadata record."]
      [:p "Any difficulties?  Please contact " [m4/mailto-data-manager-link]]
      [:p "The Data Manager will be notified of your submission and will be in contact
               if any further information is required. Once approved, your data will be archived
               for discovery in the " [m4/portal-link] "."]
      [:p "How complete is your data?"]
      [m4/note-for-data-manager
       {:form-id   [:form]
        :data-path ["noteForDataManager"]}]
      [m4/lodge-button]
      [m4/lodge-status-info]
      [:div.user-export
       [:p [:strong "Want to keep a personal copy of your metadata record?"]]
       [:p
        [m4/xml-export-link {:label "Click here"}]
        " to generate an XML version of your metadata submission. "
        "The file generated includes all of the details you have provided under the
         tabs, but not files you have uploaded."]
       [:p
        "Please note: this XML file is not the recommended way to share your metadata.
         We want you to submit your data via 'lodging' the information.
         This permits multi-user access via the portal in a more friendly format."]]]]})

(set! low-code4/template-registry
      (merge edit-templates
             {::components4/create-document-modal-form
              components4/create-document-modal-template}))
