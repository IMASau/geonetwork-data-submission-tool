(ns metcalf4.handlers
  (:require [metcalf4.blocks :as blocks]
            [metcalf4.actions :as actions]
            [metcalf4.schema :as schema]))


(defn db-path
  [{:keys [form-id data-path]}]
  (vec (flatten [form-id :state (blocks/block-path data-path)])))


(defn init-db
  [_ [_ payload]]
  (-> {:db {}}
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