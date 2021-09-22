(ns metcalf.tern.config
  (:require [metcalf3.low-code :as low-code]
            [metcalf3.fx :as fx]
            [metcalf3.handlers :as handlers]
            [metcalf3.ins :as ins]
            [metcalf3.subs :as subs]
            [metcalf3.views :as views]
            [re-frame.core :as rf]))

(rf/reg-event-fx :handlers/load-api-options-resp handlers/load-api-options-resp)
(rf/reg-event-fx :handlers/load-es-options-resp handlers/load-es-options-resp)
(rf/reg-event-fx :handlers/close-modal handlers/close-modal)
(rf/reg-event-fx :handlers/close-and-cancel handlers/close-and-cancel)
(rf/reg-event-fx :handlers/close-and-confirm handlers/close-and-confirm)
(rf/reg-event-fx :handlers/open-modal handlers/open-modal-handler)
(rf/reg-event-db :handlers/del-value handlers/del-value)
(rf/reg-event-db :handlers/new-field! handlers/new-field!)
(rf/reg-event-db :handlers/add-field! handlers/add-field!)
(rf/reg-event-db :handlers/add-value! handlers/add-value!)
(rf/reg-event-db :handlers/save-current-document-success handlers/save-current-document-success)
(rf/reg-event-db :handlers/add-attachment handlers/add-attachment)
(rf/reg-event-db :handlers/back handlers/back)
(rf/reg-event-db :handlers/update-dp-term handlers/update-dp-term)
(rf/reg-event-db :handlers/update-nasa-list-value handlers/update-nasa-list-value)
(rf/reg-event-db :handlers/update-boxes handlers/update-boxes)
(rf/reg-event-db :handlers/update-method-term handlers/update-method-term)
(rf/reg-event-db :handlers/update-method-name handlers/update-method-name)
(rf/reg-event-db :handlers/setter handlers/setter)
(rf/reg-event-db :handlers/check-unsaved-keyword-input handlers/check-unsaved-keyword-input)
(rf/reg-event-db :handlers/remove-party handlers/remove-party)
(rf/reg-event-db :handlers/reset-form handlers/reset-form)
(rf/reg-event-db :handlers/show-errors handlers/show-errors)
(rf/reg-event-db :handlers/hide-errors handlers/hide-errors)
(rf/reg-event-db :handlers/toggle-status-filter handlers/toggle-status-filter)
(rf/reg-event-db :handlers/show-all-documents handlers/show-all-documents)
(rf/reg-event-db :handlers/load-error-page handlers/load-error-page)
(rf/reg-event-db :handlers/set-value handlers/set-value)
(rf/reg-event-db :date-field/value-change handlers/date-field-value-change)
(rf/reg-event-db ::views/date-field-with-label-value-change handlers/date-field-value-change)
(rf/reg-event-db :textarea-field/value-change handlers/textarea-field-value-change)
(rf/reg-event-db ::views/textarea-field-with-label-value-changed handlers/textarea-field-value-change)
(rf/reg-event-db :handlers/set-geographic-element handlers/set-geographic-element)
(rf/reg-event-db :handlers/person-detail-changed handlers/person-detail-changed)
(rf/reg-event-db :handlers/value-changed handlers/value-changed)
(rf/reg-event-db ::views/input-field-with-label-value-changed handlers/value-changed)
(rf/reg-event-db :handlers/set-tab handlers/set-tab)
(rf/reg-event-db :handlers/load-errors handlers/load-errors)
(rf/reg-event-db :handlers/add-keyword-extra handlers/add-keyword-extra)
(rf/reg-event-db :handlers/del-keyword-extra handlers/del-keyword-extra)
(rf/reg-event-db :handlers/add-nodes handlers/add-nodes)
(rf/reg-event-db :handlers/dashboard-create-click handlers/dashboard-create-click)
(rf/reg-event-db :handlers/transite-doc-success handlers/transite-doc-success)
(rf/reg-event-db :handlers/lodge-submit-success handlers/lodge-submit-success)
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
(ins/reg-global-singleton ins/form-ticker)
(ins/reg-global-singleton ins/breadcrumbs)
(set! low-code/component-registry
      {'metcalf3.view/DataParametersTable       views/DataParametersTable
       'metcalf3.view/date-field-with-label     views/date-field-with-label
       'metcalf3.view/textarea-field            views/textarea-field
       'metcalf3.view/textarea-field-with-label views/textarea-field-with-label
       'metcalf3.view/Methods                   views/Methods
       'metcalf3.view/UseLimitations            views/UseLimitations
       'metcalf3.view/SelectField               views/SelectField
       'metcalf3.view/NasaListSelectField       views/NasaListSelectField
       'metcalf3.view/GeographicCoverage        views/GeographicCoverage
       'metcalf3.view/DataSources               views/DataSources
       'metcalf3.view/PageErrors                views/PageErrors
       'metcalf3.view/VerticalCoverage          views/VerticalCoverage
       'metcalf3.view/TopicCategories           views/TopicCategories
       'metcalf3.view/ResourceConstraints       views/ResourceConstraints
       'metcalf3.view/input-field-with-label    views/input-field-with-label
       'metcalf3.view/Lodge                     views/Lodge
       'metcalf3.view/SupportingResource        views/SupportingResource
       'metcalf3.view/SupplementalInformation   views/SupplementalInformation
       'metcalf3.view/ThemeKeywords             views/ThemeKeywords
       'metcalf3.view/UploadData                views/UploadData
       'metcalf3.view/TaxonKeywordsExtra        views/TaxonKeywordsExtra
       'metcalf3.view/Who                       views/Who
       'metcalf3.view/ThemeKeywordsExtra        views/ThemeKeywordsExtra})
(set! low-code/template-registry
      '{:data-identification
        [:div
         [metcalf3.view/PageErrors {:page :data-identification :path [:form]}]
         [:h2 "1. Data Identification"]
         [metcalf3.view/input-field-with-label
          {:path        [:form :fields :identificationInfo :title]
           :label       "Title"
           :placeholder "Provide a descriptive title for the data set including the subject of study, the study location and time period. Example: TERN OzFlux Arcturus Emerald Tower Site 2014-ongoing"
           :helperText  "Clear and concise description of the content of the resource including What, Where, (How), When e.g. Fractional Cover for Australia 2014 ongoing"
           :maxLength   250
           :required    true}]
         [metcalf3.view/date-field-with-label
          {:path     [:form :fields :identificationInfo :dateCreation]
           :label    "Date the resource was created"
           :required true
           :minDate  #inst "1900-01-01"
           :maxDate  #inst "2100-01-01"}]
         [metcalf3.view/TopicCategories {:path [:form :fields :identificationInfo :topicCategory]}]
         [metcalf3.view/SelectField {:path [:form :fields :identificationInfo :status]}]
         [metcalf3.view/SelectField {:path [:form :fields :identificationInfo :maintenanceAndUpdateFrequency]}]
         [:div.link-right-container [:a.link-right {:href "#what"} "Next"]]]

        :what
        [:div
         [metcalf3.view/PageErrors {:page :what :path [:form]}]
         [:h2 "2. What"]
         [metcalf3.view/textarea-field-with-label
          {:path        [:form :fields :identificationInfo :abstract]
           :label       "Abstract"
           :placeholder "Provide a brief summary of What, Where, When, Why, Who and How for the collected the data."
           :helperText  "Describe the content of the resource; e.g. what information was collected, how was it collected"
           :toolTip     "Example: The Arcturus greenhouse gas (GHG) monitoring station was established in July 2010 48 km southeast of Emerald, Queensland, with flux tower measurements starting in June 2011 until early 2014. The station was part of a collaborative project between Geoscience Australia (GA) and CSIRO Marine and Atmospheric Research (CMAR). Elevation of the site was approximately 170m asl and mean annual precipitation was 572mm. The tower borderered 2 land use types split N-S: To the west lightly forested tussock grasslands; To the east crop lands, cycling through fallow periods.The instruments were installed on a square lattice tower with an adjustable pulley lever system to raise and lower the instrument arm. The tower was 5.6m tall with the instrument mast extending a further 1.1m above, totalling a height of 6.7m. Fluxes of heat, water vapour, methane and carbon dioxide were measured using the open-path eddy flux technique. Supplementary measurements above the canopy included temperature, humidity, windspeed, wind direction, rainfall, and the 4 components of net radiation. Soil heat flux, soil moisture and soil temperature measurements were also collected."
           :maxLength   2500
           :required    true}]
         [metcalf3.view/textarea-field-with-label
          {:path        [:form :fields :identificationInfo :purpose]
           :label       "Purpose"
           :placeholder "Provide a brief summary of the purpose for collecting the data including the potential use."
           :maxLength   1000
           :helperText  "Brief statement about the purpose of the study"
           :toolTip     "The Arcturus flux station data was collected to gain an understanding of natural background carbon dioxide and methane fluxes in the region prior to carbon sequestration and coal seam gas activities take place and to assess the feasibility of using this type of instrumentation for baseline studies prior to industry activities that will be required to monitor and assess CO2 or CH4 leakage to atmosphere in the future"}]
         [metcalf3.view/ThemeKeywords :keywordsTheme]
         [metcalf3.view/ThemeKeywords :keywordsThemeAnzsrc]
         [metcalf3.view/ThemeKeywordsExtra nil]
         [metcalf3.view/TaxonKeywordsExtra nil]
         [:div.link-right-container [:a.link-right {:href "#when"} "Next"]]]

        :when
        [:div
         [metcalf3.view/PageErrors {:page :when :path [:form]}]
         [:h2 "3. When was the data acquired?"]
         [metcalf3.view/date-field-with-label
          {:path     [:form :fields :identificationInfo :beginPosition]
           :label    "Start date"
           :required true
           :minDate  #inst "1900-01-01"
           :maxDate  #inst "2100-01-01"}]
         [metcalf3.view/date-field-with-label
          {:path     [:form :fields :identificationInfo :endPosition]
           :label    "End date"
           :required true
           :minDate  #inst "1900-01-01"
           :maxDate  #inst "2100-01-01"}]
         [:div.row
          [:div.col-md-4
           [metcalf3.view/NasaListSelectField {:keyword :samplingFrequency
                                               :path    [:form :fields :identificationInfo]}]]]
         [:div.link-right-container [:a.link-right {:href "#where"} "Next"]]]

        :where
        [:div
         [metcalf3.view/PageErrors {:page :where :path [:form]}]
         [:h2 "4. Where"]
         [metcalf3.view/GeographicCoverage nil]
         [metcalf3.view/VerticalCoverage]
         [:div.link-right-container [:a.link-right {:href "#how"} "Next"]]]

        :who
        [:div
         [metcalf3.view/Who nil]
         [:div.link-right-container [:a.link-right {:href "#about"} "Next"]]]

        :how
        [:div
         [metcalf3.view/PageErrors {:page :how :path [:form]}]
         [:h2 "5: How"]
         [metcalf3.view/Methods {:path [:form :fields :resourceLineage :processStep]}]
         [metcalf3.view/textarea-field-with-label
          {:path  [:form :fields :dataQualityInfo :methods]
           :label "Method"}]
         [metcalf3.view/textarea-field-with-label
          {:path        [:form :fields :dataQualityInfo :results]
           :label       "Data Quality Results"
           :placeholder "Provide a statement regarding the data quality assessment results."
           :toolTip     "Example: RMSE relative to reference data set; horizontal or vertical positional accuracy; etc."
           :maxLength   1000
           :rows        20}]
         [:div.link-right-container [:a.link-right {:href "#who"} "Next"]]]

        :about
        [:div
         [metcalf3.view/PageErrors {:page :about :path [:form]}]
         [:h2 "7: About Dataset"]
         [:h4 "Data parameters"]
         [metcalf3.view/DataParametersTable {:path [:form :fields :identificationInfo :dataParameters]}]
         [:br]
         [:h4 "Pixel Size"]
         [:div.row
          [:div.col-md-6
           [metcalf3.view/NasaListSelectField {:keyword :horizontalResolution
                                               :path    [:form :fields :identificationInfo]}]]]
         [:br]
         [:h4 "Resource constraints"]
         [metcalf3.view/ResourceConstraints]
         [metcalf3.view/UseLimitations {:path [:form :fields :identificationInfo :useLimitations]}]
         [:br]
         [:h4 "Supplemental information"]
         [metcalf3.view/SupportingResource {:path [:form :fields :supportingResources]}]
         [metcalf3.view/SupplementalInformation [:form :fields :identificationInfo :supplementalInformation]]
         [:br]
         [:h4 "Distribution"]
         [metcalf3.view/input-field-with-label
          {:path        [:form :fields :distributionInfo :distributionFormat :name]
           :label       "Data file format"
           :placeholder "e.g. Microsoft Excel, CSV, NetCDF"
           :helperText  nil
           :toolTip     nil
           :maxLength   100
           :required    nil}]
         [metcalf3.view/input-field-with-label
          {:path        [:form :fields :distributionInfo :distributionFormat :version]
           :label       "Data file format date/version"
           :placeholder "Date format date or version if applicable"
           :helperText  nil
           :toolTip     nil
           :maxLength   20
           :required    nil}]
         [metcalf3.view/textarea-field-with-label
          {:path        [:form :fields :resourceLineage :lineage]
           :label       "Lineage"
           :placeholder "Provide a brief summary of the source of the data and related collection and/or processing methods."
           :toolTip     "Example: Data was collected at the site using the methods described in yyy Manual, refer to https://doi.org/10.5194/bg-14-2903-2017"
           :maxLength   1000}]
         [:div.link-right-container [:a.link-right {:href "#upload"} "Next"]]]

        :upload
        [:div
         [metcalf3.view/PageErrors {:page :upload :path [:form]}]
         [:h2 "8: Upload Data"]
         [metcalf3.view/UploadData nil]
         [:h2 "Data Services"]
         [metcalf3.view/DataSources {:path [:form :fields :dataSources]}]
         [:div.link-right-container [:a.link-right {:href "#lodge"} "Next"]]]

        :lodge
        [:div
         [metcalf3.view/PageErrors {:page :lodge :path [:form]}]
         [:h2 "9: Lodge Metadata Draft"]
         [metcalf3.view/Lodge nil]]})