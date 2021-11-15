(ns ^:dev/always metcalf.tern.config
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
            [metcalf.tern.handlers :as tern-handlers]
            [metcalf.tern.subs :as tern-subs]
            [re-frame.core :as rf]))

(rf/reg-event-fx ::components4/boxes-changed handlers4/boxes-changed)
(rf/reg-event-fx ::components4/create-document-modal-clear-click handlers3/close-modal)
(rf/reg-event-fx ::components4/create-document-modal-close-click handlers3/close-modal)
(rf/reg-event-fx ::components4/create-document-modal-save-click handlers4/create-document-modal-save-click)
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
(rf/reg-event-fx :app/upload-data-confirm-upload-click-add-attachment handlers3/add-attachment)
(rf/reg-event-fx :app/upload-data-file-upload-failed handlers3/open-modal-handler)
(rf/reg-event-fx :app/upload-max-filesize-exceeded handlers3/open-modal-handler)
(rf/reg-event-fx :metcalf.imas.handlers/-init-db-load-api-options handlers3/load-api-options)
(rf/reg-event-fx :metcalf.tern.core/init-db tern-handlers/init-db)
(rf/reg-event-fx :metcalf.common.actions/-create-document handlers4/-create-document-handler)
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
(rf/reg-sub ::components4/get-block-data subs4/form-state-signal subs4/get-block-data-sub)
(rf/reg-sub ::components4/get-block-props subs4/form-state-signal subs4/get-block-props-sub)
(rf/reg-sub ::components4/get-yes-no-field-props subs4/form-state-signal subs4/get-block-props-sub)
(rf/reg-sub ::low-code4/get-data-schema subs4/get-data-schema-sub)
(rf/reg-sub ::subs4/get-form-state subs4/get-form-state)
(rf/reg-sub ::tern-subs/get-edit-tabs tern-subs/get-edit-tabs)
(rf/reg-sub :app/get-dashboard-props subs3/get-dashboard-props)
(rf/reg-sub :app/get-progress-bar-props :<- [:subs/get-derived-state] subs3/get-progress-props)
(rf/reg-sub :subs/get-app-root-modal-props subs4/get-modal-props)
(rf/reg-sub :subs/get-app-root-page-name subs4/get-page-name)
(rf/reg-sub :subs/get-derived-path :<- [:subs/get-derived-state] subs3/get-derived-path)
(rf/reg-sub :subs/get-derived-state subs3/get-derived-state)
(rf/reg-sub :subs/get-edit-tab-props :<- [:subs/get-page-props] :<- [:subs/get-derived-state] :<- [::tern-subs/get-edit-tabs] tern-subs/get-edit-tab-props)
(rf/reg-sub :subs/get-form-dirty subs4/get-form-dirty?)
(rf/reg-sub :subs/get-page-props subs3/get-page-props)
(ins4/reg-global-singleton ins4/form-ticker)
(ins4/reg-global-singleton ins4/breadcrumbs)
(set! rules4/rule-registry
      {"requiredField"     rules4/required-field
       "requiredWhenYes"   rules4/required-when-yes
       "requiredAllNone"   rules4/required-all-or-nothing
       "maxLength"         rules4/max-length
       "geographyRequired" rules4/geography-required
       "licenseOther"      rules4/license-other
       "numericOrder"      rules4/numeric-order
       "dateOrder"         rules4/date-order
       "dateBeforeToday"   rules4/date-before-today
       "endPosition"       rules4/end-position
       "maintFreq"         rules4/maint-freq
       "verticalRequired"  rules4/vertical-required})
(set! low-code4/component-registry
      {
       'm4/async-item-picker             {:view components4/async-item-picker :init components4/async-item-picker-settings}
       'm4/async-list-picker             {:view components4/async-list-picker :init components4/async-list-picker-settings}
       'm4/async-select-option           {:view components4/async-select-option :init components4/async-select-option-settings}
       'm4/async-select-value            {:view components4/async-select-value :init components4/async-select-value-settings}
       'm4/boxmap-field                  {:view components4/boxmap-field :init components4/boxmap-field-settings}
       'm4/breadcrumb-list-option-picker {:view components4/breadcrumb-list-option-picker :init components4/breadcrumb-list-option-picker-settings}
       'm4/breadcrumb-selection-list     {:view components4/breadcrumb-selection-list :init components4/breadcrumb-selection-list-settings}
       'm4/checkbox-field                {:view components4/checkbox-field :init components4/checkbox-field-settings}
       'm4/date-field                    {:view components4/date-field :init components4/date-field-settings}
       'm4/date-field-with-label         {:view components4/date-field-with-label :init components4/date-field-settings}
       'm4/expanding-control             {:view components4/expanding-control :init components4/expanding-control-settings}
       'm4/form-group                    {:view components4/form-group :init components4/form-group-settings}
       'm4/inline-form-group             {:view components4/inline-form-group :init components4/inline-form-group-settings}
       'm4/input-field                   {:view components4/input-field :init components4/input-field-settings}
       'm4/input-field-with-label        {:view components4/input-field-with-label :init components4/input-field-settings}
       'm4/item-add-button               {:view components4/item-add-button :init components4/item-add-button-settings}
       'm4/item-edit-dialog              {:view components4/item-edit-dialog :init components4/item-edit-dialog-settings}
       'm4/list-add-button               {:view components4/list-add-button :init components4/list-add-button-settings}
       'm4/list-edit-dialog              {:view components4/list-edit-dialog :init components4/list-edit-dialog-settings}
       'm4/typed-list-edit-dialog        {:view components4/typed-list-edit-dialog :init components4/typed-list-edit-dialog-settings}
       'm4/numeric-input-field           {:view components4/numeric-input-field :init components4/numeric-input-field-settings}
       'm4/page-errors                   {:view components4/page-errors :init components4/page-errors-settings}
       'm4/select-option                 {:view components4/select-option :init components4/select-option-settings}
       'm4/select-option-with-label      {:view components4/select-option-with-label :init components4/select-option-settings}
       'm4/select-value                  {:view components4/select-value :init components4/select-value-settings}
       'm4/select-value-with-label       {:view components4/select-value-with-label :init components4/select-value-settings}
       'm4/simple-list-option-picker     {:view components4/simple-list-option-picker :init components4/simple-list-option-picker-settings}
       'm4/selection-list                {:view components4/selection-list :init components4/selection-list-settings}
       'm4/simple-selection-list         {:view components4/simple-selection-list :init components4/simple-selection-list-settings}
       'm4/value-selection-list          {:view components4/value-selection-list :init components4/value-selection-list-settings}
       'm4/table-list-option-picker      {:view components4/table-list-option-picker :init components4/table-list-option-picker-settings}
       'm4/table-selection-list          {:view components4/table-selection-list :init components4/table-selection-list-settings}
       'm4/textarea-field                {:view components4/textarea-field :init components4/textarea-field-settings}
       'm4/textarea-field-with-label     {:view components4/textarea-field-with-label :init components4/textarea-field-settings}
       'm4/when-data                     {:view components4/when-data :init components4/when-data-settings}
       'm4/get-data                      {:view components4/get-data :init components4/get-data-settings}
       'm4/yes-no-field                  {:view components4/yes-no-field :init components4/yes-no-field-settings}
       'm4/simple-list                   {:view components4/simple-list :init components4/simple-list-settings}
       'm4/text-add-button               {:view components4/text-add-button :init components4/text-add-button-settings}
       })

(def edit-templates
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
       :toolTip   "TODO"}
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
       :toolTip   "TODO"}
      [m4/textarea-field
       {:form-id     ?form-id
        :data-path   [?data-path "source"]
        :placeholder "E.g. Creator (Publication year).  Title.  Version.  Publisher.  Resource type.  Identifier.  "}]]

     [m4/inline-form-group
      {:form-id    ?form-id
       :data-path  [?data-path "serial"]
       :label      "Serial Number"
       :helperText "Optional"
       :toolTip    "TODO"}
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
       :toolTip   [:div
                   "Select a from the predefined list.  "
                   "If the required unit is not found within the list you can use the "
                   [:code "Add"]
                   " button to define your own"]}

      [:div.bp3-control-group
       [:div.bp3-fill
        [m4/async-select-option
         {:form-id    ?form-id
          :data-path  [?data-path "unit"]
          :uri        "/api/What9"
          :label-path ["label"]
          :value-path ["uri"]
          :added-path ["isUserDefined"]}]]
       [m4/item-add-button
        {:form-id    ?form-id
         :data-path  [?data-path "unit"]
         :value-path ["uri"]
         :added-path ["isUserDefined"]}]]

      [m4/item-edit-dialog
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

     [m4/input-field-with-label
      {:form-id     [:form]
       :data-path   ["identificationInfo" "title"]
       :label       "Title"
       :placeholder "Provide a descriptive title for the data set including the subject of study, the study location and time period. Example: TERN OzFlux Arcturus Emerald Tower Site 2014-ongoing"
       :helperText  "Clear and concise description of the content of the resource including What, Where, (How), When e.g. Fractional Cover for Australia 2014 ongoing"}]

     [m4/form-group
      {:form-id [:form]
       :label   "Parent Metadata"}
      [m4/yes-no-field
       {:form-id   [:form]
        :data-path ["parentMetadata" "parentMetadataFlag"]
        :label     "Does this record have a parent dataset?"}]
      [m4/async-select-option
       {:form-id         [:form]
        :data-path       ["parentMetadata" "record"]
        :kind            :breadcrumb
        :uri             "/api/terngeonetwork"
        :label-path      ["label"]
        :value-path      ["uri"]
        :breadcrumb-path ["uuid"]}]]

     [m4/select-value-with-label
      {:form-id     [:form]
       :data-path   ["identificationInfo" "topicCategory"]
       :placeholder "Start typing to filter list..."
       :label-path  ["label"]
       :value-path  ["value"]
       :options     [{"value" "biota" "label" "biota"}
                     {"value" "climatology/meteorology/atmosphere" "label" "climatology/meteorology/atmosphere"}
                     {"value" "oceans" "label" "oceans"}
                     {"value" "geoscientificInformation" "label" "geoscientificInformation"}
                     {"value" "inlandWater" "label" "inlandWater"}]}]

     [:div {:style {:display               "grid"
                    :grid-column-gap       "1em"
                    :grid-template-columns "1fr 1fr 1fr"}}

      [:div

       ;; FIXME: Should this be use api for options?
       [m4/select-value-with-label
        {:form-id    [:form]
         :data-path  ["identificationInfo" "status"]
         :label      "Status of Data"
         :value-path ["value"]
         :label-path ["label"]
         :options    [{"value" "onGoing" "label" "ongoing"}
                      {"value" "planned" "label" "planned"}
                      {"value" "completed" "label" "completed"}]}]]
      [:div
       [m4/input-field-with-label
        {:form-id    [:form]
         :data-path  ["identificationInfo" "version"]
         :label      "Version"
         :helperText "Version number of the resource"
         :required   true}]]

      [:div

       ;; FIXME: Should this be use api for options?
       [m4/select-value-with-label
        {:form-id    [:form]
         :data-path  ["identificationInfo" "maintenanceAndUpdateFrequency"]
         :label      "Maintenance/Update Freq"
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
                      {"value" "biennially" "label" "Every 2 years"}]}]]]

     [m4/date-field-with-label
      {:form-id   [:form]
       :data-path ["identificationInfo" "dateCreation"]
       :label     "Date the resource was created"
       :required  true
       :minDate   "1900-01-01"
       :maxDate   "2100-01-01"}]

     [m4/yes-no-field
      {:form-id   [:form]
       :data-path ["identificationInfo" "datePublicationFlag"]
       :label     "Has the data been published before?"}]

     ;; FIXME: I think this should be formatted as YYYY or YYYY-MM (according to the commented template)
     [m4/date-field-with-label
      {:form-id   [:form]
       :data-path ["identificationInfo" "datePublication"]
       :label     "Previous Publication Date"
       :required  true
       :minDate   "1900-01-01"
       :maxDate   "2100-01-01"}]

     [:label "TODO: revision date?"]

     [:div.link-right-container [:a.link-right {:href "#what"} "Next"]]]

    :what
    [:div
     #_[m4/page-errors
        {:form-id    [:form]
         :data-path  []
         :data-paths [["identificationInfo" "abstract"]
                      ["identificationInfo" "purpose"]]}]
     [:h2 "2. What"]
     [:p "TODO: Lorem ipsum..."]
     [m4/textarea-field-with-label
      {:form-id     [:form]
       :data-path   ["identificationInfo" "abstract"]
       :label       "Abstract"
       :placeholder "Provide a brief summary of What, Where, When, Why, Who and How for the collected the data."
       :helperText  "Describe the content of the resource; e.g. what information was collected, how was it collected"
       :toolTip     "Example: The Arcturus greenhouse gas (GHG) monitoring station was established in July 2010 48 km southeast of Emerald, Queensland, with flux tower measurements starting in June 2011 until early 2014. The station was part of a collaborative project between Geoscience Australia (GA) and CSIRO Marine and Atmospheric Research (CMAR). Elevation of the site was approximately 170m asl and mean annual precipitation was 572mm. The tower borderered 2 land use types split N-S: To the west lightly forested tussock grasslands; To the east crop lands, cycling through fallow periods.The instruments were installed on a square lattice tower with an adjustable pulley lever system to raise and lower the instrument arm. The tower was 5.6m tall with the instrument mast extending a further 1.1m above, totalling a height of 6.7m. Fluxes of heat, water vapour, methane and carbon dioxide were measured using the open-path eddy flux technique. Supplementary measurements above the canopy included temperature, humidity, windspeed, wind direction, rainfall, and the 4 components of net radiation. Soil heat flux, soil moisture and soil temperature measurements were also collected."
       :maxLength   2500
       :required    true}]
     [m4/textarea-field-with-label
      {:form-id     [:form]
       :data-path   ["identificationInfo" "purpose"]
       :label       "Purpose"
       :placeholder "Provide a brief summary of the purpose for collecting the data including the potential use."
       :maxLength   1000
       :helperText  "Brief statement about the purpose of the study"
       :toolTip     "The Arcturus flux station data was collected to gain an understanding of natural background carbon dioxide and methane fluxes in the region prior to carbon sequestration and coal seam gas activities take place and to assess the feasibility of using this type of instrumentation for baseline studies prior to industry activities that will be required to monitor and assess CO2 or CH4 leakage to atmosphere in the future"}]

     [m4/form-group
      {:label      "Descriptive keywords"
       :toolTip    "TODO"
       :helperText "Vocabulary terms that describe the general science categories, general location, organizations, projects, platforms, instruments associated with the resource."}]

     [m4/expanding-control
      {:label    "GCMD Science keywords"
       :required true}

      [m4/form-group
       {:label "Select research theme keywords - maximum of 12 allowed"}
       [m4/async-list-picker
        {:form-id         [:form]
         :data-path       ["identificationInfo" "keywordsTheme" "keywords"]
         :kind            :breadcrumb
         :uri             "/api/sciencekeyword"
         :label-path      ["label"]
         :value-path      ["uri"]
         :breadcrumb-path ["breadcrumb"]}]
       [m4/breadcrumb-selection-list
        {:form-id         [:form]
         :data-path       ["identificationInfo" "keywordsTheme" "keywords"]
         :label-path      ["label"]
         :value-path      ["uri"]
         :breadcrumb-path ["breadcrumb"]}]]]

     [m4/expanding-control {:label "ANZSRC Fields keywords" :required true}
      [m4/form-group
       {:label "Select research theme keywords - maximum of 12 allowed"}
       [m4/async-list-picker
        {:form-id         [:form]
         :data-path       ["identificationInfo" "keywordsThemeAnzsrc" "keywords"]
         :kind            :breadcrumb
         :uri             "/api/anzsrckeyword"
         :label-path      ["label"]
         :value-path      ["uri"]
         :breadcrumb-path ["breadcrumb"]}]
       [m4/breadcrumb-selection-list
        {:form-id         [:form]
         :data-path       ["identificationInfo" "keywordsThemeAnzsrc" "keywords"]
         :label-path      ["label"]
         :value-path      ["uri"]
         :breadcrumb-path ["breadcrumb"]}]]]

     [m4/expanding-control {:label "Platforms" :required true}
      ;; TODO: also need a user-added option
      [m4/form-group
       {:label   "Select a platform for the data measurement"
        :toolTip [:div
                  "Select a platform from the predefined list.  "
                  "If the required platform is not found within the list you can use the "
                  [:code "Add"]
                  " button to define your own"]}

       [:div.bp3-control-group
        [:div.bp3-fill
         [m4/async-list-picker
          {:form-id    [:form]
           :data-path  ["identificationInfo" "keywordsPlatform" "keywords"]
           :uri        "/api/ternplatforms"
           :label-path ["label"]
           :value-path ["uri"]}]]
        [m4/list-add-button
         {:form-id     [:form]
          :data-path   ["identificationInfo" "keywordsPlatform" "keywords"]
          :button-text "Add"
          :value-path  ["uri"]
          :added-path  ["isUserDefined"]}]]

       [m4/simple-selection-list
        {:form-id    [:form]
         :data-path  ["identificationInfo" "keywordsPlatform" "keywords"]
         :label-path ["label"]
         :value-path ["uri"]
         :added-path ["isUserDefined"]}]

       [m4/list-edit-dialog
        {:form-id     [:form]
         :data-path   ["identificationInfo" "keywordsPlatform" "keywords"]
         :title       "Platform"
         :template-id :platform/user-defined-entry-form}]]]

     [m4/expanding-control {:label "Instruments" :required true}
      ;; TODO: also need a user-added option
      [m4/form-group
       {:label "Select the instrument used for the platform"}

       [:div.bp3-control-group
        [:div.bp3-fill
         [m4/async-list-picker
          {:form-id    [:form]
           :data-path  ["identificationInfo" "keywordsInstrument" "keywords"]
           :uri        "/api/terninstruments"
           :label-path ["label"]
           :value-path ["uri"]}]]
        [m4/list-add-button
         {:form-id     [:form]
          :data-path   ["identificationInfo" "keywordsInstrument" "keywords"]
          :button-text "Add"
          :value-path  ["uri"]
          :added-path  ["isUserDefined"]}]]
       [m4/table-selection-list
        {:form-id    [:form]
         :data-path  ["identificationInfo" "keywordsInstrument" "keywords"]
         :value-path ["uri"]
         :added-path ["isUserDefined"]
         :columns    [{:columnHeader "Instrument" :label-path ["label"] :flex 2}
                      {:columnHeader "Serial no." :label-path ["serial"] :flex 3}]}]
       [m4/list-edit-dialog
        {:form-id     [:form]
         :data-path   ["identificationInfo" "keywordsInstrument" "keywords"]
         :title       "Instrument"
         :template-id :instrument/user-defined-entry-form}]]]

     [m4/expanding-control {:label "Parameters" :required true}

      ;; TODO: also need a user-added option
      [m4/form-group
       {:label "Select the name of the measured parameter, e.g. vegetation height"}

       [:div.bp3-control-group
        [:div.bp3-fill
         [m4/async-list-picker
          {:form-id    [:form]
           :data-path  ["identificationInfo" "keywordsParameters" "keywords"]
           :uri        "/api/terninstruments"
           :label-path ["label"]
           :value-path ["uri"]}]]
        [m4/list-add-button
         {:form-id     [:form]
          :data-path   ["identificationInfo" "keywordsParameters" "keywords"]
          :button-text "Add"
          :value-path  ["uri"]
          :added-path  ["isUserDefined"]}]]
       [m4/table-selection-list
        {:form-id    [:form]
         :data-path  ["identificationInfo" "keywordsParameters" "keywords"]
         :value-path ["uri"]
         :added-path ["isUserDefined"]
         :columns    [{:columnHeader "Name" :label-path ["label"] :flex 2}
                      {:columnHeader "Units" :label-path ["unit" "label"] :flex 3}]}]
       [m4/list-edit-dialog
        {:form-id     [:form]
         :data-path   ["identificationInfo" "keywordsParameters" "keywords"]
         :title       "Parameter"
         :template-id :parameter/user-defined-entry-form}]]]

     [m4/expanding-control {:label "Temporal Resolution" :required true}
      [m4/form-group
       {:label   "Select a Temporal Resolution range"
        :toolTip "How frequently is the data collected?"}
       [m4/async-select-option
        {:form-id    [:form]
         :data-path  ["identificationInfo" "keywordsTemporal" "keywords"]
         :uri        "/api/samplingfrequency"
         :label-path ["label"]
         :value-path ["uri"]}]]]

     [m4/expanding-control {:label "Horizontal Resolution" :required true}
      [m4/form-group
       {:label   "Select a Horizontal Resolution range"
        :toolTip "For gridded data, select the pixel size of the data, for field plots, select average width"}
       [m4/async-select-option
        {:form-id    [:form]
         :data-path  ["identificationInfo" "keywordsHorizontal" "keywords"]
         :uri        "/api/horizontalresolution"
         :label-path ["label"]
         :value-path ["uri"]}]]]

     [m4/expanding-control {:label "Vertical Resolution (Optional)" :required false}
      [m4/form-group
       {:label   "Select a Vertical Resolution range"
        :toolTip "Select the smallest vertical distance between successive elements of data in a dataset. This is synonymous with terms such as sample spacing and pixel size"}
       [m4/async-select-option
        {:form-id    [:form]
         :data-path  ["identificationInfo" "keywordsVertical" "keywords"]
         :uri        "/api/verticalresolution"
         :label-path ["label"]
         :value-path ["uri"]}]]]

     [m4/expanding-control {:label "Australian Plant Name Index (Optional)" :required false}
      [m4/form-group
       {:label   "Select Plant Name Indexes keywords"
        :toolTip "Species Taxa"}
       [m4/async-list-picker
        {:form-id         [:form]
         :data-path       ["identificationInfo" "keywordsFlora" "keywords"]
         :kind            :breadcrumb
         :uri             "/api/ausplantnames"              ; TODO: testing required; currently nothing in index
         :label-path      ["label"]
         :value-path      ["uri"]
         :breadcrumb-path ["breadcrumb"]}]
       [m4/breadcrumb-selection-list
        {:form-id         [:form]
         :data-path       ["identificationInfo" "keywordsFlora" "keywords"]
         :label-path      ["label"]
         :value-path      ["uri"]
         :breadcrumb-path ["breadcrumb"]}]]]

     [m4/expanding-control {:label "Australian Faunal Directory (Optional)" :required false}
      [m4/form-group
       {:label   "Select Australian Faunal Directory keywords"
        :toolTip "Species Taxa"}
       [m4/async-list-picker
        {:form-id         [:form]
         :data-path       ["identificationInfo" "keywordsFauna" "keywords"]
         :kind            :breadcrumb
         :uri             "/api/ausfaunalnames"
         :label-path      ["label"]
         :value-path      ["uri"]
         :breadcrumb-path ["breadcrumb"]}]
       [m4/breadcrumb-selection-list
        {:form-id         [:form]
         :data-path       ["identificationInfo" "keywordsFauna" "keywords"]
         :label-path      ["label"]
         :value-path      ["uri"]
         :breadcrumb-path ["breadcrumb"]}]]]

     [m4/expanding-control {:label "Additional Keywords (Optional)" :required false}
      [m4/form-group
       {:label   "Additional theme keywords can be added for review and approval process"
        :toolTip "Enter your own additional theme keywords as required and click to add"}
       [m4/value-selection-list
        {:form-id   [:form]
         :data-path ["identificationInfo" "keywordsAdditional" "keywords"]}]
       [m4/text-add-button
        {:form-id     [:form]
         :data-path   ["identificationInfo" "keywordsAdditional" "keywords"]
         :button-text "Add"}]]]

     [:div.link-right-container [:a.link-right {:href "#when"} "Next"]]]

    :when
    [:div
     #_[m4/page-errors
        {:form-id    [:form]
         :data-path  []
         :data-paths [["identificationInfo" "beginPosition"]
                      ["identificationInfo" "endPosition"]]}]

     [:h2 "4. When"]
     [:p "Lorem ipsum..."]

     [:div
      {:style {:display               "grid"
               :grid-column-gap       "1em"
               :grid-template-columns "repeat(auto-fill, minmax(10em, 1fr))"}}
      [m4/date-field-with-label
       {:form-id   [:form]
        :data-path ["identificationInfo" "beginPosition"]
        :label     "Start date"
        :required  true
        :minDate   "1900-01-01"
        :maxDate   "2100-01-01"}]
      [m4/date-field-with-label
       {:form-id   [:form]
        :data-path ["identificationInfo" "endPosition"]
        :label     "End date"
        :required  true
        :minDate   "1900-01-01"
        :maxDate   "2100-01-01"}]]

     [:div.link-right-container [:a.link-right {:href "#where"} "Next"]]]

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

       [m4/textarea-field-with-label
        {:form-id     [:form]
         :data-path   ["identificationInfo" "geographicElement" "siteDescription"]
         :label       "Provide a site description (optional)"
         :placeholder "A descriptive reference for the coverage. May include a project code. Example: Geelong (Site: G145), VIC, Australia"
         :toolTip     "A descriptive reference for the coverage. May include a project code. Example: Geelong (Site: G145), VIC, Australia"}]

       [:p
        "Please input in decimal degrees in coordinate reference system WGS84."
        "Geoscience Australia see "
        [:a {:href   "https://geodesyapps.ga.gov.au/grid-to-geographic"
             :target "_blank"}
         "Grid to Geographic converter"]]

       [m4/form-group
        {:label    "Limits"
         :required true}
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
          :button-text "Add"
          :value-path  ["uri"]
          :added-path  ["isUserDefined"]}]

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
          :toolTip  "TODO"}
         [m4/select-value
          {:form-id     [:form]
           :data-path   ["referenceSystemInfo" "crsCode"]
           :label-path  ["label"]
           :value-path  ["value"]
           :placeholder "Select from list"
           ;; FIXME: Placeholders for now:
           :options     [{"label" "WGS 84 - EPSG:4326" "value" "EPSG:4326"}
                         {"label" "WGS 84 / World Mercator - EPSG:3395" "value" "EPSG:3395"}
                         {"label" "GDA94 - EPSG:4283" "value" "EPSG:4283"}]}]]

        [m4/form-group
         {:form-id   [:form]
          :label     "Date of dynamic datum"
          :toolTip   "TODO"}
         [m4/date-field-with-label
          {:form-id   [:form]
           :data-path ["referenceSystemInfo" "DateOfDynamicDatum"]
           :minDate   "1900-01-01"
           :maxDate   "2100-01-01"}]]]

       [:h3 "Vertical extent (optional)"]
       [:p "The vertial extent is optional.  If you choose to enter details then the following fields are mandatory"]

       [m4/inline-form-group
        {:label    "Vertical Coordinate Reference System"
         :required true
         :toolTip  "TODO"}
        [m4/async-select-option
         {:form-id     [:form]
          :data-path   ["identificationInfo" "verticalCoordinateReferenceSystem"]
          :uri         "/api/What9"
          :label-path  ["label"]
          :value-path  ["uri"]
          :placeholder "Select from list"}]]

       [m4/inline-form-group
        {:label    "Minimum"
         :required true
         :toolTip  "TODO"}
        [m4/numeric-input-field
         {:form-id   [:form]
          :data-path ["identificationInfo" "verticalElement" "minimumValue"]
          :unit      "meters"}]]

       [m4/inline-form-group
        {:label    "Maximum"
         :required true
         :toolTip  "TODO"}
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
           :toolTip  "TODO"}
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
            :unit      ""                                   ; Driven by logic
            }]]]]]]

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

    :who
    [:div

     [m4/expanding-control {:label "Responsible for the creation of dataset" :required true}

      [:p
       "Please assign a person and/or an organisation as responsible for the creation of the dataset. "
       "More than one person or an organisation can be included as well."]

      [m4/selection-list
       {:form-id     [:form]
        :data-path   ["identificationInfo" "citedResponsibleParty"]
        :template-id :party/list-item
        :value-path  ["uri"]
        :added-path  ["isUserDefined"]}]

      [m4/list-add-button
       {:form-id       [:form]
        :data-path     ["identificationInfo" "citedResponsibleParty"]
        :button-text   "Add person"
        :value-path    ["uri"]
        :added-path    ["isUserDefined"]
        :item-defaults {"partyType" "person"}}]

      [m4/list-add-button
       {:form-id       [:form]
        :data-path     ["identificationInfo" "citedResponsibleParty"]
        :button-text   "Add organisation"
        :value-path    ["uri"]
        :added-path    ["isUserDefined"]
        :item-defaults {"partyType" "organisation"}}]

      [m4/typed-list-edit-dialog
       {:form-id   [:form]
        :data-path ["identificationInfo" "citedResponsibleParty"]
        :type-path ["partyType"]
        :templates {"person"
                    {:title       "Person"
                     :template-id :person/user-defined-entry-form}
                    "organisation"
                    {:title       "Organisation"
                     :template-id :organisation/user-defined-entry-form}}}]]

     ;[m4/expanding-control {:label "Point of contact for dataset" :required true}]
     ;[m4/list-add-button
     ; {:form-id    [:form]
     ;  :data-path  ["identificationInfo" "PointOfContactForDataset"]
     ;  :text       "Add"
     ;  :value-path ["uri"]
     ;  :added-path ["isUserDefined"]}]
     ;
     ;[m4/table-selection-list
     ; {:form-id    [:form]
     ;  :data-path  ["identificationInfo" "PointOfContactForDataset"]
     ;  :label-path ["contact" "label"]
     ;  :value-path ["uri"]
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
        {:credit-path [:form :fields :identificationInfo :credit]}]
     [:div.link-right-container [:a.link-right {:href "#how"} "Next"]]]

    :party/list-item
    [:div
     [m4/get-data {:form-id ?form-id :data-path [?data-path "partyType"]}] ": "
     [m4/get-data {:form-id ?form-id :data-path [?data-path "contact" "givenName"]}] " "
     [m4/get-data {:form-id ?form-id :data-path [?data-path "contact" "familyName"]}] " / "
     [m4/get-data {:form-id ?form-id :data-path [?data-path "organisation" "organisationName"]}]]

    :person/user-defined-entry-form
    [:div

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "role"]
       :label     "Role"}
      [m4/async-select-option
       {:form-id    ?form-id
        :data-path  [?data-path "role"]
        :uri        "/api/What9"
        :label-path ["label"]
        :value-path ["value"]}]]

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "contact"]
       :label     "Contact"}
      [m4/async-item-picker
       {:form-id     ?form-id
        :data-path   [?data-path "contact"]
        :uri         "/api/What9"
        :label-path  ["label"]
        :value-path  ["uri"]
        :placeholder "Search for contact details"}]]

     [:p "If contact is not available, please enter the contact details below."]

     [:div {:style {:display               "grid"
                    :grid-column-gap       "1em"
                    :grid-template-columns "1fr 1fr"}}

      [m4/form-group
       {:form-id   ?form-id
        :data-path [?data-path "contact" "givenName"]
        :label     "Given name"}
       [m4/input-field
        {:form-id   ?form-id
         :data-path [?data-path "contact" "givenName"]}]]

      [m4/form-group
       {:form-id   ?form-id
        :data-path [?data-path "contact" "familyName"]
        :label     "Surname"}
       [m4/input-field
        {:form-id   ?form-id
         :data-path [?data-path "contact" "familyName"]}]]]

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "contact" "electronicMailAddress"]
       :label     "Email address"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "contact" "electronicMailAddress"]}]]

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
       :label     "Select associated Organisation"}

      [:div.bp3-control-group
       [:div.bp3-fill
        [m4/async-select-option
         {:form-id    ?form-id
          :data-path  [?data-path "organisation"]
          :uri        "/api/What9"
          :label-path ["organisationName"]
          :value-path ["organisationIdentifier"]}]]
       [m4/item-add-button
        {:form-id    ?form-id
         :data-path  [?data-path "organisation"]
         :value-path ["organisationIdentifier"]
         :added-path ["isUserDefined"]}]]

      [m4/item-edit-dialog
       {:form-id     ?form-id
        :data-path   [?data-path "organisation"]
        :title       "Organisation"
        :template-id :organisation/user-defined-entry-form}]]]

    :organisation/user-defined-entry-form
    [:div

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "role"]
       :label     "Role"}
      [m4/async-select-option
       {:form-id    ?form-id
        :data-path  [?data-path "role"]
        :uri        "/api/What9"
        :label-path ["label"]
        :value-path ["value"]}]]

     [m4/form-group
      {:form-id   ?form-id
       :data-path [?data-path "organisation" "organisationName"]
       :label     "Organisation Name"}
      [m4/input-field
       {:form-id   ?form-id
        :data-path [?data-path "organisation" "organisationName"]}]]]

    :how
    [:div
     #_[m4/page-errors
        {:form-id    [:form]
         :data-path  []
         :data-paths [["resourceLineage" "processStep"]
                      ["dataQualityInfo" "methods"]
                      ["dataQualityInfo" "results"]]}]
     [:h2 "6: How"]

     [:p "This section is optional.  You can add meethod/s used for the collection of the data and provide the Data Quality description and the associated results"]

     [m4/expanding-control {:label "Data creation procedure details (Optional)"}

      [m4/textarea-field-with-label
       {:form-id    [:form]
        :data-path  ["resourceLineage" "statement"]
        :label      "Provide a brief summary of the source of the data and related collection and/or processing methods."
        :required   true
        :toolTip    "TODO"
        :helperText "e.g. Data was collected at the site using the meethod described in XXX Manual, refer to URL..."}]]

     [m4/form-group
      {:label    "Method documentation"
       :required true}
      [m4/table-selection-list
       {:form-id    [:form]
        :data-path  ["resourceLineage" "onlineMethods"]
        :value-path ["uri"]
        :added-path ["isUserDefined"]
        :columns    [{:columnHeader "Title" :label-path ["title"] :flex 1}
                     {:columnHeader "URL" :label-path ["url"] :flex 1}]}]

      [m4/list-add-button
       {:form-id     [:form]
        :data-path   ["resourceLineage" "onlineMethods"]
        :button-text "Add"
        :value-path  ["uri"]
        :added-path  ["isUserDefined"]}]

      [m4/list-edit-dialog
       {:form-id     [:form]
        :data-path   ["resourceLineage" "onlineMethods"]
        :value-path  ["uri"]
        :added-path  ["isUserDefined"]
        :title       "Method Document"
        :template-id :method-doc/user-defined-entry-form}]]

     [:div.link-right-container [:a.link-right {:href "#quality"} "Next"]]]

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
     [m4/textarea-field-with-label
      {:form-id     [:form]
       :data-path   ["dataQualityInfo" "methodSummary"]
       :label       "Provide a summary of the scope of the Data Quality Assessment"
       :maxLength   1000
       :placeholder "The data quality was assessed by ..."}]
     [:h4 "Online data quality report"
      [:p "TODO"]]
     [m4/textarea-field-with-label
      {:form-id     [:form]
       :data-path   ["dataQualityInfo" "results"]
       :label       "Provide a statement regarding the Data Quality Assessment outcome"
       :maxLength   1000
       :placeholder "A statement regarding the data quality assessment results. Examples: RMSE relative to reference data set; horizontal or vertical positional accuracy; etc."}]
     [:div.link-right-container [:a.link-right {:href "#about"} "Next"]]]

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
     [:h4 "Limitation/Constraints"
      [:p TODO]]
     [m4/form-group
      {:label "Security Classification"}
      [:p "TODO"]]
     [m4/expanding-control {:label "Environment Description (Optional)"}
      [m4/textarea-field-with-label
       {:form-id     [:form]
        :data-path   ["identificationInfo" "environment"]
        :label       "Environmental description"
        :placeholder "Information about the source and software to process the resource"
        :helperText  "Software, computer operating system, file name, or dataset size"
        :maxLength   1000}]]
     [m4/expanding-control {:label "Associated Documentation (Optional)"}
      [m4/textarea-field-with-label
       {:form-id     [:form]
        :data-path   ["identificationInfo" "supplemental"]
        :label       "Supplemental Information"
        :placeholder "Information about how to interpret the resource, example: Pixel value indicates the number of days since reference date 1970-01-01"
        :helperText  "Any supplemental information needed to interpret the resource"
        :maxLength   1000}]]
     [m4/expanding-control {:label "Resource specific usage (Optional)"}
      [m4/textarea-field-with-label
       {:form-id     [:form]
        :data-path   ["identificationInfo" "resourceSpecificUsage"]
        :label       "Resource specific usage"
        :placeholder "Resource specific usage..."
        :helperText  "What can this resource be used for environmental research?"
        :maxLength   1000}]]
     [m4/expanding-control {:label "Acknowledgment (Optional)"}
      [m4/textarea-field-with-label
       {:form-id     [:form]
        :data-path   ["identificationInfo" "credit"]
        :label       "Acknowledgment"
        :placeholder "The project was funded by xxx and yyy"
        :helperText  "Write a sentence acknowledging sponsors, data providers or funding organisations"
        :maxLength   1000}]]
     [m4/expanding-control {:label "Citation (Optional)"}
      [m4/textarea-field-with-label
       {:form-id    [:form]
        :data-path  ["identificationInfo" "customCitation"]
        :label      "Specific citation"
        :helperText "The format of the standard citation is provided at https://ternaus.atlassian.net/wiki/spaces/TERNSup/pages/1223163969/How+is+the+citation+constructed+from+the+metadata  For a non-standard citation, provide the details below."
        :maxLength  1000}]]

     [:div.link-right-container [:a.link-right {:href "#upload"} "Next"]]]

    :upload
    [:div
     #_[m4/page-errors {:form-id [:form] :data-paths []}]
     [:h2 "9. Data Sources"]
     [m3/UploadData
      {:attachments-path [:form :fields :attachments]}]
     [:h2 "Data Services"]
     [m3/DataSources {:form-id   [:form]
                      :data-path ["dataSources"]}]
     [:div.link-right-container [:a.link-right {:href "#lodge"} "Next"]]]

    :lodge
    [:div
     #_[m4/page-errors {:form-id [:form] :data-paths []}]
     [:h2 "10: Lodge Metadata Draft"]
     [m3/Lodge
      {:note-for-data-manager-path [:form :fields :noteForDataManager]
       :agreed-to-terms-path       [:form :fields :agreedToTerms]
       :doi-requested-path         [:form :fields :doiRequested]
       :current-doi-path           [:form :fields :identificationInfo :doi]}]]})

(set! low-code4/template-registry
      (merge edit-templates
             {::components4/create-document-modal-form
              components4/create-document-modal-template}))
