(ns metcalf3.handlers
  (:require [cljs.spec.alpha :as s]
            [clojure.string :as str]
            [clojure.walk :as walk]
            [goog.object :as gobj]
            [interop.moment :as moment]
            [metcalf3.fx :as fx3]
            [metcalf3.logic :as logic3]
            [metcalf3.utils :as utils]
            [re-frame.core :as rf]
            [metcalf4.blocks :as blocks]
            [metcalf4.rules :as rules]))

(defn load-api-options
  [{:keys [db]} [_ api-path]]
  (let [{:keys [uri options]} (get-in db api-path)]
    (when (nil? options)
      {::fx3/xhrio-get-json {:uri uri :resp-v [::-load-api-options api-path]}})))

(defn -load-api-options
  [{:keys [db]} [_ api-path json]]
  (let [results (gobj/get json "results")]
    {:db (update-in db api-path assoc :options results)}))

(defn build-es-query
  [query]
  (.stringify js/JSON (clj->js
                        {:query query})))

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

(defn open-modal
  [db props]
  (update db :alert
          (fn [alerts]
            (when-not (= (peek alerts) props)
              (conj alerts props)))))

(defn open-modal-handler
  [{:keys [db]} [_ props]]
  {:db (open-modal db props)})

(defn handle-page-view-edit-archive-click
  [{:keys [db]} _]
  {:db (open-modal db {:type       :confirm
                       :title      "Archive?"
                       :message    "Are you sure you want to archive this record?"
                       :on-confirm #(rf/dispatch [:app/page-view-edit-archive-click-confirm])})})

(defn document-teaser-clone-click
  [{:keys [db]} [_ clone_url]]
  {:db (open-modal db {:type       :confirm
                       :title      "Clone?"
                       :message    "Are you sure you want to clone this record?"
                       :on-confirm (fn [] (rf/dispatch [:app/clone-doc-confirm clone_url]))})})

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
    {::fx3/archive-current-document
     {:url       transition_url
      :success-v [::-archive-current-document-success success_url]
      :error-v   [::open-modal {:type :alert :message "Unable to delete"}]}}))

(defn -archive-current-document-success
  [_ [_ url]]
  {::fx3/set-location-href url})

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

(defn update-person
  [{:keys [db]} [_ person-path option]]
  (let [option (if (map? option) option (utils/js-lookup option))
        option (walk/keywordize-keys option)
        {:keys [givenName prefLabel familyName uri orcid
                orgUri isUserAdded electronicMailAddress]} option
        org (when orgUri
              (first (filter (fn [x]
                               (= orgUri (gobj/get x "uri"))) (:options (get-in db [:api :api/institution])))))
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

(defn remove-party
  [{:keys [db]} [_ parties-path item]]
  {:db (update-in db parties-path update :value utils/vec-remove item)})

(defn show-errors
  [{:keys [db]} [_ path]]
  (s/assert vector path)
  {:db (update-in db path assoc :show-errors true)})

(defn hide-errors
  [{:keys [db]} [_ path]]
  {:db (update-in db path assoc :show-errors false)})

(defn toggle-status-filter
  [{:keys [db]} [_ {:keys [status-id status-filter]}]]
  (let [status-filter (get-in db [:page :status-filter] status-filter)
        status-filter (if (contains? status-filter status-id)
                        (disj status-filter status-id)
                        (conj status-filter status-id))]
    {:db (assoc-in db [:page :status-filter] status-filter)}))

(defn show-all-documents
  [{:keys [db]} _]
  (let [documents (get-in db [:context :documents])
        status-freq (frequencies (map :status documents))]
    {:db (assoc-in db [:page :status-filter] (set (keys status-freq)))}))

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

(defn dashboard-create-click
  [{:keys [db]} _]
  {:db (open-modal db {:type :DashboardCreateModal})})

(defn -dashboard-create-save-success
  [_ [_ data]]
  {::fx3/set-location-href (-> data :document :url)})

(defn -dashboard-create-save-error
  [{:keys [db]} [_ data]]
  (if (= (:status data) 400)
    (-> {:db db}
        (assoc-in [:db :page :name] "Error")
        (assoc-in [:db :page :text] (-> data :response :message))
        (assoc-in [:db :page :code] (-> data :status))
        (assoc-in [:db :page :detail] (-> data :response)))
    (-> {:db db}
        (update-in [:db :create_form] assoc :show-errors true)
        (update-in [:db :create_form] logic3/load-errors (:response data)))))

(defn dashboard-create-save
  [{:keys [db]} _]
  (let [{:keys [url] :as form} (get-in db [:create_form])
        form (logic3/validate-required-fields form)]
    {::fx3/create-document
     {:url       url
      :params    (blocks/as-data (:state form))
      :success-v [::-dashboard-create-save-success]
      :error-v   [::-dashboard-create-save-error]}}))

(defn clone-document
  [_ [_ url]]
  {::fx3/clone-document
   {:url       url
    :success-v [::-clone-document-success]
    :error-v   [::-clone-document-error]}})

(defn -clone-document-success
  [_ [_ data]]
  {::fx3/set-location-href (get-in data [:document :url])})

(defn -clone-document-error
  [_ _]
  {:dispatch [::open-modal {:type :alert :message "Unable to clone"}]})

(defn transite-doc-click
  [transition]
  (fn [_ [_ url]]
    (let [trans-name (first (clojure.string/split transition "_"))]
      {:dispatch [::open-modal
                  {:type       :confirm
                   :title      trans-name
                   :message    (str "Are you sure you want to " trans-name " this record?")
                   :on-confirm #(rf/dispatch [::-transite-doc-click-confirm url transition])}]})))

(defn -transite-doc-click-confirm
  [_ [_ url transition]]
  {::fx3/transition-current-document
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
    {:dispatch [::open-modal
                {:type    :alert
                 :message (str "Unable to " trans-name)}]}))

(defn lodge-click
  [{:keys [db]} _]
  (let [url (get-in db [:form :url])
        state0 (get-in db [:form :state])
        state1 (blocks/postwalk rules/apply-rules state0)
        data (blocks/as-data state1)]
    {:db (assoc-in db [:page :metcalf3.handlers/saving?] true)
     ::fx3/save-current-document
         {:url       url
          :data      data
          :success-v [::-lodge-click-success data]
          :error-v   [::-lodge-click-error]}}))

(defn lodge-save-success
  [{:keys [db]} [_ data]]
  (let [url (get-in db [:context :document :transition_url])]
    {:db (-> db
             (assoc-in [:form :data] data)
             (assoc-in [:page :metcalf3.handlers/saving?] true))
     ::fx3/submit-current-document
         {:url       url
          :success-v [::-lodge-save-success]
          :error-v   [::-lodge-save-error]}}))

(defn -lodge-save-success
  [{:keys [db]} [_ resp]]
  (let [document (get-in resp [:document])]
    (s/assert map? document)
    {:db (-> db
             (assoc-in [:page :metcalf3.handlers/saving?] false)
             (assoc-in [:context :document] document))}))

(defn lodge-error
  [{:keys [db]} [_ {:keys [status failure]}]]
  {:db       (assoc-in db [:page :metcalf3.handlers/saving?] false)
   :dispatch [::open-modal
              {:type    :alert
               :message (str "Unable to lodge: " status " " failure)}]})