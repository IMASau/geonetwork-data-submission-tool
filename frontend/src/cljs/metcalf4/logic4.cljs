(ns metcalf4.logic4
  (:require [metcalf4.schema4 :as schema]
            [metcalf4.blocks4 :as blocks]))

(defn massage-form
  [{:keys [data schema url]}]
  (let [data (schema/massage-data-payload data)
        schema (schema/massage-schema-payload schema)]
    {:data   data
     :schema schema
     :state  (blocks/as-blocks {:data data :schema schema})
     :url    url}))
