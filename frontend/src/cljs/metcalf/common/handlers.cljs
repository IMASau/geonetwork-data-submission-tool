(ns metcalf.common.handlers
  (:require [metcalf.common.blocks :as blocks]))


(defn db-path
  [{:keys [form-id data-path]}]
  (vec (flatten [form-id (blocks/block-path data-path)])))


(defn value-changed-handler
  [{:keys [db]} [_ ctx value]]
  (let [path (db-path ctx)]
    {:db (-> db
             (assoc-in (conj path :props :value) value)
             (assoc-in (conj path :props :show-errors) true))}))

