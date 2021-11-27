(ns metcalf.imas.handlers
  (:require [metcalf.common.actions4 :as actions4]
            [metcalf.common.schema4 :as schema4]
            [metcalf.common.db4 :as db4]))

(defn init-db
  [_ [_ payload]]
  (case (get-in payload [:page :name])

    "Dashboard"
    (-> {:db db4/default-db
         :fx [[:ui/setup-blueprint]]}
        (update :db merge payload)
        (actions4/load-page-action payload)
        (actions4/init-create-form-action payload)
        (actions4/load-dashboard-document-data payload))

    "Edit"
    (-> {:db db4/default-db
         :fx [[:ui/setup-blueprint]]}
        (update :db merge payload)
        (actions4/load-page-action payload)
        (actions4/load-form-action payload))
    ))
