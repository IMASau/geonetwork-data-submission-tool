(ns metcalf4.handlers
  (:require [metcalf4.blocks :as blocks]
            [metcalf4.actions :as actions]
            [metcalf4.schema :as schema]
            [metcalf4.utils :as utils4]))


(defn db-path
  [{:keys [form-id data-path]}]
  (vec (flatten [form-id :state (blocks/block-path data-path)])))


(defn init-db
  [_ [_ payload]]
  (-> {:db {} :fx [[:ui/setup-blueprint]]}
      (actions/load-page-action payload)
      (actions/load-form-action payload)
      (actions/load-apis-action
        payload
        {:parametername        "/api/ternparameters"
         :parameterunit        "/api/qudtunits"
         :parameterinstrument  "/api/terninstruments"
         :parameterplatform    "/api/ternplatforms"
         :rolecode             "/api/rolecode.json"
         :samplingFrequency    "/api/samplingfrequency.json"
         :horizontalResolution "/api/horizontalresolution.json"
         :person               "/api/person.json"
         :institution          "/api/institution.json"
         :topiccategory        "/api/topiccategory.json"})))


(defn value-changed-handler
  [{:keys [db]} [_ ctx value]]
  (let [path (db-path ctx)]
    {:db (-> db
             (assoc-in (conj path :props :value) value)
             (assoc-in (conj path :props :show-errors) true))}))

(defn option-change-handler
  [{:keys [db]} [_ ctx option]]
  (let [{:keys [form-id data-path]} ctx
        schema (get-in db (flatten [form-id :schema (schema/schema-path data-path)]))
        state (blocks/as-blocks {:schema schema :data option})
        path (db-path ctx)]
    {:db (-> db
             (assoc-in path state)
             (assoc-in (conj path :props :show-errors) true))}))

; WIP
(defn list-add-click-handler
  [{:keys [db]} [_ ctx]]
  (let [{:keys [form-id data-path]} ctx]
    (-> {:db db}
        (actions/save-snapshot-action form-id)
        (actions/new-item-action form-id data-path)
        (actions/select-last-item-action form-id data-path))))

(defn list-add-with-defaults-click-handler
  [{:keys [db]} [_ props]]
  (let [{:keys [form-id data-path valueKey addedKey]} props
        defaults {valueKey (str (random-uuid)) addedKey true}]
    (-> {:db db}
        (actions/save-snapshot-action form-id)
        (actions/add-item-action form-id data-path defaults)
        (actions/select-last-item-action form-id data-path))))

(defn boxes-changed
  [{:keys [db]} [_ ctx geojson]]
  (let [{:keys [form-id data-path]} ctx
        geometries (mapv :geometry (:features geojson))
        boxes (mapv utils4/geometry->box-value geometries)
        schema (get-in db (flatten [form-id :schema (schema/schema-path data-path)]))
        state (blocks/as-blocks {:schema schema :data boxes})
        path (db-path ctx)]
    {:db (-> db
             (assoc-in path state)
             (assoc-in (conj path :props :show-errors) true))}))

(defn list-option-picker-change
  [{:keys [db]} [_ ctx option]]
  (let [{:keys [form-id data-path]} ctx]
    (-> {:db db}
        (actions/add-item-action form-id data-path option))))

; NOTE: assumes we only ever select user added items.  Might need to grow.
(defn selection-list-item-click
  [{:keys [db]} [_ props idx]]
  (let [{:keys [form-id data-path addedKey]} props]
    (-> {:db db}
        (cond-> addedKey
          (actions/select-user-defined-list-item-action form-id data-path idx addedKey)))))

(defn selection-list-remove-click
  [{:keys [db]} [_ ctx idx]]
  (let [{:keys [form-id data-path]} ctx]
    (-> {:db db}
        (actions/del-item-action form-id data-path idx))))

(defn selection-list-reorder
  [{:keys [db]} [_ ctx src-idx dst-idx]]
  (let [{:keys [form-id data-path]} ctx]
    (-> {:db db}
        (actions/genkey-action form-id data-path)
        (actions/move-item-action form-id data-path src-idx dst-idx))))

(defn boxmap-coordinates-open-add-modal
  [{:keys [db] :as s} [_ {:keys [ctx coord-field initial-data idx on-close on-save]}]]
  (let [{:keys [form-id data-path]} ctx
        ;; If this is identical to an existing item, actions/add-item-action will not result in
        ;; a new item. So, we run an identical check here so we can calculate the correct idx
        ;; for the modal to use. We can't be sure that the previous record (idx-1) is the correct
        ;; item to use, but it's the most likely place for an all-0s record to be.
        db-path (utils4/as-path [:db form-id :state (blocks/block-path data-path)])
        items (set (blocks/as-data (get-in s db-path)))
        idx (if (contains? items initial-data) (dec idx) idx)
        new-field-path (conj data-path idx)]
    (-> {:db db}
        (actions/add-item-action form-id data-path initial-data)
        (actions/open-modal {:type           :m4/table-modal-add-form
                             :form           coord-field
                             :path           new-field-path
                             :title          "Geographic Coordinates"
                             :on-close-click #(on-close idx)
                             :on-save-click  on-save}))))

(defn boxmap-coordinates-open-edit-modal
  [{:keys [db]} [_ {:keys [ctx coord-field on-delete on-cancel on-save]}]]
  (let [{:keys [data-path]} ctx]
    (-> {:db db}
        (actions/open-modal {:type            :m4/table-modal-edit-form
                             :form            coord-field
                             :path            data-path
                             :title           "Geographic Coordinates"
                             :on-delete-click on-delete
                             :on-close-click  on-cancel
                             :on-save-click   on-save}))))

(defn boxmap-coordinates-click-confirm-delete
  [{:keys [db]} [_ on-confirm]]
  (-> {:db db}
      (actions/open-modal {:type       :confirm
                           :title      "Delete"
                           :message    "Are you sure you want to delete?"
                           :on-confirm on-confirm})))

(defn boxmap-coordinates-list-delete
  [{:keys [db]} [_ ctx idx]]
  (let [{:keys [form-id data-path]} ctx]
    (-> {:db db}
        (actions/del-item-action form-id data-path idx)
        (update-in [:db :alert] pop))))

(defn -load-api-handler
  [{:keys [db]} [_ api results]]
  (actions/-load-api-action {:db db} api results))

(defn save-current-document
  [{:keys [db]} _]
  (let [url (get-in db [:form :url])
        data (-> db :form :state blocks/as-data)]
    {:db (assoc-in db [:page :metcalf3.handlers/saving?] true)
     :fx/save-current-document
         {:url       url
          :data      data
          :success-v [::-save-current-document-success data]
          :error-v   [::-save-current-document-error]}}))

(defn -save-current-document-success
  [{:keys [db]} [_ data resp]]
  (let [doc (get-in resp [:form :document])]
    (-> {:db db}
        (assoc-in [:db :form :data] data)
        (assoc-in [:db :page :metcalf3.handlers/saving?] false)
        (update-in [:db :context :document] merge doc))))

(defn -save-current-document-error
  [{:keys [db]} _]
  (-> {:db db}
      (assoc-in [:db :page :metcalf3.handlers/saving?] false)))

(defn list-edit-dialog-close-handler
  [{:keys [db]} [_ ctx]]
  (let [{:keys [form-id data-path]} ctx]
    (-> {:db db}
        (actions/restore-snapshot-action form-id)
        (actions/unselect-list-item-action form-id data-path))))

(defn list-edit-dialog-cancel-handler
  [{:keys [db]} [_ ctx]]
  (let [{:keys [form-id data-path]} ctx]
    (-> {:db db}
        (actions/restore-snapshot-action form-id)
        (actions/unselect-list-item-action form-id data-path))))

(defn list-edit-dialog-save-handler
  [{:keys [db]} [_ ctx]]
  (let [{:keys [form-id data-path]} ctx]
    (-> {:db db}
        (actions/discard-snapshot-action form-id)
        (actions/unselect-list-item-action form-id data-path))))
