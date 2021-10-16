(ns metcalf3.handlers
  (:require [cljs.core.async :refer []]
            [cljs.spec.alpha :as s]
            [clojure.string :as str]
            [clojure.walk :as walk]
            [goog.object :as gobj]
            [interop.moment :as moment]
            [metcalf3.logic :as logic3]
            [metcalf3.utils :as utils]
            [re-frame.core :as rf]))

(defn init-db
  [_ [_ payload]]
  (let [db' (logic3/initial-state payload)]
    {:db         db'
     :fx         [[:ui/setup-blueprint]]
     :dispatch-n (for [api-key (keys (get db' :api))]
                   [:handlers/load-api-options [:api api-key]])}))

(defn load-api-options
  [{:keys [db]} [_ api-path]]
  (let [{:keys [uri options]} (get-in db api-path)]
    (when (nil? options)
      {:xhrio/get-json {:uri uri :resp-v [::-load-api-options api-path]}})))

(defn -load-api-options
  [{:keys [db]} [_ api-path json]]
  (let [results (gobj/get json "results")]
    {:db (update-in db api-path assoc :options results)}))

(defn build-es-query
  [query]
  (.stringify js/JSON (clj->js
                        {:query query})))

(defn load-es-options
  [{:keys [db]} [_ api-path query]]
  (let [{:keys [uri]} (get-in db api-path)]
    {:xhrio/post-json {:uri uri :data (build-es-query query) :resp-v [::-load-es-options api-path query]}
     :db              (update-in db api-path assoc :most-recent-query query)}))

(defn -load-es-options
  [{:keys [db]} [_ api-path query json]]
  (let [most-recent-query (get-in db (conj api-path :most-recent-query))
        results (gobj/get json "hits")
        hits (gobj/get results "hits")
        reshaped (clj->js (into []
                                (map (fn [x] {:is_selectable     true
                                              :vocabularyTermURL (get-in x [:_source :uri])
                                              :term              (let [term (get-in x [:_source :label])] (if (vector? term) (first term) term))
                                              :code              (get-in x [:_source :ucumCode])
                                              :breadcrumb        (get-in x [:_source :breadcrumb])
                                              :altLabel          (clojure.string/join ", " (get-in x [:_source :altLabel]))
                                              })
                                     (js->clj hits :keywordize-keys true))))]
    (when (or (= most-recent-query query) (and (not most-recent-query) query))
      {:db (update-in db api-path assoc :options reshaped)})))

(defn close-modal
  [{:keys [db]}]
  {:db (update db :alert pop)})

(defn close-and-cancel
  [{:keys [db]} _]
  (let [{:keys [on-cancel]} (peek (:alert db))]
    ; TODO: can we refactor around specific handlers to avoid this?
    (when on-cancel (on-cancel))
    {:db (update db :alert pop)}))

(defn close-and-confirm
  [{:keys [db]} _]
  (let [{:keys [on-confirm]} (peek (:alert db))]
    ; TODO: can we refactor around specific handlers to avoid this?
    (when on-confirm (on-confirm))
    {:db (update db :alert pop)}))

(defn open-modal [db props]
  (update db :alert
          (fn [alerts]
            (when-not (= (peek alerts) props)
              (conj alerts props)))))

(defn open-modal-handler
  [{:keys [db]} [_ props]]
  {:db (open-modal db props)})

(defn del-value
  [{:keys [db]} [_ many-field-path i]]
  {:db (update-in db many-field-path update :value utils/vec-remove i)})

(defn new-field!
  [{:keys [db]} [_ path]]
  {:db (let [many-field (get-in db path)
             new-field (logic3/new-value-field many-field)]
         (update-in db path update :value #(conj % new-field)))})

(defn add-field!
  [{:keys [db]} [_ path field]]
  {:db (update-in db path update :value conj field)})

(defn add-value!
  [{:keys [db]} [_ path value]]
  (let [many-field (get-in db path)
        new-field (-> (logic3/new-value-field many-field)
                      (assoc :value value))]
    {:db (update-in db path update :value conj new-field)}))

(defn add-attachment
  [{:keys [db]} [_ attachment-data]]
  (let [data (select-keys attachment-data [:file :name :delete_url])
        template (get-in db [:form :fields :attachments :fields])
        new-value (reduce (fn [form-acc [k v]]
                            (assoc-in form-acc [k :value] v))
                          template data)]
    {:db (update-in db [:form :fields :attachments :value] conj {:value new-value})}))

(defn archive-current-document
  [{:keys [db]} _]
  (let [transition_url (-> db :context :document :transition_url)
        success_url (-> db :context :urls :Dashboard)]
    {:fx/archive-current-document
     {:url       transition_url
      :success-v [:handlers/archive-current-document-success success_url]
      :error-v   [:handlers/open-modal {:type :alert :message "Unable to delete"}]}}))

(defn archive-current-document-success
  [_ [_ url]] {:fx/set-location-href url})

(defn back
  [{:keys [db]} _]
  (let [back (get-in db [:page :back])
        back (into {} back)]
    {:db (assoc db :page back)}))

(defn ror
  "Reverse OR: use it to update source value only if destination value is not falsey."
  [a b]
  (or b a))

(defn update-address
  [{:keys [db]} [_ contact-path data]]
  (let [{:strs [city deliveryPoint deliveryPoint2
                postalCode country administrativeArea]
         :or   {city               ""
                deliveryPoint      ""
                deliveryPoint2     ""
                postalCode         ""
                country            ""
                administrativeArea ""}} data]
    {:db (-> db
             (update-in contact-path update-in [:address :deliveryPoint :value] ror deliveryPoint)
             (update-in contact-path update-in [:address :deliveryPoint2 :value] ror deliveryPoint2)
             (update-in contact-path update-in [:address :city :value] ror city)
             (update-in contact-path update-in [:address :administrativeArea :value] ror administrativeArea)
             (update-in contact-path update-in [:address :postalCode :value] ror postalCode)
             (update-in contact-path update-in [:address :country :value] ror country))}))

(defn update-dp-term
  [{:keys [db]} [_ dp-term-path sub-paths option]]
  (let [option (if (map? option) option (utils/js-lookup option))
        {:keys [term vocabularyTermURL vocabularyVersion termDefinition]} option]
    {:db (-> db
             (update-in dp-term-path assoc-in [(:term sub-paths) :value] term)
             (update-in dp-term-path assoc-in [(:vocabularyTermURL sub-paths) :value] vocabularyTermURL)
             (update-in dp-term-path assoc-in [(:vocabularyVersion sub-paths) :value] vocabularyVersion)
             (update-in dp-term-path assoc-in [(:termDefinition sub-paths) :value] termDefinition))}))

(defn update-nasa-list-value
  [{:keys [db]} [_ path option]]
  (let [option (if (map? option) option (utils/js-lookup option))
        {:keys [prefLabel uri]} option]
    {:db (-> db
             (update-in path assoc-in [:prefLabel :value] prefLabel)
             (update-in path assoc-in [:uri :value] uri))}))

(defn update-boxes
  [{:keys [db]} [_ boxes-path geojson]]
  (s/assert map? geojson)
  (let [geometries (mapv :geometry (:features geojson))
        boxes (mapv utils/geometry->box-value geometries)
        boxes-value-path (conj boxes-path :value)
        box-values (mapv (fn [box] {:value box}) boxes)
        db (assoc-in db boxes-value-path box-values)]
    {:db db}))

(defn update-method-term
  [{:keys [db]} [_ method-path option]]
  (let [option (if (map? option) option (utils/js-lookup option))
        {:keys [term vocabularyTermURL termDefinition]} option]
    {:db (-> db
             (update-in method-path assoc-in [:name :value] term)
             (update-in method-path assoc-in [:uri :value] vocabularyTermURL)
             (update-in method-path assoc-in [:description :value] termDefinition))}))

(defn update-person
  [{:keys [db]} [_ person-path option]]
  (let [option (if (map? option) option (utils/js-lookup option))
        option (walk/keywordize-keys option)
        {:keys [givenName prefLabel familyName uri orcid
                orgUri isUserAdded electronicMailAddress]} option
        org (when orgUri
              (first (filter (fn [x]
                               (= orgUri (gobj/get x "uri"))) (:options (get-in db [:api :institution])))))
        org (js->clj org)
        db (-> db
               (update-in person-path assoc-in [:givenName :value] givenName)
               (update-in person-path assoc-in [:uri :value] uri)
               (update-in person-path assoc-in [:orcid :value] orcid)
               (update-in person-path assoc-in [:individualName :value] prefLabel)
               (update-in person-path assoc-in [:familyName :value] familyName)
               (update-in person-path assoc-in [:electronicMailAddress :value] electronicMailAddress)
               (update-in person-path assoc-in [:isUserAdded :value] isUserAdded))]
    {:db       db
     :dispatch [::-update-person person-path org]}))

(defn update-method-name
  [{:keys [db]} [_ method-path name]]
  {:db (-> db
           (update-in method-path assoc-in [:name :value] name)
           (update-in method-path assoc-in [:uri :value] "XXX"))})

(defn setter
  [{:keys [db]} [_ path k v]]
  {:db (update-in db path assoc k v)})

(defn unsaved-input-check-helper [{:keys [new-value errors] :as keywords-data}]
  (assoc keywords-data
    :errors
    (if (str/blank? new-value)
      (disj (set errors) "Unsaved value in the keyword input field")
      (conj (set errors) "Unsaved value in the keyword input field"))))

(defn check-unsaved-keyword-input
  [{:keys [db]} [_ keywords-path]]
  {:db (update-in db keywords-path unsaved-input-check-helper)})

(defn remove-party
  [{:keys [db]} [_ parties-path item]]
  {:db (update-in db parties-path update :value utils/vec-remove item)})

(defn reset-form
  [{:keys [db]} [_ form-path]]
  (s/assert vector? form-path)
  {:db (update-in db form-path logic3/reset-form)})

(defn show-errors
  [{:keys [db]} [_ path]]
  (s/assert vector path)
  {:db (update-in db path assoc :show-errors true)})

(defn hide-errors
  [{:keys [db]} [_ path]]
  {:db (update-in db path assoc :show-errors false)})

(defn toggle-status-filter
  [{:keys [db]} [_ status status-filter]]
  (let [status-filter (get-in db [:page :status-filter] status-filter)
        status-filter (if (contains? status-filter status)
                        (disj status-filter status)
                        (conj status-filter status))]
    {:db (assoc-in db [:page :status-filter] status-filter)}))

(defn show-all-documents
  [{:keys [db]} _]
  (let [documents (get-in db [:context :documents])
        status-freq (frequencies (map :status documents))]
    {:db (assoc-in db [:page :status-filter] (set (keys status-freq)))}))

(defn load-error-page
  [{:keys [db]} [_ data]]
  {:db (-> db
           (assoc-in [:page :name] "Error")
           (assoc-in [:page :text] (-> data :response :message))
           (assoc-in [:page :code] (-> data :status))
           (assoc-in [:page :detail] (-> data :response)))})

(defn set-value
  [{:keys [db]} [_ field-path value]]
  {:db (update-in db field-path assoc :value value)})

(defn date-field-value-change
  [{:keys [db]} [_ field-path widget-value]]
  (let [field-value (when widget-value (moment/format widget-value "YYYY-MM-DD"))]
    {:db (-> db
             (update-in field-path assoc :value field-value)
             (update-in field-path assoc :show-errors true))}))

(defn textarea-field-value-change
  [{:keys [db]} [_ field-path value]]
  {:db (-> db
           (update-in field-path assoc :value value)
           (update-in field-path assoc :show-errors true))})

(defn org-changed
  [{:keys [db]} [_ path value]]
  {:db       (-> db
                 (update-in (conj path :organisationName) assoc :value (get value "organisationName") :show-errors true)
                 (update-in (conj path :organisationIdentifier) assoc :value (str (get value "uri") "||" (get value "city")) :show-errors true))
   :dispatch [::-org-changed path value]})

(defn person-detail-changed
  [{:keys [db]} [_ path field value isUserAdded]]
  ; if they change the name and it's not already a custom user, generate a new uuid
  (let [new-uuid (random-uuid)
        person-uri (str "https://w3id.org/tern/resources/" new-uuid)
        current-value (get-in db (conj path field :value))
        value-changed (not= current-value value)
        db' (if (:value isUserAdded)
              db
              (-> db
                  (assoc-in (conj path :uri :value) person-uri)
                  (update-in (conj path :isUserAdded) assoc :value true :show-errors true)))]
    (s/assert vector? path)
    (when value-changed
      {:db (-> db'
               (update-in (conj path field) assoc :value value :show-errors true)
               (update-in (conj path :individualName) assoc :value "" :show-errors true))})))

(defn value-changed
  [{:keys [db]} [_ path value]]
  (s/assert vector? path)
  {:db (update-in db path assoc :value value :show-errors true)})

(defn set-tab
  [{:keys [db]} [_ id]]
  {:db (assoc-in db [:page :tab] id)})

(defn load-errors
  [{:keys [db]} [_ form-path data]]
  {:db (update-in db form-path logic3/load-errors data)})

(defn add-keyword-extra
  [{:keys [db]} [_ keywords-path value]]
  {:db (let [keywords (get-in db keywords-path)]
         (if-not (empty? value)
           (assoc-in db keywords-path (vec (conj keywords {:value value})))
           db))})

(defn del-keyword-extra
  [{:keys [db]} [_ keywords-path value]]
  (let [keywords (get-in db keywords-path)]
    {:db (assoc-in db keywords-path (vec (remove #(= value (:value %)) keywords)))}))

(defn add-nodes
  [{:keys [db]} [_ api-path nodes]]
  {:db (update-in db api-path update :options into nodes)})

(defn dashboard-create-click
  [{:keys [db]} _]
  {:db (open-modal db {:type :DashboardCreateModal})})

(defn create-document-success
  [{:keys [db]} [_ data]]
  (-> {:db db}
      (update-in [:db :create_form] logic3/reset-form)
      (update-in [:db :create_form] assoc :show-errors false)
      (update-in [:db :alert] pop)
      (update :fx conj [:fx/set-location-href (-> data :document :url)])))

(defn create-document-error
  [{:keys [db]} [_ data]]
  (if (= (:status data) 400)
    {:dispatch [:handlers/load-error-page data]}
    {:db       (update-in db [:create_form] assoc :show-errors true)
     :dispatch [:handlers/load-errors [:create_form] (:response data)]}))

(defn dashboard-create-save
  [{:keys [db]} _]
  (let [{:keys [url] :as form} (get-in db [:create_form])
        form (logic3/validate-required-fields form)]
    (if (logic3/is-valid? form)
      {:fx/create-document {:url       url
                            :params    (logic3/extract-data form)
                            :success-v [:handlers/create-document-success]
                            :error-v   [:handlers/create-document-error]}}
      {:db (update-in db [:create_form] assoc :show-errors true)})))

(defn clone-document
  [_ [_ url]]
  {:fx/clone-document {:url       url
                       :success-v [:handlers/clone-document-success]
                       :error-v   [:handlers/clone-document-error]}})

(defn clone-document-success
  [_ [_ data]]
  {:fx/set-location-href (get-in data [:document :url])})

(defn clone-document-error
  [_ _]
  {:dispatch [:handlers/open-modal {:type :alert :message "Unable to clone"}]})

(defn transite-doc-click
  [transition]
  (fn [_ [_ url]]
    (let [trans-name (first (clojure.string/split transition "_"))]
      {:dispatch [:handlers/open-modal
                  {:type       :confirm
                   :title      trans-name
                   :message    (str "Are you sure you want to " trans-name " this record?")
                   :on-confirm #(rf/dispatch [::-transite-doc-click-confirm url transition])}]})))

(defn -transite-doc-click-confirm
  [_ [_ url transition]]
  {:fx/transition-current-document
   {:url        url
    :transition transition
    :success-v  [::-transite-doc-confirm-success transition]
    :error-v    [::-transite-doc-confirm-error]}})

(defn -transite-doc-confirm-success
  [{:keys [db]} [_ transition data]]
  (let [{{:keys [uuid] :as doc} :document} data]
    {:db (update-in db [:context :documents]
                    (fn [docs]
                      (reduce #(if (= uuid (:uuid %2))
                                 (if (= transition "delete_archived")
                                   %1
                                   (conj %1 doc))
                                 (conj %1 %2))
                              [] docs)))}))

(defn -transite-doc-confirm-error
  [_ [_ transition]]
  (let [trans-name (first (clojure.string/split transition "_"))]
    {:dispatch [:handlers/open-modal
                {:type    :alert
                 :message (str "Unable to " trans-name)}]}))

(defn lodge-click
  [{:keys [db]} _]
  (let [url (get-in db [:form :url])
        data (-> db :form :fields logic3/extract-field-values)]
    {:db (assoc-in db [:page :metcalf3.handlers/saving?] true)
     :fx/save-current-document
         {:url       url
          :data      data
          :success-v [:handlers/lodge-save-success data]
          :error-v   [:handlers/lodge-error]}}))

(defn lodge-save-success
  [{:keys [db]} [_ data]]
  (let [url (get-in db [:context :document :transition_url])]
    {:db (-> db
             (assoc-in [:form :data] data)
             (assoc-in [:page :metcalf3.handlers/saving?] true))
     :fx/submit-current-document
         {:url       url
          :success-v [:handlers/lodge-submit-success]
          :error-v   [:handlers/lodge-error]}}))

(defn lodge-submit-success
  [{:keys [db]} [_ resp]]
  (let [document (get-in resp [:document])]
    (s/assert map? document)
    {:db (-> db
             (assoc-in [:page :metcalf3.handlers/saving?] false)
             (assoc-in [:context :document] document))}))

(defn lodge-error
  [{:keys [db]} [_ {:keys [status failure]}]]
  {:db       (assoc-in db [:page :metcalf3.handlers/saving?] false)
   :dispatch [:handlers/open-modal
              {:type    :alert
               :message (str "Unable to lodge: " status " " failure)}]})

(defn help-menu-open
  [_ [_ url]]
  {:window/open {:url url :windowName "_blank"}})
