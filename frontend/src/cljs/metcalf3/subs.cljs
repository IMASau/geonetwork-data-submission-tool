(ns metcalf3.subs
  (:require [re-frame.core :as rf]
            [metcalf3.logic :as logic]
            [metcalf3.globals :as globals]
            [clojure.set :as set]
            [interop.moment :as moment]
            [clojure.string :as string]))

(rf/reg-sub
  :subs/get-derived-state
  (fn [db _]
    (logic/derived-state db)))

(rf/reg-sub
  :metcalf3/form-dirty?
  :<- [:subs/get-derived-state]
  (fn [db _]
    (get-in db [:form :dirty])))

(rf/reg-sub
  :subs/is-page-name-nil?
  (fn [db _]
    (-> db :page :name nil?)))

(rf/reg-sub
  :subs/get-derived-path
  :<- [:subs/get-derived-state]
  (fn [db [_ path]]
    (get-in db path)))

(rf/reg-sub
  :subs/get-page-props
  (fn [db _]
    (get-in db [:page])))

(rf/reg-sub
  :subs/get-page-name
  (fn [db _]
    (get-in db [:page :name])))

(rf/reg-sub
  :subs/get-modal-props
  (fn [db _]
    (let [modal-stack (:alert db)
          modal-props (peek modal-stack)]
      (when modal-props
        (let [breadcrumbs (mapv :title modal-stack)]
          (assoc modal-props :breadcrumbs breadcrumbs))))))

(rf/reg-sub
  :subs/get-dashboard-props
  (fn [db _]
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
       :urls                   urls})))

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

(rf/reg-sub
  :subs/get-edit-tab-props
  :<- [:subs/get-page-props]
  :<- [:subs/get-derived-state]
  (fn [[page derived-db]]
    (let [selected-tab (get page :tab :data-identification)]
      {:selected-tab selected-tab
       :tab-props    (mapv
                       (fn [{:keys [id text]}]
                         (let [progress (get-in derived-db [:progress])
                               error-count (get-in progress [:page-errors id])]
                           {:id          id
                            :title       text
                            :has-errors? (and error-count (> error-count 0))}))
                       edit-tabs)})))

(rf/reg-sub
  :progress/get-props
  :<- [:subs/get-derived-state]
  (fn [derived-db _]
    (let [{:keys [fields empty errors]} (:progress derived-db)
          can-submit? (= errors 0)]
      (when (pos-int? fields)
        {:can-submit? can-submit?
         :value       (/ (- fields empty) fields)}))))

(rf/reg-sub
  :date-field/get-props
  :<- [:subs/get-derived-state]
  (fn [derived-db [_ path]]
    (js/console.log ::path path)
    (let [{:keys [label help required disabled value show-errors errors] :as field} (get-in derived-db path)
          value (if (= value "") nil value)
          error-help (when (and show-errors (seq errors))
                       (string/join ". " errors))]
      (js/console.log :date-field/get-props.field field)
      {:label      label
       :labelInfo  (when required "*")
       :helperText (or error-help help)
       :value      (when value (moment/to-date (moment/moment value "YYYY-MM-DD")))
       :disabled   disabled
       :change-v   [:date-field/value-change path]
       :intent     (when error-help "danger")})))

(rf/reg-sub
  :textarea-field/get-props
  :<- [:subs/get-derived-state]
  (fn [derived-db [_ path]]
    (js/console.log ::path path)
    (let [{:keys [label help required disabled value show-errors errors] :as field} (get-in derived-db path)
          error-help (when (and show-errors (seq errors))
                       (string/join ". " errors))]
      (js/console.log :textarea-field/get-props.field {:field field
                                                       :path path
                                                       :db derived-db})
      {:label      label
       :labelInfo  (when required "*")
       :helperText (or error-help help)
       :value      (or value "")
       :disabled   disabled
       :change-v   [:textarea-field/value-change path]
       :intent     (when error-help "danger")})))

(rf/reg-sub
  :map/props
  (fn [db _] (:map db)))
