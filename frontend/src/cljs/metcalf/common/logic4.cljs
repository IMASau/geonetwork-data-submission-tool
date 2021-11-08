(ns metcalf.common.logic4
  (:require [metcalf.common.schema4 :as schema4]
            [metcalf.common.blocks4 :as blocks4]))

(defn massage-form
  [{:keys [data schema url]}]
  (let [data (schema4/massage-data-payload data)
        schema (schema4/massage-schema-payload schema)]
    {:data   data
     :schema schema
     :state  (blocks4/as-blocks {:data data :schema schema})
     :url    url}))
