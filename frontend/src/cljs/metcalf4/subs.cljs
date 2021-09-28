(ns metcalf4.subs
  (:require [cljs.spec.alpha :as s]
            [metcalf3.logic :as logic3]
            [metcalf4.blocks :as blocks]
            [re-frame.core :as rf]))


(defn get-form-state
  [db [_ path]]
  (s/assert vector? path)
  (update-in db path logic3/validate-rules))


(defn form-state-signal
  [[_ {:keys [form-id]}]]
  (s/assert vector? form-id)
  (rf/subscribe [::get-form-state form-id]))


; FIXME: leaking empty strings for date values  from payload.forms.data
(defn get-block-props-sub
  [state [_ {:keys [data-path]}]]
  (s/assert vector? data-path)
  (let [path (blocks/block-path data-path)]
    (get-in state (conj path :props))))
