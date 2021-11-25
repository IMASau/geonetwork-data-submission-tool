(ns metcalf.imas.handlers
  (:require [metcalf.common.actions4 :as actions4]
            [metcalf.common.logic3 :as logic3]
            [metcalf.common.schema4 :as schema4]))

(defn init-db
  [_ [_ payload]]
  (case (get-in payload [:page :name])

    "Dashboard"
    (-> {:db payload
         :fx [[:ui/setup-blueprint]]}
        (logic3/initial-state-action payload)
        (actions4/init-create-form-action payload)
        (actions4/load-dashboard-document-data payload))

    "Edit"
    (-> {:db payload
         :fx [[:ui/setup-blueprint]]}
        (logic3/initial-state-action payload)
        (actions4/init-create-form-action payload))
    ))
