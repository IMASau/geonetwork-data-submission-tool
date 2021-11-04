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

(defn get-all-parents
  "This helper allows for value to appear in tree multiple times"
  [value-key value options]
  (->> options
       (filter #(is-selected? value-key value %))
       (mapcat #(node-parents options %))))

(defn render-tree-term-option
  [{:keys [option is-selected is-expandable is-expanded toggle-option select-option display-key]}]
  (let [{:keys [depth is_selectable lft rgt]} option]
    [:div.tree-term-item
     {:class (str (when is-selected " active")
                  (when is_selectable " is-selectable")
                  (when is-expanded " is-expanded"))}

     (cond

       (and is-expandable is-expanded)
       [:div.expander
        {:on-click #(toggle-option option)
         :style    {:margin-left (str (- depth 1) "em")}}
        [:span.glyphicon.glyphicon-triangle-bottom]]

       (and is-expandable (not is-expanded))
       [:div.expander
        {:on-click #(toggle-option option)
         :style    {:margin-left (str (- depth 1) "em")}}
        [:div.glyphicon.glyphicon-triangle-right]]

       :else
       [:div.expander
        {:style {:margin-left (str (- depth 1) "em")}}
        [:div.glyphicon.glyphicon-file
         {:style {:visibility "hidden"}}]])

     [:div.term
      {:on-click (when (:is_selectable option) #(select-option option))}
      [:span (display-key option)]]

     [:div.children_count
      (when is-expandable
        [:span.badge (/ (- rgt lft 1) 2)])]]))