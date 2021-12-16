(ns metcalf.common.subs4
  (:require [cljs.spec.alpha :as s]
            [metcalf.common.blocks4 :as blocks4]
            [metcalf.common.rules4 :as rules4]
            [metcalf.common.schema4 :as schema4]
            [metcalf.common.utils4 :as utils4]
            [re-frame.core :as rf]))

(def prewalk-xform
  (comp blocks4/propagate-disabled))

(def postwalk-xform
  (comp blocks4/progress-score-analysis
        rules4/apply-rules))

(defn get-form-state
  [db [_ form-id]]
  (when (vector? form-id)
    (let [{:keys [state]} (get-in db form-id)]
      (->> state
           (blocks4/prewalk prewalk-xform)
           (blocks4/postwalk postwalk-xform)))))

(defn form-state-signal
  [[_ {:keys [form-id]}]]
  (rf/subscribe [::get-form-state form-id]))

(defn get-list-edit-can-save-sub
  [state [_ {:keys [data-path]}]]
  (s/assert (s/nilable ::utils4/data-path) data-path)
  (s/assert vector? data-path)
  (let [path (blocks4/block-path data-path)
        logic (get-in state path)
        {:keys [progress/errors]} (:progress/score logic)]
    (not (pos? errors))))

(defn has-block-errors?
  [state [_ {:keys [data-path]}]]
  (s/assert (s/nilable ::utils4/data-path) data-path)
  (s/assert vector? data-path)
  (let [path (blocks4/block-path data-path)
        logic (get-in state path)
        {:keys [progress/errors]} (:progress/score logic)]
    (pos? errors)))

(defn get-block-props-sub
  "take config and merge with block props"
  [state [_ {:keys [data-path] :as config}]]
  (s/assert (s/nilable ::utils4/data-path) data-path)
  (let [logic (when (vector? data-path)
                (s/assert some? state)
                (let [path (blocks4/block-path data-path)]
                  (get-in state (conj path :props))))]
    (merge config logic)))

(defn get-block-data-sub
  [state [_ {:keys [data-path]}]]
  (s/assert ::utils4/data-path data-path)
  (let [path (blocks4/block-path data-path)]
    (blocks4/as-data (get-in state path))))

(defn get-data-schema-sub
  [db [_ {:keys [form-id data-path]}]]
  (s/assert (s/nilable ::utils4/data-path) data-path)
  (when (and form-id data-path)
    (get-in db (utils4/as-path [form-id :schema (schema4/schema-path data-path)]))))


(defn get-form-dirty?
  [db [_ form-id]]
  (let [form-id (or form-id [:form])
        data0 (get-in db (conj form-id :data))
        state1 (get-in db (conj form-id :state))
        data1 (blocks4/as-data (blocks4/postwalk rules4/apply-rules state1))]
    (not= data0 data1)))

(defn create-document-modal-can-save?
  [db _]
  (let [state0 (get-in db [:create_form :state])
        state1 (blocks4/postwalk postwalk-xform state0)
        {:keys [progress/errors]} (:progress/score state1)]
    (not (pos? errors))))

(defn contributors-modal-props
  [db [_ uuid]]
  (let [{:keys [contributors]} (get-in db [:app/document-data uuid])]
    {:uuid   uuid
     :emails (mapv :email contributors)}))

(defn get-page-name
  [db _]
  (get-in db [:page :name]))

(defn get-modal-props
  [db _]
  (let [modal-stack (:modal/stack db)
        modal-props (peek modal-stack)]
    (when modal-props
      (let [breadcrumbs (mapv :title modal-stack)]
        (assoc modal-props :breadcrumbs breadcrumbs)))))
