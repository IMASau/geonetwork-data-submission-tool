(ns ^:dev/always metcalf.tern.config
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
            [re-frame.core :as rf]
            [interop.ui :as ui]))

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
(rf/reg-event-fx ::components4/value-changed handlers4/value-changed-handler)
(rf/reg-event-fx ::components4/option-change handlers4/option-change-handler)
(rf/reg-event-fx ::components4/list-add-click handlers4/list-add-click-handler)
(rf/reg-event-fx ::components4/list-add-with-defaults-click-handler handlers4/list-add-with-defaults-click-handler2)
(rf/reg-event-fx ::components4/item-add-with-defaults-click-handler handlers4/item-add-with-defaults-click-handler2)
(rf/reg-event-fx ::components4/list-option-picker-change handlers4/list-option-picker-change)
(rf/reg-event-fx ::components4/item-option-picker-change handlers4/item-option-picker-change)
(rf/reg-event-fx ::components4/selection-list-item-click handlers4/selection-list-item-click2)
(rf/reg-event-fx ::components4/selection-list-remove-click handlers4/selection-list-remove-click)
(rf/reg-event-fx ::components4/selection-list-reorder handlers4/selection-list-reorder)
(rf/reg-event-fx ::components4/list-edit-dialog-close handlers4/list-edit-dialog-close-handler)
(rf/reg-event-fx ::components4/list-edit-dialog-cancel handlers4/list-edit-dialog-cancel-handler)
(rf/reg-event-fx ::components4/list-edit-dialog-save handlers4/list-edit-dialog-save-handler)
(rf/reg-event-fx ::components4/item-edit-dialog-close handlers4/item-edit-dialog-close-handler)
(rf/reg-event-fx ::components4/item-edit-dialog-cancel handlers4/item-edit-dialog-cancel-handler)
(rf/reg-event-fx ::components4/item-edit-dialog-save handlers4/item-edit-dialog-save-handler)
(rf/reg-event-fx ::components4/boxes-changed handlers4/boxes-changed)
(rf/reg-fx :ui/setup-blueprint ui/setup-blueprint)
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
(rf/reg-sub ::components4/get-yes-no-field-props subs4/form-state-signal subs4/get-block-props-sub)
(rf/reg-sub ::components4/get-block-props subs4/form-state-signal subs4/get-block-props-sub)
(rf/reg-sub ::components4/get-block-data subs4/form-state-signal subs4/get-block-data-sub)
(rf/reg-sub ::low-code/get-data-schema subs4/get-data-schema-sub)
(rf/reg-sub ::components4/get-data-schema subs4/get-data-schema-sub)
(rf/reg-sub ::views/get-props subs4/form-state-signal subs4/get-block-props-sub)
(ins/reg-global-singleton ins/form-ticker)
(ins/reg-global-singleton ins/breadcrumbs)
(set! rules/rule-registry
      {"requiredField"     rules/required-field
       "requiredWhenYes"   rules/required-when-yes
       "maxLength"         rules/max-length
       "geographyRequired" rules/geography-required
       "licenseOther"      rules/license-other
       "dateOrder"         rules/date-order
       "endPosition"       rules/end-position
       "maintFreq"         rules/maint-freq
       "verticalRequired"  rules/vertical-required})
(set! low-code/component-registry
      {
       'm4/async-list-picker             {:view components4/async-list-picker :init components4/async-list-picker-settings}
       'm4/async-item-picker             {:view components4/async-item-picker :init components4/async-item-picker-settings}
       'm4/async-select-option           {:view components4/async-select-option :init components4/async-select-option-settings}
       'm4/breadcrumb-list-option-picker {:view components4/breadcrumb-list-option-picker :init components4/breadcrumb-list-option-picker-settings}
       'm4/breadcrumb-selection-list     {:view components4/breadcrumb-selection-list :init components4/breadcrumb-selection-list-settings}
       'm4/date-field                    {:view components4/date-field :init components4/date-field-settings}
       'm4/date-field-with-label         {:view components4/date-field-with-label :init components4/date-field-settings}
       'm4/expanding-control             {:view components4/expanding-control :init components4/expanding-control-settings}
       'm4/form-group                    {:view components4/form-group :init components4/form-group-settings}
       'm4/inline-form-group             {:view components4/inline-form-group :init components4/inline-form-group-settings}
       'm4/input-field                   {:view components4/input-field :init components4/input-field-settings}
       'm4/numeric-input-field           {:view components4/numeric-input-field :init components4/numeric-input-field-settings}
       'm4/input-field-with-label        {:view components4/input-field-with-label :init components4/input-field-settings}
       'm4/item-add-button               {:view components4/item-add-button :init components4/item-add-button-settings}
       'm4/item-edit-dialog              {:view components4/item-edit-dialog :init components4/item-edit-dialog-settings}
       'm4/boxmap-field                  {:view components4/boxmap-field :init components4/boxmap-field-settings}
       'm4/list-add-button               {:view components4/list-add-button :init components4/list-add-button-settings}
       'm4/list-edit-dialog              {:view components4/list-edit-dialog :init components4/list-edit-dialog-settings}
       'm4/page-errors                   {:view components4/page-errors :init components4/page-errors-settings}
       'm4/select-option                 {:view components4/select-option :init components4/select-option-settings}
       'm4/select-option-with-label      {:view components4/select-option-with-label :init components4/select-option-settings}
       'm4/select-value                  {:view components4/select-value :init components4/select-value-settings}
       'm4/select-value-with-label       {:view components4/select-value-with-label :init components4/select-value-settings}
       'm4/simple-list-option-picker     {:view components4/simple-list-option-picker :init components4/simple-list-option-picker-settings}
       'm4/simple-selection-list         {:view components4/simple-selection-list :init components4/simple-selection-list-settings}
       'm4/table-list-option-picker      {:view components4/table-list-option-picker :init components4/table-list-option-picker-settings}
       'm4/table-selection-list          {:view components4/table-selection-list :init components4/table-selection-list-settings}
       'm4/textarea-field                {:view components4/textarea-field :init components4/textarea-field-settings}
       'm4/textarea-field-with-label     {:view components4/textarea-field-with-label :init components4/textarea-field-settings}
       'm4/yes-no-field                  {:view components4/yes-no-field :init components4/yes-no-field-settings}})

(set! low-code/template-registry
      '{

        :platform/user-defined-entry-form
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

         [:label "TODO: parent metadata"]

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
           :data-path ["identificationInfo" "previouslyPublishedFlag"]
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
             :uri             "/api/What3"
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
             :uri             "/api/What4"
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
             {:form-id    [:form]
              :data-path  ["identificationInfo" "keywordsPlatform" "keywords"]
              :value-path ["uri"]
              :added-path ["isUserDefined"]}]]

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
             {:form-id    [:form]
              :data-path  ["identificationInfo" "keywordsInstrument" "keywords"]
              :value-path ["uri"]
              :added-path ["isUserDefined"]}]]
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
             {:form-id    [:form]
              :data-path  ["identificationInfo" "keywordsParameters" "keywords"]
              :value-path ["uri"]
              :added-path ["isUserDefined"]}]]
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
            :toolTip "TODO"}
           [m4/async-select-option
            {:form-id    [:form]
             :data-path  ["What10"]
             :uri        "/api/What10"
             :label-path ["label"]
             :value-path ["uri"]}]]]

         [m4/expanding-control {:label "Horizontal Resolution" :required true}
          [m4/form-group
           {:label   "Select a Horizontal Resolution range"
            :toolTip "TODO"}
           [m4/async-select-option
            {:form-id    [:form]
             :data-path  ["What11"]
             :uri        "/api/What11"
             :label-path ["label"]
             :value-path ["uri"]}]]]

         [m4/expanding-control {:label "Vertical Resolution (Optional)" :required true}
          [m4/form-group
           {:label   "Select a Vertical Resolution range"
            :toolTip "TODO"}
           [m4/async-select-option
            {:form-id    [:form]
             :data-path  ["What12"]
             :uri        "/api/What12"
             :label-path ["label"]
             :value-path ["uri"]}]]]

         [m4/expanding-control {:label "Australian Plant Name Index (Optional)" :required true}
          [m4/form-group
           {:label   "Select Plant Name Indexes keywords"
            :toolTip "TODO"}
           [m4/async-list-picker
            {:form-id         [:form]
             :data-path       ["What13"]
             :kind            :breadcrumb
             :uri             "/api/What13"
             :label-path      ["label"]
             :value-path      ["uri"]
             :breadcrumb-path ["breadcrumb"]}]
           [m4/breadcrumb-selection-list
            {:form-id         [:form]
             :data-path       ["What13"]
             :label-path      ["label"]
             :value-path      ["uri"]
             :breadcrumb-path ["breadcrumb"]}]]]

         [m4/expanding-control {:label "Australian Faunal Directory (Optional)" :required true}
          [m4/form-group
           {:label   "Select Australian Faunal Directory keywords"
            :toolTip "TODO"}
           [m4/async-list-picker
            {:form-id         [:form]
             :data-path       ["What14"]
             :kind            :breadcrumb
             :uri             "/api/What14"
             :label-path      ["label"]
             :value-path      ["uri"]
             :breadcrumb-path ["breadcrumb"]}]
           [m4/breadcrumb-selection-list
            {:form-id         [:form]
             :data-path       ["What14"]
             :label-path      ["label"]
             :value-path      ["uri"]
             :breadcrumb-path ["breadcrumb"]}]]]

         [m4/expanding-control {:label "Additional Keywords (Optional)" :required true}
          [m4/form-group
           {:label   "Additional theme keywords can be added for review and approval"
            :toolTip "TODO"}

           [:p "TODO: Keyword input field"]
           [:p "TODO: Keyword list"]

           ]]

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
                   :grid-template-columns "1fr 1fr 1fr"}}
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
            {:form-id   [:form]
             :data-path ["identificationInfo" "geographicElement" "siteDescription"]
             :label     "Provide a site description (optional)"
             :toolTip   "TODO"}]

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
             {:form-id    [:form]
              :data-path  ["identificationInfo" "geographicElement" "boxes"]
              :value-path ["uri"]
              :added-path ["isUserDefined"]}]

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
             [m4/async-select-option
              {:form-id     [:form]
               :data-path   ["identificationInfo" "CoordinateReferenceSystem"]
               :uri         "/api/What9"
               :label-path  ["label"]
               :value-path  ["uri"]
               :placeholder "Select from list"}]]

            [m4/form-group
             {:form-id   [:form]
              :data-path ["identificationInfo" "DateOfDynamicDatum"]
              :label     "Date of dynamic datum"
              :toolTip   "TODO"}
             [m4/date-field-with-label
              {:form-id   [:form]
               :data-path ["identificationInfo" "DateOfDynamicDatum"]
               :minDate   "1900-01-01"
               :maxDate   "2100-01-01"}]]]

           [:h3 "Vertical extent (optional)"]
           [:p "The vertial extent is optional.  If you choose to enter details then the following fields are mandatory"]

           [m4/inline-form-group
            {:label    "Vertical Coordinte Reference System"
             :required true
             :toolTip  "TODO"}
            [m4/async-select-option
             {:form-id     [:form]
              :data-path   ["identificationInfo" "verticalElement" "CoordinteReferenceSystem"]
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
                :data-path   ["identificationInfo" "topicCategory"]
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
                :unit      ""                               ; Driven by logic
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
            :data-path [?data-path "westBoundLongitude"]}]]

         ]

        :who
        [:div


         ;[m4/expanding-control {:label "Responsible for the creation of dataset" :required true}]

         [m4/list-add-button
          {:form-id    [:form]
           :data-path  ["identificationInfo" "citedResponsibleParty"]
           :value-path ["uri"]
           :added-path ["isUserDefined"]}]

         [m4/simple-selection-list
          {:form-id    [:form]
           :data-path  ["identificationInfo" "citedResponsibleParty"]
           :label-path ["contact" "familyName"]
           :value-path ["uri"]
           :added-path ["isUserDefined"]}]

         [m4/list-edit-dialog
          {:form-id     [:form]
           :data-path   ["identificationInfo" "citedResponsibleParty"]
           :title       "Responsible for creating the data"
           :template-id :person/user-defined-entry-form}]

         ;[m4/expanding-control {:label "Point of contact for dataset" :required true}]
         ;[m4/list-add-button
         ; {:form-id    [:form]
         ;  :data-path  ["identificationInfo" "PointOfContactForDataset"]
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
            :template-id :organisation/user-defined-entry-form}]]
         ]

        :organisation/user-defined-entry-form
        [:div

         [m4/form-group
          {:form-id   ?form-id
           :data-path [?data-path "organisationName"]
           :label     "Organisation Name"}
          [m4/input-field
           {:form-id   ?form-id
            :data-path [?data-path "organisationName"]}]]]

        :how
        [:div
         #_[m4/page-errors
            {:form-id    [:form]
             :data-path  []
             :data-paths [["resourceLineage" "processStep"]
                          ["dataQualityInfo" "methods"]
                          ["dataQualityInfo" "results"]]}]
         [:h2 "6: How"]
         [m3/Methods {:form-id   [:form]
                      :data-path ["resourceLineage" "processStep"]}]
         [m4/textarea-field-with-label
          {:form-id     [:form]
           :data-path   ["dataQualityInfo" "methods"]
           :placeholder "Placeholder text=Provide a brief summary of the source of the data and related collection and/or processing methods."
           :label       "Method"}]
         [m4/textarea-field-with-label
          {:form-id     [:form]
           :data-path   ["dataQualityInfo" "results"]
           :label       "Data Quality Results"
           :placeholder "Provide a statement regarding the data quality assessment results."
           :toolTip     "Example: RMSE relative to reference data set; horizontal or vertical positional accuracy; etc."
           :maxLength   1000
           :rows        20}]
         [:div.link-right-container [:a.link-right {:href "#quality"} "Next"]]]

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
         [:h4 "Environment Description (Optional)"]
         [m4/textarea-field-with-label
          {:form-id     [:form]
           :data-path   ["identificationInfo" "environment"]
           :label       "Environmental description"
           :placeholder "Information about the source and software to process the resource"
           :helperText  "Software, computer operating system, file name, or dataset size"
           :maxLength   1000}]
         [:h4 "Association Documentation (Optional)"]
         [m4/textarea-field-with-label
          {:form-id     [:form]
           :data-path   ["identificationInfo" "supplemental"]
           :label       "Supplemental Information"
           :placeholder "Information about how to interpret the resource, example: Pixel value indicates the number of days since reference date 1970-01-01"
           :helperText  "Any supplemental information needed to interpret the resource"
           :maxLength   1000}]
         [:h4 "Resource specific usage (Optional)"]
         [m4/textarea-field-with-label
          {:form-id     [:form]
           :data-path   ["identificationInfo" "resourceSpecificUsage"]
           :label       "Resource specific usage"
           :placeholder "Resource specific usage..."
           :helperText  "What can this resource be used for environmental research?"
           :maxLength   1000}]
         [:h4 "Acknowledgment (Optional)"]
         [m4/textarea-field-with-label
          {:form-id     [:form]
           :data-path   ["identificationInfo" "credit"]
           :label       "Acknowledgment"
           :placeholder "The project was funded by xxx and yyy"
           :helperText  "Write a sentence acknowledging sponsors, data providers or funding organisations"
           :maxLength   1000}]
         [:h4 "Citation (Optional)"]
         [m4/textarea-field-with-label
          {:form-id    [:form]
           :data-path  ["identificationInfo" "customCitation"]
           :label      "Specific citation"
           :helperText "The format of the standard citation is provided at https://ternaus.atlassian.net/wiki/spaces/TERNSup/pages/1223163969/How+is+the+citation+constructed+from+the+metadata  For a non-standard citation, provide the details below."
           :maxLength  1000}]

         [:h4 "Data parameters"]
         [m3/DataParametersTable {:form-id   [:form]
                                  :data-path ["identificationInfo" "dataParameters"]}]
         [:br]
         [:h4 "Pixel Size"]
         [:div.row
          [:div.col-md-6
           [m3/NasaListSelectField {:keyword   :horizontalResolution
                                    :form-id   [:form]
                                    :data-path ["identificationInfo"]}]]]
         [:br]
         [:h4 "Resource constraints"]
         [m3/ResourceConstraints]
         [m3/UseLimitations {:form-id   [:form]
                             :data-path ["identificationInfo" "useLimitations"]}]
         [:br]
         [:h4 "Supplemental information"]
         [m3/SupportingResource {:form-id   [:form]
                                 :data-path ["supportingResources"]}]
         [:form-id [:form]
          [m3/SupplementalInformation [:identificationInfo :supplementalInformation]]]
         [:br]
         [:h4 "Distribution"]
         [m4/input-field-with-label
          {:form-id     [:form]
           :data-path   ["distributionInfo" "distributionFormat" "name"]
           :label       "Data file format"
           :placeholder "e.g. Microsoft Excel, CSV, NetCDF"
           :helperText  nil
           :toolTip     nil
           :maxLength   100
           :required    nil}]
         [m4/input-field-with-label
          {:form-id     [:form]
           :data-path   ["distributionInfo" "distributionFormat" "version"]
           :label       "Data file format date/version"
           :placeholder "Date format date or version if applicable"
           :helperText  nil
           :toolTip     nil
           :maxLength   20
           :required    nil}]
         [m4/textarea-field-with-label
          {:form-id     [:form]
           :data-path   ["resourceLineage" "lineage"]
           :label       "Lineage"
           :placeholder "Provide a brief summary of the source of the data and related collection and/or processing methods."
           :toolTip     "Example: Data was collected at the site using the methods described in yyy Manual, refer to https://doi.org/10.5194/bg-14-2903-2017"
           :maxLength   1000}]
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
