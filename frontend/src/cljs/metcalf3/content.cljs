(ns metcalf3.content)

(def term-defaults
  {:term              {:type nil}
   :vocabularyTermURL {:type nil}
   :vocabularyVersion {:type nil}
   :termDefinition    {:type nil}})

(def responsible-party-defaults
  {:individualName        {:type        nil
                           :label       "Contact name"
                           :placeholder "Contact name"
                           :required    true
                           :page        :who}
   :uri                   {:type        nil
                           :label       "Person"
                           :placeholder "Person"
                           :required    false
                           :page        :who}
   :familyName            {:type        nil
                           :label       "Surname"
                           :placeholder "Surname"
                           :maxlength   50
                           :required    true
                           :page        :who}
   :givenName             {:type        nil
                           :label       "Given name"
                           :placeholder "Given name"
                           :maxlength   50
                           :required    true
                           :page        :who}
   :orcid                 {:type        nil
                           :label       "ORCID ID"
                           :placeholder "XXXX-XXXX-XXXX-XXXX"
                           :page        :who}
   :role                  {:type        nil
                           :label       "Role"
                           :placeholder "Role"
                           :required    true
                           :page        :who}
   :organisationName      {:type        nil
                           :label       "Organisation"
                           :placeholder "Start typing to filter list..."
                           :required    true
                           :page        :who}
   :phone                 {:type  nil
                           :label "Phone number"
                           :maxlength   20
                           :page  :who}
   :facsimile             {:type  nil
                           :label "Fax number"
                           :maxlength   20
                           :page  :who}
   :electronicMailAddress {:type  nil
                           :label "Email address"
                           :page  :who}
   :address               {:deliveryPoint      {:type nil :maxlength 100}
                           :deliveryPoint2     {:type nil :maxlength 100}
                           :city               {:type nil :help "City" :maxlength 100}
                           :administrativeArea {:type nil :help "State/territory" :maxlength 100}
                           :postalCode         {:type nil :help "Postal / Zip code" :maxlength 100}
                           :country            {:type nil :help "Country" :maxlength 100}}
   :isUserAdded           {:type     nil
                           :required false
                           :value    false
                           :page     :who}})

(def default-payload
  "This is merged in.  Allows for some defaults to be applied.  Mostly used for bits of content and field props."
  {:form
   {:fields
    {:dataSources         {:type   nil
                           :page   :upload
                           :label  "Data services"
                           :help   "Please note: This is intended for advanced users only"
                           :many   true
                           :value  []
                           :fields {:description {:type        nil
                                                  :page        :upload
                                                  :label       "Title"
                                                  :maxlength   250
                                                  :placeholder "e.g. Map of Argo profiles"}
                                    :url         {:type        nil
                                                  :page        :upload
                                                  :label       "URL"
                                                  :maxlength   250
                                                  :placeholder "e.g. http://geoserver-123.aodn.org.au/geoserver/wms"}
                                    :name        {:type        nil
                                                  :page        :upload
                                                  :maxlength   250
                                                  :label       "Layer"
                                                  :placeholder "e.g. imos.argo_profile_map"}
                                    :protocol    {:type  nil
                                                  :page  :upload
                                                  :label "Protocol"
                                                  :options
                                                         [["WWW:DOWNLOAD-1.0-http--download" "HTTP"]
                                                          ["OGC:WCS-1.1.0-http-get-capabilities" "OGC Web Coverage Service (WCS)"]
                                                          ["OGC:WMS-1.3.0-http-get-map" "OGC Web Map Service (WMS)"]
                                                          ["OGC:WFS-1.1.0-http-get-capabilities" "OGC Web Feature Service (WFS)"]
                                                          ["WWW:LINK-1.0-http--opendap" "OPeNDAP"]
                                                          ["FTP" "FTP"]
                                                          ["WWW:DOWNLOAD-1.0-http--downloaddata" "Other/unknown"]]}}}
     :attachments         {:type   nil
                           :page   :upload
                           :label  "Attachments"
                           :many   true
                           :value  []
                           :fields {:file       {:type  nil
                                                 :page  :upload
                                                 :label "File"}
                                    :name       {:type  nil
                                                 :page  :upload
                                                 :label "Name"}
                                    :delete_url {:type nil}}}
     :supportingResources {:type   nil
                           :page   :about
                           :label  "Supporting resources"
                           :many   true
                           :value  []
                           :fields {:name {:type  nil
                                           :page  :about
                                           :maxlength 250
                                           :label "Title"}
                                    :url  {:type  nil
                                           :page  :about
                                           :label "URL"}}}
     :dataQualityInfo     {:methods {:type        nil
                                     :rows        20
                                     :label       "Data Quality Methods"
                                     :maxlength   1000
                                     :placeholder "Provide a summary of the data quality assessment method. Example: The data were compared to xyz reference data."
                                     :value       nil
                                     :page        :how}
                           :results {:type        nil
                                     :rows        20
                                     :label       "Data Quality Results"
                                     :maxlength   1000
                                     :placeholder "Provide a statement regarding the data quality assessment results.  Examples: RMSE relative to reference data set; horizontal or vertical positional accuracy; etc."
                                     :value       nil
                                     :page        :how}}
     :identificationInfo
                          {:title                         {:type        nil
                                                           :label       "Title"
                                                           :placeholder "Provide a descriptive title for the data set including the subject of study, the study location and time period. Example: TERN OzFlux Arcturus Emerald Tower Site 2014-ongoing"
                                                           :help        "Clear and concise description of the content of the resource"
                                                           :rows        3
                                                           :maxlength 250
                                                           :required    true
                                                           :page        :data-identification}
                           :dateCreation                  {:type     nil
                                                           :label    "Date the resource was created"
                                                           :required true
                                                           :page     :data-identification}
                           :topicCategory                 {:type        nil
                                                           :label       "Topic Categories"
                                                           :placeholder "Search for topic categories"
                                                           :required    true
                                                           :page        :data-identification}
                           :status                        {:type     nil
                                                           :label    "Status of data"
                                                           :required true
                                                           :options  [["onGoing" "ongoing"]
                                                                      ["completed" "completed"]]
                                                           :page     :data-identification}
                           :maintenanceAndUpdateFrequency {:type     nil
                                                           :label    "Maintenance and update frequency"
                                                           :required true
                                                           :options  [["continually" "Continually"]
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
                                                                      ["biennially" "Every 2 years"]]
                                                           :page     :data-identification}
                           :abstract                      {:type        nil
                                                           :label       "Abstract"
                                                           :maxlength   2500
                                                           :placeholder "Provide a brief summary of What, Where, When, Why, Who and How for the collected the data.\n          Example: The Arcturus greenhouse gas (GHG) monitoring station was established in July 2010 48 km southeast of Emerald, Queensland, with flux tower measurements starting in June 2011 until early 2014. The station was part of a collaborative project between Geoscience Australia (GA) and CSIRO Marine and Atmospheric Research (CMAR). Elevation of the site was approximately 170m asl and mean annual precipitation was 572mm. The tower borderered 2 land use types split N-S: To the west lightly forested tussock grasslands; To the east crop lands, cycling through fallow periods.The instruments were installed on a square lattice tower with an adjustable pulley lever system to raise and lower the instrument arm. The tower was 5.6m tall with the instrument mast extending a further 1.1m above, totalling a height of 6.7m. Fluxes of heat, water vapour, methane and carbon dioxide were measured using the open-path eddy flux technique. Supplementary measurements above the canopy included temperature, humidity, windspeed, wind direction, rainfall, and the 4 components of net radiation. Soil heat flux, soil moisture and soil temperature measurements were also collected."
                                                           :help        "Describe the content of the resource; e.g. what information was collected,
                                          how was it collected"
                                                           :rows        10
                                                           :required    true
                                                           :page        :what}
                           :purpose                       {:type        nil
                                                           :label       "Purpose"
                                                           :maxlength   1000
                                                           :help        "Brief statement about the purpose of the study"
                                                           :placeholder "Provide a brief summary of the purpose for collecting the data including the potential use. Example: The Arcturus flux station data was collected to gain an understanding of natural background carbon dioxide and methane fluxes in the region prior to carbon sequestration and coal seam gas activities take place and to assess the feasibility of using this type of instrumentation for baseline studies prior to industry activities that will be required to monitor and assess CO2 or CH4 leakage to atmosphere in the future"
                                                           :page        :what}
                           :keywordsTheme                 {:keywords {:type        nil
                                                                      :label       "Research theme keywords, GCMD Science Keywords"
                                                                      :help        "Select up to 12 research theme keywords describing your data"
                                                                      :placeholder "Start typing to filter list..."
                                                                      :required    true
                                                                      :page        :what}}
                           :keywordsThemeAnzsrc           {:keywords {:type        nil
                                                                      :label       "Research theme keywords, ANZSRC Fields of Research"
                                                                      :help        "Select up to 12 research theme keywords describing your data"
                                                                      :placeholder "Start typing to filter list..."
                                                                      :required    false
                                                                      :page        :what}}
                           :keywordsThemeExtra            {:keywords {:type        nil
                                                                      :label       "Additional theme keywords"
                                                                      :help        "Enter your own additional theme keywords as required and click + to add"
                                                                      :placeholder "Enter a theme keyword"
                                                                      :maxlength   100
                                                                      :page        :what}}
                           :keywordsTaxonExtra            {:keywords {:type        nil
                                                                      :placeholder "Enter a taxon keyword"
                                                                      :help        "Add any taxon names describing your data and click + to add"
                                                                      :label       "Taxon keywords"
                                                                      :maxLength   250
                                                                      :page        :what}}

                           :beginPosition                 {:type     nil
                                                           :label    "Start date"
                                                           :required true
                                                           :page     :when}
                           :endPosition                   {:type     nil
                                                           :label    "End date"
                                                           :required true
                                                           :page     :when}
                           :samplingFrequency             {:label     "Sampling frequency"
                                                           :help      "How frequently was the data collected?"
                                                           :uri       {:type nil
                                                                       :page :when}
                                                           :prefLabel {:type nil
                                                                       :page :when}}
                           :horizontalResolution          {:label     "Horizontal resolution"
                                                           :uri       {:type nil
                                                                       :page :about}
                                                           :prefLabel {:type nil
                                                                       :page :about}}
                           :geographicElement             {:hasGeographicCoverage {:type  nil
                                                                                   :label "Does data have a geographic coverage?"
                                                                                   :page  :where
                                                                                   :value true}
                                                           :siteDescription       {:type        nil
                                                                                   :label       "Site description"
                                                                                   :placeholder "A descriptive reference for the site locality. May include a project code. Example: Geelong (Site: G145), VIC, Australia"
                                                                                   :maxlength   250
                                                                                   :page        "where"}
                                                           :boxes                 {:type   nil
                                                                                   :page   :where
                                                                                   :label  "Geographic coverage"
                                                                                   :many   true
                                                                                   :value  []
                                                                                   :fields {:northBoundLatitude {:max      90
                                                                                                                 :min      -90
                                                                                                                 :required true
                                                                                                                 :label    "North limit"
                                                                                                                 :page     :where}
                                                                                            :southBoundLatitude {:max      90
                                                                                                                 :min      -90
                                                                                                                 :required true
                                                                                                                 :label    "South limit"
                                                                                                                 :page     :where}
                                                                                            :eastBoundLongitude {:max      180
                                                                                                                 :min      -180
                                                                                                                 :required true
                                                                                                                 :label    "East limit"
                                                                                                                 :page     :where}
                                                                                            :westBoundLongitude {:max      180
                                                                                                                 :min      -180
                                                                                                                 :required true
                                                                                                                 :label    "West limit"
                                                                                                                 :page     :where}}}}


                           :verticalElement               {:hasVerticalExtent {:type  nil
                                                                               :label "Does data have a vertical (depth or altitude) component?"
                                                                               :page  :where}
                                                           :minimumValue      {:type     nil
                                                                               :label    "Minimum (m)"
                                                                               :required true
                                                                               :help     "Specify the vertical extent of the data relative to a ground surface defined as 0m"
                                                                               :page     :where}
                                                           :maximumValue      {:type     nil
                                                                               :label    "Maximum (m)"
                                                                               :required true
                                                                               :page     :where}
                                                           :method            {:type     nil
                                                                               :label    "Method"
                                                                               :required false
                                                                               :options  [["not specified" "not specified"]
                                                                                          ["GPS" "GPS"]
                                                                                          ["Map" "Map"]
                                                                                          ["Other" "Other"]]
                                                                               :page     :where}
                                                           :elevation         {:type     nil
                                                                               :label    "Elevation in metres"
                                                                               :required true
                                                                               :page     :where}}
                           :credit                        {:type  nil
                                                           :help  "Acknowledge the contribution of any funding schemes or organisations."
                                                           :maxlength 1000
                                                           :label "Other credits"}

                           :dataParameters                {:type   nil
                                                           :value  []
                                                           :label  "Data parameters"
                                                           :many   true
                                                           :page   :about
                                                           :fields {:longName_term                {:type        nil
                                                                                                   :page        :about
                                                                                                   :label       "Name"
                                                                                                   :help        "Name of the measured parameter e.g. Vegetation height"
                                                                                                   :placeholder "Start typing to filter the list"
                                                                                                   :required    true}
                                                                    :longName_vocabularyTermURL   {:type nil
                                                                                                   :page :about}
                                                                    :longName_vocabularyVersion   {:type nil
                                                                                                   :page :about}
                                                                    :longName_termDefinition      {:type nil
                                                                                                   :page :about}
                                                                    :name                         {:type        nil
                                                                                                   :label       ""
                                                                                                   :placeholder "Name in dataset (optional)"
                                                                                                   :page        :about}
                                                                    :serialNumber                 {:type        nil
                                                                                                   :label       "Serial Number"
                                                                                                   :placeholder ""
                                                                                                   :page        :about}
                                                                    :unit_term                    {:type        nil
                                                                                                   :label       "Unit"
                                                                                                   :required    true
                                                                                                   :page        :about
                                                                                                   :placeholder "Start typing to filter the list"
                                                                                                   :help        "Unit of measurement"}
                                                                    :unit_vocabularyTermURL       {:type nil
                                                                                                   :page :about}
                                                                    :unit_vocabularyVersion       {:type nil
                                                                                                   :page :about}
                                                                    :unit_termDefinition          {:type nil
                                                                                                   :page :about}
                                                                    :instrument_term              {:type        nil
                                                                                                   :label       "Instrument"
                                                                                                   :placeholder "Start typing to filter the list"
                                                                                                   :help        "Sensor used to measure the parameter"}
                                                                    :instrument_vocabularyTermURL {:type nil
                                                                                                   :page :about}
                                                                    :instrument_vocabularyVersion {:type nil
                                                                                                   :page :about}
                                                                    :instrument_termDefinition    {:type nil
                                                                                                   :page :about}
                                                                    :platform_term                {:type        nil
                                                                                                   :label       "Platform"
                                                                                                   :placeholder "Start typing to filter the list"
                                                                                                   :help        "Is the data measured from land, air, satellite or a model?"
                                                                                                   :page        :about}
                                                                    :platform_vocabularyTermURL   {:type nil
                                                                                                   :page :about}
                                                                    :platform_vocabularyVersion   {:type nil
                                                                                                   :page :about}
                                                                    :platform_termDefinition      {:type nil
                                                                                                   :page :about}}}
                           :useLimitations                {:type        nil
                                                           :label       "Use limitations"
                                                           :many        true
                                                           :page        :about
                                                           :maxlength   1000
                                                           :placeholder "While every care is taken to ensure the accuracy of this information, the author makes no representations or warranties about its accuracy, reliability, completeness or suitability for any particular purpose and disclaims all responsibility and all liability (including without limitation, liability in negligence) for all expenses, losses, damages (including indirect or consequential damage) and costs which might be incurred as a result of the information being inaccurate or incomplete in any way and for any reason."}
                           :supplementalInformation       {:type  nil
                                                           :page  :about
                                                           :maxlength 1000
                                                           :label "Publications associated with dataset"
                                                           :many  true}
                           :citedResponsibleParty         {:type   nil
                                                           :many   true
                                                           :fields responsible-party-defaults
                                                           :page   :who
                                                           :label  "Responsible parties"}
                           :pointOfContact                {:type     nil
                                                           :many     true
                                                           :fields   responsible-party-defaults
                                                           :page     :who
                                                           :required true
                                                           :label    "Point of contact"}}
     :distributionInfo    {:distributionFormat
                           {:version {:type        nil
                                      :label       "Data file format date/version"
                                      :maxlength   20
                                      :placeholder "Date format date or version if applicable"}
                            :name    {:type        nil
                                      :label       "Data file format"
                                      :placeholder "e.g. Microsoft Excel, CSV, NetCDF"}}}
     :noteForDataManager  {:type  nil
                           :label "Include a note for the data manager"
                           :maxlength 1000
                           :style {:min-height "80px"}
                           :page  :lodge}
     :who-authorRequired  {:type  nil
                           :label "Author role"
                           :page  :who}
     :agreedToTerms       {:type  nil
                           :label "I have read and agree to the terms and conditions"
                           :page  :lodge
                           :value false}
     :doiRequested        {:type  nil
                           :label "Please mint a DOI for this submission"
                           :page  :lodge
                           :value false}
     :resourceLineage     {:lineage     {:type        nil
                                         :label       "Lineage"
                                         :placeholder "Provide a brief summary of the source of the data and related collection and/or processing methods. \n               Example: Data was collected at the site using the methods described in yyy Manual, refer to https://doi.org/10.5194/bg-14-2903-2017"
                                         :maxlength   1000
                                         :page        :about}
                           :processStep {:type   nil
                                         :many   true
                                         :fields {:description {:type  nil
                                                                :label "Description"
                                                                :page  :how}
                                                  :name        {:type  nil
                                                                :label "Name"
                                                                :page  :how}
                                                  :uri         {:type  nil
                                                                :label "URI"
                                                                :page  :how}}}}}}})

(def contact-groups
  [{:path  [:form :fields :identificationInfo :pointOfContact]
    :title "Point of contact for dataset"}
   {:path  [:form :fields :identificationInfo :citedResponsibleParty]
    :title "Responsible parties for creating dataset"}])
