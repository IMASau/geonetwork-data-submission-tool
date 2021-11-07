(ns metcalf.imas.pages
  (:require [metcalf3.views :as views3]
            [re-frame.core :as rf]
            [metcalf4.views :as views4]
            [metcalf4.components :as components4]))

(defn app-root
  []
  (let [page-name @(rf/subscribe [:subs/get-app-root-page-name])
        modal-props @(rf/subscribe [:subs/get-app-root-modal-props])]
    [:div
     (when modal-props
       (case (:type modal-props)
         :TableModalEditForm
         [views3/modal-dialog-table-modal-edit-form modal-props]
         :TableModalAddForm
         [views3/modal-dialog-table-modal-add-form modal-props]
         :m4/table-modal-edit-form
         [views4/m4-modal-dialog-table-modal-edit-form modal-props]
         :m4/table-modal-add-form
         [views4/m4-modal-dialog-table-modal-add-form modal-props]
         :ThemeKeywords
         [views3/modal-dialog-theme-keywords (select-keys modal-props [:keyword-type :keywords-path])]
         :DashboardCreateModal
         [components4/create-document-modal]
         :alert
         [views4/modal-dialog-alert
          {:message    (:message modal-props)
           :on-dismiss #(rf/dispatch [:app/modal-dialog-alert-dismiss])
           :on-save    #(rf/dispatch [:app/modal-dialog-alert-save])}]
         :confirm
         [views4/modal-dialog-confirm
          {:title      (:title modal-props)
           :message    (:message modal-props)
           :on-dismiss #(rf/dispatch [:app/modal-dialog-confirm-dismiss])
           :on-cancel  #(rf/dispatch [:app/modal-dialog-confirm-cancel])
           :on-save    #(rf/dispatch [:app/modal-dialog-confirm-save])}]
         nil))
     (case page-name
       "404" [views3/PageView404 nil]
       "Error" [views3/PageViewError nil]
       "Edit" [views3/PageViewEdit nil]
       "Dashboard" [views3/dashboard nil]
       nil)]))
