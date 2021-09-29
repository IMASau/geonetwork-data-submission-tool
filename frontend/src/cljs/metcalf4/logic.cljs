(ns metcalf4.logic
  (:require [metcalf4.blocks :as blocks]))

(defn initial-state
  "Massage raw payload for use as app-state"
  [payload]
  (let [data (get-in payload [:form :data])
        schema (get-in payload [:form :schema])
        state (blocks/as-blocks {:data data :schema schema})]
    {:form {:data   data                                    ; initial data used for 'is dirty' checks
            :schema schema                                  ; data schema used to generate new array items
            :state  state                                   ; form state used to hold props/values
            }}))
