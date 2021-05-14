(ns metcalf.dev
  (:require-macros condense.macros)
  (:require [metcalf.core :as core]
            [figwheel.client.heads-up :as heads-up]
            [cljs.core.async :refer [put!]]
            [metcalf.test-runner :refer [test-runner]]))

(enable-console-print!)

(defn test-and-main []
  (let [{:keys [fail error] :as results} (test-runner)]
    (when (> (+ fail error) 0)
      (heads-up/display-system-warning "Tests failed" (str results))))
  (core/main))

(test-and-main)
