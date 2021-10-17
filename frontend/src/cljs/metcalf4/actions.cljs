(ns metcalf4.actions
  (:require [goog.object :as gobj]
            [metcalf3.fx :as fx3]
            [metcalf3.utils :as utils3]
            [metcalf4.blocks :as blocks]
            [metcalf4.schema :as schema]
            [metcalf4.utils :as utils4]))

(defn load-api-action
  [s api-id api-uri]
  (-> s
      (assoc-in [:db :api api-id :uri] api-uri)
      (assoc-in [:db :api api-id :options] nil)
      ; TODO: use js/fetch
      (update :fx conj
              [::fx3/xhrio-get-json {:uri api-uri :resp-v [::-load-api api-id]}])))

(defn -load-api-action
  [s api-id json]
  (let [results (gobj/get json "results")]
    (assoc-in s [:db :api api-id :options] results)))

(defn load-apis-action
  [s payload api-paths]
  (let [URL_ROOT (-> payload :context :URL_ROOT (or ""))]
    (reduce (fn [s [api-id api-path]]
              (load-api-action s api-id (str URL_ROOT api-path)))
            s
            api-paths)))

(defn load-page-action
  [s payload]
  (let [page-name (get-in payload [:page :name])]
    (assoc-in s [:db :page :name] page-name)))

(defn save-snapshot-action
  [s form-id]
  (let [snapshots-path (utils4/as-path [:db form-id :snapshots])
        state-path (utils4/as-path [:db form-id :state])
        state-data (get-in s state-path)]
    (update-in s snapshots-path conj state-data)))

(defn discard-snapshot-action
  [s form-id]
  (let [snapshots-path (utils4/as-path [:db form-id :snapshots])]
    (cond-> s
      (seq (get-in s snapshots-path))
      (update-in snapshots-path pop))))

(defn restore-snapshot-action
  [s form-id]
  (let [snapshots-path (utils4/as-path [:db form-id :snapshots])
        state-path (utils4/as-path [:db form-id :state])
        state-data (peek (get-in s snapshots-path))]
    (cond-> s
      (seq (get-in s snapshots-path))
      (-> (assoc-in state-path state-data)
          (discard-snapshot-action form-id)))))

(defn unselect-list-item-action
  [s form-id data-path]
  (let [block-path (utils4/as-path [:db form-id :state (blocks/block-path data-path)])]
    (update-in s (conj block-path :props) dissoc :selected)))

(defn select-list-item-action
  [s form-id data-path idx]
  (let [block-path (utils4/as-path [:db form-id :state (blocks/block-path data-path)])
        block-data (get-in s block-path)]
    (cond-> s
      (contains? (:content block-data) idx)
      (assoc-in (conj block-path :props :selected) idx))))

(defn select-user-defined-list-item-action2
  "Select item, but only if it's user defined"
  [s form-id data-path idx added-path]
  (let [block-path (utils4/as-path [:db form-id :state (blocks/block-path data-path)])
        block-data (get-in s block-path)
        added? (get-in block-data (utils4/as-path [:content idx (blocks/block-path added-path) :props :value]))]
    (cond-> s
      added?
      (assoc-in (conj block-path :props :selected) idx))))

(defn select-user-defined-list-item-action
  "Select item, but only if it's user defined"
  [s form-id data-path idx addedKey]
  (let [block-path (utils4/as-path [:db form-id :state (blocks/block-path data-path)])
        block-data (get-in s block-path)
        added? (get-in block-data [:content idx :content addedKey :props :value])]
    (cond-> s
      added?
      (assoc-in (conj block-path :props :selected) idx))))

(defn select-last-item-action
  [s form-id data-path]
  (let [block-path (utils4/as-path [:db form-id :state (blocks/block-path data-path)])
        last-idx (dec (count (get-in s (conj block-path :content))))]
    (cond-> s
      (not (neg? last-idx))
      (assoc-in (conj block-path :props :selected) last-idx))))

(defn new-item-action
  [s form-id data-path]
  (let [schema-path (utils4/as-path [:db form-id :schema (schema/schema-path data-path) :items])
        list-path (utils4/as-path [:db form-id :state (blocks/block-path data-path) :content])
        schema (get-in s schema-path)
        new-item (blocks/new-item schema)]
    (update-in s list-path conj new-item)))

(defn del-item-action
  [s form-id data-path idx]
  (let [list-path (utils4/as-path [:db form-id :state (blocks/block-path data-path) :content])]
    (update-in s list-path utils3/vec-remove idx)))

(defn dialog-open-action
  [s form-id data-path]
  (let [is-open-path (utils4/as-path [:db form-id :state (blocks/block-path data-path) :props :isOpen])]
    (assoc-in s is-open-path true)))

(defn dialog-close-action
  [s form-id data-path]
  (let [is-open-path (utils4/as-path [:db form-id :state (blocks/block-path data-path) :props :isOpen])]
    (assoc-in s is-open-path false)))

(defn add-item-action
  [s form-id data-path data]
  (let [schema (get-in s (utils4/as-path [:db form-id :schema (schema/schema-path data-path) :items]))
        state (blocks/as-blocks {:schema schema :data data})
        db-path (utils4/as-path [:db form-id :state (blocks/block-path data-path)])
        items (set (blocks/as-data (get-in s db-path)))]
    (-> s
        (cond-> (not (contains? items data))
                (update-in (conj db-path :content) conj state))
        (assoc-in (conj db-path :props :show-errors) true))))

(defn set-value-action
  [s form-id data-path option]
  (let [schema (get-in s (flatten [:db form-id :schema (schema/schema-path data-path)]))
        state (blocks/as-blocks {:schema schema :data option})
        db-path (utils4/as-path [:db form-id :state (blocks/block-path data-path)])]
    (-> s
        (assoc-in db-path state)
        (assoc-in (conj db-path :props :show-errors) true))))

(defn move-item-action
  [s form-id data-path src-idx dst-idx]
  (let [list-path (utils4/as-path [:db form-id :state (blocks/block-path data-path) :content])
        item (get-in s (conj list-path src-idx))]
    (-> s
        (update-in list-path utils3/vec-remove src-idx)
        (update-in list-path utils3/vec-insert dst-idx item))))

(def genkey-counter (atom 10000))

(defn genkey []
  (str ::genkey (swap! genkey-counter inc)))

(defn genkey-action
  [s form-id data-path]
  (let [tick-path (utils4/as-path [:db form-id :state (blocks/block-path data-path) :props :key])]
    (assoc-in s tick-path (genkey))))

(defn open-modal
  [s modal-props]
  (update-in s [:db :alert] (fn [alerts]
                              (when-not (= (peek alerts) modal-props)
                                (conj alerts modal-props)))))

(defn load-form-action
  "Massage raw payload for use as app-state"
  [s payload]
  (let [data (get-in payload [:form :data])
        schema (get-in payload [:form :schema])
        state (blocks/as-blocks {:data data :schema schema})]
    (schema/assert-schema-data schema data)
    (-> s
        (assoc-in [:db :form :data] data)                   ; initial data used for 'is dirty' checks
        (assoc-in [:db :form :schema] schema)               ; data schema used to generate new array items
        (assoc-in [:db :form :state] state)                 ; form state used to hold props/values
        )))