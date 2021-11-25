(ns metcalf.tern.handlers
  (:require [clojure.edn :as edn]
            [metcalf.common.low-code4 :as low-code4]
            [metcalf.common.actions4 :as actions4]
            [metcalf.common.logic3 :as logic3]))

(defn init-db
  [_ [_ payload]]
  (let [ui-data (some-> payload :ui_payload edn/read-string)
        editor-tabs (:low-code/edit-tabs ui-data)]

    (case (get-in payload [:page :name])

      "Dashboard"
      (-> {:db payload
           :fx [[:ui/setup-blueprint]
                [::low-code4/init! ui-data]]}
          (logic3/setup-alerts)
          (logic3/initial-state-action payload)
          (actions4/init-create-form-action payload)
          (actions4/load-dashboard-document-data payload))

      "Edit"
      (-> {:db payload
           :fx [[:ui/setup-blueprint]
                [::low-code4/init! ui-data]]}
          (logic3/setup-alerts)
          (logic3/initial-state-action payload)
          (cond-> editor-tabs (assoc-in [:db :low-code/edit-tabs] editor-tabs))))))
