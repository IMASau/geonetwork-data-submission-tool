(ns metcalf.common.subs3
  (:require [clojure.set :as set]))

(defn get-attachments
  [db [_ path]]
  (get-in db path))

(defn get-context
  [db]
  (get-in db [:context]))

(defn get-progress
  [db]
  (get-in db [:progress]))

(defn get-form
  [db]
  (get-in db [:form]))

(defn get-upload-form
  [db]
  (get-in db [:upload_form]))

(defn get-form-disabled?
  [db]
  (get-in db [:form :state :props :disabled]))

(defn get-page-props
  [db _]
  (get-in db [:page]))

(defn get-dashboard-props
  [db _]
  (let [{:keys [status-filter]
         :or   {status-filter #{"Draft" "Submitted"}}
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
  [form-state _]
  (let [{:progress/keys [fields required required-errors errors]
         :or            {errors 0 required-errors 0}}
        (:progress/score form-state)
        can-submit? (= errors 0)]
    (when (pos-int? fields)
      {:can-submit? can-submit?
       :value       (/ (- required required-errors) required)})))
