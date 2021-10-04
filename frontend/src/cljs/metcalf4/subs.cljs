(ns metcalf4.subs
  (:require [cljs.spec.alpha :as s]
            [metcalf4.blocks :as blocks]
            [re-frame.core :as rf]
            [metcalf4.rules :as rules]))


(defn get-form-state
  [db [_ form-id]]
  (when (vector? form-id)
    (let [path (conj form-id :state)
          state (get-in db path)]
      (blocks/postwalk rules/apply-rules state))))


(defn form-state-signal
  [[_ {:keys [form-id]}]]
  (rf/subscribe [::get-form-state form-id]))


; FIXME: leaking empty strings for date values from payload.forms.data
(defn get-block-props-sub
  [state [_ {:keys [data-path]}]]
  (when (vector? data-path)
    (s/assert some? state)
    (let [path (blocks/block-path data-path)]
      (get-in state (conj path :props)))))

(defn get-block-data-sub
  [state [_ {:keys [data-path]}]]
  (s/assert vector? data-path)
  (let [path (blocks/block-path data-path)]
    (blocks/as-data (get-in state path))))


; FIXME: hardcoded path
; FIXME: apply rules?
(defn get-form-dirty?
  [db]
  (let [data0 (get-in db [:form :data])
        state1 (get-in db [:form :state])
        data1 (blocks/as-data state1)]
    (not= data0 data1)))
