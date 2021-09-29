(ns metcalf4.actions
  (:require [metcalf4.blocks :as blocks]
            [goog.object :as gobj]))

(defn load-form-action
  "Massage raw payload for use as app-state"
  [s payload]
  (let [data (get-in payload [:form :data])
        schema (get-in payload [:form :schema])
        state (blocks/as-blocks {:data data :schema schema})]
    (-> s
        (assoc-in [:db :form :data] data)                   ; initial data used for 'is dirty' checks
        (assoc-in [:db :form :schema] schema)               ; data schema used to generate new array items
        (assoc-in [:db :form :state] state)                 ; form state used to hold props/values
        )))

(defn load-api-action
  [s api-id api-uri]
  (-> s
      (assoc-in [:db :api api-id :uri] api-uri)
      (assoc-in [:db :api api-id :options] nil)
      (update :fx conj
              [:xhrio/get-json {:uri api-uri :resp-v [::-load-api api-id]}])))

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

