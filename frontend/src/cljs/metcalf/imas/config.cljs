(ns ^:dev/always metcalf.imas.config
  (:require [metcalf.imas.handlers :as imas-handlers]
            [metcalf.imas.subs :as imas-subs]
            [metcalf3.fx :as fx3]
            [metcalf3.handlers :as handlers3]
            [metcalf4.ins :as ins4]
            [metcalf3.subs :as subs3]
            [metcalf3.views :as views3]
            [metcalf4.components :as components4]
            [metcalf4.handlers :as handlers4]
            [metcalf4.low-code :as low-code]
            [metcalf4.rules :as rules]
            [metcalf4.subs :as subs4]
            [metcalf4.views :as views4]
            [re-frame.core :as rf]
            [interop.ui :as ui]
            [metcalf.common-config]))

(rf/reg-event-fx ::handlers4/-save-current-document-success handlers4/-save-current-document-success)
(rf/reg-event-fx ::handlers4/-save-current-document-error handlers4/-save-current-document-error)
(rf/reg-event-fx :textarea-field/value-change handlers3/textarea-field-value-change)
(rf/reg-event-fx :metcalf.imas.core/init-db imas-handlers/init-db)
(rf/reg-event-fx ::views3/PageViewEdit-save-button-click handlers4/save-current-document)
(rf/reg-event-fx :help-menu/open handlers3/help-menu-open)
(rf/reg-event-fx ::components4/value-changed handlers4/value-changed-handler)
(rf/reg-event-fx ::components4/option-change handlers4/option-change-handler)
(rf/reg-event-fx ::components4/list-add-click handlers4/list-add-click-handler)
(rf/reg-event-fx ::components4/list-option-picker-change handlers4/list-option-picker-change)
(rf/reg-event-fx ::components4/selection-list-item-click handlers4/selection-list-item-click2)
(rf/reg-event-fx ::components4/selection-list-remove-click handlers4/selection-list-remove-click)
(rf/reg-event-fx ::components4/selection-list-reorder handlers4/selection-list-reorder)
(rf/reg-event-fx ::components4/boxes-changed handlers4/boxes-changed)
(rf/reg-event-fx ::components4/boxmap-coordinates-open-add-modal handlers4/boxmap-coordinates-open-add-modal)
(rf/reg-event-fx ::components4/boxmap-coordinates-open-edit-modal handlers4/boxmap-coordinates-open-edit-modal)
(rf/reg-event-fx ::components4/boxmap-coordinates-click-confirm-delete handlers4/boxmap-coordinates-click-confirm-delete)
(rf/reg-event-fx ::components4/boxmap-coordinates-list-delete handlers4/boxmap-coordinates-list-delete)
(rf/reg-event-fx ::components4/list-edit-dialog-close handlers4/list-edit-dialog-close-handler)
(rf/reg-event-fx ::components4/list-edit-dialog-cancel handlers4/list-edit-dialog-cancel-handler)
(rf/reg-event-fx ::components4/list-edit-dialog-save handlers4/list-edit-dialog-save-handler)
(rf/reg-event-fx ::components4/item-edit-dialog-close handlers4/item-edit-dialog-close-handler)
(rf/reg-event-fx ::components4/item-edit-dialog-cancel handlers4/item-edit-dialog-cancel-handler)
(rf/reg-event-fx ::components4/item-edit-dialog-save handlers4/item-edit-dialog-save-handler)
(rf/reg-fx :ui/setup-blueprint ui/setup-blueprint)
(rf/reg-sub :subs/get-form-dirty subs4/get-form-dirty?)
(rf/reg-sub ::subs4/get-form-state subs4/get-form-state)
(rf/reg-sub ::components4/get-block-props subs4/form-state-signal subs4/get-block-props-sub)
(rf/reg-sub ::components4/get-block-data subs4/form-state-signal subs4/get-block-data-sub)
(rf/reg-sub ::components4/get-data-schema subs4/get-data-schema-sub)
(rf/reg-sub ::low-code/get-data-schema subs4/get-data-schema-sub)
(rf/reg-sub ::views3/get-props subs4/form-state-signal subs4/get-block-props-sub)
(rf/reg-sub :subs/get-edit-tab-props :<- [:subs/get-page-props] :<- [:subs/get-derived-state] imas-subs/get-edit-tab-props)
(defmethod views3/modal :TableModalEditForm [modal-props] [views3/modal-dialog-table-modal-edit-form modal-props])
(defmethod views3/modal :TableModalAddForm [modal-props] [views3/modal-dialog-table-modal-add-form modal-props])
(defmethod views3/modal :m4/table-modal-edit-form [modal-props] [views4/m4-modal-dialog-table-modal-edit-form modal-props])
(defmethod views3/modal :m4/table-modal-add-form [modal-props] [views4/m4-modal-dialog-table-modal-add-form modal-props])
(defmethod views3/modal :ThemeKeywords [modal-props] [views3/modal-dialog-theme-keywords (select-keys modal-props [:keyword-type :keywords-path])])
(defmethod views3/modal :parametername [modal-props] [views3/modal-dialog-parametername modal-props])
(defmethod views3/modal :parameterunit [modal-props] [views3/modal-dialog-parameterunit modal-props])
(defmethod views3/modal :parameterinstrument [modal-props] [views3/modal-dialog-parameterinstrument modal-props])
(defmethod views3/modal :parameterplatform [modal-props] [views3/modal-dialog-parameterplatform modal-props])
(defmethod views3/modal :person [modal-props] [views3/modal-dialog-person modal-props])
(defmethod views3/modal :DashboardCreateModal [modal-props] [views3/modal-dialog-dashboard-create-modal modal-props])
(defmethod views3/modal :alert [modal-props] [views3/modal-dialog-alert modal-props])
(defmethod views3/modal :confirm [modal-props] [views3/modal-dialog-confirm modal-props])
(ins4/reg-global-singleton ins4/form-ticker)
(ins4/reg-global-singleton ins4/breadcrumbs)
(set! rules/rule-registry
      {"requiredField"        rules/required-field
       "maxLength"            rules/max-length
       "geographyRequired"    rules/geography-required
       "imasVerticalRequired" rules/imas-vertical-required
       "licenseOther"         rules/license-other
       "dateOrder"            rules/date-order
       "endPosition"          rules/end-position
       "maintFreq"            rules/maint-freq})
(set! low-code/component-registry
      {
       'm3/DataParametersTable            {:view views3/DataParametersTable}
       'm3/DataSources                    {:view views3/DataSources}
       'm3/IMASSupplementalInformation    {:view views3/IMASSupplementalInformation}
       'm3/IMASSupportingResource         {:view views3/IMASSupportingResource}
       'm3/NasaListSelectField            {:view views3/NasaListSelectField}
       'm3/UploadData                     {:view views3/UploadData}
       'm3/UseLimitations                 {:view views3/UseLimitations}
       'm3/Who                            {:view views3/Who}
       'm4/async-list-picker              {:view components4/async-list-picker :init components4/async-list-picker-settings}
       'm4/async-select-option            {:view components4/async-select-option :init components4/async-select-option-settings}
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
       'm4/simple-selection-list          {:view components4/simple-selection-list :init components4/simple-selection-list-settings}
       'm4/table-list-option-picker       {:view components4/table-list-option-picker :init components4/table-list-option-picker-settings}
       'm4/table-selection-list           {:view components4/table-selection-list :init components4/table-selection-list-settings}
       'm4/textarea-field                 {:view components4/textarea-field :init components4/textarea-field-settings}
       'm4/textarea-field-with-label      {:view components4/textarea-field-with-label :init components4/textarea-field-settings}
       'm4/yes-no-field                   {:view components4/yes-no-field :init components4/yes-no-field-settings}
       'm4/xml-export-link                {:view components4/xml-export-link :init components4/xml-export-link-settings}
       })
(set! low-code/template-registry
      '{:data-identification
        [:div
         [m4/page-errors
          {:form-id    [:form]
           :data-path  []
           :data-paths [["identificationInfo" "title"]
                        ["identificationInfo" "dateCreation"]]}]
         [:h2 "1. Data Identification"]
         [m4/input-field-with-label
          {:form-id    [:form]
           :data-path  ["identificationInfo" "title"]
           :helperText "Clear and concise description of the content of the resource"}]
         [m4/date-field-with-label
          {:form-id   [:form]
           :data-path ["identificationInfo" "dateCreation"]
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
         [:span.abstract-textarea
          [m4/textarea-field-with-label
           {:form-id     [:form]
            :data-path   ["identificationInfo" "abstract"]
            :placeholder nil
            :helperText  "Describe the content of the resource; e.g. what information was collected, how was it collected"}]]
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
         [:div.row
          [:div.col-md-4
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
                          {"value" "none-planned" "label" "None planned"}]}]]]
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
           [m4/coordinates-modal-field
            {:form-id   [:form]
             :data-path ["identificationInfo" "geographicElement" "boxes"]}]]]
         [:h3 "Vertical Coverage"]
         [m4/checkbox-field
          {:form-id   [:form]
           :data-path ["identificationInfo" "verticalElement" "hasVerticalExtent"]
           :label     "Does data have a vertical coverage?"}]
         [m4/input-field-with-label
          {:form-id    [:form]
           :data-path  ["identificationInfo" "verticalElement" "minimumValue"]
           :class      "wauto"
           :helperText "Shallowest depth / lowest altitude"}]
         [m4/input-field-with-label
          {:form-id    [:form]
           :data-path  ["identificationInfo" "verticalElement" "maximumValue"]
           :class      "wauto"
           :helperText "Deepest depth / highest altitude"}]
         [:div.link-right-container [:a.link-right {:href "#how"} "Next"]]]

        :who
        [:div
         ;; FIXME make this view configurable for IMAS, or create IMAS-specific view.
         ;; - No name lookup, just Given Name and Surname fields
         ;; - Move ORCID ID and Email field positions to match AODN
         ;; - Reduce Role codes
         ;; - Make Responsible Parties mandatory
         ;; FIXME Copy Person functionality isn't working.
         [m3/Who
          {:credit-path [:form :fields :identificationInfo :credit]}]
         [:div.link-right-container [:a.link-right {:href "#how"} "Next"]]]

        :how
        [:div
         [m4/page-errors
          {:form-id    [:form]
           :data-path  []
           :data-paths [["resourceLineage" "lineage"]]}]
         [:h2 "6: How"]
         [:div.lineage-textarea
          [m4/textarea-field-with-label
           {:form-id     [:form]
            :data-path   ["resourceLineage" "lineage"]
            :placeholder nil
            :helperText  "Provide a brief statement of the methods used for collection of the
                         data, can include information regarding sampling equipment (collection hardware),
                         procedures, and precision/resolution of data collected."
            :required    true}]]
         [:div.link-right-container [:a.link-right {:href "#about"} "Next"]]]

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
         [m3/DataParametersTable {:form-id   [:form]
                                  :data-path ["identificationInfo" "dataParameters"]}]
         [:br]
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
           :label       "Additional license requirements"   ;; FIXME
           :placeholder "Enter additional license requirements"
           :required    true}]

         [m3/UseLimitations {:form-id   [:form]
                             :data-path ["identificationInfo" "useLimitations"]}]
         [:br]
         [:h4 "Supplemental information"]
         [m3/IMASSupplementalInformation
          {:form-id   [:form]
           :data-path ["identificationInfo" "supplementalInformation"]}]
         [m3/IMASSupportingResource
          {:form-id   [:form]
           :data-path ["supportingResources"]}]
         [:br]
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

        :upload
        [:div
         [m4/page-errors
          {:form-id    [:form]
           :data-path  []
           :data-paths [["attachments"]]}]
         [:h2 "8: Upload Data"]
         [m3/UploadData
          {:attachments-path [:form :fields :attachments]}]
         [:h2 "Data Services"]
         ;; FIXME reduce protocol options to the below for IMAS:
         ;; [["OGC:WMS-1.3.0-http-get-map" "OGC Web Map Service (WMS)"]
         ;;  ["OGC:WFS-1.0.0-http-get-capabilities" "OGC Web Feature Service (WFS)"]
         ;;  ["WWW:LINK-1.0-http--downloaddata" "Other/unknown"]]
         [m3/DataSources {:form-id   [:form]
                          :data-path ["dataSources"]}]
         [:div.link-right-container [:a.link-right {:href "#lodge"} "Next"]]]

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
           {:form-id    [:form]
            :data-path  ["noteForDataManager"]
            :notes-path [:form :fields :noteForDataManager]}]
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
