(ns metcalf.common.subs
  (:require [metcalf.common.blocks :as blocks]))


(defn get-input-field-with-label-props
  [block-state [_ data-path]]
  (let [block-path (conj (blocks/block-path data-path) :properties)]
    (get-in block-state block-path)))


(defn get-textarea-field-with-label-props
  [block-state [_ data-path]]
  (let [block-path (conj (blocks/block-path data-path) :properties)]
    (get-in block-state block-path)))


; FIXME: leaking empty strings instead of null from payload.forms.data
(defn get-date-field-with-label-props
  [block-state [_ data-path]]
  (let [block-path (conj (blocks/block-path data-path) :properties)]
    (get-in block-state block-path)))
