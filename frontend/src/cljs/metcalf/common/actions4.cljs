(ns metcalf.common.actions4
  (:require [metcalf.common.blocks4 :as blocks4]
            [metcalf.common.logic4 :as logic4]
            [metcalf.common.schema4 :as schema4]
            [metcalf.common.utils3 :as utils3]
            [metcalf.common.utils4 :as utils4]))

(defn load-dashboard-document-data
  "Massage document-data in payload and add to app-db."
  [s payload]
  (let [documents (get-in payload [:context :documents])
        data (zipmap (map :uuid documents) documents)]
    (assoc-in s [:db :app/document-data] data)))

(defn save-snapshot-action
  "Save a snapshot of form state.  Can be used to restore state when user cancels out of modal."
  [s form-id]
  (let [snapshots-path (utils4/as-path [:db form-id :snapshots])
        state-path (utils4/as-path [:db form-id :state])
        state-data (get-in s state-path)]
    (update-in s snapshots-path conj state-data)))

(defn discard-snapshot-action
  "Discard the latest snapshot when no longer needed.  e.g. modal is closed without cancelling"
  [s form-id]
  (let [snapshots-path (utils4/as-path [:db form-id :snapshots])]
    (cond-> s
            (seq (get-in s snapshots-path))
            (update-in snapshots-path pop))))

(defn restore-snapshot-action
  "Restore form state from the latest snapshot.  e.g. user makes changes in modal then cancels"
  [s form-id]
  (let [snapshots-path (utils4/as-path [:db form-id :snapshots])
        state-path (utils4/as-path [:db form-id :state])
        state-data (peek (get-in s snapshots-path))]
    (cond-> s
            (seq (get-in s snapshots-path))
            (-> (assoc-in state-path state-data)
                (discard-snapshot-action form-id)))))

(defn unselect-list-item-action
  "Clears the :list-item-selected-idx prop on an array block.  Used to dismiss modals."
  [s form-id data-path]
  (let [block-path (utils4/as-path [:db form-id :state (blocks4/block-path data-path)])]
    (update-in s (conj block-path :props) dissoc :list-item-selected-idx)))

(defn select-user-defined-list-item-action2
  "Set the :list-item-selected-idx prop on an array block.  Does nothing if idx is not a user defined item."
  [s form-id data-path idx added-path]
  (let [block-path (utils4/as-path [:db form-id :state (blocks4/block-path data-path)])
        block-data (get-in s block-path)
        added? (get-in block-data (utils4/as-path [:content idx (blocks4/block-path added-path) :props :value]))]
    (cond-> s
            added?
            (assoc-in (conj block-path :props :list-item-selected-idx) idx))))

(defn select-last-item-action
  "Set the :list-item-selected-idx prop to point at the last item in an array block.  Does nothing if list is empty."
  [s form-id data-path]
  (let [block-path (utils4/as-path [:db form-id :state (blocks4/block-path data-path)])
        last-idx (dec (count (get-in s (conj block-path :content))))]
    (cond-> s
            (not (neg? last-idx))
            (assoc-in (conj block-path :props :list-item-selected-idx) last-idx))))

(defn del-item-action
  "Remove the item at idx from an array block."
  [s form-id data-path idx]
  (let [list-path (utils4/as-path [:db form-id :state (blocks4/block-path data-path) :content])]
    (update-in s list-path utils3/vec-remove idx)))

(defn dialog-open-action
  [s form-id data-path]
  (let [is-open-path (utils4/as-path [:db form-id :state (blocks4/block-path data-path) :props :isOpen])]
    (assoc-in s is-open-path true)))

(defn dialog-close-action
  [s form-id data-path]
  (let [is-open-path (utils4/as-path [:db form-id :state (blocks4/block-path data-path) :props :isOpen])]
    (assoc-in s is-open-path false)))

(defn add-value-action
  [s form-id data-path value]
  (let [schema (get-in s (utils4/as-path [:db form-id :schema (schema4/schema-path data-path) :items]))
        state (blocks4/as-blocks {:schema schema :data value})
        db-path (utils4/as-path [:db form-id :state (blocks4/block-path data-path)])
        items (set (blocks4/as-data (get-in s db-path)))]
    (-> s
        (cond-> (not (contains? items value))
                (update-in (conj db-path :content) conj state))
        ;; TODO: split out?
        (assoc-in (conj db-path :props :show-errors) true))))

(defn add-item-action
  [s form-id data-path value-path data]
  (let [schema (get-in s (utils4/as-path [:db form-id :schema (schema4/schema-path data-path) :items]))
        state (blocks4/as-blocks {:schema schema :data data})
        db-path (utils4/as-path [:db form-id :state (blocks4/block-path data-path)])
        ids (set (map #(get-in % value-path) (blocks4/as-data (get-in s db-path))))]
    (-> s
        (cond-> (not (contains? ids (get-in data value-path)))
                (update-in (conj db-path :content) conj state))
        ;; TODO: split out?
        (assoc-in (conj db-path :props :show-errors) true))))

; TODO: Split out "clear state"
; TODO: Split out "show errors"
(defn set-data-action
  "Replace block state at data-path based on data."
  [s form-id data-path data]
  (let [schema (get-in s (flatten [:db form-id :schema (schema4/schema-path data-path)]))
        state (blocks4/as-blocks {:schema schema :data data})
        db-path (utils4/as-path [:db form-id :state (blocks4/block-path data-path)])]
    (-> s
        (assoc-in db-path state)
        (assoc-in (conj db-path :props :show-errors) true))))

(defn move-item-action
  [s form-id data-path src-idx dst-idx]
  (let [list-path (utils4/as-path [:db form-id :state (blocks4/block-path data-path) :content])
        item (get-in s (conj list-path src-idx))]
    (-> s
        (update-in list-path utils3/vec-remove src-idx)
        (update-in list-path utils3/vec-insert dst-idx item))))

(def genkey-counter (atom 10000))

(defn genkey
  "Generate a unique string.  Helper for resetting stateful components."
  []
  (str ::genkey (swap! genkey-counter inc)))

(defn genkey-action
  [s form-id data-path]
  (let [tick-path (utils4/as-path [:db form-id :state (blocks4/block-path data-path) :props :key])]
    (assoc-in s tick-path (genkey))))

(defn open-modal-action
  [s modal-props]
  (update-in s [:db :modal/stack] (fn [alerts]
                                    (when-not (= (peek alerts) modal-props)
                                      (conj alerts modal-props)))))

(defn close-modal-action
  [s]
  (update-in s [:db :modal/stack] pop))

(def disabled-statuses #{"Archived" "Deleted" "Uploaded"})

(defn load-edit-form-action
  "Massage raw payload for use as app-state"
  [s {:keys [url data schema]}]
  (let [data (schema4/massage-data-payload data)
        schema (schema4/massage-schema-payload schema)
        state (blocks4/as-blocks {:data data :schema schema})
        disabled? (contains? disabled-statuses (get-in s [:db :context :document :status]))]
    (schema4/assert-schema-data {:data data :schema schema})
    (-> s
        (assoc-in [:db :form :url] url)
        (assoc-in [:db :form :data] data)                   ; initial data used for 'is dirty' checks
        (assoc-in [:db :form :schema] schema)               ; data schema used to generate new array items
        (assoc-in [:db :form :state] state)                 ; form state used to hold props/values
        (cond-> disabled?
                (assoc-in [:db :form :state :props :disabled] true)))))

(defn init-create-form-action
  [s create_form]
  (assoc-in s [:db :create_form] (logic4/massage-form create_form)))

(defn create-document-action
  [s url data]
  (update s :fx conj [:app/post-data-fx {:url url :data data :resolve [::-create-document]}]))

(defn clear-errors-action
  [s form-path]
  (let [form-state-path (utils4/as-path [:db form-path :state])]
    (update-in s form-state-path #(blocks4/postwalk blocks4/clear-error-props %))))

(defn set-errors-action
  [s form-path error-map]
  (let [path-errors (utils4/path-vals error-map)
        path (utils4/as-path [:db form-path :state])]
    (update-in s path (fn [state] (reduce (fn [state [path error]]
                                            (blocks4/set-error-prop state path error))
                                          state
                                          path-errors)))))

(defn get-document-data-action
  "Refresh contributor data in modal state"
  [s uuid]
  (update s :fx conj [:app/get-json-fx
                      {:url     (str "/api/document-info/" uuid "/")
                       :resolve [::-get-document-data-action uuid]}]))

(defn upload-attachment
  [s {:keys [config doc-uuid file]}]
  (let [url (str "/upload/" doc-uuid "/")
        data {:document doc-uuid
              :name     (.-name file)
              :file     file}]
    (update s :fx conj [:app/post-multipart-form
                        {:url     url
                         :data    data
                         :resolve [::-upload-attachment config]}])))
