(ns metcalf.imas.pages
  (:require [metcalf.common.components4 :as components4]
            [metcalf.common.views4 :as views4]
            [re-frame.core :as rf]))

(defn navbar
  []
  [views4/navbar
   {:context @(rf/subscribe [:subs/get-derived-path [:context]])}])

(defn dashboard
  []
  [views4/dashboard
   {:dashboard-props                @(rf/subscribe [:app/get-dashboard-props])
    :dashboard-create-click         #(rf/dispatch [:app/dashboard-create-click])
    :dashboard-show-all-click       #(rf/dispatch [:app/dashboard-show-all-click])
    :dashboard-toggle-status-filter #(rf/dispatch [:app/dashboard-toggle-status-filter %])
    :document-archive-click         #(rf/dispatch [:app/document-teaser-archive-click (:transition_url %)])
    :document-delete-archived-click #(rf/dispatch [:app/document-teaser-delete-archived-click (:transition_url %)])
    :document-restore-click         #(rf/dispatch [:app/document-teaser-restore-click (:transition_url %)])
    :document-clone-click           #(rf/dispatch [:app/document-teaser-clone-click (:clone_url %)])
    :document-edit-click            #(aset js/location "href" (:url %))}])

(defn page-edit
  []
  [views4/PageViewEdit
   {:page             @(rf/subscribe [:subs/get-page-props])
    :context          @(rf/subscribe [:subs/get-derived-path [:context]])
    :form             @(rf/subscribe [:subs/get-derived-path [:form]])
    :dirty            @(rf/subscribe [:subs/get-form-dirty [:form]])
    :tab-props        @(rf/subscribe [:subs/get-edit-tab-props])
    :progress-props   @(rf/subscribe [:app/get-progress-bar-props])
    :on-pick-tab      #(rf/dispatch [:app/edit-tabs-pick-click %])
    :on-save-click    #(rf/dispatch [:app/PageViewEdit-save-button-click])
    :on-archive-click #(rf/dispatch [:app/handle-page-view-edit-archive-click])}])

(defn page-error
  []
  (let [{:keys [text code]} @(rf/subscribe [:subs/get-page-props])]
    [views4/page-error
     {:text text
      :code code}]))

(defn page-404
  [_]
  (let [{:keys [name]} @(rf/subscribe [:subs/get-page-props])]
    [views4/page-404
     {:name name}]))

(defn app-root
  []
  (let [page-name @(rf/subscribe [:subs/get-app-root-page-name])
        modal-props @(rf/subscribe [:subs/get-app-root-modal-props])]
    [:div
     (when modal-props
       (case (:type modal-props)
         :m4/table-modal-edit-form
         [views4/m4-modal-dialog-table-modal-edit-form modal-props]
         :m4/table-modal-add-form
         [views4/m4-modal-dialog-table-modal-add-form modal-props]
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
       "404"
       [page-404]
       "Error"
       [:div [navbar] [page-error]]
       "Edit"
       [:div [navbar] [page-edit]]
       "Dashboard"
       [:div [navbar] [dashboard]]
       nil)]))
