(ns metcalf.imas.handlers
  (:require [metcalf.common.actions4 :as actions4]))

(defn init-db
  [_ [_ payload]]
  (case (get-in payload [:page :name])

    "Dashboard"
    (let [{:keys [context create_form page]} payload]
      (-> {:db {:modal/stack []
                :context     context
                :page        page}
           :fx [[:ui/setup-blueprint]]}
          (actions4/init-create-form-action create_form)
          (actions4/load-dashboard-document-data payload)))

    "Edit"
    (let [{:keys [context form upload_form attachments page]} payload]
      (-> {:db {:modal/stack []
                :context     context
                :upload_form upload_form
                ;:data data
                :attachments attachments
                :page        page}
           :fx [[:ui/setup-blueprint]]}
          (actions4/load-edit-form-action form)))))
