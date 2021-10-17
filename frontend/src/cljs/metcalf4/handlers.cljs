(ns metcalf4.handlers
  (:require [metcalf4.blocks :as blocks]
            [metcalf4.actions :as actions]
            [metcalf4.schema :as schema]
            [metcalf4.utils :as utils4]
            [metcalf4.rules :as rules]
            [metcalf3.fx :as fx3]))

(defn db-path
  [{:keys [form-id data-path]}]
  (utils4/as-path [form-id :state (blocks/block-path data-path)]))

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

(defn list-add-click-handler
  [{:keys [db]} [_ ctx]]
  (let [{:keys [form-id data-path]} ctx]
    (-> {:db db}
        (actions/save-snapshot-action form-id)
        (actions/new-item-action form-id data-path)
        (actions/select-last-item-action form-id data-path))))

(defn list-add-with-defaults-click-handler2
  [{:keys [db]} [_ config]]
  (let [{:keys [form-id data-path value-path added-path]} config
        defaults (-> {}
                     (assoc-in value-path (str (random-uuid)))
                     (assoc-in added-path true))]
    (-> {:db db}
        (actions/save-snapshot-action form-id)
        (actions/add-item-action form-id data-path defaults)
        (actions/select-last-item-action form-id data-path))))

(defn list-add-with-defaults-click-handler
  [{:keys [db]} [_ props]]
  (let [{:keys [form-id data-path value-path added-path]} props
        defaults {value-path (str (random-uuid)) added-path true}]
    (-> {:db db}
        (actions/save-snapshot-action form-id)
        (actions/add-item-action form-id data-path defaults)
        (actions/select-last-item-action form-id data-path))))

(defn item-add-with-defaults-click-handler
  [{:keys [db]} [_ props]]
  (let [{:keys [form-id data-path value-path added-path]} props
        defaults {value-path (str (random-uuid)) added-path true}]
    (-> {:db db}
        (actions/save-snapshot-action form-id)
        (actions/set-value-action form-id data-path defaults)
        (actions/dialog-open-action form-id data-path))))

(defn item-add-with-defaults-click-handler2
  [{:keys [db]} [_ props]]
  (let [{:keys [form-id data-path value-path added-path]} props
        defaults (-> {}
                     (assoc-in value-path (str (random-uuid)))
                     (assoc-in added-path true))]
    (-> {:db db}
        (actions/save-snapshot-action form-id)
        (actions/set-value-action form-id data-path defaults)
        (actions/dialog-open-action form-id data-path))))

(defn boxes-changed
  [{:keys [db]} [_ config geojson]]
  (let [{:keys [form-id data-path value-path added-path]} config
        geometries (mapv :geometry (:features geojson))
        boxes (mapv utils4/geometry->box-value geometries)
        boxes (map-indexed (fn [idx m] (assoc-in m value-path idx)) boxes)
        boxes (map (fn [m] (assoc-in m added-path true)) boxes)
        schema (get-in db (flatten [form-id :schema (schema/schema-path data-path)]))
        state (blocks/as-blocks {:schema schema :data boxes})
        db-path (utils4/as-path [:db form-id :state (blocks/block-path data-path)])]
    (-> {:db db}
        (assoc-in db-path state)
        (assoc-in (conj db-path :props :show-errors) true)
        (actions/genkey-action form-id data-path))))

(defn list-option-picker-change
  [{:keys [db]} [_ ctx option]]
  (let [{:keys [form-id data-path]} ctx]
    (-> {:db db}
        (actions/add-item-action form-id data-path option))))

(defn item-option-picker-change
  [{:keys [db]} [_ ctx option]]
  (let [{:keys [form-id data-path]} ctx]
    (-> {:db db}
        (actions/set-value-action form-id data-path option))))

(defn selection-list-item-click2
  [{:keys [db]} [_ props idx]]
  (let [{:keys [form-id data-path added-path]} props]
    (cond-> {:db db}
      added-path
      (actions/select-user-defined-list-item-action2 form-id data-path idx added-path))))

(defn selection-list-item-click
  [{:keys [db]} [_ props idx]]
  (let [{:keys [form-id data-path added-path]} props]
    (cond-> {:db db}
      added-path
      (actions/select-user-defined-list-item-action form-id data-path idx added-path))))

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
        state0 (get-in db [:form :state])
        state1 (blocks/postwalk rules/apply-rules state0)
        data (blocks/as-data state1)]
    {:db (assoc-in db [:page :metcalf3.handlers/saving?] true)
     ; TODO: put logic in handler, use generic js/fetch fx
     ::fx3/save-current-document
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

(defn item-edit-dialog-close-handler
  [{:keys [db]} [_ ctx]]
  (let [{:keys [form-id data-path]} ctx]
    (-> {:db db}
        (actions/restore-snapshot-action form-id)
        (actions/dialog-close-action form-id data-path))))

(defn item-edit-dialog-cancel-handler
  [{:keys [db]} [_ ctx]]
  (let [{:keys [form-id data-path]} ctx]
    (-> {:db db}
        (actions/restore-snapshot-action form-id)
        (actions/dialog-close-action form-id data-path))))

(defn item-edit-dialog-save-handler
  [{:keys [db]} [_ ctx]]
  (let [{:keys [form-id data-path]} ctx]
    (-> {:db db}
        (actions/discard-snapshot-action form-id)
        (actions/dialog-close-action form-id data-path))))
