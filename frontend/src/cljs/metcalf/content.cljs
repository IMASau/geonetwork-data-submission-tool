(ns metcalf.content)

(def address-defaults
  {:deliveryPoint      {}
   :deliveryPoint2     {}
   :city               {:help "City"}
   :administrativeArea {:help "State/territory"}
   :postalCode         {:help "Postal / Zip code"}
   :country            {:help "Country"}})

(def responsible-party-defaults
  {:individualName        {:label       "Contact name"
                           :placeholder "Last name, First name"
                           :required true
                           :page :who}
   :orcid                 {:label "ORCID ID"
                           :placeholder "ORCID researcher page URL"
                           :page :who}
   :role                  {:label   "Role"
                           :required true
                           :page :who
                           :options [["pointOfContact" "Point of contact"]
                                     ["principalInvestigator" "Principal Investigator"]
                                     ["owner" "Owner"]
                                     ["distributor" "Distributor"]
                                     ["custodian" "Custodian"]]}
   :organisationName      {:label       "Organisation"
                           :placeholder "Organisation"
                           :required true
                           :page :who}
   :phone                 {:label "Phone number"}
   :facsimile             {:label "Fax number"}
   :electronicMailAddress {:label "Email address"
                           :required true
                           :page :who}
   :address               {:label  "Postal address"
                           :fields address-defaults}})

(def default-payload
  "This is merged in.  Allows for some defaults to be applied.  Mostly used for bits of content and field props."
  {:form
   {:fields
    {:dataQualityInfo
     {:statement {:rows  20
                  :label "Methodological information"
                  :help  "Provide a brief statement of the methods used for collection of the
                                   data, can include information regarding sampling equipment (collection hardware),
                                   procedures, and precision/resolution of data collected."
                  :value nil
                  :required true
                  :page  :how}}
     :identificationInfo
     {:title                         {:label "Title"
                                      :help  "Clear and concise description of the content of the resource"
                                      :rows  3
                                      :required true
                                      :page  :data-identification}
      :dateCreation                  {:label "Date of record creation"
                                      :required true
                                      :page  :data-identification}
      :topicCategory                 {:label   "Topic category"
                                      :required true
                                      :options [["biota" "biota"]
                                                ["climatology/meteorology/atmosphere" "climatology/meteorology/atmosphere"]
                                                ["oceans" "oceans"]
                                                ["geoscientificInformation" "geoscientificInformation"]
                                                ["inlandWater" "inlandWater"]]
                                      :page    :data-identification}
      :status                        {:label   "Status of data"
                                      :required true
                                      :options [["onGoing" "ongoing"]
                                                ["complete" "complete"]]
                                      :page    :data-identification}
      :maintenanceAndUpdateFrequency {:label   "Maintenance and update frequency"
                                      :required true
                                      :options [["daily" "Daily"]
                                                ["weekly" "Weekly"]
                                                ["monthly" "Monthly"]
                                                ["quarterly" "Quarterly"]
                                                ["annually" "Annually"]
                                                ["ongoing" "Ongoing"]
                                                ["asNeeded" "As required"]
                                                ["none-planned" "None planned"]]
                                      :page    :data-identification}
      :abstract                      {:label "Abstract"
                                      :help  "Describe the content of the resource; e.g. what information was collected,
                                          how was it collected, brief statement about the purpose of the study"
                                      :rows  10
                                      :required true
                                      :page  :what}
      :keywordsTheme                 {:keywords {:label       "Research theme keywords"
                                                 :help        "Select up to 12 research theme keywords describing your data"
                                                 :placeholder "Search for keywords"
                                                 :required    true
                                                 :page        :what}}
      :keywordsThemeExtra            {:keywords {:label       "Additional theme keywords"
                                                 :help        "Enter your own additional theme keywords as required"
                                                 :placeholder "Enter a theme keyword"
                                                 :page        :what}}
      :keywordsTaxonExtra            {:label    "Taxon keywords"
                                      :page     :what
                                      :keywords {:placeholder "Enter a taxon keyword"
                                                 :help        "Add any taxon names describing your data"}}

      :beginPosition                 {:label "Start date"
                                      :required true
                                      :page  :when}
      :endPosition                   {:label "End date"
                                      :required true
                                      :page  :when}
      :samplingFrequency             {:label "Sampling frequency"
                                      :options [["daily" "Daily"]
                                                ["weekly" "Weekly"]
                                                ["monthly" "Monthly"]
                                                ["quarterly" "Quarterly"]
                                                ["annually" "Annually"]
                                                ["ongoing" "Ongoing"]
                                                ["asNeeded" "As required"]
                                                ["irregular" "Irregular"]
                                                ["none-planned" "None planned"]]
                                      :page :when}
      :geographicElement             {:required true
                                      :page :where
                                      :label "Geographic coverage"}

      :verticalElement               {:hasVerticalExtent {:label "Does data have a vertical (depth or altitude) component?"
                                                          :page  :where}
                                      :minimumValue      {:label "Minimum (m)"
                                                          :required true
                                                          :page  :where}
                                      :maximumValue      {:label "Maximum (m)"
                                                          :required true
                                                          :page  :where}
                                      :verticalCRS       {:label   "Vertical type"
                                                          :required true
                                                          :options [["EPSG::5715" "Depth (distance below mean sea level)"]
                                                                    ["EPSG::5714" "Altitude (height above mean sea level)"]]
                                                          :page    :where}}
      :credit                        {:help  "Acknowledge the contribution of any funding schemes or organisations."
                                      :label "Other credits"}

      :dataParameters                {:label "Description of parameters in dataset"
                                      :page  :about
                                      :fields {:longName {:label "Parameter name (full)"
                                                          :page :about}
                                               :name {:label "Parameter name (in dataset)"
                                                      :page :about}
                                               :unit {:label "Parameter unit of measure"
                                                      :page :about}
                                               :parameterDescription {:placeholder "Any supplementary notes if required, e.g. instrument or method used to collect information."
                                                                      :page :about}}}
      :creativeCommons               {:label   "License"
                                      :required true
                                      :page  :about
                                      :options {"http://creativecommons.org/licenses/by/3.0/au/"    "Creative Commons by Attribution (recommended​)"
                                                "http://creativecommons.org/licenses/by-nc/3.0/au/" "Creative Commons, Non-commercial Use only"
                                                "http://creativecommons.org/licenses/other"         "Other constraints"}}
      :otherConstraints              {:placeholder "Enter additional license requirements"
                                      :is-hidden true
                                      :page :about}
      :useLimitation                 {:label "Use limitations"}
      :supplementalInformation       {:label "Publications associated with dataset"
                                      :page  :about}
      :citedResponsibleParty         {:fields responsible-party-defaults
                                      :page :who
                                      :label "Responsible parties"}
      :pointOfContact                {:fields responsible-party-defaults
                                      :page :who
                                      :required true
                                      :label "Point of contact"}}
     :distributionInfo
     {:distributionFormat
      {:version {:label       "Data file format date/version"
                 :placeholder "Date format date or version if applicable"}
       :name    {:label       "Data file format"
                 :placeholder "e.g. Microsoft Excel, CSV, NetCDF"}}}}}})

(def contact-groups
  [{:path [:form :fields :identificationInfo :pointOfContact]
    :title "Point of contact for dataset"}
   {:path [:form :fields :identificationInfo :citedResponsibleParty]
    :title "Responsible parties for creating dataset"}])