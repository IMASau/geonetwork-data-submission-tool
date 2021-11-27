(ns metcalf.tern.handlers
  (:require [clojure.edn :as edn]
            [metcalf.common.low-code4 :as low-code4]
            [metcalf.common.actions4 :as actions4]
            [metcalf.common.db4 :as db4]))

(defn init-db
  [_ [_ payload]]
  (let [ui-data (some-> payload :ui_payload edn/read-string)
        editor-tabs (:low-code/edit-tabs ui-data)]

    (case (get-in payload [:page :name])

      "Dashboard"
      (let [{:keys [context create_form page]} payload]
        (-> {:db {:modal/stack []
                  :context     context
                  :page        page}
             :fx [[:ui/setup-blueprint]
                  [::low-code4/init! ui-data]]}
            (actions4/init-create-form-action create_form)
            (actions4/load-dashboard-document-data payload)))

      "Edit"
      (let [{:keys [context form upload_form attachments page]} payload]
        (-> {:db {:modal/stack []
                  :context     context
                  :upload_form upload_form
                  ;:data        data
                  :attachments attachments
                  :page        page}
             :fx [[:ui/setup-blueprint]
                  [::low-code4/init! ui-data]]}
            (actions4/load-edit-form-action form)
            (cond-> editor-tabs (assoc-in [:db :low-code/edit-tabs] editor-tabs)))))))
