(ns metcalf.common.handlers
  (:require [metcalf.common.blocks :as blocks]))


(defn input-field-with-label-value-changed
  [{:keys [db]} [_ db-path value]]
  {:db (-> db
           (assoc-in (conj db-path :properties :value) value)
           (assoc-in (conj db-path :properties :show-errors) true))})


(defn textarea-field-with-label-value-change
  [{:keys [db]} [_ db-path value]]
  {:db (-> db
           (assoc-in (conj db-path :properties :value) value)
           (assoc-in (conj db-path :properties :show-errors) true))})


(defn date-field-with-label-value-change
  [{:keys [db]} [_ db-path value]]
  {:db (-> db
           (assoc-in (conj db-path :properties :value) value)
           (assoc-in (conj db-path :properties :show-errors) true))})
