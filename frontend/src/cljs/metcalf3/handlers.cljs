(ns metcalf3.handlers
  (:require-macros [cljs.core.async.macros :refer [go alt! go-loop]])
  (:require [clojure.string :as str]
            [cljs.core.async :refer [put! <! alts! chan pub sub timeout dropping-buffer]]
            [re-frame.db :refer [app-db]]
            [metcalf3.fx :as fx]
            [metcalf3.logic :as logic :refer [reset-field field-zipper]]
            [goog.object :as gobj]
            [re-frame.core :as rf]
            [metcalf3.ins :as ins]
            [metcalf3.utils :as utils]
            [cljs.spec.alpha :as s]
            [interop.moment :as moment])
  (:import [goog.net Cookies]))

(rf/reg-event-fx
  :handlers/init-db
  ins/std-ins
  (fn [_ _]
    (let [db' (logic/initial-state (js->clj (aget js/window "payload") :keywordize-keys true))]
      {:db         db'
       :dispatch-n (for [api-key (keys (get db' :api))]
                     [:handlers/load-api-options [:api api-key]])})))

(rf/reg-event-fx
  :handlers/load-api-options
  ins/std-ins
  (fn [{:keys [db]} [_ api-path]]
    (let [{:keys [uri options]} (get-in db api-path)]
      (when (nil? options)
        {:xhrio/get-json {:uri uri :resp-v [:handlers/load-api-options-resp api-path]}}))))

(rf/reg-event-db
  :handlers/load-api-options-resp
  ins/std-ins
  (fn [db [_ api-path json]]
    (let [results (gobj/get json "results")]
      (update-in db api-path assoc :options results))))

(rf/reg-event-fx
  :handlers/load-es-options
  ins/std-ins
  (fn [{:keys [db]} [_ api-path query]]
    (let [{:keys [uri]} (get-in db api-path)]
      {:xhrio/get-json {:uri (str uri query) :resp-v [:handlers/load-es-options-resp api-path query]}
       :db (update-in db api-path assoc :most-recent-query query)})))

(defn build-es-query
  [query]
  (.stringify js/JSON (clj->js
                       {:size 50
                        :query {:match_phrase_prefix {:label query}}})))

(rf/reg-event-fx
 :handlers/search-es-options
 ins/std-ins
 (fn [{:keys [db]} [_ api-path query]]
   (let [{:keys [uri]} (get-in db api-path)]
     {:xhrio/post-json {:uri uri :data (build-es-query query) :resp-v [:handlers/load-es-options-resp api-path query]}
      :db (update-in db api-path assoc :most-recent-query query)})))


(defn build-es-query-units
  [query]
  (.stringify js/JSON (clj->js
                       {:size 50
                        :query {:multi_match {:query query :type "phrase_prefix" :fields ["label", "ucumCode"]}}})))

(rf/reg-event-fx
 :handlers/search-es-options-units
 ins/std-ins
 (fn [{:keys [db]} [_ api-path query]]
   (let [{:keys [uri]} (get-in db api-path)]
     {:xhrio/post-json {:uri uri :data (build-es-query-units query) :resp-v [:handlers/load-es-options-resp api-path query]}
      :db (update-in db api-path assoc :most-recent-query query)})))


(rf/reg-event-db
  :handlers/load-es-options-resp
  ins/std-ins
  (fn [db [_ api-path query json]]
    (let [most-recent-query (get-in db (conj api-path :most-recent-query))
          results (gobj/get json "hits")
          hits (gobj/get results "hits")
          reshaped (clj->js (into []
                                  (map (fn [x] {:is_selectable true
                                                :vocabularyTermURL (get-in x [:_source :uri])
                                                :term (let [term (get-in x [:_source :label])] (if (vector? term) (first term) term))
                                                :code (get-in x [:_source :ucumCode])}) (js->clj hits :keywordize-keys true))))]
      (if (or (= most-recent-query query) (and (not most-recent-query) query))
        (update-in db api-path assoc :options reshaped)
        db))))

(rf/reg-event-db
  :handlers/close-modal
  ins/std-ins
  (fn [db]
    (update db :alert pop)))

(rf/reg-event-db
  :handlers/close-and-cancel
  ins/std-ins
  (fn [db _]
    (let [{:keys [on-cancel]} (peek (:alert db))]
      (when on-cancel (on-cancel))
      (update db :alert pop))))

(rf/reg-event-db
  :handlers/close-and-confirm
  ins/std-ins
  (fn [db _]
    (let [{:keys [on-confirm]} (peek (:alert db))]
      (when on-confirm (on-confirm))
      (update db :alert pop))))

(defn open-modal [db props]
  (update db :alert
          (fn [alerts]
            (when-not (= (peek alerts) props)
              (conj alerts props)))))

(rf/reg-event-db
  :handlers/open-modal
  ins/std-ins
  (fn [db [_ props]]
    (open-modal db props)))

(rf/reg-event-db
  :handlers/del-value
  ins/std-ins
  (fn [db [_ many-field-path i]]
    (update-in db many-field-path update :value utils/vec-remove i)))

(rf/reg-event-db
  :handlers/new-field!
  ins/std-ins
  (fn [db [_ path]]
    (let [many-field (get-in db path)
          new-field (logic/new-value-field many-field)]
      (update-in db path update :value #(conj % new-field)))))

(rf/reg-event-db
  :handlers/add-field!
  ins/std-ins
  (fn [db [_ path field]]
    (update-in db path update :value conj field)))

(rf/reg-event-db
  :handlers/add-value!
  ins/std-ins
  (fn [db [_ path value]]
    (let [many-field (get-in db path)
          new-field (-> (logic/new-value-field many-field)
                        (assoc :value value))]
      (update-in db path update :value conj new-field))))

(rf/reg-event-fx
  :handlers/save-current-document
  ins/std-ins
  (fn [{:keys [db]} _]
    (let [url (get-in db [:form :url])
          data (-> db :form :fields logic/extract-field-values)]
      {:db (assoc-in db [:page ::saving?] true)
       :fx/save-current-document
       {:url       url
        :data      data
        :success-v [:handlers/save-current-document-success data]
        :error-v   [:handlers/save-current-document-success]}})))

(rf/reg-event-db
  :handlers/save-current-document-success
  ins/std-ins
  (fn [db [_ data resp]]
    (let [doc (get-in resp [:form :document])]
      (-> db
          (assoc-in [:form :data] data)
          (assoc-in [:page ::saving?] false)
          (update-in [:context :document] merge doc)))))

(rf/reg-event-fx
  :handlers/save-current-document-error
  ins/std-ins
  (fn [_ [_ done-ch]]
    (put! done-ch true)
    {}))

(rf/reg-event-db
  :handlers/add-attachment
  ins/std-ins
  (fn [db [_ attachment-data]]
    (let [data (select-keys attachment-data [:file :name :delete_url])
          template (get-in db [:form :fields :attachments :fields])
          new-value (reduce (fn [form-acc [k v]]
                              (assoc-in form-acc [k :value] v))
                            template data)]
      (update-in db [:form :fields :attachments :value] conj {:value new-value}))))

(rf/reg-event-fx
  :handlers/archive-current-document
  ins/std-ins
  (fn [{:keys [db]} _]
    (let [transition_url (-> db :context :document :transition_url)
          success_url (-> db :context :urls :Dashboard)]
      {:fx/archive-current-document
       {:url transition_url
        :success-v        [:handlers/archive-current-document-success success_url]
        :error-v  [:handlers/open-modal {:type :alert :message "Unable to delete"}]}})))

(rf/reg-event-fx :handlers/archive-current-document-success ins/std-ins (fn [_ [_ url]] {:fx/set-location-href url}))

(rf/reg-event-db
  :handlers/back
  (fn [db _]
    (let [back (get-in db [:page :back])
          back (into {} back)]
      (assoc db :page back))))

(defn ror
  "Reverse OR: use it to update source value only if destination value is not falsey."
  [a b]
  (or b a))

(rf/reg-event-fx
  :handlers/update-address
  ins/std-ins
  (fn [{:keys [db]} [_ contact-path data]]
    (let [{:strs [city organisationName deliveryPoint deliveryPoint2
                  postalCode country administrativeArea]
           :or   {city               ""
                  organisationName   ""
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
               (update-in contact-path update-in [:address :country :value] ror country))})))

(rf/reg-event-db
  :handlers/update-dp-term
  ins/std-ins
  (fn [db [_ dp-term-path sub-paths option]]
    (let [option (if (map? option) option (utils/js-lookup option))
          {:keys [term vocabularyTermURL vocabularyVersion termDefinition]} option]
      (-> db
          (update-in dp-term-path assoc-in [(:term sub-paths) :value] term)
          (update-in dp-term-path assoc-in [(:vocabularyTermURL sub-paths) :value] vocabularyTermURL)
          (update-in dp-term-path assoc-in [(:vocabularyVersion sub-paths) :value] vocabularyVersion)
          (update-in dp-term-path assoc-in [(:termDefinition sub-paths) :value] termDefinition)))))

(rf/reg-event-db
  :handlers/update-nasa-list-value
  ins/std-ins
  (fn [db [_ path option]]
    (let [option (if (map? option) option (utils/js-lookup option))
          {:keys [prefLabel uri]} option]
      (-> db
          (update-in path assoc-in [:prefLabel :value] prefLabel)
          (update-in path assoc-in [:uri :value] uri)))))

(rf/reg-event-db
  :handlers/update-boxes
  ins/std-ins
  (fn [db [_ boxes-path boxes]]
    (let [boxes-value-path (conj boxes-path :value)
          box-values (mapv (fn [box] {:value box}) boxes)
          db (assoc-in db boxes-value-path box-values)]
      db)))


(rf/reg-event-db
  :handlers/update-method-term
  ins/std-ins
  (fn [db [_ method-path option]]
    (let [option (if (map? option) option (utils/js-lookup option))
          {:keys [term vocabularyTermURL termDefinition]} option]
      (-> db
          (update-in method-path assoc-in [:name :value] term)
          (update-in method-path assoc-in [:uri :value] vocabularyTermURL)
          (update-in method-path assoc-in [:description :value] termDefinition)))))


(rf/reg-event-fx
  :handlers/update-person
  ins/std-ins
  (fn [{:keys [db]} [_ person-path option]]
    (let [option (if (map? option) option (utils/js-lookup option))
          option (clojure.walk/keywordize-keys option)
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
      {:db db
       :dispatch [:handlers/org-changed person-path org]})))

(rf/reg-event-db
  :handlers/update-method-name
  ins/std-ins
  (fn [db [_ method-path name]]
    (-> db
        (update-in method-path assoc-in [:name :value] name)
        (update-in method-path assoc-in [:uri :value] "XXX"))))

(rf/reg-event-db
  :handlers/setter
  ins/std-ins
  (fn [db [_ path k v]]
    (swap! app-db update-in path assoc k v)))

(defn unsaved-input-check-helper [{:keys [new-value errors] :as keywords-data}]
  (assoc keywords-data
    :errors
    (if (str/blank? new-value)
      (disj (set errors) "Unsaved value in the keyword input field")
      (conj (set errors) "Unsaved value in the keyword input field"))))

(rf/reg-event-db
  :handlers/check-unsaved-keyword-input
  ins/std-ins
  (fn [db [_ keywords-path]]
    (update-in db keywords-path unsaved-input-check-helper)))

(rf/reg-event-db
  :handlers/remove-party
  ins/std-ins
  (fn [db [_ parties-path item]]
    (update-in db parties-path update :value utils/vec-remove item)))

(rf/reg-event-db
  :handlers/reset-form
  ins/std-ins
  (fn [db [_ form-path]]
    (s/assert vector? form-path)
    (update-in db form-path logic/reset-form)))

(rf/reg-event-db
  :handlers/show-errors
  ins/std-ins
  (fn [db [_ path]]
    (s/assert vector path)
    (update-in db path assoc :show-errors true)))

(rf/reg-event-db
  :handlers/hide-errors
  ins/std-ins
  (fn [db [_ path]]
    (update-in db path assoc :show-errors false)))

(rf/reg-event-db
  :handlers/toggle-status-filter
  ins/std-ins
  (fn [db [_ status status-filter]]
    (let [status-filter (get-in db [:page :status-filter] status-filter)
          status-filter (if (contains? status-filter status)
                          (disj status-filter status)
                          (conj status-filter status))]
      (assoc-in db [:page :status-filter] status-filter))))

(rf/reg-event-db
  :handlers/show-all-documents
  ins/std-ins
  (fn [db _]
    (let [documents (get-in db [:context :documents])
          status-freq (frequencies (map :status documents))]
      (assoc-in db [:page :status-filter] (set (keys status-freq))))))

(rf/reg-event-db
  :handlers/load-error-page
  ins/std-ins
  (fn [db [_ data]]
    (-> db
        (assoc-in [:page :name] "Error")
        (assoc-in [:page :text] (-> data :response :message))
        (assoc-in [:page :code] (-> data :status))
        (assoc-in [:page :detail] (-> data :response)))))

(rf/reg-event-db
  :handlers/set-value
  ins/std-ins
  (fn [db [_ field-path value]]
    (update-in db field-path assoc :value value)))

(rf/reg-event-db
  :date-field/value-change
  ins/std-ins
  (fn [db [_ field-path widget-value]]
    (let [field-value (when widget-value (moment/format widget-value "YYYY-MM-DD"))]
      (-> db
          (update-in field-path assoc :value field-value)
          (update-in field-path assoc :show-errors true)))))

(rf/reg-event-db
  :textarea-field/value-change
  ins/std-ins
  (fn [db [_ field-path value]]
    (-> db
        (update-in field-path assoc :value value)
        (update-in field-path assoc :show-errors true))))

(rf/reg-event-db
  :handlers/set-geographic-element
  ins/std-ins
  (fn [db [_ many-field-path values]]
    (let [many-field (get-in db many-field-path)
          new-fields (for [value values]
                       (-> (logic/new-value-field many-field)
                           (update-in [:value :northBoundLatitude] merge (:northBoundLatitude value))
                           (update-in [:value :southBoundLatitude] merge (:southBoundLatitude value))
                           (update-in [:value :eastBoundLongitude] merge (:eastBoundLongitude value))
                           (update-in [:value :westBoundLongitude] merge (:westBoundLongitude value))))]
      (update-in db many-field-path assoc :value (vec new-fields)))))



(rf/reg-event-fx
  :handlers/org-changed
  ins/std-ins
  (fn [{:keys [db]} [_ path value]]
    {:db       (-> db
                   (update-in (conj path :organisationName) assoc :value (get value "organisationName") :show-errors true)
                   (update-in (conj path :organisationIdentifier) assoc :value (str (get value "uri") "||" (get value "city")) :show-errors true))
     :dispatch [:handlers/update-address path value]}))

(rf/reg-event-db
  :handlers/person-detail-changed
  ins/std-ins
  (fn [db [_ path field value isUserAdded]]
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
      (if value-changed
        (-> db'
            (update-in (conj path field) assoc :value value :show-errors true)
            (update-in (conj path :individualName) assoc :value "" :show-errors true))
        db))))

(rf/reg-event-db
  :handlers/value-changed
  ins/std-ins
  (fn [db [_ path value]]
    (s/assert vector? path)
    (update-in db path assoc :value value :show-errors true)))

(rf/reg-event-db
  :handlers/set-tab
  ins/std-ins
  (fn [db [_ id]]
    (assoc-in db [:page :tab] id)))

(rf/reg-event-db
  :handlers/load-errors
  ins/std-ins
  (fn [db [_ form-path data]]
    (update-in db form-path logic/load-errors data)))

(rf/reg-event-db
  :handlers/add-keyword-extra
  ins/std-ins
  (fn [db [_ keywords-path value]]
    (let [keywords (get-in db keywords-path)]
      (if-not (empty? value)
        (assoc-in db keywords-path (vec (conj keywords {:value value})))
        db))))

(rf/reg-event-db
  :handlers/del-keyword-extra
  ins/std-ins
  (fn [db [_ keywords-path value]]
    (let [keywords (get-in db keywords-path)]
      (assoc-in db keywords-path (vec (remove #(= value (:value %)) keywords))))))

(rf/reg-event-db
  :handlers/add-nodes
  ins/std-ins
  (fn [db [_ api-path nodes]]
    (update-in db api-path update :options into nodes)))

(defn dashboard-create-click
  [db _]
  (open-modal db {:type :DashboardCreateModal}))

(rf/reg-event-db :handlers/dashboard-create-click ins/std-ins dashboard-create-click)

(rf/reg-event-fx
  :handlers/create-document-success
  ins/std-ins
  (fn [_ [_ data]]
    {:dispatch-n           [[:handlers/reset-form [:create_form]]
                            [:handlers/hide-errors [:create_form]]
                            [:handlers/close-modal]]
     :fx/set-location-href (-> data :document :url)}))

(rf/reg-event-fx
  :handlers/create-document-error
  ins/std-ins
  (fn [_ [_ data]]
    (if (= (:status data) 400)
      {:dispatch [:handlers/load-error-page data]}
      {:dispatch-n [[:handlers/load-errors [:create_form] (:response data)]
                    [:handlers/show-errors [:create_form]]]})))

(rf/reg-event-fx
  :handlers/dashboard-create-save
  ins/std-ins
  (fn [{:keys [db]} _]
    (let [{:keys [url] :as form} (get-in db [:create_form])
          form (logic/validate-required-fields form)]
      (if (logic/is-valid? form)
        {:fx/create-document {:url       url
                              :form      form
                              :success-v [:handlers/create-document-success]
                              :error-v   [:handlers/create-document-error]}}
        {:dispatch [:handlers/show-errors [:create_form]]}))))

(rf/reg-event-fx
  :handlers/clone-document
  ins/std-ins
  (fn [_ [_ url]]
    {:fx/clone-document {:url       url
                         :success-v [:handlers/clone-document-success]
                         :error-v   [:handlers/clone-document-error]}}))

(rf/reg-event-fx
  :handlers/clone-document-success
  ins/std-ins
  (fn [_ [_ data]]
    {:fx/set-location-href (get-in data [:document :url])}))

(rf/reg-event-fx
  :handlers/clone-document-error
  ins/std-ins
  (fn [_ _]
    {:dispatch [:handlers/open-modal {:type :alert :message "Unable to clone"}]}))

(defn transite-doc-click
  [transition]
  (fn [_ [_ url]]
    (let [trans-name (first (clojure.string/split transition "_"))]
      {:dispatch [:handlers/open-modal
                  {:type       :confirm
                   :title      trans-name
                   :message    (str "Are you sure you want to " trans-name " this record?")
                   :on-confirm #(rf/dispatch [:handlers/transite-doc-confirm url transition])}]})))

(rf/reg-event-fx :handlers/archive-doc-click ins/std-ins (transite-doc-click "archive"))
(rf/reg-event-fx :handlers/delete-archived-doc-click ins/std-ins (transite-doc-click "delete_archived"))
(rf/reg-event-fx :handlers/restore-doc-click ins/std-ins (transite-doc-click "restore"))

(rf/reg-event-fx
  :handlers/transite-doc-confirm
  ins/std-ins
  (fn [_ [_ url transition]]
    {:fx/transition-current-document
     {:url        url
      :transition transition
      :success-v  [:handlers/transite-doc-success transition]
      :error-v    [:handlers/transite-doc-error]}}))

(rf/reg-event-db
  :handlers/transite-doc-success
  ins/std-ins
  (fn [db [_ transition data]]
    (let [{{:keys [uuid] :as doc} :document} data]
      (update-in db [:context :documents]
                 (fn [docs]
                   (reduce #(if (= uuid (:uuid %2))
                              (if (= transition "delete_archived")
                                %1
                                (conj %1 doc))
                              (conj %1 %2))
                           [] docs))))))

(rf/reg-event-fx
  :handlers/transite-doc-error
  ins/std-ins
  (fn [_ [_ transition]]
    (let [trans-name (first (clojure.string/split transition "_"))]
      {:dispatch [:handlers/open-modal
                  {:type    :alert
                   :message (str "Unable to " trans-name)}]})))

(rf/reg-event-fx
  :handlers/lodge-click
  ins/std-ins
  (fn [{:keys [db]} _]
    (let [url (get-in db [:form :url])
          data (-> db :form :fields logic/extract-field-values)]
      {:db (assoc-in db [:page ::saving?] true)
       :fx/save-current-document
           {:url       url
            :data      data
            :success-v [:handlers/lodge-save-success data]
            :error-v   [:handlers/lodge-error]}})))

(rf/reg-event-fx
  :handlers/lodge-save-success
  ins/std-ins
  (fn [{:keys [db]} [_ data]]
    (let [url (get-in db [:context :document :transition_url])]
      {:db (-> db
               (assoc-in [:form :data] data)
               (assoc-in [:page ::saving?] true))
       :fx/submit-current-document
           {:url       url
            :success-v [:handlers/lodge-submit-success]
            :error-v   [:handlers/lodge-error]}})))

(rf/reg-event-db
  :handlers/lodge-submit-success
  ins/std-ins
  (fn [db [_ resp]]
    (let [document (get-in resp [:document])]
      (s/assert map? document)
      (-> db
          (assoc-in [:page ::saving?] false)
          (assoc-in [:context :document] document)))))

(rf/reg-event-fx
  :handlers/lodge-error
  ins/std-ins
  (fn [{:keys [db]} [_ {:keys [status failure]}]]
    {:db       (assoc-in db [:page ::saving?] false)
     :dispatch [:handlers/open-modal
                {:type    :alert
                 :message (str "Unable to lodge: " status " " failure)}]}))

(rf/reg-event-fx
  :help-menu/open
  (fn [_ [_ url]]
    {:window/open {:url url :windowName "_blank"}}))
