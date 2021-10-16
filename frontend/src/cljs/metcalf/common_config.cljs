(ns metcalf.common-config
  (:require [re-frame.core :as rf]
            [metcalf3.handlers :as handlers3]
            [metcalf3.fx :as fx3]
            [metcalf3.subs :as subs3]
            [metcalf3.views :as views3]))

;Temporary namespace to ease refactoring

(rf/reg-event-fx ::handlers3/-dashboard-create-save-error handlers3/-dashboard-create-save-error)
(rf/reg-event-fx ::handlers3/-dashboard-create-save-success handlers3/-dashboard-create-save-success)
(rf/reg-event-fx ::handlers3/-init-db-load-api-options handlers3/load-api-options)
(rf/reg-event-fx ::handlers3/-load-api-options handlers3/-load-api-options)
(rf/reg-event-fx ::handlers3/-load-es-options handlers3/-load-es-options)
(rf/reg-event-fx ::handlers3/-lodge-click-error handlers3/lodge-error)
(rf/reg-event-fx ::handlers3/-lodge-click-success handlers3/lodge-save-success)
(rf/reg-event-fx ::handlers3/-lodge-save-error handlers3/lodge-error)
(rf/reg-event-fx ::handlers3/-lodge-save-success handlers3/-lodge-save-success)
(rf/reg-event-fx ::handlers3/-org-changed handlers3/update-address)
(rf/reg-event-fx ::handlers3/-transite-doc-click-confirm handlers3/-transite-doc-click-confirm)
(rf/reg-event-fx ::handlers3/-transite-doc-confirm-error handlers3/-transite-doc-confirm-error)
(rf/reg-event-fx ::handlers3/-transite-doc-confirm-success handlers3/-transite-doc-confirm-success)
(rf/reg-event-fx ::handlers3/-update-person handlers3/org-changed)
(rf/reg-event-fx ::handlers3/open-modal handlers3/open-modal-handler)
(rf/reg-event-fx ::views3/dashboard-show-all-click handlers3/show-all-documents)
(rf/reg-event-fx ::views3/dashboard-toggle-status-filter handlers3/toggle-status-filter)
(rf/reg-event-fx ::views3/del-value handlers3/del-value)
(rf/reg-event-fx ::views3/document-teaser-archive-click (handlers3/transite-doc-click "archive"))
(rf/reg-event-fx ::views3/document-teaser-delete-archived-click (handlers3/transite-doc-click "delete_archived"))
(rf/reg-event-fx ::views3/document-teaser-restore-click (handlers3/transite-doc-click "restore"))
(rf/reg-event-fx ::views3/edit-tabs-pick-click handlers3/set-tab)
(rf/reg-event-fx ::views3/elasticsearch-select-field-input-change handlers3/load-es-options)
(rf/reg-event-fx ::views3/elasticsearch-select-field-mount handlers3/load-es-options)
(rf/reg-event-fx ::views3/keywords-theme-table-add-value handlers3/add-value!)
(rf/reg-event-fx ::views3/load-api-options3 handlers3/load-api-options)
(rf/reg-event-fx ::views3/nasa-list-select-field-change handlers3/update-nasa-list-value)
(rf/reg-event-fx ::views3/open-modal handlers3/open-modal-handler)
(rf/reg-event-fx ::views3/organisation-input-field-change handlers3/org-changed)
(rf/reg-event-fx ::views3/page-errors-hide-click handlers3/hide-errors)
(rf/reg-event-fx ::views3/parties-list-remove-party-confirm handlers3/remove-party)
(rf/reg-event-fx ::views3/person-input-field-picker-change handlers3/update-person)
(rf/reg-event-fx ::views3/person-list-field-change handlers3/update-person)
(rf/reg-event-fx ::views3/responsible-party-field-family-name-changed handlers3/person-detail-changed)
(rf/reg-event-fx ::views3/responsible-party-field-given-name-changed handlers3/person-detail-changed)
(rf/reg-event-fx ::views3/select-field-blur handlers3/show-errors)
(rf/reg-event-fx ::views3/table-modal-edit-new-field handlers3/new-field!)
(rf/reg-event-fx ::views3/update-dp-term handlers3/update-dp-term)
(rf/reg-event-fx ::views3/value-changed handlers3/value-changed)
(rf/reg-event-fx ::views3/who-new-add-value handlers3/add-value!)
(rf/reg-event-fx ::views3/who-new-field handlers3/new-field!)
(rf/reg-event-fx :handlers/add-attachment handlers3/add-attachment)
(rf/reg-event-fx :handlers/add-field! handlers3/add-field!)
(rf/reg-event-fx :handlers/add-keyword-extra handlers3/add-keyword-extra)
(rf/reg-event-fx :handlers/add-nodes handlers3/add-nodes)
(rf/reg-event-fx :handlers/archive-current-document handlers3/archive-current-document)
(rf/reg-event-fx :handlers/archive-current-document-success handlers3/archive-current-document-success)
(rf/reg-event-fx :handlers/back handlers3/back)
(rf/reg-event-fx :handlers/check-unsaved-keyword-input handlers3/check-unsaved-keyword-input)
(rf/reg-event-fx :handlers/clone-document handlers3/clone-document)
(rf/reg-event-fx :handlers/clone-document-error handlers3/clone-document-error)
(rf/reg-event-fx :handlers/clone-document-success handlers3/clone-document-success)
(rf/reg-event-fx :handlers/close-and-cancel handlers3/close-and-cancel)
(rf/reg-event-fx :handlers/close-and-confirm handlers3/close-and-confirm)
(rf/reg-event-fx ::views3/close-modal handlers3/close-modal)
(rf/reg-event-fx :handlers/close-modal handlers3/close-modal)
(rf/reg-event-fx ::views3/dashboard-create-click handlers3/dashboard-create-click)
(rf/reg-event-fx ::views3/modal-dialog-dashboard-create-modal-save-click handlers3/dashboard-create-save)
(rf/reg-event-fx :metcalf.imas.handlers/-init-db-load-api-options handlers3/load-api-options)
(rf/reg-event-fx :metcalf4.components/lodge-button-click handlers3/lodge-click)

(rf/reg-fx :fx/archive-current-document fx3/archive-current-document)
(rf/reg-fx :fx/clone-document fx3/clone-document)
(rf/reg-fx :fx/create-document fx3/create-document)
(rf/reg-fx :fx/save-current-document fx3/save-current-document)
(rf/reg-fx :fx/set-location-href fx3/set-location-href)
(rf/reg-fx :fx/submit-current-document fx3/submit-current-document)
(rf/reg-fx :fx/transition-current-document fx3/transition-current-document)
(rf/reg-fx :window/open fx3/window-open)
(rf/reg-fx :xhrio/get-json fx3/xhrio-get-json)
(rf/reg-fx :xhrio/post-json fx3/xhrio-post-json)

(rf/reg-sub ::views3/get-progress-bar-props :<- [:subs/get-derived-state] subs3/get-progress-props)
(rf/reg-sub ::views3/get-dashboard-props subs3/get-dashboard-props)
(rf/reg-sub :subs/get-derived-path :<- [:subs/get-derived-state] subs3/get-derived-path)
(rf/reg-sub :subs/get-derived-state subs3/get-derived-state)
(rf/reg-sub ::views3/get-textarea-widget-key subs3/get-form-tick)
(rf/reg-sub ::views3/get-app-root-modal-props subs3/get-modal-props)
(rf/reg-sub ::views3/get-app-root-page-name subs3/get-page-name)
(rf/reg-sub :subs/get-page-props subs3/get-page-props)
(rf/reg-sub :subs/platform-selected? subs3/platform-selected?)
(rf/reg-sub :textarea-field/get-many-field-props :<- [:subs/get-derived-state] subs3/get-textarea-field-many-props)