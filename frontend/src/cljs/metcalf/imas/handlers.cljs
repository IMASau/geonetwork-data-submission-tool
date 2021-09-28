(ns metcalf.imas.handlers
  (:require [metcalf3.logic :as logic3]))

(defn init-db
  [_ _]
  (let [payload (js->clj (aget js/window "payload") :keywordize-keys true)
        db' (logic3/initial-state payload)
        db' (update db' :api #(dissoc % :parametername :parameterunit :parameterinstrument
                                      :horizontalResolution :parameterplatform :topiccategory))]
    {:db         db'
     :dispatch-n (for [api-key (keys (get db' :api))]
                   [:handlers/load-api-options [:api api-key]])}))
