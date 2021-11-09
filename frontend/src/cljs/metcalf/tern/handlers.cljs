(ns metcalf.tern.handlers
  (:require [clojure.edn :as edn]
            [metcalf.common.actions4 :as actions4]
            [metcalf.common.logic3 :as logic3]
            [metcalf.common.schema4 :as schema4]))

(defn init-db
  [_ [_ payload]]
  (let [ui-data (some-> payload :ui_payload edn/read-string)
        editor-tabs (:low-code/edit-tabs ui-data)
        db (-> (logic3/initial-state payload)
               (cond-> editor-tabs (assoc :low-code/edit-tabs editor-tabs)))]
    (schema4/assert-schema-data (:form db))
    (-> {:db db
         :fx [[:ui/setup-blueprint]
              [:metcalf.common.low-code/init! ui-data]]}
        (actions4/init-create-form-action payload))))
