(ns metcalf3.subs
  (:require [re-frame.core :as rf]
            [metcalf3.logic :as logic]
            [metcalf3.globals :as globals]
            [clojure.set :as set]
            [interop.moment :as moment]
            [clojure.string :as string]
            [cljs.spec.alpha :as s]))

(defn get-derived-state
  [db _]
  (logic/derived-state db))

(defn form-dirty?
  [db _]
  (get-in db [:form :dirty]))

(defn is-page-name-nil?
  [db _]
  (-> db :page :name nil?))

(defn get-derived-path
  [db [_ path]]
  (get-in db path))

(defn get-page-props
  [db _]
  (get-in db [:page]))

(defn get-page-name
  [db _]
  (get-in db [:page :name]))

(defn get-modal-props
  [db _]
  (let [modal-stack (:alert db)
        modal-props (peek modal-stack)]
    (when modal-props
      (let [breadcrumbs (mapv :title modal-stack)]
        (assoc modal-props :breadcrumbs breadcrumbs)))))

(defn get-dashboard-props
  [db _]
  (let [{:keys [status-filter]
         :or   {status-filter logic/active-status-filter}
         :as   page} (get-in db [:page])
        {:keys [documents status urls user]} (get-in db [:context])
        status-freq (frequencies (map :status documents))
        all-statuses (set (keys status-freq))
        relevant-status-filter (set/intersection status-filter all-statuses)
        filtered-docs (->> documents
                           (filter (fn [{:keys [status]}]
                                     (contains? relevant-status-filter status)))
                           (sort-by :last_updated)
                           (reverse))
        has-documents? (empty? documents)]
    {:filtered-docs          filtered-docs
     :has-documents?         has-documents?
     :user                   user
     :status-filter          status-filter
     :page                   page
     :status                 status
     :status-freq            status-freq
     :relevant-status-filter relevant-status-filter
     :urls                   urls}))

(def edit-tabs
  [{:id :data-identification :text "Data identification"}
   {:id :what :text "What"}
   {:id :when :text "When"}
   {:id :where :text "Where"}
   {:id :how :text "How"}
   {:id :who :text "Who"}
   {:id :about :text "About"}
   {:id :upload :text "Data sources"}
   {:id :lodge :text "Lodge"}])

(defn get-edit-tab-props
  [[page derived-db]]
  (let [selected-tab (get page :tab :data-identification)]
    {:selected-tab selected-tab
     :tab-props    (mapv
                     (fn [{:keys [id text]}]
                       (let [progress (get-in derived-db [:progress])
                             error-count (get-in progress [:page-errors id])]
                         {:id          id
                          :title       text
                          :has-errors? (and error-count (> error-count 0))}))
                     edit-tabs)}))

(defn get-progress-props
  [derived-db _]
  (let [{:keys [fields empty errors]} (:progress derived-db)
        can-submit? (= errors 0)]
    (when (pos-int? fields)
      {:can-submit? can-submit?
       :value       (/ (- fields empty) fields)})))

(defn get-date-field-props
  [derived-db [_ path]]
  ;    (when js/goog.DEBUG (js/console.log ::path path))
  (let [{:keys [label help required disabled value show-errors errors minDate maxDate] :as field} (get-in derived-db path)
        value (if (= value "") nil value)
        error-help (when (and show-errors (seq errors))
                     (string/join ". " errors))]
    (when js/goog.DEBUG (js/console.log :date-field/get-props.field field))
    {:label      label
     :labelInfo  (when required "*")
     :helperText (or error-help help)
     :value      (when value (moment/to-date (moment/moment value "YYYY-MM-DD")))
     :disabled   disabled
     :change-v   [:date-field/value-change path]
     :minDate    (s/assert (s/nilable inst?) minDate)
     :maxDate    (s/assert (s/nilable inst?) maxDate)
     :intent     (when error-help "danger")}))

(defn get-textarea-field-props
  [derived-db [_ path]]
  ;    (when js/goog.DEBUG (js/console.log ::path path))
  (let [{:keys [label help required disabled value show-errors errors placeholder maxlength] :as field} (get-in derived-db path)
        error-help (when (and show-errors (seq errors))
                     (string/join ". " errors))]
    ;      (when js/goog.DEBUG (js/console.log :textarea-field/get-props.field {:field field
    ;                                                                           :path  path
    ;                                                                           :db    derived-db}))
    {:label       label
     :labelInfo   (when required "*")
     :helperText  (or error-help help)
     :value       (or value "")
     :disabled    disabled
     :maxlength   maxlength
     :placeholder placeholder
     :change-v    [:textarea-field/value-change path]
     :intent      (when error-help "danger")}))

(defn get-textarea-field-many-props
  [derived-db [_ path field]]
  (let [{:keys [placeholder maxlength]} (get-in derived-db [:form :fields :identificationInfo field])
        {:keys [label help required disabled value show-errors errors]} (get-in derived-db path)
        error-help (when (and show-errors (seq errors))
                     (string/join ". " errors))]
    {:label       label
     :labelInfo   (when required "*")
     :helperText  (or error-help help)
     :value       (or value "")
     :disabled    disabled
     :maxlength   maxlength
     :placeholder placeholder
     :change-v    [:textarea-field/value-change path]
     :intent      (when error-help "danger")}))

(defn map-props
  [db _]
  (:map db))

(defn get-form-tick
  [db _]
  (get db :form/tick 0))

(defn get-menuitems
  [db _]
  (let [{:keys [guide_pdf roadmap_pdf releasenotes_url]} (get-in db [:context :site])]
    (seq (cond-> []
                 guide_pdf (conj ["Guide" [:help-menu/open guide_pdf]])
                 roadmap_pdf (conj ["Roadmap" [:help-menu/open roadmap_pdf]])
                 releasenotes_url (conj ["Release Notes" [:help-menu/open releasenotes_url]])))))

(defn platform-selected?
  [db [_ form-position]]
  (get-in (get (get-in db [:form :fields :identificationInfo :dataParameters :value]) form-position) [:value :platform_vocabularyTermURL :value]))
