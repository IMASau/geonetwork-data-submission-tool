(ns ^:figwheel-always metcalf.test-runner
  (:require [cemerick.cljs.test :refer [run-all-tests]]
    ; NOTE: require test namespaces
          #_metcalf.test-logic
          #_metcalf.test-core))


(defn test-runner
  "Run tests.  Return true if there are no failures or errors."
  []
  (run-all-tests #"(metcalf|condense).*"))