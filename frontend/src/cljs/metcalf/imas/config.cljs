(ns metcalf.imas.config
  (:require [metcalf.imas.handlers :as imas-handlers]
            [metcalf3.fx :as fx]
            [metcalf3.handlers :as handlers3]
            [metcalf3.ins :as ins]
            [metcalf3.subs :as subs3]
            [metcalf3.views :as views]
            [metcalf4.components :as components4]
            [metcalf4.handlers :as handlers4]
            [metcalf4.low-code :as low-code]
            [metcalf4.rules :as rules]
            [metcalf4.subs :as subs4]
            [re-frame.core :as rf]))

(rf/reg-event-fx :handlers/load-api-options-resp handlers3/load-api-options-resp)
(rf/reg-event-fx :handlers/load-es-options-resp handlers3/load-es-options-resp)
(rf/reg-event-fx :handlers/close-modal handlers3/close-modal)
(rf/reg-event-fx :handlers/close-and-cancel handlers3/close-and-cancel)
(rf/reg-event-fx :handlers/close-and-confirm handlers3/close-and-confirm)
(rf/reg-event-fx :handlers/open-modal handlers3/open-modal-handler)
(rf/reg-event-fx :handlers/del-value handlers3/del-value)
(rf/reg-event-fx :handlers/new-field! handlers3/new-field!)
(rf/reg-event-fx :handlers/add-field! handlers3/add-field!)
(rf/reg-event-fx :handlers/add-value! handlers3/add-value!)
(rf/reg-event-fx ::handlers4/-save-current-document-success handlers4/-save-current-document-success)
(rf/reg-event-fx ::handlers4/-save-current-document-error handlers4/-save-current-document-error)
(rf/reg-event-fx :handlers/add-attachment handlers3/add-attachment)
(rf/reg-event-fx :handlers/back handlers3/back)
(rf/reg-event-fx :handlers/update-dp-term handlers3/update-dp-term)
(rf/reg-event-fx :handlers/update-nasa-list-value handlers3/update-nasa-list-value)
(rf/reg-event-fx :handlers/update-boxes handlers3/update-boxes)
(rf/reg-event-fx :handlers/update-method-term handlers3/update-method-term)
(rf/reg-event-fx :handlers/update-method-name handlers3/update-method-name)
(rf/reg-event-fx :handlers/setter handlers3/setter)
(rf/reg-event-fx :handlers/check-unsaved-keyword-input handlers3/check-unsaved-keyword-input)
(rf/reg-event-fx :handlers/remove-party handlers3/remove-party)
(rf/reg-event-fx :handlers/reset-form handlers3/reset-form)
(rf/reg-event-fx :handlers/show-errors handlers3/show-errors)
(rf/reg-event-fx :handlers/hide-errors handlers3/hide-errors)
(rf/reg-event-fx :handlers/toggle-status-filter handlers3/toggle-status-filter)
(rf/reg-event-fx :handlers/show-all-documents handlers3/show-all-documents)
(rf/reg-event-fx :handlers/load-error-page handlers3/load-error-page)
(rf/reg-event-fx :handlers/set-value handlers3/set-value)
(rf/reg-event-fx :textarea-field/value-change handlers3/textarea-field-value-change)
(rf/reg-event-fx :handlers/set-geographic-element handlers3/set-geographic-element)
(rf/reg-event-fx :handlers/person-detail-changed handlers3/person-detail-changed)
(rf/reg-event-fx :handlers/value-changed handlers3/value-changed)
(rf/reg-event-fx :handlers/set-tab handlers3/set-tab)
(rf/reg-event-fx :handlers/load-errors handlers3/load-errors)
(rf/reg-event-fx :handlers/add-keyword-extra handlers3/add-keyword-extra)
(rf/reg-event-fx :handlers/del-keyword-extra handlers3/del-keyword-extra)
(rf/reg-event-fx :handlers/add-nodes handlers3/add-nodes)
(rf/reg-event-fx :handlers/dashboard-create-click handlers3/dashboard-create-click)
(rf/reg-event-fx :handlers/transite-doc-success handlers3/transite-doc-success)
(rf/reg-event-fx :handlers/lodge-submit-success handlers3/lodge-submit-success)
(rf/reg-event-fx :metcalf.imas.core/init-db imas-handlers/init-db)
(rf/reg-event-fx :handlers/load-api-options handlers3/load-api-options)
(rf/reg-event-fx :handlers/load-es-options handlers3/load-es-options)
(rf/reg-event-fx :handlers/search-es-options handlers3/search-es-options)
(rf/reg-event-fx ::views/PageViewEdit-save-button-click handlers4/save-current-document)
(rf/reg-event-fx :handlers/archive-current-document handlers3/archive-current-document)
(rf/reg-event-fx :handlers/archive-current-document-success handlers3/archive-current-document-success)
(rf/reg-event-fx :handlers/update-address handlers3/update-address)
(rf/reg-event-fx :handlers/update-person handlers3/update-person)
(rf/reg-event-fx :handlers/org-changed handlers3/org-changed)
(rf/reg-event-fx :handlers/create-document-success handlers3/create-document-success)
(rf/reg-event-fx :handlers/create-document-error handlers3/create-document-error)
(rf/reg-event-fx :handlers/dashboard-create-save handlers3/dashboard-create-save)
(rf/reg-event-fx :handlers/clone-document handlers3/clone-document)
(rf/reg-event-fx :handlers/clone-document-success handlers3/clone-document-success)
(rf/reg-event-fx :handlers/clone-document-error handlers3/clone-document-error)
(rf/reg-event-fx :handlers/archive-doc-click (handlers3/transite-doc-click "archive"))
(rf/reg-event-fx :handlers/delete-archived-doc-click (handlers3/transite-doc-click "delete_archived"))
(rf/reg-event-fx :handlers/restore-doc-click (handlers3/transite-doc-click "restore"))
(rf/reg-event-fx :handlers/transite-doc-confirm handlers3/transite-doc-confirm)
(rf/reg-event-fx :handlers/transite-doc-error handlers3/transite-doc-error)
(rf/reg-event-fx :handlers/lodge-click handlers3/lodge-click)
(rf/reg-event-fx :handlers/lodge-save-success handlers3/lodge-save-success)
(rf/reg-event-fx :handlers/lodge-error handlers3/lodge-error)
(rf/reg-event-fx :help-menu/open handlers3/help-menu-open)
(rf/reg-event-fx ::components4/value-changed handlers4/value-changed-handler)
(rf/reg-event-fx ::components4/option-change handlers4/option-change-handler)
(rf/reg-event-fx ::components4/list-add-click handlers4/list-add-click-handler)
(rf/reg-event-fx ::components4/list-option-picker-change handlers4/list-option-picker-change)
(rf/reg-event-fx ::components4/selection-list-remove-click handlers4/selection-list-remove-click)
(rf/reg-event-fx ::components4/selection-list-reorder handlers4/selection-list-reorder)
(rf/reg-event-fx ::components4/boxes-changed handlers4/boxes-changed)
(rf/reg-event-fx ::components4/boxmap-coordinates-open-add-modal handlers4/boxmap-coordinates-open-add-modal)
(rf/reg-event-fx ::components4/boxmap-coordinates-open-edit-modal handlers4/boxmap-coordinates-open-edit-modal)
(rf/reg-event-fx ::components4/boxmap-coordinates-click-confirm-delete handlers4/boxmap-coordinates-click-confirm-delete)
(rf/reg-event-fx ::components4/boxmap-coordinates-list-delete handlers4/boxmap-coordinates-list-delete)
(rf/reg-event-fx ::components4/edit-dialog-close handlers4/edit-dialog-close-handler)
(rf/reg-event-fx ::components4/edit-dialog-cancel handlers4/edit-dialog-cancel-handler)
(rf/reg-event-fx ::components4/edit-dialog-save handlers4/edit-dialog-save-handler)
(rf/reg-event-fx ::components4/selection-edit-dialog-close handlers4/selection-edit-dialog-close-handler)
(rf/reg-event-fx ::components4/selection-edit-dialog-cancel handlers4/selection-edit-dialog-cancel-handler)
(rf/reg-event-fx ::components4/selection-edit-dialog-save handlers4/selection-edit-dialog-save-handler)
(rf/reg-fx :xhrio/get-json fx/xhrio-get-json)
(rf/reg-fx :xhrio/post-json fx/xhrio-post-json)
(rf/reg-fx :fx/set-location-href fx/set-location-href)
(rf/reg-fx :fx/create-document fx/create-document)
(rf/reg-fx :fx/clone-document fx/clone-document)
(rf/reg-fx :fx/transition-current-document fx/transition-current-document)
(rf/reg-fx :fx/submit-current-document fx/submit-current-document)
(rf/reg-fx :fx/save-current-document fx/save-current-document)
(rf/reg-fx :fx/archive-current-document fx/archive-current-document)
(rf/reg-fx :window/open fx/window-open)
(rf/reg-sub :subs/get-form-dirty subs4/get-form-dirty?)
(rf/reg-sub :subs/get-derived-state subs3/get-derived-state)
(rf/reg-sub :subs/is-page-name-nil? subs3/is-page-name-nil?)
(rf/reg-sub :subs/get-derived-path :<- [:subs/get-derived-state] subs3/get-derived-path)
(rf/reg-sub :subs/get-page-props subs3/get-page-props)
(rf/reg-sub :subs/get-page-name subs3/get-page-name)
(rf/reg-sub :subs/get-modal-props subs3/get-modal-props)
(rf/reg-sub :subs/get-dashboard-props subs3/get-dashboard-props)
(rf/reg-sub :subs/get-edit-tab-props :<- [:subs/get-page-props] :<- [:subs/get-derived-state] subs3/get-imas-edit-tab-props)
(rf/reg-sub :progress/get-props :<- [:subs/get-derived-state] subs3/get-progress-props)
(rf/reg-sub :textarea-field/get-props :<- [:subs/get-derived-state] subs3/get-textarea-field-props)
(rf/reg-sub :textarea-field/get-many-field-props :<- [:subs/get-derived-state] subs3/get-textarea-field-many-props)
(rf/reg-sub :subs/get-form-tick subs3/get-form-tick)
(rf/reg-sub :help/get-menuitems subs3/get-menuitems)
(rf/reg-sub :subs/platform-selected? subs3/platform-selected?)
(rf/reg-sub ::subs4/get-form-state subs4/get-form-state)
(rf/reg-sub ::components4/get-block-props subs4/form-state-signal subs4/get-block-props-sub)
(rf/reg-sub ::components4/get-block-data subs4/form-state-signal subs4/get-block-data-sub)
(rf/reg-sub ::components4/get-data-schema subs4/get-data-schema-sub)
(rf/reg-sub ::views/get-props subs4/form-state-signal subs4/get-block-props-sub)
(ins/reg-global-singleton ins/form-ticker)
(ins/reg-global-singleton ins/breadcrumbs)
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
       'm3/DataParametersTable            views/DataParametersTable
       'm3/DataSources                    views/DataSources
       'm3/IMASSupplementalInformation    views/IMASSupplementalInformation
       'm3/IMASSupportingResource         views/IMASSupportingResource
       'm3/NasaListSelectField            views/NasaListSelectField
       'm3/UploadData                     views/UploadData
       'm3/UseLimitations                 views/UseLimitations
       'm3/Who                            views/Who
       'm4/async-list-picker              components4/async-list-picker
       'm4/async-select-option            components4/async-select-option
       'm4/async-select-option-with-label components4/async-select-option-with-label
       'm4/boxmap-field                   components4/boxmap-field
       'm4/checkbox-field-with-label      components4/checkbox-field-with-label
       'm4/coordinates-modal-field        components4/coordinates-modal-field
       'm4/date-field-with-label          components4/date-field-with-label
       'm4/input-field-with-label         components4/input-field-with-label
       'm4/lodge-button                   components4/lodge-button
       'm4/lodge-status-info              components4/lodge-status-info
       'm4/mailto-data-manager-link       components4/mailto-data-manager-link
       'm4/note-for-data-manager          components4/note-for-data-manager
       'm4/numeric-input-field-with-label components4/numeric-input-field-with-label
       'm4/page-errors                    components4/page-errors
       'm4/portal-link                    components4/portal-link
       'm4/select-option                  components4/select-option
       'm4/select-option-with-label       components4/select-option-with-label
       'm4/select-value-with-label        components4/select-value-with-label
       'm4/textarea-field-with-label      components4/textarea-field-with-label
       'm4/xml-export-link                components4/xml-export-link
       })
(set! low-code/template-registry
      '{:data-identification
        [:div
         [m4/page-errors
          {:form-id    [:form]
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
          {:form-id   [:form]
           :data-path ["identificationInfo" "topicCategory"]
           :valueKey  "value"
           :labelKey  "label"
           :options   [{"value" "biota" "label" "biota"}
                       {"value" "climatology/meteorology/atmosphere" "label" "climatology/meteorology/atmosphere"}
                       {"value" "oceans" "label" "oceans"}
                       {"value" "geoscientificInformation" "label" "geoscientificInformation"}
                       {"value" "inlandWater" "label" "inlandWater"}]}]
         [m4/select-value-with-label
          {:form-id   [:form]
           :data-path ["identificationInfo" "status"]
           :valueKey  "value"
           :labelKey  "label"
           :options   [{"value" "onGoing" "label" "ongoing"}
                       {"value" "planned" "label" "planned"}
                       {"value" "completed" "label" "completed"}]}]
         [m4/select-value-with-label
          {:form-id   [:form]
           :data-path ["identificationInfo" "maintenanceAndUpdateFrequency"]
           :valueKey  "value"
           :labelKey  "label"
           :options   [{"value" "continually" "label" "Continually"}
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
            {:form-id   [:form]
             :data-path ["identificationInfo" "samplingFrequency"]
             :valueKey  "value"
             :labelKey  "label"
             :options   [{"value" "daily" "label" "Daily"}
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
           :data-paths [["identificationInfo" "geographicElement" "boxes"]
                        ["identificationInfo" "verticalElement" "minimumValue"]
                        ["identificationInfo" "verticalElement" "maximumValue"]]}]
         [:h2 "4. Where"]
         [m4/checkbox-field-with-label
          {:form-id   [:form]
           :data-path ["identificationInfo" "geographicElement" "hasGeographicCoverage"]}]
         [:div.row
          [:div.col-sm-6
           ;; FIXME add toggle for satellite imagery.
           [m4/boxmap-field
            {:form-id   [:form]
             :data-path ["identificationInfo" "geographicElement" "boxes"]}]]
          [:div.col-sm-6
           [m4/coordinates-modal-field
            {:form-id   [:form]
             :data-path ["identificationInfo" "geographicElement" "boxes"]}]]]
         [:h3 "Vertical Coverage"]
         [m4/checkbox-field-with-label
          {:form-id   [:form]
           :data-path ["identificationInfo" "verticalElement" "hasVerticalExtent"]}]
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
          {:form-id   [:form]
           :data-path ["identificationInfo" "creativeCommons"]
           :help      [:span "Learn more about which license is right for you at "
                       [:a {:href   "https://creativecommons.org/choose/"
                            :target "_blank"}
                        "Creative Commons"]]
           :label     "License"
           :required  true
           :kind      "simple"
           :valueKey  "value"
           :labelKey  "label"
           :options   [{"value" "http://creativecommons.org/licenses/by/4.0/" "label" "Creative Commons by Attribution (recommended​)"}
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
