(ns metcalf.tern.config
  (:require [metcalf3.fx :as fx]
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
(rf/reg-event-fx :date-field/value-change handlers3/date-field-value-change)
(rf/reg-event-fx ::views/date-field-with-label-value-changed handlers3/date-field-value-change)
(rf/reg-event-fx :textarea-field/value-change handlers3/textarea-field-value-change)
(rf/reg-event-fx ::views/textarea-field-with-label-value-changed handlers3/textarea-field-value-change)
(rf/reg-event-fx :handlers/set-geographic-element handlers3/set-geographic-element)
(rf/reg-event-fx :handlers/person-detail-changed handlers3/person-detail-changed)
(rf/reg-event-fx :handlers/value-changed handlers3/value-changed)
(rf/reg-event-fx ::views/input-field-with-label-value-changed handlers3/value-changed)
(rf/reg-event-fx :handlers/set-tab handlers3/set-tab)
(rf/reg-event-fx :handlers/load-errors handlers3/load-errors)
(rf/reg-event-fx :handlers/add-keyword-extra handlers3/add-keyword-extra)
(rf/reg-event-fx :handlers/del-keyword-extra handlers3/del-keyword-extra)
(rf/reg-event-fx :handlers/add-nodes handlers3/add-nodes)
(rf/reg-event-fx :handlers/dashboard-create-click handlers3/dashboard-create-click)
(rf/reg-event-fx :handlers/transite-doc-success handlers3/transite-doc-success)
(rf/reg-event-fx :handlers/lodge-submit-success handlers3/lodge-submit-success)
(rf/reg-event-fx :metcalf.tern.core/init-db handlers3/init-db)
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
(rf/reg-event-fx ::components4/input-field-with-label-value-changed handlers4/value-changed-handler)
(rf/reg-event-fx ::components4/textarea-field-with-label-value-changed handlers4/value-changed-handler)
(rf/reg-event-fx ::components4/yes-no-field-value-changed handlers4/value-changed-handler)
(rf/reg-event-fx ::components4/date-field-with-label-value-changed handlers4/value-changed-handler)
(rf/reg-event-fx ::components4/select-option-with-label-value-changed handlers4/value-changed-handler)
(rf/reg-event-fx ::components4/async-select-option-with-label-value-changed handlers4/value-changed-handler)
(rf/reg-event-fx ::components4/select-value-with-label-changed handlers4/value-changed-handler)
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
(rf/reg-sub ::views/get-input-field-with-label-props :<- [:subs/get-derived-state] subs3/get-input-field-with-label-props)
(rf/reg-sub :subs/get-page-props subs3/get-page-props)
(rf/reg-sub :subs/get-page-name subs3/get-page-name)
(rf/reg-sub :subs/get-modal-props subs3/get-modal-props)
(rf/reg-sub :subs/get-dashboard-props subs3/get-dashboard-props)
(rf/reg-sub :subs/get-edit-tab-props :<- [:subs/get-page-props] :<- [:subs/get-derived-state] subs3/get-edit-tab-props)
(rf/reg-sub :progress/get-props :<- [:subs/get-derived-state] subs3/get-progress-props)
(rf/reg-sub :date-field/get-props :<- [:subs/get-derived-state] subs3/get-date-field-props)
(rf/reg-sub ::views/get-date-field-with-label-props :<- [:subs/get-derived-state] subs3/get-date-field-with-label-props)
(rf/reg-sub :textarea-field/get-props :<- [:subs/get-derived-state] subs3/get-textarea-field-props)
(rf/reg-sub ::views/get-textarea-field-with-label-props :<- [:subs/get-derived-state] subs3/get-textarea-field-with-label-props)
(rf/reg-sub :textarea-field/get-many-field-props :<- [:subs/get-derived-state] subs3/get-textarea-field-many-props)
(rf/reg-sub :subs/get-form-tick subs3/get-form-tick)
(rf/reg-sub :help/get-menuitems subs3/get-menuitems)
(rf/reg-sub :subs/platform-selected? subs3/platform-selected?)
(rf/reg-sub ::subs4/get-form-state subs4/get-form-state)
(rf/reg-sub ::components4/get-input-field-with-label-props subs4/form-state-signal subs4/get-block-props-sub)
(rf/reg-sub ::components4/get-textarea-field-with-label-props subs4/form-state-signal subs4/get-block-props-sub)
(rf/reg-sub ::components4/get-yes-no-field-props subs4/form-state-signal subs4/get-block-props-sub)
(rf/reg-sub ::components4/get-date-field-with-label-props subs4/form-state-signal subs4/get-block-props-sub)
(rf/reg-sub ::components4/get-async-select-option-with-label-props subs4/form-state-signal subs4/get-block-props-sub)
(rf/reg-sub ::components4/get-select-option-with-label-props subs4/form-state-signal subs4/get-block-props-sub)
(rf/reg-sub ::components4/get-select-value-with-label-props subs4/form-state-signal subs4/get-block-props-sub)
(rf/reg-sub ::views/get-props subs4/form-state-signal subs4/get-block-props-sub)
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
      {
       'm4/textarea-field-with-label      components4/textarea-field-with-label
       'm4/input-field-with-label         components4/input-field-with-label
       'm4/date-field-with-label          components4/date-field-with-label
       'm4/select-value-with-label        components4/select-value-with-label
       'm4/select-option-with-label       components4/select-option-with-label
       'm4/async-select-option-with-label components4/async-select-option-with-label
       'm4/yes-no-field                   components4/yes-no-field
       'm4/page-errors                    components4/page-errors
       })
(set! low-code/template-registry
      '{:data-identification
        [:div
         [m4/page-errors
          {:form-id    [:form]
           :data-paths [[:identificationInfo :title]
                        [:identificationInfo :dateCreation]
                        [:identificationInfo :topicCategory]
                        [:identificationInfo :status]
                        [:identificationInfo :maintenanceAndUpdateFrequency]
                        [:identificationInfo :version]]}]
         [:h2 "1. Data Identification"]
         [m4/input-field-with-label
          {:form-id     [:form]
           :data-path   [:identificationInfo :title]
           :label       "Title"
           :placeholder "Provide a descriptive title for the data set including the subject of study, the study location and time period. Example: TERN OzFlux Arcturus Emerald Tower Site 2014-ongoing"
           :helperText  "Clear and concise description of the content of the resource including What, Where, (How), When e.g. Fractional Cover for Australia 2014 ongoing"}]
         [m4/date-field-with-label
          {:form-id   [:form]
           :data-path [:identificationInfo :dateCreation]
           :label     "Date the resource was created"
           :required  true
           :minDate   "1900-01-01"
           :maxDate   "2100-01-01"}]
         [m3/TopicCategories {:form-id   [:form]
                              :data-path [:identificationInfo :topicCategory]}]
         [m3/SelectField {:form-id   [:form]
                          :data-path [:identificationInfo :status]}]
         [m3/SelectField {:form-id   [:form]
                          :data-path [:identificationInfo :maintenanceAndUpdateFrequency]}]
         [m4/input-field-with-label
          {:form-id    [:form]
           :data-path  [:identificationInfo :version]
           :label      "Version"
           :helperText "Version number of the resource"
           :required   true}]
         [m4/yes-no-field
          {:form-id [:form]
           :data-path [:identificationInfo :previouslyPublishedFlag]
           :label "Has the data been published before?"}]
         ;; FIXME: I think this should be formatted as YYYY or YYYY-MM (according to the commented template)
         [m4/date-field-with-label
          {:form-id   [:form]
           :data-path [:identificationInfo :datePublication]
           :label     "Previous Publication Date"
           :required  true
           :minDate   "1900-01-01"
           :maxDate   "2100-01-01"}]
         [:div.link-right-container [:a.link-right {:href "#what"} "Next"]]]

        :what
        [:div
         [m4/page-errors
          {:form-id    [:form]
           :data-paths [[:identificationInfo :abstract]
                        [:identificationInfo :purpose]]}]
         [:h2 "2. What"]
         [m4/textarea-field-with-label
          {:form-id     [:form]
           :data-path   [:identificationInfo :abstract]
           :label       "Abstract"
           :placeholder "Provide a brief summary of What, Where, When, Why, Who and How for the collected the data."
           :helperText  "Describe the content of the resource; e.g. what information was collected, how was it collected"
           :toolTip     "Example: The Arcturus greenhouse gas (GHG) monitoring station was established in July 2010 48 km southeast of Emerald, Queensland, with flux tower measurements starting in June 2011 until early 2014. The station was part of a collaborative project between Geoscience Australia (GA) and CSIRO Marine and Atmospheric Research (CMAR). Elevation of the site was approximately 170m asl and mean annual precipitation was 572mm. The tower borderered 2 land use types split N-S: To the west lightly forested tussock grasslands; To the east crop lands, cycling through fallow periods.The instruments were installed on a square lattice tower with an adjustable pulley lever system to raise and lower the instrument arm. The tower was 5.6m tall with the instrument mast extending a further 1.1m above, totalling a height of 6.7m. Fluxes of heat, water vapour, methane and carbon dioxide were measured using the open-path eddy flux technique. Supplementary measurements above the canopy included temperature, humidity, windspeed, wind direction, rainfall, and the 4 components of net radiation. Soil heat flux, soil moisture and soil temperature measurements were also collected."
           :maxLength   2500
           :required    true}]
         [m4/textarea-field-with-label
          {:form-id     [:form]
           :data-path   [:identificationInfo :purpose]
           :label       "Purpose"
           :placeholder "Provide a brief summary of the purpose for collecting the data including the potential use."
           :maxLength   1000
           :helperText  "Brief statement about the purpose of the study"
           :toolTip     "The Arcturus flux station data was collected to gain an understanding of natural background carbon dioxide and methane fluxes in the region prior to carbon sequestration and coal seam gas activities take place and to assess the feasibility of using this type of instrumentation for baseline studies prior to industry activities that will be required to monitor and assess CO2 or CH4 leakage to atmosphere in the future"}]
         [m3/ThemeKeywords
          {:keyword-type        :keywordsTheme
           :keywords-theme-path [:form :fields :identificationInfo :keywordsTheme]}]
         [m3/ThemeKeywords
          {:keyword-type        :keywordsThemeAnzsrc
           :keywords-theme-path [:form :fields :identificationInfo :keywordsThemeAnzsrc]}]
         [m3/ThemeKeywordsExtra
          {}]
         [m3/TaxonKeywordsExtra
          {}]
         [:div.link-right-container [:a.link-right {:href "#when"} "Next"]]]

        :when
        [:div
         [m4/page-errors
          {:form-id    [:form]
           :data-paths [[:identificationInfo :beginPosition]
                        [:identificationInfo :endPosition]]}]
         [:h2 "3. When was the data acquired?"]
         [m4/date-field-with-label
          {:form-id   [:form]
           :data-path [:identificationInfo :beginPosition]
           :label     "Start date"
           :required  true
           :minDate   "1900-01-01"
           :maxDate   "2100-01-01"}]
         [m4/date-field-with-label
          {:form-id   [:form]
           :data-path [:identificationInfo :endPosition]
           :label     "End date"
           :required  true
           :minDate   "1900-01-01"
           :maxDate   "2100-01-01"}]
         [:div.row
          [:div.col-md-4
           [m3/NasaListSelectField {:keyword   :samplingFrequency
                                    :form-id   [:form]
                                    :data-path [:identificationInfo]}]]]
         [:div.link-right-container [:a.link-right {:href "#where"} "Next"]]]

        :where
        [:div
         [m4/page-errors
          {:form-id    [:form]
           :data-paths [[:identificationInfo :geographicElement :siteDescription]]}]
         [:h2 "4. Where"]
         [m4/textarea-field-with-label
          {:form-id     [:form]
           :data-path   [:identificationInfo :geographicElement :siteDescription]
           :label       "Site Description"
           :placeholder "A descriptive reference for the coverage. May include a project code. Example: Geelong (Site: G145), VIC, Australia"}]
         [m3/GeographicCoverage
          {:has-coverage-path     [:form :fields :identificationInfo :geographicElement :hasGeographicCoverage]
           :boxes-path            [:form :fields :identificationInfo :geographicElement :boxes]
           :site-description-path [:form :fields :identificationInfo :geographicElement :siteDescription]}]

         [:div.VerticalCoverage
          [:h4 "Vertical Coverage"]
          [CheckboxField
           {:path [:form :fields :identificationInfo :verticalElement :hasVerticalExtent]}]
          [:div
           [SelectField {:path [:form :fields :identificationInfo :verticalElement :method]}]
           [InputField
            {:path  [:form :fields :identificationInfo :verticalElement :elevation]
             :class "wauto"}]
           [InputField
            {:path  [:form :fields :identificationInfo :verticalElement :minimumValue]
             :class "wauto"}]
           [InputField
            {:path  [:form :fields :identificationInfo :verticalElement :maximumValue]
             :class "wauto"}]]]

         [:div.link-right-container [:a.link-right {:href "#who"} "Next"]]]

        :who
        [:div
         [m3/Who
          {:credit-path [:form :fields :identificationInfo :credit]}]
         [:div.link-right-container [:a.link-right {:href "#how"} "Next"]]]

        :how
        [:div
         [m4/page-errors
          {:form-id    [:form]
           :data-paths [[:resourceLineage :processStep]
                        [:dataQualityInfo :methods]
                        [:dataQualityInfo :results]]}]
         [:h2 "6: How"]
         [m3/Methods {:form-id   [:form]
                      :data-path [:resourceLineage :processStep]}]
         [m4/textarea-field-with-label
          {:form-id     [:form]
           :data-path   [:dataQualityInfo :methods]
           :placeholder "Placeholder text=Provide a brief summary of the source of the data and related collection and/or processing methods."
           :label       "Method"}]
         [m4/textarea-field-with-label
          {:form-id     [:form]
           :data-path   [:dataQualityInfo :results]
           :label       "Data Quality Results"
           :placeholder "Provide a statement regarding the data quality assessment results."
           :toolTip     "Example: RMSE relative to reference data set; horizontal or vertical positional accuracy; etc."
           :maxLength   1000
           :rows        20}]
         [:div.link-right-container [:a.link-right {:href "#quality"} "Next"]]]

        :quality
        [:div
         [m4/page-errors
          {:form-id    [:form]
           :data-paths [[:dataQualityInfo :methodSummary]
                        [:dataQualityInfo :results]]}]
         [:h2 "7. Data Quality"]
         [:i "This section is optional"]
         [m4/textarea-field-with-label
          {:form-id     [:form]
           :data-path   [:dataQualityInfo :methodSummary]
           :label       "Provide a summary of the scope of the Data Quality Assessment"
           :maxLength   1000
           :placeholder "The data quality was assessed by ..."}]
         [m4/textarea-field-with-label
          {:form-id     [:form]
           :data-path   [:dataQualityInfo :results]
           :label       "Provide a statement regarding the Data Quality Assessment outcome"
           :maxLength   1000
           :placeholder "A statement regarding the data quality assessment results. Examples: RMSE relative to reference data set; horizontal or vertical positional accuracy; etc."}]
         [:div.link-right-container [:a.link-right {:href "#about"} "Next"]]]

        :about
        [:div
         [m4/page-errors
          {:form-id    [:form]
           :data-paths [[:identificationInfo :environment]
                        [:identificationInfo :supplemental]
                        [:identificationInfo :resourceSpecificUsage]
                        [:identificationInfo :credit]
                        [:identificationInfo :customCitation]]}]
         [:h2 "8: About Dataset"]
         [:h4 "Environment Description (Optional)"]
         [m4/textarea-field-with-label
          {:form-id     [:form]
           :data-path   [:identificationInfo :environment]
           :label       "Environmental description"
           :placeholder "Information about the source and software to process the resource"
           :helperText  "Software, computer operating system, file name, or dataset size"
           :maxLength   1000}]
         [:h4 "Association Documentation (Optional)"]
         [m4/textarea-field-with-label
          {:form-id     [:form]
           :data-path   [:identificationInfo :supplemental]
           :label       "Supplemental Information"
           :placeholder "Information about how to interpret the resource, example: Pixel value indicates the number of days since reference date 1970-01-01"
           :helperText  "Any supplemental information needed to interpret the resource"
           :maxLength   1000}]
         [:h4 "Resource specific usage (Optional)"]
         [m4/textarea-field-with-label
          {:form-id     [:form]
           :data-path   [:identificationInfo :resourceSpecificUsage]
           :label       "Resource specific usage"
           :placeholder "Resource specific usage..."
           :helperText  "What can this resource be used for environmental research?"
           :maxLength   1000}]
         [:h4 "Acknowledgment (Optional)"]
         [m4/textarea-field-with-label
          {:form-id     [:form]
           :data-path   [:identificationInfo :credit]
           :label       "Acknowledgment"
           :placeholder "The project was funded by xxx and yyy"
           :helperText  "Write a sentence acknowledging sponsors, data providers or funding organisations"
           :maxLength   1000}]
         [:h4 "Citation (Optional)"]
         [m4/textarea-field-with-label
          {:form-id    [:form]
           :data-path  [:identificationInfo :customCitation]
           :label      "Specific citation"
           :helperText "The format of the standard citation is provided at https://ternaus.atlassian.net/wiki/spaces/TERNSup/pages/1223163969/How+is+the+citation+constructed+from+the+metadata  For a non-standard citation, provide the details below."
           :maxLength  1000}]

         [:h4 "Data parameters"]
         [m3/DataParametersTable {:form-id   [:form]
                                  :data-path [:identificationInfo :dataParameters]}]
         [:br]
         [:h4 "Pixel Size"]
         [:div.row
          [:div.col-md-6
           [m3/NasaListSelectField {:keyword   :horizontalResolution
                                    :form-id   [:form]
                                    :data-path [:identificationInfo]}]]]
         [:br]
         [:h4 "Resource constraints"]
         [m3/ResourceConstraints]
         [m3/UseLimitations {:form-id   [:form]
                             :data-path [:identificationInfo :useLimitations]}]
         [:br]
         [:h4 "Supplemental information"]
         [m3/SupportingResource {:form-id   [:form]
                                 :data-path [:supportingResources]}]
         [:form-id [:form]
          [m3/SupplementalInformation [:identificationInfo :supplementalInformation]]]
         [:br]
         [:h4 "Distribution"]
         [m4/input-field-with-label
          {:form-id     [:form]
           :data-path   [:distributionInfo :distributionFormat :name]
           :label       "Data file format"
           :placeholder "e.g. Microsoft Excel, CSV, NetCDF"
           :helperText  nil
           :toolTip     nil
           :maxLength   100
           :required    nil}]
         [m4/input-field-with-label
          {:form-id     [:form]
           :data-path   [:distributionInfo :distributionFormat :version]
           :label       "Data file format date/version"
           :placeholder "Date format date or version if applicable"
           :helperText  nil
           :toolTip     nil
           :maxLength   20
           :required    nil}]
         [m4/textarea-field-with-label
          {:form-id     [:form]
           :data-path   [:resourceLineage :lineage]
           :label       "Lineage"
           :placeholder "Provide a brief summary of the source of the data and related collection and/or processing methods."
           :toolTip     "Example: Data was collected at the site using the methods described in yyy Manual, refer to https://doi.org/10.5194/bg-14-2903-2017"
           :maxLength   1000}]
         [:div.link-right-container [:a.link-right {:href "#upload"} "Next"]]]

        :upload
        [:div
         [m4/page-errors {:form-id [:form] :data-paths []}]
         [:h2 "9. Data Sources"]
         [m3/UploadData
          {:attachments-path [:form :fields :attachments]}]
         [:h2 "Data Services"]
         [m3/DataSources {:form-id   [:form]
                          :data-path [:dataSources]}]
         [:div.link-right-container [:a.link-right {:href "#lodge"} "Next"]]]

        :lodge
        [:div
         [m4/page-errors {:form-id [:form] :data-paths []}]
         [:h2 "10: Lodge Metadata Draft"]
         [m3/Lodge
          {:note-for-data-manager-path [:form :fields :noteForDataManager]
           :agreed-to-terms-path       [:form :fields :agreedToTerms]
           :doi-requested-path         [:form :fields :doiRequested]
           :current-doi-path           [:form :fields :identificationInfo :doi]}]]})
