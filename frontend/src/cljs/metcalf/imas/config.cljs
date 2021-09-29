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
(rf/reg-event-fx :handlers/save-current-document-success handlers3/save-current-document-success)
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
(rf/reg-event-fx :handlers/save-current-document handlers3/save-current-document)
(rf/reg-event-fx :handlers/save-current-document-error handlers3/save-current-document-error)
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
(rf/reg-event-fx ::components4/date-field-with-label-value-changed handlers4/value-changed-handler)
(rf/reg-event-fx ::components4/select-field-with-label-value-changed handlers4/value-changed-handler)
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
(rf/reg-sub :subs/get-edit-tab-props :<- [:subs/get-page-props] :<- [:subs/get-derived-state] subs3/get-edit-tab-props)
(rf/reg-sub :progress/get-props :<- [:subs/get-derived-state] subs3/get-progress-props)
(rf/reg-sub :textarea-field/get-props :<- [:subs/get-derived-state] subs3/get-textarea-field-props)
(rf/reg-sub :textarea-field/get-many-field-props :<- [:subs/get-derived-state] subs3/get-textarea-field-many-props)
(rf/reg-sub :subs/get-form-tick subs3/get-form-tick)
(rf/reg-sub :help/get-menuitems subs3/get-menuitems)
(rf/reg-sub :subs/platform-selected? subs3/platform-selected?)
(rf/reg-sub ::subs4/get-form-state subs4/get-form-state)
(rf/reg-sub ::components4/get-input-field-with-label-props subs4/form-state-signal subs4/get-block-props-sub)
(rf/reg-sub ::components4/get-textarea-field-with-label-props subs4/form-state-signal subs4/get-block-props-sub)
(rf/reg-sub ::components4/get-date-field-with-label-props subs4/form-state-signal subs4/get-block-props-sub)
(rf/reg-sub ::components4/get-select-field-with-label-props subs4/form-state-signal subs4/get-block-props-sub)
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
      {'m3/DataParametersTable         views/DataParametersTable
       'm3/CheckboxField               views/CheckboxField
       'm3/date-field-with-label       views/date-field-with-label
       'm3/textarea-field-with-label   views/textarea-field-with-label
       'm3/UseLimitations              views/UseLimitations
       'm3/NasaListSelectField         views/NasaListSelectField
       'm3/GeographicCoverage          views/GeographicCoverage
       'm3/DataSources                 views/DataSources
       'm3/PageErrors                  views/PageErrors
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
       'm4/page-errors                 components4/page-errors
       'm4/textarea-field-with-label   components4/textarea-field-with-label
       'm4/input-field-with-label      components4/input-field-with-label
       'm4/date-field-with-label       components4/date-field-with-label
       'm4/select-field-with-label     components4/select-field-with-label
       })
(set! low-code/template-registry
      '{:data-identification
        [:div

         ; Relies on schema to hold label at right location which is suitable
         [m4/page-errors
          {:form-id    [:form]
           :data-paths [[:identificationInfo :title]
                        [:identificationInfo :dateCreation]]}]

         ; Explicit label allows flexibility at cost of duplication
         [m4/page-errors
          {:form-id [:form]
           :checks  [{:label "Title" :data-path [:identificationInfo :title]}
                     {:label "Date created" :data-path [:identificationInfo :dateCreation]}]}]



         [:h2 "1. Data Identification"]
         [m4/input-field-with-label
          {:form-id    [:form]
           :data-path  [:identificationInfo :title]
           :helperText "Clear and concise description of the content of the resource"}]
         [m4/date-field-with-label
          {:form-id   [:form]
           :data-path [:identificationInfo :dateCreation]
           :minDate   "1900-01-01"
           :maxDate   "2100-01-01"}]
         [m4/select-field-with-label
          {:form-id   [:form]
           :data-path [:identificationInfo :topicCategory]
           :options   [{:value "biota" :label "biota"}
                       {:value "climatology/meteorology/atmosphere" :label "climatology/meteorology/atmosphere"}
                       {:value "oceans" :label "oceans"}
                       {:value "geoscientificInformation" :label "geoscientificInformation"}
                       {:value "inlandWater" :label "inlandWater"}]}]
         [m4/select-field-with-label
          {:form-id   [:form]
           :data-path [:identificationInfo :status]
           :options   [{:value "onGoing" :label "ongoing"}
                       {:value "planned" :label "planned"}
                       {:value "completed" :label "completed"}]}]
         [m4/select-field-with-label
          {:form-id   [:form]
           :data-path [:identificationInfo :maintenanceAndUpdateFrequency]
           :options   [{:value "continually" :label "Continually"}
                       {:value "daily" :label "Daily"}
                       {:value "weekly" :label "Weekly"}
                       {:value "fortnightly" :label "Fortnightly"}
                       {:value "monthly" :label "Monthly"}
                       {:value "quarterly" :label "Quarterly"}
                       {:value "biannually" :label "Twice each year"}
                       {:value "annually" :label "Annually"}
                       {:value "asNeeded" :label "As required"}
                       {:value "irregular" :label "Irregular"}
                       {:value "notPlanned" :label "None planned"}
                       {:value "unknown" :label "Unknown"}
                       {:value "periodic" :label "Periodic"}
                       {:value "semimonthly" :label "Twice a month"}
                       {:value "biennially" :label "Every 2 years"}]}]
         [:div.link-right-container [:a.link-right {:href "#what"} "Next"]]]

        :what
        [:div
         [m3/PageErrors {:page :what :path [:form]}]
         [:h2 "2. What"]
         [:span.abstract-textarea
          [m4/textarea-field-with-label
           {:form-id     [:form]
            :data-path   [:identificationInfo :abstract]
            :label       "Abstract"
            :placeholder nil
            :helperText  "Describe the content of the resource; e.g. what information was collected, how was it collected"
            ;; FIXME this isn't enforced.
            :maxLength   2500
            :required    true}]]
         [m3/ThemeKeywords
          {:keyword-type        :keywordsTheme
           :keywords-theme-path [:form :fields :identificationInfo :keywordsTheme]}]
         ;; FIXME Anzsrc should be optional, but this required doesn't hook up to anything.
         [m3/ThemeKeywords
          {:keyword-type        :keywordsThemeAnzsrc
           :keywords-theme-path [:form :fields :identificationInfo :keywordsThemeAnzsrc]
           :required            false}]
         ;; TODO Add Geographic Extent vocab here.
         [m3/ThemeKeywordsExtra
          {:keywords-path [:form :fields :identificationInfo :keywordsThemeExtra :keywords]}]
         [m3/TaxonKeywordsExtra
          {:keywords-path [:form :fields :identificationInfo :keywordsTaxonExtra :keywords]}]
         [:div.link-right-container [:a.link-right {:href "#when"} "Next"]]]

        :when
        [:div
         [m3/PageErrors {:page :when :path [:form]}]
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
           :minDate   "1900-01-01"
           :maxDate   "2100-01-01"}]
         [:div.row
          [:div.col-md-4
           ;; TODO does IMAS want the old sample frequency (min daily) or this one (min <1 second)?
           [m3/NasaListSelectField {:keyword   :samplingFrequency
                                    :form-id   [:form]
                                    :data-path [:identificationInfo]}]]]
         [:div.link-right-container [:a.link-right {:href "#where"} "Next"]]]

        :where
        [:div
         [m3/PageErrors {:page :where :path [:form]}]
         [:h2 "4. Where"]
         ;; FIXME add toggle for satellite imagery.
         ;; FIXME hide the siteDescription textarea.
         ;; FIXME remove the "Grid to Geographic converter" link text
         [m3/GeographicCoverage
          {:has-coverage-path     [:form :fields :identificationInfo :geographicElement :hasGeographicCoverage]
           :boxes-path            [:form :fields :identificationInfo :geographicElement :boxes]
           :site-description-path [:form :fields :identificationInfo :geographicElement :siteDescription]}]
         [:div.VerticalCoverage
          ;; FIXME use h3 not h4. Restyle if necessary.
          [:h4 "Vertical Coverage"]
          [metcalf3.view/CheckboxField
           {:path [:form :fields :identificationInfo :verticalElement :hasVerticalExtent]}]
          ;; FIXME hide the below fields when hasVerticalExtent checkbox is unchecked.
          [m4/input-field-with-label
           {:form-id    [:form]
            :data-path  [:identificationInfo :verticalElement :minimumValue]
            :class      "wauto"
            :label      "Minimum (m)"
            :helperText "Shallowest depth / lowest altitude"
            :required   true}]
          [m4/input-field-with-label
           {:form-id    [:form]
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
           {:form-id     [:form]
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
         [m3/DataParametersTable {:form-id   [:form]
                                  :data-path [:identificationInfo :dataParameters]}]
         [:br]
         [:h4 "Resource constraints"]
         ;; FIXME license selection isn't being included in XML export.
         [m4/select-field-with-label
          {:form-id   [:form]
           :data-path [:identificationInfo :creativeCommons]
           :help      [:span "Learn more about which license is right for you at "
                       [:a {:href   "https://creativecommons.org/choose/"
                            :target "_blank"}
                        "Creative Commons"]]
           :label     "License"
           :required  true
           :options   [{:value "http://creativecommons.org/licenses/by/4.0/" :label "Creative Commons by Attribution (recommended​)"}
                       {:value "http://creativecommons.org/licenses/by-nc/4.0/" :label "Creative Commons, Non-commercial Use only"}
                       {:value "http://creativecommons.org/licenses/other" :label "Other constraints"}]}]
         [m4/input-field-with-label
          {:form-id     [:form]
           :data-path   [:identificationInfo :otherConstraints]
           :label       "Additional license requirements"   ;; FIXME
           :placeholder "Enter additional license requirements"
           :required    true}]

         [m3/UseLimitations {:form-id   [:form]
                             :data-path [:identificationInfo :useLimitations]}]
         [:br]
         [:h4 "Supplemental information"]
         [:form-id [:form]
          mdata-etcalf3.view/IMASSupplementalInformation [:identificationInfo :supplementalInformation]]
         [m3/IMASSupportingResource {:form-id   [:form]
                                     :data-path [:supportingResources]}]
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
         [:div.link-right-container [:a.link-right {:href "#upload"} "Next"]]]

        :upload
        [:div
         [m3/PageErrors {:page :upload :path [:form]}]
         [:h2 "8: Upload Data"]
         [m3/UploadData
          {:attachments-path [:form :fields :attachments]}]
         [:h2 "Data Services"]
         ;; FIXME reduce protocol options to the below for IMAS:
         ;; [["OGC:WMS-1.3.0-http-get-map" "OGC Web Map Service (WMS)"]
         ;;  ["OGC:WFS-1.0.0-http-get-capabilities" "OGC Web Feature Service (WFS)"]
         ;;  ["WWW:LINK-1.0-http--downloaddata" "Other/unknown"]]
         [m3/DataSources {:form-id   [:form]
                          :data-path [:dataSources]}]
         [:div.link-right-container [:a.link-right {:href "#lodge"} "Next"]]]

        :lodge
        [:div
         [m3/PageErrors {:page :lodge :path [:form]}]
         [:h2 "9: Lodge Metadata Draft"]
         [m3/IMASLodge
          {:notes-path [:form :fields :noteForDataManager]}]]})
