(ns metcalf4.subs
  (:require [cljs.spec.alpha :as s]
            [metcalf4.blocks :as blocks]
            [re-frame.core :as rf]
            [metcalf4.rules :as rules]
            [metcalf4.schema :as schema]
            [metcalf4.utils :as utils4]))


(defn get-form-state
  [db [_ form-id]]
  (when (vector? form-id)
    (let [path (conj form-id :state)
          state (get-in db path)]
      (blocks/postwalk rules/apply-rules state))))


(defn form-state-signal
  [[_ {:keys [form-id]}]]
  (rf/subscribe [::get-form-state form-id]))


(defn get-block-props-sub
  "take config and merge with block props"
  [state [_ {:keys [data-path] :as config}]]
  (s/assert (s/nilable ::utils4/data-path) data-path)
  (let [logic (when (vector? data-path)
                (s/assert some? state)
                (let [path (blocks/block-path data-path)]
                  (get-in state (conj path :props))))]
    (merge config logic)))


(defn get-block-data-sub
  [state [_ {:keys [data-path]}]]
  (s/assert ::utils4/data-path data-path)
  (let [path (blocks/block-path data-path)]
    (blocks/as-data (get-in state path))))


(defn get-data-schema-sub
  [db [_ {:keys [form-id data-path]}]]
  (s/assert (s/nilable ::utils4/data-path) data-path)
  (when (and form-id data-path)
    (get-in db (utils4/as-path [form-id :schema (schema/schema-path data-path)]))))


(defn get-form-dirty?
  [db [_ form-id]]
  (let [data0 (get-in db (conj form-id :data))
        state1 (get-in db (conj form-id :state))
        data1 (blocks/as-data (blocks/postwalk rules/apply-rules state1))]
    (not= data0 data1)))