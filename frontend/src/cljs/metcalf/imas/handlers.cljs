(ns metcalf.imas.handlers
  (:require [metcalf3.logic :as logic3]
            [metcalf4.schema :as schema]
            [metcalf4.actions :as actions]))

(defn init-db
  [_ [_ payload]]
  (let [db' (logic3/initial-state payload)
        db (update db' :api #(dissoc % :api/ternparameters :api/qudtunits :api/terninstruments
                                     :api/horizontalresolution :api/ternplatforms :api/topiccategory))]
    (schema/assert-schema-data (:form db))
    (-> {:db         db
         :fx         [[:ui/setup-blueprint]]
         ; TODO: use action
         :dispatch-n (for [api-key (keys (get db :api))]
                       [::-init-db-load-api-options [:api api-key]])}
        (actions/init-create-form-action))))
