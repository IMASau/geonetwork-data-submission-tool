(ns metcalf.common-config
  (:require [metcalf3.fx :as fx3]
            [metcalf3.handlers :as handlers3]
            [metcalf3.subs :as subs3]
            [metcalf3.views :as views3]
            [re-frame.core :as rf]))

;Temporary namespace to ease refactoring

(rf/reg-event-fx :app/-archive-current-document-success handlers3/-archive-current-document-success)
(rf/reg-event-fx :app/-clone-document-error handlers3/-clone-document-error)
(rf/reg-event-fx :app/-clone-document-success handlers3/-clone-document-success)
(rf/reg-event-fx :app/-load-api-options handlers3/-load-api-options)
(rf/reg-event-fx :app/-lodge-click-error handlers3/lodge-error)
(rf/reg-event-fx :app/-lodge-click-success handlers3/lodge-save-success)
(rf/reg-event-fx :app/-lodge-save-error handlers3/lodge-error)
(rf/reg-event-fx :app/-lodge-save-success handlers3/-lodge-save-success)
(rf/reg-event-fx :app/-transite-doc-click-confirm handlers3/-transite-doc-click-confirm)
(rf/reg-event-fx :app/-transite-doc-confirm-error handlers3/-transite-doc-confirm-error)
(rf/reg-event-fx :app/-transite-doc-confirm-success handlers3/-transite-doc-confirm-success)
(rf/reg-event-fx :app/open-modal handlers3/open-modal-handler)
(rf/reg-event-fx :app/delete-attachment-confirm handlers3/del-value)
(rf/reg-event-fx :app/upload-max-filesize-exceeded handlers3/open-modal-handler)
(rf/reg-event-fx :app/delete-attachment-click handlers3/open-modal-handler)
(rf/reg-event-fx :app/upload-data-file-upload-failed handlers3/open-modal-handler)
(rf/reg-event-fx :app/upload-data-confirm-upload-click-add-attachment handlers3/add-attachment)
(rf/reg-event-fx :app/clone-doc-confirm handlers3/clone-document)
(rf/reg-event-fx :app/dashboard-create-click handlers3/dashboard-create-click)
(rf/reg-event-fx :app/dashboard-show-all-click handlers3/show-all-documents)
(rf/reg-event-fx :app/dashboard-toggle-status-filter handlers3/toggle-status-filter)
(rf/reg-event-fx :app/document-teaser-archive-click (handlers3/transite-doc-click "archive"))
(rf/reg-event-fx :app/document-teaser-clone-click handlers3/document-teaser-clone-click)
(rf/reg-event-fx :app/document-teaser-delete-archived-click (handlers3/transite-doc-click "delete_archived"))
(rf/reg-event-fx :app/document-teaser-restore-click (handlers3/transite-doc-click "restore"))
(rf/reg-event-fx :app/edit-tabs-pick-click handlers3/set-tab)
(rf/reg-event-fx :app/handle-page-view-edit-archive-click handlers3/handle-page-view-edit-archive-click)
(rf/reg-event-fx :app/modal-dialog-alert-dismiss handlers3/close-modal)
(rf/reg-event-fx :app/modal-dialog-alert-save handlers3/close-modal)
(rf/reg-event-fx :app/modal-dialog-confirm-cancel handlers3/close-and-cancel)
(rf/reg-event-fx :app/modal-dialog-confirm-dismiss handlers3/close-and-cancel)
(rf/reg-event-fx :app/modal-dialog-confirm-save handlers3/close-and-confirm)
(rf/reg-event-fx :app/page-view-edit-archive-click-confirm handlers3/archive-current-document)
(rf/reg-event-fx :metcalf.imas.handlers/-init-db-load-api-options handlers3/load-api-options)
(rf/reg-event-fx :metcalf4.components/coordinates-modal-field-close-modal handlers3/close-modal)
(rf/reg-event-fx :metcalf4.components/lodge-button-click handlers3/lodge-click)
(rf/reg-fx ::fx3/archive-current-document fx3/archive-current-document)
(rf/reg-fx ::fx3/clone-document fx3/clone-document)
(rf/reg-fx ::fx3/create-document fx3/create-document)
(rf/reg-fx ::fx3/save-current-document fx3/save-current-document)
(rf/reg-fx ::fx3/set-location-href fx3/set-location-href)
(rf/reg-fx ::fx3/submit-current-document fx3/submit-current-document)
(rf/reg-fx ::fx3/transition-current-document fx3/transition-current-document)
(rf/reg-fx ::fx3/xhrio-get-json fx3/xhrio-get-json)
(rf/reg-fx ::fx3/xhrio-post-json fx3/xhrio-post-json)
(rf/reg-sub :app/get-dashboard-props subs3/get-dashboard-props)
(rf/reg-sub :app/get-progress-bar-props :<- [:subs/get-derived-state] subs3/get-progress-props)
(rf/reg-sub :subs/get-derived-path :<- [:subs/get-derived-state] subs3/get-derived-path)
(rf/reg-sub :subs/get-derived-state subs3/get-derived-state)
(rf/reg-sub :subs/get-page-props subs3/get-page-props)