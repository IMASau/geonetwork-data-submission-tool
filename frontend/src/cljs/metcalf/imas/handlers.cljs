(ns metcalf.imas.handlers
  (:require [metcalf3.logic :as logic]))

(defn init-db
  [_ _]
  (let [payload (js->clj (aget js/window "payload") :keywordize-keys true)
        db' (logic/initial-state payload)
        db' (update db' :api #(dissoc % :parametername :parameterunit :parameterinstrument :parameterplatform))]
    {:db         db'
     :dispatch-n (for [api-key (keys (get db' :api))]
                   [:handlers/load-api-options [:api api-key]])}))
