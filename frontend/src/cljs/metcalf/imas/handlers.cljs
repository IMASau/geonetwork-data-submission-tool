(ns metcalf.imas.handlers
  (:require [metcalf.common.actions4 :as actions4]
            [metcalf.common.logic3 :as logic3]
            [metcalf.common.schema4 :as schema4]))

(defn init-db
  [_ [_ payload]]
  (-> {:fx [[:ui/setup-blueprint]]}
      (logic3/initial-state-action payload)
      ;(actions4/load-page-action payload)
      ;(actions4/load-form-action payload)
      (actions4/init-create-form-action payload)))
