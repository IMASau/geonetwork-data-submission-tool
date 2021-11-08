(ns metcalf4.logic
  (:require [metcalf4.schema :as schema]
            [metcalf4.blocks :as blocks]))

(defn massage-form
  [{:keys [data schema url]}]
  (let [data (schema/massage-data-payload data)
        schema (schema/massage-schema-payload schema)]
    {:data   data
     :schema schema
     :state  (blocks/as-blocks {:data data :schema schema})
     :url    url}))
