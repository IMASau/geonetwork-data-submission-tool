(ns metcalf.imas.config
  (:require [metcalf4.low-code :as low-code]
            [metcalf4.rules :as rules]
            [metcalf.imas.handlers :as imas-handlers]
            [metcalf3.fx :as fx]
            [metcalf3.handlers :as handlers]
            [metcalf3.ins :as ins]
            [metcalf3.subs :as subs]
            [metcalf3.views :as views]
            [metcalf4.components :as common-components]
            [metcalf4.handlers :as common-handlers]
            [metcalf4.subs :as common-subs]
            [re-frame.core :as rf]))

(rf/reg-event-fx :handlers/load-api-options-resp handlers/load-api-options-resp)
(rf/reg-event-fx :handlers/load-es-options-resp handlers/load-es-options-resp)
(rf/reg-event-fx :handlers/close-modal handlers/close-modal)
(rf/reg-event-fx :handlers/close-and-cancel handlers/close-and-cancel)
(rf/reg-event-fx :handlers/close-and-confirm handlers/close-and-confirm)
(rf/reg-event-fx :handlers/open-modal handlers/open-modal-handler)
(rf/reg-event-fx :handlers/del-value handlers/del-value)
(rf/reg-event-fx :handlers/new-field! handlers/new-field!)
(rf/reg-event-fx :handlers/add-field! handlers/add-field!)
(rf/reg-event-fx :handlers/add-value! handlers/add-value!)
(rf/reg-event-fx :handlers/save-current-document-success handlers/save-current-document-success)
(rf/reg-event-fx :handlers/add-attachment handlers/add-attachment)
(rf/reg-event-fx :handlers/back handlers/back)
(rf/reg-event-fx :handlers/update-dp-term handlers/update-dp-term)
(rf/reg-event-fx :handlers/update-nasa-list-value handlers/update-nasa-list-value)
(rf/reg-event-fx :handlers/update-boxes handlers/update-boxes)
(rf/reg-event-fx :handlers/update-method-term handlers/update-method-term)
(rf/reg-event-fx :handlers/update-method-name handlers/update-method-name)
(rf/reg-event-fx :handlers/setter handlers/setter)
(rf/reg-event-fx :handlers/check-unsaved-keyword-input handlers/check-unsaved-keyword-input)
(rf/reg-event-fx :handlers/remove-party handlers/remove-party)
(rf/reg-event-fx :handlers/reset-form handlers/reset-form)
(rf/reg-event-fx :handlers/show-errors handlers/show-errors)
(rf/reg-event-fx :handlers/hide-errors handlers/hide-errors)
(rf/reg-event-fx :handlers/toggle-status-filter handlers/toggle-status-filter)
(rf/reg-event-fx :handlers/show-all-documents handlers/show-all-documents)
(rf/reg-event-fx :handlers/load-error-page handlers/load-error-page)
(rf/reg-event-fx :handlers/set-value handlers/set-value)
(rf/reg-event-fx :textarea-field/value-change handlers/textarea-field-value-change)
(rf/reg-event-fx :handlers/set-geographic-element handlers/set-geographic-element)
(rf/reg-event-fx :handlers/person-detail-changed handlers/person-detail-changed)
(rf/reg-event-fx :handlers/value-changed handlers/value-changed)
(rf/reg-event-fx :handlers/set-tab handlers/set-tab)
(rf/reg-event-fx :handlers/load-errors handlers/load-errors)
(rf/reg-event-fx :handlers/add-keyword-extra handlers/add-keyword-extra)
(rf/reg-event-fx :handlers/del-keyword-extra handlers/del-keyword-extra)
(rf/reg-event-fx :handlers/add-nodes handlers/add-nodes)
(rf/reg-event-fx :handlers/dashboard-create-click handlers/dashboard-create-click)
(rf/reg-event-fx :handlers/transite-doc-success handlers/transite-doc-success)
(rf/reg-event-fx :handlers/lodge-submit-success handlers/lodge-submit-success)
(rf/reg-event-fx :metcalf.imas.core/init-db imas-handlers/init-db)
(rf/reg-event-fx :handlers/load-api-options handlers/load-api-options)
(rf/reg-event-fx :handlers/load-es-options handlers/load-es-options)
(rf/reg-event-fx :handlers/search-es-options handlers/search-es-options)
(rf/reg-event-fx :handlers/save-current-document handlers/save-current-document)
(rf/reg-event-fx :handlers/save-current-document-error handlers/save-current-document-error)
(rf/reg-event-fx :handlers/archive-current-document handlers/archive-current-document)
(rf/reg-event-fx :handlers/archive-current-document-success handlers/archive-current-document-success)
(rf/reg-event-fx :handlers/update-address handlers/update-address)
(rf/reg-event-fx :handlers/update-person handlers/update-person)
(rf/reg-event-fx :handlers/org-changed handlers/org-changed)
(rf/reg-event-fx :handlers/create-document-success handlers/create-document-success)
(rf/reg-event-fx :handlers/create-document-error handlers/create-document-error)
(rf/reg-event-fx :handlers/dashboard-create-save handlers/dashboard-create-save)
(rf/reg-event-fx :handlers/clone-document handlers/clone-document)
(rf/reg-event-fx :handlers/clone-document-success handlers/clone-document-success)
(rf/reg-event-fx :handlers/clone-document-error handlers/clone-document-error)
(rf/reg-event-fx :handlers/archive-doc-click (handlers/transite-doc-click "archive"))
(rf/reg-event-fx :handlers/delete-archived-doc-click (handlers/transite-doc-click "delete_archived"))
(rf/reg-event-fx :handlers/restore-doc-click (handlers/transite-doc-click "restore"))
(rf/reg-event-fx :handlers/transite-doc-confirm handlers/transite-doc-confirm)
(rf/reg-event-fx :handlers/transite-doc-error handlers/transite-doc-error)
(rf/reg-event-fx :handlers/lodge-click handlers/lodge-click)
(rf/reg-event-fx :handlers/lodge-save-success handlers/lodge-save-success)
(rf/reg-event-fx :handlers/lodge-error handlers/lodge-error)
(rf/reg-event-fx :help-menu/open handlers/help-menu-open)
(rf/reg-event-fx ::common-components/input-field-with-label-value-changed common-handlers/value-changed-handler)
(rf/reg-event-fx ::common-components/textarea-field-with-label-value-changed common-handlers/value-changed-handler)
(rf/reg-event-fx ::common-components/date-field-with-label-value-changed common-handlers/value-changed-handler)
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
(rf/reg-sub :subs/get-derived-state subs/get-derived-state)
(rf/reg-sub :metcalf3/form-dirty? :<- [:subs/get-derived-state] subs/form-dirty?)
(rf/reg-sub :subs/is-page-name-nil? subs/is-page-name-nil?)
(rf/reg-sub :subs/get-derived-path :<- [:subs/get-derived-state] subs/get-derived-path)
(rf/reg-sub :subs/get-page-props subs/get-page-props)
(rf/reg-sub :subs/get-page-name subs/get-page-name)
(rf/reg-sub :subs/get-modal-props subs/get-modal-props)
(rf/reg-sub :subs/get-dashboard-props subs/get-dashboard-props)
(rf/reg-sub :subs/get-edit-tab-props :<- [:subs/get-page-props] :<- [:subs/get-derived-state] subs/get-edit-tab-props)
(rf/reg-sub :progress/get-props :<- [:subs/get-derived-state] subs/get-progress-props)
(rf/reg-sub ::views/get-select-field-with-label-props :<- [:subs/get-derived-state] subs/get-select-field-with-label-props)
(rf/reg-sub :textarea-field/get-props :<- [:subs/get-derived-state] subs/get-textarea-field-props)
(rf/reg-sub :textarea-field/get-many-field-props :<- [:subs/get-derived-state] subs/get-textarea-field-many-props)
(rf/reg-sub :subs/get-form-tick subs/get-form-tick)
(rf/reg-sub :help/get-menuitems subs/get-menuitems)
(rf/reg-sub :subs/platform-selected? subs/platform-selected?)
(rf/reg-sub ::common-subs/get-form-state common-subs/get-form-state)
(rf/reg-sub ::common-components/get-input-field-with-label-props common-subs/form-state-signal common-subs/get-block-props-sub)
(rf/reg-sub ::common-components/get-textarea-field-with-label-props common-subs/form-state-signal common-subs/get-block-props-sub)
(rf/reg-sub ::common-components/get-date-field-with-label-props common-subs/form-state-signal common-subs/get-block-props-sub)
(ins/reg-global-singleton ins/form-ticker)
(ins/reg-global-singleton ins/breadcrumbs)
(set! rules/rule-registry
      {"requiredField"     rules/required-field
       "maxLength"         rules/max-length
       "geographyRequired" rules/geography-required
       "licenseOther"      rules/license-other
       "dateOrder"         rules/date-order
       "endPosition"       rules/end-position
       "maintFreq"         rules/maint-freq
       "verticalRequired"  rules/vertical-required})
(set! low-code/component-registry
      {'m3/DataParametersTable         views/DataParametersTable
       'm3/CheckboxField               views/CheckboxField
       'm3/date-field-with-label       views/date-field-with-label
       'm3/textarea-field-with-label   views/textarea-field-with-label
       'm3/UseLimitations              views/UseLimitations
       'm3/select-field-with-label     views/select-field-with-label
       'm3/NasaListSelectField         views/NasaListSelectField
       'm3/GeographicCoverage          views/GeographicCoverage
       'm3/DataSources                 views/DataSources
       'm3/PageErrors                  views/PageErrors
       'm3/VerticalCoverage            views/VerticalCoverage
       'm3/TopicCategories             views/TopicCategories
       'm3/ResourceConstraints         views/ResourceConstraints
       'm3/input-field-with-label      views/input-field-with-label
       'm3/IMASLodge                   views/IMASLodge
       'm3/IMASSupportingResource      views/IMASSupportingResource
       'm3/IMASSupplementalInformation views/IMASSupplementalInformation
       'm3/TaxonKeywordsExtra          views/TaxonKeywordsExtra
       'm3/ThemeKeywords               views/ThemeKeywords
       'm3/ThemeKeywordsExtra          views/ThemeKeywordsExtra
       'm3/UploadData                  views/UploadData
       'm3/Who                         views/Who
       'm4/textarea-field-with-label   common-components/textarea-field-with-label
       'm4/input-field-with-label      common-components/input-field-with-label
       'm4/date-field-with-label       common-components/date-field-with-label
       })
(set! low-code/template-registry
      '{:data-identification
        [:div
         [m3/PageErrors {:page :data-identification :path [:form]}]
         [:h2 "1. Data Identification"]
         [m4/input-field-with-label
          {:form-id    [:form :state]
           :data-path  [:identificationInfo :title]
           :label      "Title"
           :helperText "Clear and concise description of the content of the resource"}]
         [m4/date-field-with-label
          {:form-id   [:form :state]
           :data-path [:identificationInfo :dateCreation]
           :label     "Date of record creation"
           :required  true
           :minDate   "1900-01-01"
           :maxDate   "2100-01-01"}]
         [m3/select-field-with-label
          {:form-id   [:form :state]
           :data-path [:identificationInfo :topicCategory]
           :label     "Topic categories"
           :required  true
           :options   [["biota" "biota"]
                       ["climatology/meteorology/atmosphere" "climatology/meteorology/atmosphere"]
                       ["oceans" "oceans"]
                       ["geoscientificInformation" "geoscientificInformation"]
                       ["inlandWater" "inlandWater"]]}]
         [m3/select-field-with-label
          {:form-id   [:form :state]
           :data-path [:identificationInfo :status]
           :label     "Status of data"
           :required  true
           :options   [["onGoing" "ongoing"]
                       ["planned" "planned"]
                       ["completed" "completed"]]}]
         [m3/select-field-with-label
          {:form-id   [:form :state]
           :data-path [:identificationInfo :maintenanceAndUpdateFrequency]
           :label     "Maintenance and update frequency"
           :required  true
           :options   [["continually" "Continually"]
                       ["daily" "Daily"]
                       ["weekly" "Weekly"]
                       ["fortnightly" "Fortnightly"]
                       ["monthly" "Monthly"]
                       ["quarterly" "Quarterly"]
                       ["biannually" "Twice each year"]
                       ["annually" "Annually"]
                       ["asNeeded" "As required"]
                       ["irregular" "Irregular"]
                       ["notPlanned" "None planned"]
                       ["unknown" "Unknown"]
                       ["periodic" "Periodic"]
                       ["semimonthly" "Twice a month"]
                       ["biennially" "Every 2 years"]]}]
         [:div.link-right-container [:a.link-right {:href "#what"} "Next"]]]

        :what
        [:div
         [m3/PageErrors {:page :what :path [:form]}]
         [:h2 "2. What"]
         [:span.abstract-textarea
          [m4/textarea-field-with-label
           {:form-id     [:form :state]
            :data-path   [:identificationInfo :abstract]
            :label       "Abstract"
            :placeholder nil
            :helperText  "Describe the content of the resource; e.g. what information was collected, how was it collected"
            ;; FIXME this isn't enforced.
            :maxLength   2500
            :required    true}]]
         [m3/ThemeKeywords :keywordsTheme]
         ;; FIXME Anzsrc should be optional, but this required doesn't hook up to anything.
         [m3/ThemeKeywords :keywordsThemeAnzsrc {:required false}]
         ;; TODO Add Geographic Extent vocab here.
         [m3/ThemeKeywordsExtra nil]
         [m3/TaxonKeywordsExtra nil]
         [:div.link-right-container [:a.link-right {:href "#when"} "Next"]]]

        :when
        [:div
         [m3/PageErrors {:page :when :path [:form]}]
         [:h2 "3. When was the data acquired?"]
         [m4/date-field-with-label
          {:form-id   [:form :state]
           :data-path [:identificationInfo :beginPosition]
           :label     "Start date"
           :required  true
           :minDate   "1900-01-01"
           :maxDate   "2100-01-01"}]
         [m4/date-field-with-label
          {:form-id   [:form :state]
           :data-path [:identificationInfo :endPosition]
           :label     "End date"
           :minDate   "1900-01-01"
           :maxDate   "2100-01-01"}]
         [:div.row
          [:div.col-md-4
           ;; TODO does IMAS want the old sample frequency (min daily) or this one (min <1 second)?
           [m3/NasaListSelectField {:keyword   :samplingFrequency
                                    :form-id   [:form :state]
                                    :data-path [:identificationInfo]}]]]
         [:div.link-right-container [:a.link-right {:href "#where"} "Next"]]]

        :where
        [:div
         [m3/PageErrors {:page :where :path [:form]}]
         [:h2 "4. Where"]
         ;; FIXME add toggle for satellite imagery.
         ;; FIXME hide the siteDescription textarea.
         ;; FIXME remove the "Grid to Geographic converter" link text
         [m3/GeographicCoverage nil]
         [:div.VerticalCoverage
          ;; FIXME use h3 not h4. Restyle if necessary.
          [:h4 "Vertical Coverage"]
          [:form-id [:form :state]
           mdata-etcalf3.view/CheckboxField
           [:identificationInfo :verticalElement :hasVerticalExtent]]
          ;; FIXME hide the below fields when hasVerticalExtent checkbox is unchecked.
          [m4/input-field-with-label
           {:form-id    [:form :state]
            :data-path  [:identificationInfo :verticalElement :minimumValue]
            :class      "wauto"
            :label      "Minimum (m)"
            :helperText "Shallowest depth / lowest altitude"
            :required   true}]
          [m4/input-field-with-label
           {:form-id    [:form :state]
            :data-path  [:identificationInfo :verticalElement :maximumValue]
            :class      "wauto"
            :label      "Maximum (m)"
            :helperText "Deepest depth / highest altitude"
            :required   true}]]
         [:div.link-right-container [:a.link-right {:href "#how"} "Next"]]]

        :how
        [:div
         [m3/PageErrors {:page :how :path [:form]}]
         [:h2 "5: How"]
         [:div.lineage-textarea
          [m4/textarea-field-with-label
           {:form-id     [:form :state]
            :data-path   [:resourceLineage :lineage]
            :label       "Methodological information"
            :placeholder nil
            :helperText  "Provide a brief statement of the methods used for collection of the
                         data, can include information regarding sampling equipment (collection hardware),
                         procedures, and precision/resolution of data collected."
            :required    true}]]
         [:div.link-right-container [:a.link-right {:href "#who"} "Next"]]]

        :who
        [:div
         ;; FIXME make this view configurable for IMAS, or create IMAS-specific view.
         ;; - No name lookup, just Given Name and Surname fields
         ;; - Move ORCID ID and Email field positions to match AODN
         ;; - Reduce Role codes
         ;; - Make Responsible Parties mandatory
         ;; FIXME Copy Person functionality isn't working.
         [m3/Who nil]
         [:div.link-right-container [:a.link-right {:href "#about"} "Next"]]]

        :about
        [:div
         [m3/PageErrors {:page :about :path [:form]}]
         [:h2 "7: About Dataset"]
         [:h4 "Data parameters"]
         [m3/DataParametersTable {:form-id   [:form :state]
                                  :data-path [:identificationInfo :dataParameters]}]
         [:br]
         [:h4 "Resource constraints"]
         ;; FIXME license selection isn't being included in XML export.
         [m3/select-field-with-label
          {:form-id   [:form :state]
           :data-path [:identificationInfo :creativeCommons]
           :help      [:span "Learn more about which license is right for you at "
                       [:a {:href   "https://creativecommons.org/choose/"
                            :target "_blank"}
                        "Creative Commons"]]
           :label     "License"
           :required  true
           :options   [["http://creativecommons.org/licenses/by/4.0/" "Creative Commons by Attribution (recommended​)"]
                       ["http://creativecommons.org/licenses/by-nc/4.0/" "Creative Commons, Non-commercial Use only"]
                       ["http://creativecommons.org/licenses/other" "Other constraints"]]}]
         [m4/input-field-with-label
          {:form-id     [:form :state]
           :data-path   [:identificationInfo :otherConstraints]
           :label       "Additional license requirements"   ;; FIXME
           :placeholder "Enter additional license requirements"
           :required    true}]

         [m3/UseLimitations {:form-id   [:form :state]
                             :data-path [:identificationInfo :useLimitations]}]
         [:br]
         [:h4 "Supplemental information"]
         [:form-id [:form :state]
          mdata-etcalf3.view/IMASSupplementalInformation [:identificationInfo :supplementalInformation]]
         [m3/IMASSupportingResource {:form-id   [:form :state]
                                     :data-path [:supportingResources]}]
         [:br]
         [:h4 "Distribution"]
         [m4/input-field-with-label
          {:form-id     [:form :state]
           :data-path   [:distributionInfo :distributionFormat :name]
           :label       "Data file format"
           :placeholder "e.g. Microsoft Excel, CSV, NetCDF"
           :helperText  nil
           :toolTip     nil
           :maxLength   100
           :required    nil}]
         [m4/input-field-with-label
          {:form-id     [:form :state]
           :data-path   [:distributionInfo :distributionFormat :version]
           :label       "Data file format date/version"
           :placeholder "Date format date or version if applicable"
           :helperText  nil
           :toolTip     nil
           :maxLength   20
           :required    nil}]
         [:div.link-right-container [:a.link-right {:href "#upload"} "Next"]]]

        :upload
        [:div
         [m3/PageErrors {:page :upload :path [:form]}]
         [:h2 "8: Upload Data"]
         [m3/UploadData nil]
         [:h2 "Data Services"]
         ;; FIXME reduce protocol options to the below for IMAS:
         ;; [["OGC:WMS-1.3.0-http-get-map" "OGC Web Map Service (WMS)"]
         ;;  ["OGC:WFS-1.0.0-http-get-capabilities" "OGC Web Feature Service (WFS)"]
         ;;  ["WWW:LINK-1.0-http--downloaddata" "Other/unknown"]]
         [m3/DataSources {:form-id   [:form :state]
                          :data-path [:dataSources]}]
         [:div.link-right-container [:a.link-right {:href "#lodge"} "Next"]]]

        :lodge
        [:div
         [m3/PageErrors {:page :lodge :path [:form]}]
         [:h2 "9: Lodge Metadata Draft"]
         [m3/IMASLodge nil]]})
