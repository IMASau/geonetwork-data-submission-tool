(ns metcalf3.subs
  (:require [clojure.set :as set]
            [clojure.string :as string]
            [metcalf3.logic :as logic3]))

(defn get-derived-state
  [db _]
  (logic3/derived-state db))

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
         :or   {status-filter logic3/active-status-filter}
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

(defn get-progress-props
  [derived-db _]
  (let [{:progress/keys [fields empty errors]} (:progress derived-db)
        can-submit? (= errors 0)]
    (when (pos-int? fields)
      {:can-submit? can-submit?
       :value       (/ (- fields empty) fields)})))

(defn get-form-tick
  [db _]
  (get db :form/tick 0))

(defn platform-selected?
  [db [_ form-position]]
  (get-in (get (get-in db [:form :fields :identificationInfo :dataParameters :value]) form-position) [:value :platform_vocabularyTermURL :value]))
