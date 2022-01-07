(ns metcalf.common.viz4
  (:require [interop.blueprint :as bp3]
            [reagent.core :as r]
            [goog.object :as gobj]))

(def *expanded-paths (r/atom #{}))
(def *selected-path (r/atom nil))

(defn handle-node-expand [node]
  (swap! *expanded-paths conj (:path (gobj/get node "nodeData"))))

(defn handle-node-collapse [node]
  (swap! *expanded-paths disj (:path (gobj/get node "nodeData"))))

(defn handle-node-click [node]
  (reset! *selected-path (:path (gobj/get node "nodeData"))))

(defn schema-type
  [{:keys [type items]}]
  (if (= type "array")
    (str "array of " (:type items) "s")
    (or type "unknown")))

(defn schema-child-node
  [{:keys [schema path]}]
  (js/console.log ::schema-child-node.schema schema)
  (let [{:keys [type items properties]} schema]
    (case type
      "object" (into-array
                 (for [[k m] properties]
                   (let [path (into path k)]
                     #js {:key        (name k)
                          :label      (str k " (" (schema-type m) ")")
                          :isExpanded (contains? @*expanded-paths path)
                          :isSelected (= @*selected-path path)
                          :nodeData   {:path path}
                          :childNodes (schema-child-node {:schema m :path path})})))
      "array" (schema-child-node {:schema items :path path})
      nil)))

(defn schema-viz
  [{:keys [schema]}]
  (let [path []
        child-nodes (filterv some? (schema-child-node {:schema schema :path path}))]
    [bp3/tree
     {:onNodeExpand   handle-node-expand
      :onNodeCollapse handle-node-collapse
      :onNodeClick    handle-node-click
      :contents       [#js {:label      (schema-type schema)
                            :isExpanded (contains? @*expanded-paths path)
                            :nodeData   {:path path}
                            :childNodes (into-array child-nodes)}]}]))
