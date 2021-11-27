(ns metcalf.imas.handlers
  (:require [metcalf.common.actions4 :as actions4]
            [metcalf.common.schema4 :as schema4]
            [metcalf.common.db4 :as db4]))

(defn init-db
  [_ [_ payload]]
  (case (get-in payload [:page :name])

    "Dashboard"
    (let [{:keys [context create_form page]} payload]
      (-> {:db db4/default-db
           :fx [[:ui/setup-blueprint]]}
          (assoc-in [:db :context] context)
          (assoc-in [:db :create_form] create_form)
          (assoc-in [:db :page] page)
          (actions4/load-page-action payload)
          (actions4/init-create-form-action payload)
          (actions4/load-dashboard-document-data payload)))

    "Edit"
    (let [{:keys [context form upload_form data attachments theme page]} payload]
      (-> {:db db4/default-db
           :fx [[:ui/setup-blueprint]]}
          (assoc-in [:db :context] context)
          (assoc-in [:db :form] form)
          (assoc-in [:db :upload_form] upload_form)
          (assoc-in [:db :data] data)
          (assoc-in [:db :attachments] attachments)
          (assoc-in [:db :theme] theme)
          (assoc-in [:db :page] page)
          (actions4/load-page-action payload)
          (actions4/load-form-action payload)))))
