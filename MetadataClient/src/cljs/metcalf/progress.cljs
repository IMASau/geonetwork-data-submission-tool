(ns metcalf.progress
  "Logic and procedures for validating and checking progress"
  (:require [condense.utils :refer [fmap]]
            [clojure.zip :as zip]
            [om-tick.field :refer [field-zipper field?]]))


(defn has-value? [{:keys [many value]}]
  (cond
    many (not (empty? value))
    (nil? value) false
    (string? value) (not (empty? value))
    :else value))


(defn process-node [{:keys [errors page required disabled] :as field} counter]
  (if-not disabled
    (let [errors? (not-empty errors)]
      (cond-> counter
        true (update :fields inc)
        (not (has-value? field)) (update :empty inc)
        errors? (update :errors inc)
        required (update :required inc)
        (and errors? required) (update :required-errors inc)
        (and errors? page) (update-in [:page-errors page] inc)))
    counter))


(defn progress-score
  [state]
  (let [zipper (field-zipper state)]
    (loop [loc zipper
           counter {:fields 0
                    :empty 0
                    :errors 0
                    :required 0}]
      (if (zip/end? loc)
        counter
        (let [node (zip/node loc)]
          (recur (zip/next loc) (if (field? node)
                                  (process-node node counter)
                                  counter)))))))

