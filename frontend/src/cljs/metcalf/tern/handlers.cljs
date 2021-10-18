(ns metcalf.tern.handlers
  (:require [metcalf3.logic :as logic3]
            [metcalf4.actions :as actions]
            [clojure.edn :as edn]))

(defn init-db
  [_ [_ payload]]
  (let [ui-data (some-> payload :ui_payload edn/read-string)
        editor-tabs (:low-code/edit-tabs ui-data)]
    (-> {:db (-> (logic3/initial-state payload)
                 (cond-> editor-tabs (assoc :low-code/edit-tabs editor-tabs)))
         :fx [[:ui/setup-blueprint]
              [:metcalf4.low-code/init! ui-data]]})))
