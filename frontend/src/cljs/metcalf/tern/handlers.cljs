(ns metcalf.tern.handlers
  (:require [metcalf.common.logic3 :as logic3]
            [metcalf4.actions4 :as actions]
            [clojure.edn :as edn]
            [metcalf4.schema4 :as schema]))

(defn init-db
  [_ [_ payload]]
  (let [ui-data (some-> payload :ui_payload edn/read-string)
        editor-tabs (:low-code/edit-tabs ui-data)
        db (-> (logic3/initial-state payload)
               (cond-> editor-tabs (assoc :low-code/edit-tabs editor-tabs)))]
    (schema/assert-schema-data (:form db))
    (-> {:db db
         :fx [[:ui/setup-blueprint]
              [:metcalf4.low-code/init! ui-data]]}
        (actions/init-create-form-action payload))))
