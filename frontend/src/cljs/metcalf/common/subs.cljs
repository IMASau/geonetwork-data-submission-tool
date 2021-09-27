(ns metcalf.common.subs
  (:require [metcalf.common.blocks :as blocks]
            [metcalf3.logic :as logic]
            [re-frame.core :as rf]))


(defn form-state-with-logic-applied-sub
  [db [_ path]]
  (update-in db path logic/validate-rules))


(defn form-state-signal
  [{:keys [form-id]}]
  (rf/subscribe [::form-state-with-logic-applied form-id]))


; FIXME: leaking empty strings for date values  from payload.forms.data
(defn get-block-props-sub
  [state [_ {:keys [data-path]}]]
  (let [path (blocks/block-path data-path)]
    (get-in state (conj path :props))))
