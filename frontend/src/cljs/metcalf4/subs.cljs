(ns metcalf4.subs
  (:require [cljs.spec.alpha :as s]
            [metcalf4.blocks :as blocks]
            [metcalf4.rules :as rules]
            [metcalf4.schema :as schema]
            [metcalf4.utils :as utils4]
            [re-frame.core :as rf]))

(def prewalk-xform
  (comp blocks/propagate-disabled))

(def postwalk-xform
  (comp blocks/progess-score-analysis
        rules/apply-rules))

(defn get-form-state
  [db [_ form-id]]
  (when (vector? form-id)
    (let [path (conj form-id :state)
          state (get-in db path)]
      (->> state
           (blocks/prewalk prewalk-xform)
           (blocks/postwalk postwalk-xform)))))

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
  (let [form-id (or form-id [:form])
        data0 (get-in db (conj form-id :data))
        state1 (get-in db (conj form-id :state))
        data1 (blocks/as-data (blocks/postwalk rules/apply-rules state1))]
    (not= data0 data1)))

(defn create-document-modal-can-save?
  [db _]
  (let [state0 (get-in db [:create_form :state])
        state1 (blocks/postwalk (comp utils4/score-block rules/apply-rules) state0)
        {:keys [errors]} (::utils4/score state1)]
    (not (pos? errors))))
