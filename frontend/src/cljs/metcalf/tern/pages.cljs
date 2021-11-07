(ns metcalf.tern.pages
  (:require [metcalf3.views :as views3]
            [re-frame.core :as rf]
            [metcalf4.views :as views4]))

(defn app-root
  []
  (let [page-name @(rf/subscribe [:subs/get-app-root-page-name])
        modal-props @(rf/subscribe [:subs/get-app-root-modal-props])]
    [:div
     (case (:type modal-props)
       :DashboardCreateModal
       [views3/modal-dialog-dashboard-create-modal modal-props]
       :confirm
       [views4/modal-dialog-confirm
        {:title      (:title modal-props)
         :message    (:message modal-props)
         :on-dismiss #(rf/dispatch [:app/modal-dialog-confirm-dismiss])
         :on-cancel  #(rf/dispatch [:app/modal-dialog-confirm-cancel])
         :on-save    #(rf/dispatch [:app/modal-dialog-confirm-save])}]
       :alert
       [views4/modal-dialog-alert
        {:message    (:message modal-props)
         :on-dismiss #(rf/dispatch [:app/modal-dialog-alert-dismiss])
         :on-save    #(rf/dispatch [:app/modal-dialog-alert-save])}]
       nil)
     (case page-name
       "404"
       [views3/PageView404 nil]
       "Error"
       [views3/PageViewError nil]
       "Edit"
       [views3/PageViewEdit nil]
       "Dashboard"
       [views4/dashboard
        {:dashboard-props @(rf/subscribe [::get-dashboard-props])
         :dashboard-create-click #(rf/dispatch [::dashboard-create-click])
         :dashboard-show-all-click #(rf/dispatch [::dashboard-show-all-click])
         :dashboard-toggle-status-filter #(rf/dispatch [::dashboard-toggle-status-filter %])}]
       nil)]))
