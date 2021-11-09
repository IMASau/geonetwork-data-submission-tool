(ns metcalf.imas.handlers
  (:require [metcalf.common.logic3 :as logic3]
            [metcalf.common.schema4 :as schema4]
            [metcalf.common.actions4 :as actions4]))

(defn init-db
  [_ [_ payload]]
  (let [db' (logic3/initial-state payload)
        db (update db' :api #(dissoc % :api/ternparameters :api/qudtunits :api/terninstruments
                                     :api/horizontalresolution :api/ternplatforms :api/topiccategory))]
    (schema4/assert-schema-data (:form db))
    (-> {:db         db
         :fx         [[:ui/setup-blueprint]]
         ; TODO: use action
         :dispatch-n (for [api-key (keys (get db :api))]
                       [::-init-db-load-api-options [:api api-key]])}
        (actions4/init-create-form-action payload))))
