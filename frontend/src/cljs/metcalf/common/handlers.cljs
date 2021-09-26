(ns metcalf.common.handlers
  (:require [metcalf.common.blocks :as blocks]))


; FIXME: hardcoded form location
(defn input-field-with-label-value-changed
  [{:keys [db]} [_ path value]]
  (let [block-path (into [:form :blocks] (blocks/block-path path))]
    {:db (-> db
             (assoc-in (conj block-path :properties :value) value)
             (assoc-in (conj block-path :properties :show-errors) true))}))


; FIXME: hardcoded form location
(defn textarea-field-with-label-value-change
  [{:keys [db]} [_ path value]]
  (let [block-path (into [:form :blocks] (blocks/block-path path))]
    {:db (-> db
             (assoc-in (conj block-path :properties :value) value)
             (assoc-in (conj block-path :properties :show-errors) true))}))


; FIXME: hardcoded form location
(defn date-field-with-label-value-change
  [{:keys [db]} [_ path value]]
  (let [block-path (into [:form :blocks] (blocks/block-path path))]
    {:db (-> db
             (assoc-in (conj block-path :properties :value) value)
             (assoc-in (conj block-path :properties :show-errors) true))}))
