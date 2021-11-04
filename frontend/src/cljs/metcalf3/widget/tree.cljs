(ns metcalf3.widget.tree
  (:require [reagent.core :as r]))

(defn node-parent?
  ([{:keys [lft rgt tree_id]} parent]
   (and (= (:tree_id parent) tree_id)
        (< (:lft parent) lft)
        (> (:rgt parent) rgt))))

(defn node-parents
  [nodes node]
  (->> nodes
       (filter (partial node-parent? node))
       (sort-by :lft)))

(defn is-selected?
  "This helper allows for value to appear in tree multiple times"
  [value-key value option]
  (= (get value value-key)
     (get option value-key)))