(ns metcalf.common.handlers4
  (:require [clojure.string :as string]
            [goog.object :as gobject]
            [metcalf.common.actions4 :as actions4]
            [metcalf.common.blocks4 :as blocks4]
            [metcalf.common.fx3 :as fx3]
            [metcalf.common.logic4 :as logic4]
            [metcalf.common.rules4 :as rules4]
            [metcalf.common.schema4 :as schema4]
            [metcalf.common.utils4 :as utils4]
            [cljs.spec.alpha :as s]))

(defn db-path
  [{:keys [form-id data-path]}]
  (utils4/as-path [form-id :state (blocks4/block-path data-path)]))

(defn value-changed-handler
  [{:keys [db]} [_ ctx value]]
  (let [path (db-path ctx)]
    {:db (-> db
             (assoc-in (conj path :props :value) value)
             (assoc-in (conj path :props :show-errors) true))}))

(defn option-change-handler
  [{:keys [db]} [_ ctx option]]
  (let [{:keys [form-id data-path]} ctx
        schema (get-in db (flatten [form-id :schema (schema4/schema-path data-path)]))
        state (blocks4/as-blocks {:schema schema :data option})
        path (db-path ctx)]
    {:db (-> db
             (assoc-in path state)
             (assoc-in (conj path :props :show-errors) true))}))

(defn add-record-handler
  "Used with record-add-button which adds text values to a list"
  [{:keys [db]} [_ ctx values]]
  (s/assert (s/coll-of string?) values)
  (let [{:keys [form-id data-path columns]} ctx
        data (reduce-kv (fn [m ks v] (assoc-in m ks v)) {} (zipmap (map :value-path columns) values))
        schema (get-in db (flatten [form-id :schema (schema4/schema-path data-path)]))
        state (blocks4/as-blocks {:schema schema :data data})
        path (db-path ctx)]
    {:db (-> db
             (assoc-in path state)
             (assoc-in (conj path :props :show-errors) true))}))

(defn text-value-add-click-handler
  [{:keys [db]} [_ ctx value]]
  (let [{:keys [form-id data-path]} ctx]
    (actions4/add-item-action {:db db} form-id data-path [] value)))

(defn list-add-with-defaults-click-handler2
  [{:keys [db]} [_ config]]
  (let [{:keys [form-id data-path value-path added-path item-defaults]} config
        item-data (-> item-defaults
                      (assoc-in value-path (str (random-uuid)))
                      (assoc-in added-path true))]
    (-> {:db db}
        (actions4/save-snapshot-action form-id)
        (actions4/add-item-action form-id data-path value-path item-data)
        (actions4/select-last-item-action form-id data-path))))

(defn item-add-with-defaults-click-handler
  [{:keys [db]} [_ props]]
  (let [{:keys [form-id data-path value-path added-path]} props
        defaults (-> {}
                     (assoc-in value-path (str (random-uuid)))
                     (assoc-in added-path true))]
    (-> {:db db}
        (actions4/save-snapshot-action form-id)
        (actions4/set-data-action form-id data-path defaults)
        (actions4/dialog-open-action form-id data-path))))

(defn item-edit-click-handler
  [{:keys [db]} [_ props]]
  (let [{:keys [form-id data-path]} props]
    (-> {:db db}
        (actions4/save-snapshot-action form-id)
        (actions4/dialog-open-action form-id data-path))))

(defn boxes-changed
  [{:keys [db]} [_ config geojson]]
  (let [{:keys [form-id data-path value-path added-path]} config
        geometries (map :geometry (:features geojson))
        boxes (map utils4/geometry->box-value geometries)
        boxes (map-indexed (fn [idx m] (assoc-in m value-path idx)) boxes)
        boxes (map (fn [m] (assoc-in m added-path true)) boxes)
        schema (get-in db (flatten [form-id :schema (schema4/schema-path data-path)]))
        state (blocks4/as-blocks {:schema schema :data (vec boxes)})
        db-path (utils4/as-path [:db form-id :state (blocks4/block-path data-path)])]
    (-> {:db db}
        (assoc-in db-path state)
        (assoc-in (conj db-path :props :show-errors) true)
        (actions4/genkey-action form-id data-path))))

(defn list-option-picker-change
  [{:keys [db]} [_ ctx option]]
  (let [{:keys [form-id data-path value-path]} ctx]
    (-> {:db db}
        (actions4/add-item-action form-id data-path value-path option))))

(defn item-option-picker-change
  "Handle picker change.  Uses option data to set values."
  [{:keys [db]} [_ ctx option]]
  (let [{:keys [form-id data-path]} ctx]
    (-> {:db db}
        (actions4/set-data-action form-id data-path option))))

(defn selection-list-item-click2
  [{:keys [db]} [_ props idx]]
  (let [{:keys [form-id data-path added-path]} props]
    (cond-> {:db db}
      added-path
      (actions4/select-user-defined-list-item-action2 form-id data-path idx added-path))))

(defn selection-list-remove-click
  [{:keys [db]} [_ ctx idx]]
  (let [{:keys [form-id data-path]} ctx]
    (-> {:db db}
        (actions4/del-item-action form-id data-path idx))))

(defn selection-list-reorder
  [{:keys [db]} [_ ctx src-idx dst-idx]]
  (let [{:keys [form-id data-path]} ctx]
    (-> {:db db}
        (actions4/genkey-action form-id data-path)
        (actions4/move-item-action form-id data-path src-idx dst-idx))))

(defn boxmap-coordinates-open-add-modal
  [{:keys [db] :as s} [_ {:keys [ctx coord-field initial-data idx on-close on-save]}]]
  (let [{:keys [form-id data-path]} ctx
        ;; If this is identical to an existing item, actions/add-item-action will not result in
        ;; a new item. So, we run an identical check here so we can calculate the correct idx
        ;; for the modal to use. We can't be sure that the previous record (idx-1) is the correct
        ;; item to use, but it's the most likely place for an all-0s record to be.
        db-path (utils4/as-path [:db form-id :state (blocks4/block-path data-path)])
        items (set (blocks4/as-data (get-in s db-path)))
        idx (if (contains? items initial-data) (dec idx) idx)
        new-field-path (conj data-path idx)]
    (-> {:db db}
        (actions4/add-item-action form-id data-path [] initial-data)
        (actions4/open-modal-action {:type           :m4/table-modal-add-form
                                     :form           coord-field
                                     :path           new-field-path
                                     :title          "Geographic Coordinates"
                                     :on-close-click #(on-close idx)
                                     :on-save-click  on-save}))))

(defn boxmap-coordinates-open-edit-modal
  [{:keys [db]} [_ {:keys [ctx coord-field on-delete on-cancel on-save]}]]
  (let [{:keys [data-path]} ctx]
    (-> {:db db}
        (actions4/open-modal-action {:type            :m4/table-modal-edit-form
                                     :form            coord-field
                                     :path            data-path
                                     :title           "Geographic Coordinates"
                                     :on-delete-click on-delete
                                     :on-close-click  on-cancel
                                     :on-save-click   on-save}))))

(defn boxmap-coordinates-click-confirm-delete
  [{:keys [db]} [_ on-confirm]]
  (-> {:db db}
      (actions4/open-modal-action {:type       :modal.type/confirm
                                   :title      "Delete"
                                   :message    "Are you sure you want to delete?"
                                   :on-confirm on-confirm})))

(defn boxmap-coordinates-list-delete
  [{:keys [db]} [_ ctx idx]]
  (let [{:keys [form-id data-path]} ctx]
    (-> {:db db}
        (actions4/del-item-action form-id data-path idx)
        (update-in [:db :modal/stack] pop))))

(defn save-current-document
  [{:keys [db]} _]
  (let [url (get-in db [:form :url])
        state0 (get-in db [:form :state])
        state1 (blocks4/postwalk rules4/apply-rules state0)
        data (blocks4/as-data state1)]
    {:db (assoc-in db [:page :metcalf3.handlers/saving?] true)
     ; TODO: put logic in handler, use generic js/fetch fx
     ::fx3/post-json-data
         {:url       url
          :data      data
          :success-v [::-save-current-document-success]
          :error-v   [::-save-current-document-error]}}))

(defn -save-current-document-success
  [{:keys [db]} [_ resp]]
  (let [form (get-in resp [:form])]
    (-> {:db db}
        (assoc-in [:db :form] (logic4/massage-form form))
        (assoc-in [:db :page :metcalf3.handlers/saving?] false))))

(defn -save-current-document-error
  [{:keys [db]} [_ {:keys [status response]}]]
  (let [msg (get response :message "Error saving")]
    (-> {:db db}
        (actions4/open-modal-action {:type :modal.type/alert :message (str status ": " msg)})
        (assoc-in [:db :page :metcalf3.handlers/saving?] false))))

(defn list-edit-dialog-cancel-handler
  [{:keys [db]} [_ ctx]]
  (let [{:keys [form-id data-path]} ctx]
    (-> {:db db}
        (actions4/restore-snapshot-action form-id)
        (actions4/unselect-list-item-action form-id data-path))))

(defn list-edit-dialog-save-handler
  [{:keys [db]} [_ ctx]]
  (let [{:keys [form-id data-path]} ctx]
    (-> {:db db}
        (actions4/discard-snapshot-action form-id)
        (actions4/unselect-list-item-action form-id data-path))))

(defn item-edit-dialog-close-handler
  [{:keys [db]} [_ ctx]]
  (let [{:keys [form-id data-path]} ctx]
    (-> {:db db}
        (actions4/restore-snapshot-action form-id)
        (actions4/dialog-close-action form-id data-path))))

(defn item-edit-dialog-cancel-handler
  [{:keys [db]} [_ ctx]]
  (let [{:keys [form-id data-path]} ctx]
    (-> {:db db}
        (actions4/restore-snapshot-action form-id)
        (actions4/dialog-close-action form-id data-path))))

(defn item-edit-dialog-save-handler
  [{:keys [db]} [_ ctx]]
  (let [{:keys [form-id data-path]} ctx]
    (-> {:db db}
        (actions4/discard-snapshot-action form-id)
        (actions4/dialog-close-action form-id data-path))))

(defn create-document-modal-save-click
  [{:keys [db]}]
  (let [{:keys [url state]} (get-in db [:create_form])
        data (blocks4/as-data state)]
    (-> {:db db}
        (actions4/clear-errors-action [:create_form])
        (actions4/create-document-action url data))))

(defn -create-document-handler
  "Handle server response to create document POST request.
   200: Handles success by opening new document (page will reload)
   400: Handles invalid response shows errors on the form
   Opens alert modal if status is unexpected"
  [{:keys [db]} [_ {:keys [status body]}]]
  (case status
    200 {::fx3/set-location-href (gobject/getValueByKeys body "document" "url")}
    400 (actions4/set-errors-action {:db db} [:create_form] (js->clj body))
    (actions4/open-modal-action
      {:db db}
      {:type    :modal.type/alert
       :message (str "Unexpected " status " error creating document")})))

(defn document-teaser-share-click
  [{:keys [db]} [_ uuid]]
  (-> (actions4/open-modal-action {:db db} {:type :modal.type/contributors-modal :uuid uuid})
      (update :db dissoc :contributors-modal/saving?)
      (actions4/get-document-data-action uuid)))

(defn -get-document-data-action
  [{:keys [db]} [_ uuid {:keys [status body]}]]
  (when (= 200 status)
    (assoc-in {:db db} [:db :app/document-data uuid] (js->clj body :keywordize-keys true))))

(defn contributors-modal-share-click
  [{:keys [db]} [_ {:keys [uuid email]}]]
  (let [{:keys [contributors-modal/saving?]} db
        {:keys [contributors]} (get-in db [:app/document-data uuid])
        novel? (not (contains? (set (map :email contributors)) email))]
    (when (and (not saving?) novel?)
      (-> {:db db}
          (update-in [:db :app/document-data uuid :contributors] conj {:email email})
          (assoc-in [:db :contributors-modal/saving?] true)
          (update :fx conj [:app/post-data-fx
                            {:url     (str "/share/" uuid "/")
                             :data    {:email email}
                             :resolve [::-contributors-modal-share-resolve uuid]}])))))

(defn -contributors-modal-share-resolve
  [{:keys [db]} [_ uuid {:keys [status body]}]]
  (let [{:keys [contributors-modal/saving?]} db]
    (cond
      (not saving?) {}

      (= status 200)
      (-> {:db db}
          (update :db dissoc :contributors-modal/saving?)
          (actions4/get-document-data-action uuid))

      (= status 400)
      (-> {:db db}
          (update :db dissoc :contributors-modal/saving?)
          (actions4/get-document-data-action uuid)
          (actions4/open-modal-action {:type :modal.type/alert :message (string/join ". " (mapcat val (js->clj body)))})))))

(defn contributors-modal-unshare-click
  [{:keys [db]} [_ {:keys [uuid idx]}]]
  (let [{:keys [contributors-modal/saving?]} db
        {:keys [contributors]} (get-in db [:app/document-data uuid])
        {:keys [email]} (get contributors idx)
        contributors' (filterv #(not= email (:email %)) contributors)]
    (when-not saving?
      (-> {:db db}
          (assoc-in [:db :app/document-data uuid :contributors] contributors')
          (assoc-in [:db :contributors-modal/saving?] true)
          (update :fx conj [:app/post-data-fx
                            {:url     (str "/unshare/" uuid "/")
                             :data    {:email email}
                             :resolve [::-contributors-modal-unshare-resolve uuid]}])))))

(defn -contributors-modal-unshare-resolve
  [{:keys [db]} [_ uuid {:keys [status body]}]]
  (let [{:keys [contributors-modal/saving?]} db]
    (cond
      (not saving?) {}

      (= status 200)
      (-> {:db db}
          (update :db dissoc :contributors-modal/saving?)
          (actions4/get-document-data-action uuid))

      (= status 400)
      (-> {:db db}
          (update :db dissoc :contributors-modal/saving?)
          (actions4/get-document-data-action uuid)
          (actions4/open-modal-action {:type :modal.type/alert :message (string/join ". " (mapcat val (js->clj body)))})))))

(defn create-document-modal-clear-click
  [{:keys [db]}]
  (actions4/close-modal-action {:db db}))

(defn create-document-modal-close-click
  [{:keys [db]}]
  (actions4/close-modal-action {:db db}))

(defn modal-dialog-alert-dismiss
  [{:keys [db]}]
  (actions4/close-modal-action {:db db}))

(defn modal-dialog-alert-save
  [{:keys [db]}]
  (actions4/close-modal-action {:db db}))

(defn coordinates-modal-field-close-modal
  [{:keys [db]}]
  (actions4/close-modal-action {:db db}))

(defn upload-files-drop
  [{:keys [db]} [_ config data]]
  (let [{:keys [acceptedFiles]} data
        doc-uuid (get-in db [:context :document :uuid])]
    (reduce (fn [s file]
              (actions4/upload-attachment s {:doc-uuid doc-uuid
                                             :file     file
                                             :config   config}))
            {:db db} acceptedFiles)))

(defn -upload-attachment
  [{:keys [db]} [_ config {:keys [status body]}]]
  (let [{:keys [form-id data-path value-path]} config]
    (case status
      201 (actions4/add-item-action {:db db} form-id data-path value-path (js->clj body))
      (actions4/open-modal-action {:db db}
                                  {:type    :modal.type/alert
                                   :message (str status ": Error uploading file")}))))
