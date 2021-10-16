(ns metcalf.imas.handlers
  (:require [metcalf3.logic :as logic3]))

(defn init-db
  [_ [_ payload]]
  (let [db' (logic3/initial-state payload)
        db' (update db' :api #(dissoc % :parametername :parameterunit :parameterinstrument
                                      :horizontalResolution :parameterplatform :topiccategory))]
    {:db         db'
     :fx         [[:ui/setup-blueprint]]
     ; TODO: use action
     :dispatch-n (for [api-key (keys (get db' :api))]
                   [::-init-db-load-api-options [:api api-key]])}))
