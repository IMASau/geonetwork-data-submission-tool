(ns metcalf.common.handlers3
  (:require [cljs.spec.alpha :as s]
            [goog.object :as gobj]
            [metcalf.common.blocks4 :as blocks4]
            [metcalf.common.fx3 :as fx3]
            [metcalf.common.rules4 :as rules4]
            [metcalf.common.utils3 :as utils3]
            [re-frame.core :as rf]
            [clojure.string :as string]))

(defn close-and-cancel
  [{:keys [db]} _]
  (let [{:keys [on-cancel]} (peek (:modal/stack db))]
    ; TODO: can we refactor around specific handlers to avoid this?
    (when on-cancel (on-cancel))
    {:db (update db :modal/stack pop)}))

(defn close-and-confirm
  [{:keys [db]} _]
  (let [{:keys [on-confirm]} (peek (:modal/stack db))]
    ; TODO: can we refactor around specific handlers to avoid this?
    (when on-confirm (on-confirm))
    {:db (update db :modal/stack pop)}))

(defn open-modal-action
  [s props]
  (update-in s [:db :modal/stack]
             (fn [alerts]
               (when-not (= (peek alerts) props)
                 (conj alerts props)))))

(defn open-modal-handler
  [{:keys [db]} [_ props]]
  (open-modal-action {:db db} props))

(defn delete-attachment-click
  [{:keys [db]} [_ {:keys [attachments-path attachment-idx]}]]
  (open-modal-action {:db db}
                     {:type             :modal.type/confirm
                      :title            "Delete?"
                      :message          "Are you sure you want to delete this file?"
                      :on-confirm       #(rf/dispatch [:app/delete-attachment-confirm attachments-path attachment-idx])}))

(defn upload-data-file-upload-failed
  [{:keys [db]} _]
  (open-modal-action {:db db}
                     {:type    :modal.type/alert
                      :message "File upload failed. Please try again or contact administrator."}))

(defn upload-max-filesize-exceeded
  [{:keys [db]} [_ {:keys [max-filesize]}]]
  (open-modal-action {:db db}
                     {:type    :modal.type/alert
                      :message (str "Please, choose file less than " max-filesize "mb")}))

(defn handle-page-view-edit-archive-click
  [{:keys [db]} _]
  (open-modal-action {:db db}
                     {:type       :modal.type/confirm
                      :title      "Archive?"
                      :message    "Are you sure you want to archive this record?"
                      :on-confirm #(rf/dispatch [:app/page-view-edit-archive-click-confirm])}))

(defn document-teaser-clone-click
  [{:keys [db]} [_ clone_url]]
  (open-modal-action {:db db}
                     {:type       :modal.type/confirm
                      :title      "Clone?"
                      :message    "Are you sure you want to clone this record?"
                      :on-confirm (fn [] (rf/dispatch [:app/clone-doc-confirm clone_url]))}))

(defn del-value
  [{:keys [db]} [_ many-field-path i]]
  {:db (update-in db many-field-path update :value utils3/vec-remove i)})

#_(defn add-attachment
    [{:keys [db]} [_ attachment-data]]
    (let [data (select-keys attachment-data [:file :name :delete_url])
          template (get-in db [:form :fields :attachments :fields])
          new-value (reduce (fn [form-acc [k v]]
                              (assoc-in form-acc [k :value] v))
                            template data)]
      {:db (update-in db [:form :fields :attachments :value] conj {:value new-value})}))

(defn archive-current-document
  "User wants to archive doc.  Send request."
  [{:keys [db]} _]
  (let [transition_url (-> db :context :document :transition_url)]
    {::fx3/post-json-data
     {:url       transition_url
      :data      {:transition "archive"}
      :success-v [:app/-archive-current-document-success]
      :error-v   [:app/open-modal {:type :modal.type/alert :message "Unable to delete"}]}}))

(defn -archive-current-document-success
  "Archive request succeeded.  Redirect to dashboard."
  [{:keys [db]} _]
  (let [success_url (-> db :context :urls :Dashboard)]
    {::fx3/set-location-href success_url}))

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

(defn set-tab
  [{:keys [db]} [_ id]]
  {:db (assoc-in db [:page :tab] id)})

(defn dashboard-create-click
  [{:keys [db]} _]
  (open-modal-action {:db db} {:type :modal.type/DashboardCreateModal}))

(defn clone-document
  [_ [_ url]]
  {::fx3/post
   {:url       url
    :success-v [:app/-clone-document-success]
    :error-v   [:app/-clone-document-error]}})

(defn -clone-document-success
  [_ [_ data]]
  {::fx3/set-location-href (get-in data [:document :url])})

(defn -clone-document-error
  [_ _]
  {:dispatch [:app/open-modal {:type :modal.type/alert :message "Unable to clone"}]})

(defn transite-doc-click
  [transition]
  (fn [_ [_ url]]
    (let [trans-name (first (string/split transition "_"))]
      {:dispatch [:app/open-modal
                  {:type       :modal.type/confirm
                   :title      trans-name
                   :message    (str "Are you sure you want to " trans-name " this record?")
                   :on-confirm #(rf/dispatch [:app/-transite-doc-click-confirm url transition])}]})))

(defn -transite-doc-click-confirm
  [_ [_ url transition]]
  {::fx3/post-json-data
   {:url       url
    :data      {:transition transition}
    :success-v [:app/-transite-doc-confirm-success transition]
    :error-v   [:app/-transite-doc-confirm-error]}})

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
    {:dispatch [:app/open-modal
                {:type    :modal.type/alert
                 :message (str "Unable to " trans-name)}]}))

(defn lodge-click
  [{:keys [db]} _]
  (let [url (get-in db [:form :url])
        state0 (get-in db [:form :state])
        state1 (blocks4/postwalk rules4/apply-rules state0)
        data (blocks4/as-data state1)]
    {:db (assoc-in db [:page :metcalf3.handlers/saving?] true)
     ::fx3/post-json-data
         {:url       url
          :data      data
          :success-v [:app/-lodge-click-success data]
          :error-v   [:app/-lodge-click-error]}}))

(defn lodge-save-success
  [{:keys [db]} [_ data]]
  (let [url (get-in db [:context :document :transition_url])]
    {:db (-> db
             (assoc-in [:form :data] data)
             (assoc-in [:page :metcalf3.handlers/saving?] true))
     ::fx3/post-json-data
         {:url       url
          :data      {:transition "submit"}
          :success-v [:app/-lodge-save-success]
          :error-v   [:app/-lodge-save-error]}}))

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
   :dispatch [:app/open-modal
              {:type    :modal.type/alert
               :message (str "Unable to lodge: " status " " failure)}]})