(ns metcalf.common.logic4
  (:require [metcalf.common.schema4 :as schema]
            [metcalf.common.blocks4 :as blocks]))

(defn massage-form
  [{:keys [data schema url]}]
  (let [data (schema/massage-data-payload data)
        schema (schema/massage-schema-payload schema)]
    {:data   data
     :schema schema
     :state  (blocks/as-blocks {:data data :schema schema})
     :url    url}))
