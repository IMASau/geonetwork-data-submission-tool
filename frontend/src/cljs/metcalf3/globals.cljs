(ns metcalf3.globals
  (:require [metcalf3.logic :refer [derived-state]]
            [re-frame.db :refer [app-db]]))

(defonce derived-db (atom {}))

(add-watch
  app-db :derive-db
  (fn [k r o n]
    (when-not (= o n)
      (reset! derived-db (derived-state n)))))
