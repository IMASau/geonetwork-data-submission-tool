(ns metcalf3.widget.tree
  (:require [reagent.core :as r]))

(defn parent-paths [path]
  (take-while seq (rest (iterate drop-last path))))

(defn default-render-option
  [{:keys [option is-expandable is-expanded toggle-option select-option]} _]
  (let [{:keys [path label]} option]
    [:div.tree-option
     {:style {:margin-left (str (count path) "em")}}
     (if is-expandable
       [(if is-expanded
          :span.glyphicon.glyphicon-triangle-bottom
          :span.glyphicon.glyphicon-triangle-right)
        {:style    {:cursor "pointer"}
         :on-click #(toggle-option option)}]
       [:span.glyphicon.glyphicon-file {:style {:visibility "hidden"}}])
     [:span.tree-label
      {:style    {:margin-left "0.2em"}
       :on-click #(select-option option)}] label]))

(defn Tree
  [{:keys [options on-select render-option]
    :or   {render-option default-render-option}}]
  (letfn [(init-state [this]
            {:expanded #{}})
          (render [this]
            (let [{:keys [expanded]} (r/state this)]
              (let [toggle-option (fn [{:keys [path]}]
                                    (if (contains? expanded path)
                                      (r/set-state this {:expanded (disj expanded path)})
                                      (r/set-state this {:expanded (conj expanded path)})))
                    expandable (into #{} (map (comp drop-last :path) options))
                    visible? #(every? expanded (parent-paths (:path %)))
                    visible-options (filter visible? options)]
                [:div
                 [:h2 "Tree"]
                 (for [{:keys [path] :as option} visible-options]
                   (render-option
                     {:option        option
                      :toggle-option toggle-option
                      :select-option on-select
                      :is-expandable (contains? expandable path)
                      :is-expanded   (contains? expanded path)}))])))]
    (r/create-class
      {:get-initial-state init-state
       :render            render})))

(defn matches
  ([m1]
   (partial matches m1))
  ([m1 m2]
   (reduce-kv
     (fn [t k v] (and t (= (get m2 k ::not-found) v)))
     true
     m1)))

(defn descendant-count
  [{:keys [lft rgt]}]
  (- rgt lft 1))

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

(defn BaseTermTree
  []
  (letfn [(init-state [this]
            (let [{:keys [value options value-key] :as props} (r/props this)
                  expanded-parents (get-all-parents value-key value options)
                  default-visible (if value (conj expanded-parents value)
                                            expanded-parents)
                  all-visible (set (apply concat (map (fn [y] (filter (fn [x] (and (= (:tree_id x) (:tree_id y))
                                                                                   (= (:depth x) (+ 1 (:depth y)))
                                                                                   (> (:lft x) (:lft y))
                                                                                   (< (:rgt x) (:rgt y)))) options)) expanded-parents)))
                  expanded (set expanded-parents)
                  visible (set (concat (filter (fn [x] (= (:depth x) 1)) options) default-visible all-visible))]
              {:expanded expanded
               :visible  visible}))
          (render [this]
            (let [{:keys [expanded visible]} (r/state this)
                  {:keys [value options value-key render-menu on-select visible-options]} (r/props this)]
              (let [toggle-option (fn [option]
                                    (let [{:keys [lft rgt tree_id depth]} option]
                                      (if (contains? expanded option)
                                        ;off
                                        (r/set-state this {:expanded (disj expanded option)
                                                           :visible  (set (filter (fn [x] (or (<= (:depth x) depth)
                                                                                              (not= (= (:tree_id x) tree_id))
                                                                                              (< (:lft x) lft)
                                                                                              (> (:rgt x) rgt))) visible))})
                                        ;on
                                        (r/set-state this {:expanded (conj expanded option)
                                                           :visible  (set (concat visible (apply concat (map (fn [y] (filter (fn [x] (and (= (:tree_id x) (:tree_id y) tree_id)
                                                                                                                                          (= (:depth x) (+ (:depth y) 1))
                                                                                                                                          (> (:lft x) (:lft y))
                                                                                                                                          (< (:rgt x) (:rgt y)))) options)) (conj expanded option)))))}))))
                    visible-options (doall (->> visible
                                                (sort-by (juxt :tree_id :lft))))]
                (render-menu
                  {:value           value
                   :value-key       value-key
                   :options         options
                   :expanded        expanded
                   :select-option   on-select
                   :toggle-option   toggle-option
                   :visible-options visible-options}))))]
    (r/create-class
      {:get-initial-state init-state
       :render            render})))

(defn BaseTermList
  []
  (letfn [(init-state [this]
            (let [{:keys [value options value-key]} (r/props this)]
              {:expanded (set (get-all-parents value-key value options))}))
          (render [this]
            (let [{:keys [expanded]} (r/state this)
                  {:keys [value options value-key render-menu on-select display-key]} (r/props this)]
              (let [toggle-option (fn [option]
                                    (if (contains? expanded option)
                                      (r/set-state this {:expanded (disj expanded option)})
                                      (r/set-state this {:expanded (conj expanded option)})))
                    visible? #(every? expanded (node-parents options %))
                    visible-options (filter visible? options)]
                (render-menu
                  {:value           value
                   :value-key       value-key
                   :display-key     display-key
                   :options         options
                   :expanded        expanded
                   :select-option   on-select
                   :toggle-option   toggle-option
                   :visible-options visible-options}))))]
    (r/create-class
      {:get-initial-state init-state
       :render            render})))

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
      (when (and is-expandable)
        [:span.badge (/ (- rgt lft 1) 2)])]]))

(defn render-tree-term-menu
  [{:keys [value value-key expanded select-option toggle-option visible-options display-key]}]
  [:div.pre-scrollable
   [:div
    (into
      [:div.tree-term]
      (for [option visible-options]
        (render-tree-term-option
          {:option        option
           :display-key   (or display-key :Name)
           :is-selected   (is-selected? value-key value option)
           :toggle-option toggle-option
           :select-option select-option
           :is-expandable (> (descendant-count option) 0)
           :is-expanded   (contains? expanded option)})))]])

(defn TermTree
  [props _]
  [BaseTermTree (assoc props
                  :render-menu render-tree-term-menu
                  :render-option render-tree-term-option)])

(defn TermList
  [props _]
  [BaseTermList (assoc props
                  :render-menu render-tree-term-menu
                  :render-option render-tree-term-option)])

