(ns metcalf.tern.config
  (:require [metcalf.common.low-code :as low-code]
            [metcalf.common.rules :as rules]
            [metcalf3.fx :as fx]
            [metcalf3.handlers :as handlers]
            [metcalf3.ins :as ins]
            [metcalf3.subs :as subs]
            [metcalf3.views :as views]
            [metcalf.common.components :as common-components]
            [metcalf.common.handlers :as common-handlers]
            [metcalf.common.subs :as common-subs]
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
(rf/reg-event-fx :date-field/value-change handlers/date-field-value-change)
(rf/reg-event-fx ::views/date-field-with-label-value-change handlers/date-field-value-change)
(rf/reg-event-fx :textarea-field/value-change handlers/textarea-field-value-change)
(rf/reg-event-fx ::views/textarea-field-with-label-value-changed handlers/textarea-field-value-change)
(rf/reg-event-fx :handlers/set-geographic-element handlers/set-geographic-element)
(rf/reg-event-fx :handlers/person-detail-changed handlers/person-detail-changed)
(rf/reg-event-fx :handlers/value-changed handlers/value-changed)
(rf/reg-event-fx ::views/input-field-with-label-value-changed handlers/value-changed)
(rf/reg-event-fx :handlers/set-tab handlers/set-tab)
(rf/reg-event-fx :handlers/load-errors handlers/load-errors)
(rf/reg-event-fx :handlers/add-keyword-extra handlers/add-keyword-extra)
(rf/reg-event-fx :handlers/del-keyword-extra handlers/del-keyword-extra)
(rf/reg-event-fx :handlers/add-nodes handlers/add-nodes)
(rf/reg-event-fx :handlers/dashboard-create-click handlers/dashboard-create-click)
(rf/reg-event-fx :handlers/transite-doc-success handlers/transite-doc-success)
(rf/reg-event-fx :handlers/lodge-submit-success handlers/lodge-submit-success)
(rf/reg-event-fx :metcalf.tern.core/init-db handlers/init-db)
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
(rf/reg-sub ::views/get-input-field-with-label-props :<- [:subs/get-derived-state] subs/get-input-field-with-label-props)
(rf/reg-sub :subs/get-page-props subs/get-page-props)
(rf/reg-sub :subs/get-page-name subs/get-page-name)
(rf/reg-sub :subs/get-modal-props subs/get-modal-props)
(rf/reg-sub :subs/get-dashboard-props subs/get-dashboard-props)
(rf/reg-sub :subs/get-edit-tab-props :<- [:subs/get-page-props] :<- [:subs/get-derived-state] subs/get-edit-tab-props)
(rf/reg-sub :progress/get-props :<- [:subs/get-derived-state] subs/get-progress-props)
(rf/reg-sub :date-field/get-props :<- [:subs/get-derived-state] subs/get-date-field-props)
(rf/reg-sub ::views/get-date-field-with-label-props :<- [:subs/get-derived-state] subs/get-date-field-with-label-props)
(rf/reg-sub :textarea-field/get-props :<- [:subs/get-derived-state] subs/get-textarea-field-props)
(rf/reg-sub ::views/get-textarea-field-with-label-props :<- [:subs/get-derived-state] subs/get-textarea-field-with-label-props)
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
      {'m3/DataParametersTable       views/DataParametersTable
       'm3/date-field-with-label     views/date-field-with-label
       'm3/textarea-field            views/textarea-field
       'm3/textarea-field-with-label views/textarea-field-with-label
       'm3/Methods                   views/Methods
       'm3/UseLimitations            views/UseLimitations
       'm3/SelectField               views/SelectField
       'm3/NasaListSelectField       views/NasaListSelectField
       'm3/GeographicCoverage        views/GeographicCoverage
       'm3/DataSources               views/DataSources
       'm3/PageErrors                views/PageErrors
       'm3/VerticalCoverage          views/VerticalCoverage
       'm3/TopicCategories           views/TopicCategories
       'm3/ResourceConstraints       views/ResourceConstraints
       'm3/Lodge                     views/Lodge
       'm3/SupportingResource        views/SupportingResource
       'm3/SupplementalInformation   views/SupplementalInformation
       'm3/ThemeKeywords             views/ThemeKeywords
       'm3/UploadData                views/UploadData
       'm3/TaxonKeywordsExtra        views/TaxonKeywordsExtra
       'm3/Who                       views/Who
       'm3/ThemeKeywordsExtra        views/ThemeKeywordsExtra
       'm4/textarea-field-with-label common-components/textarea-field-with-label
       'm4/input-field-with-label    common-components/input-field-with-label
       'm4/date-field-with-label     common-components/date-field-with-label
       })
(set! low-code/template-registry
      '{:data-identification
        [:div
         [m3/PageErrors {:page :data-identification :path [:form]}]
         [:h2 "1. Data Identification"]
         [m3/input-field-with-label
          {:form-id     [:form :state]
           :data-path   [:identificationInfo :title]
           :label       "Title"
           :placeholder "Provide a descriptive title for the data set including the subject of study, the study location and time period. Example: TERN OzFlux Arcturus Emerald Tower Site 2014-ongoing"
           :helperText  "Clear and concise description of the content of the resource including What, Where, (How), When e.g. Fractional Cover for Australia 2014 ongoing"}]
         [m3/date-field-with-label
          {:form-id   [:form :state]
           :data-path [:identificationInfo :dateCreation]
           :label     "Date the resource was created"
           :required  true
           :minDate   #inst "1900-01-01"
           :maxDate   #inst "2100-01-01"}]
         [m3/TopicCategories {:form-id   [:form :state]
                              :data-path [:identificationInfo :topicCategory]}]
         [m3/SelectField {:form-id   [:form :state]
                          :data-path [:identificationInfo :status]}]
         [m3/SelectField {:form-id   [:form :state]
                          :data-path [:identificationInfo :maintenanceAndUpdateFrequency]}]
         [:div.link-right-container [:a.link-right {:href "#what"} "Next"]]]

        :what
        [:div
         [m3/PageErrors {:page :what :path [:form]}]
         [:h2 "2. What"]
         [m3/textarea-field-with-label
          {:form-id     [:form :state]
           :data-path   [:identificationInfo :abstract]
           :label       "Abstract"
           :placeholder "Provide a brief summary of What, Where, When, Why, Who and How for the collected the data."
           :helperText  "Describe the content of the resource; e.g. what information was collected, how was it collected"
           :toolTip     "Example: The Arcturus greenhouse gas (GHG) monitoring station was established in July 2010 48 km southeast of Emerald, Queensland, with flux tower measurements starting in June 2011 until early 2014. The station was part of a collaborative project between Geoscience Australia (GA) and CSIRO Marine and Atmospheric Research (CMAR). Elevation of the site was approximately 170m asl and mean annual precipitation was 572mm. The tower borderered 2 land use types split N-S: To the west lightly forested tussock grasslands; To the east crop lands, cycling through fallow periods.The instruments were installed on a square lattice tower with an adjustable pulley lever system to raise and lower the instrument arm. The tower was 5.6m tall with the instrument mast extending a further 1.1m above, totalling a height of 6.7m. Fluxes of heat, water vapour, methane and carbon dioxide were measured using the open-path eddy flux technique. Supplementary measurements above the canopy included temperature, humidity, windspeed, wind direction, rainfall, and the 4 components of net radiation. Soil heat flux, soil moisture and soil temperature measurements were also collected."
           :maxLength   2500
           :required    true}]
         [m3/textarea-field-with-label
          {:form-id     [:form :state]
           :data-path   [:identificationInfo :purpose]
           :label       "Purpose"
           :placeholder "Provide a brief summary of the purpose for collecting the data including the potential use."
           :maxLength   1000
           :helperText  "Brief statement about the purpose of the study"
           :toolTip     "The Arcturus flux station data was collected to gain an understanding of natural background carbon dioxide and methane fluxes in the region prior to carbon sequestration and coal seam gas activities take place and to assess the feasibility of using this type of instrumentation for baseline studies prior to industry activities that will be required to monitor and assess CO2 or CH4 leakage to atmosphere in the future"}]
         [m3/ThemeKeywords :keywordsTheme]
         [m3/ThemeKeywords :keywordsThemeAnzsrc]
         [m3/ThemeKeywordsExtra nil]
         [m3/TaxonKeywordsExtra nil]
         [:div.link-right-container [:a.link-right {:href "#when"} "Next"]]]

        :when
        [:div
         [m3/PageErrors {:page :when :path [:form]}]
         [:h2 "3. When was the data acquired?"]
         [m3/date-field-with-label
          {:form-id   [:form :state]
           :data-path [:identificationInfo :beginPosition]
           :label     "Start date"
           :required  true
           :minDate   #inst "1900-01-01"
           :maxDate   #inst "2100-01-01"}]
         [m3/date-field-with-label
          {:form-id   [:form :state]
           :data-path [:identificationInfo :endPosition]
           :label     "End date"
           :required  true
           :minDate   #inst "1900-01-01"
           :maxDate   #inst "2100-01-01"}]
         [:div.row
          [:div.col-md-4
           [m3/NasaListSelectField {:keyword   :samplingFrequency
                                    :form-id   [:form :state]
                                    :data-path [:identificationInfo]}]]]
         [:div.link-right-container [:a.link-right {:href "#where"} "Next"]]]

        :where
        [:div
         [m3/PageErrors {:page :where :path [:form]}]
         [:h2 "4. Where"]
         [m3/GeographicCoverage nil]
         [m3/VerticalCoverage]
         [:div.link-right-container [:a.link-right {:href "#how"} "Next"]]]

        :who
        [:div
         [m3/Who nil]
         [:div.link-right-container [:a.link-right {:href "#about"} "Next"]]]

        :how
        [:div
         [m3/PageErrors {:page :how :path [:form]}]
         [:h2 "5: How"]
         [m3/Methods {:form-id   [:form :state]
                      :data-path [:resourceLineage :processStep]}]
         [m3/textarea-field-with-label
          {:form-id   [:form :state]
           :data-path [:dataQualityInfo :methods]
           :label     "Method"}]
         [m3/textarea-field-with-label
          {:form-id     [:form :state]
           :data-path   [:dataQualityInfo :results]
           :label       "Data Quality Results"
           :placeholder "Provide a statement regarding the data quality assessment results."
           :toolTip     "Example: RMSE relative to reference data set; horizontal or vertical positional accuracy; etc."
           :maxLength   1000
           :rows        20}]
         [:div.link-right-container [:a.link-right {:href "#who"} "Next"]]]

        :about
        [:div
         [m3/PageErrors {:page :about :path [:form]}]
         [:h2 "7: About Dataset"]
         [:h4 "Data parameters"]
         [m3/DataParametersTable {:form-id   [:form :state]
                                  :data-path [:identificationInfo :dataParameters]}]
         [:br]
         [:h4 "Pixel Size"]
         [:div.row
          [:div.col-md-6
           [m3/NasaListSelectField {:keyword   :horizontalResolution
                                    :form-id   [:form :state]
                                    :data-path [:identificationInfo]}]]]
         [:br]
         [:h4 "Resource constraints"]
         [m3/ResourceConstraints]
         [m3/UseLimitations {:form-id   [:form :state]
                             :data-path [:identificationInfo :useLimitations]}]
         [:br]
         [:h4 "Supplemental information"]
         [m3/SupportingResource {:form-id   [:form :state]
                                 :data-path [:supportingResources]}]
         [:form-id [:form :state]
          mdata-etcalf3.view/SupplementalInformation [:identificationInfo :supplementalInformation]]
         [:br]
         [:h4 "Distribution"]
         [m3/input-field-with-label
          {:form-id     [:form :state]
           :data-path   [:distributionInfo :distributionFormat :name]
           :label       "Data file format"
           :placeholder "e.g. Microsoft Excel, CSV, NetCDF"
           :helperText  nil
           :toolTip     nil
           :maxLength   100
           :required    nil}]
         [m3/input-field-with-label
          {:form-id     [:form :state]
           :data-path   [:distributionInfo :distributionFormat :version]
           :label       "Data file format date/version"
           :placeholder "Date format date or version if applicable"
           :helperText  nil
           :toolTip     nil
           :maxLength   20
           :required    nil}]
         [m3/textarea-field-with-label
          {:form-id     [:form :state]
           :data-path   [:resourceLineage :lineage]
           :label       "Lineage"
           :placeholder "Provide a brief summary of the source of the data and related collection and/or processing methods."
           :toolTip     "Example: Data was collected at the site using the methods described in yyy Manual, refer to https://doi.org/10.5194/bg-14-2903-2017"
           :maxLength   1000}]
         [:div.link-right-container [:a.link-right {:href "#upload"} "Next"]]]

        :upload
        [:div
         [m3/PageErrors {:page :upload :path [:form]}]
         [:h2 "8: Upload Data"]
         [m3/UploadData nil]
         [:h2 "Data Services"]
         [m3/DataSources {:form-id   [:form :state]
                          :data-path [:dataSources]}]
         [:div.link-right-container [:a.link-right {:href "#lodge"} "Next"]]]

        :lodge
        [:div
         [m3/PageErrors {:page :lodge :path [:form]}]
         [:h2 "9: Lodge Metadata Draft"]
         [m3/Lodge nil]]})