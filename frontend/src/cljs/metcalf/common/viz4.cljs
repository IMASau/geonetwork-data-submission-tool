(ns metcalf.common.viz4
  (:require [interop.blueprint :as bp3]
            [reagent.core :as r]
            [goog.object :as gobj]
            [metcalf.common.schema4 :as schema4]
            [clojure.string :as string]))

(def *expanded-paths (r/atom #{[]}))
(def *selected-path (r/atom []))

(defn handle-node-expand [node] (swap! *expanded-paths conj (:path (gobj/get node "nodeData"))))
(defn handle-node-collapse [node] (swap! *expanded-paths disj (:path (gobj/get node "nodeData"))))
(defn handle-node-click [node] (reset! *selected-path (:path (gobj/get node "nodeData"))))
(defn handle-breadcrumb-click [path] (reset! *selected-path path))

(defn schema-type
  [{:keys [type items]}]
  (if (= type "array")
    (str "array of " (:type items) "s")
    (or type "unknown")))

(defn schema-child-node
  [{:keys [schema path]}]
  (let [{:keys [type items properties]} schema]
    (case type
      "object" (into-array
                 (for [[k m] properties]
                   (let [path (conj path k)]
                     #js {:key        (name k)
                          :label      (str k " (" (schema-type m) ")")
                          :isExpanded (contains? @*expanded-paths path)
                          :isSelected (= @*selected-path path)
                          :nodeData   {:path path}
                          :childNodes (schema-child-node {:schema m :path path})})))
      "array" (schema-child-node {:schema items :path path})
      nil)))

(defn schema-meta
  [{:keys [schema]}]
  (let [{:keys [items properties type]} schema]
    [:dl
     [:<> [:dt "type"] [:dd (or type "unknown")]]
     (when items
       [:<> [:dt "items"] [:dd (schema-type items)]])
     (when properties
       [:<> [:dt "properties"] [:dd (string/join ", " (map name (keys properties)))]])
     (for [[k v] (dissoc schema :items :properties :type)]
       [:<> [:dt (name k)] [:dd (str v)]])]))

(defn schema-viz
  [{:keys [schema]}]
  (let [path []
        child-nodes (filterv some? (schema-child-node {:schema schema :path path}))
        selected-path @*selected-path]
    [:div
     [:pre (with-out-str (cljs.pprint/pprint {:selected-path  selected-path
                                              :expanded-paths @*expanded-paths}))]


     [:div {:style {:display "flex" :flex-direction "row"}}

      [:div {:style {:flex 1}}
       [bp3/tree
        {:onNodeExpand   handle-node-expand
         :onNodeCollapse handle-node-collapse
         :onNodeClick    handle-node-click
         :contents       [#js {:label      (str "root (" (schema-type schema) ")")
                               :isExpanded (contains? @*expanded-paths path)
                               :isSelected (= @*selected-path path)
                               :nodeData   {:path path}
                               :childNodes (into-array child-nodes)}]}]]
      [:div {:style {:flex 2}}
       [:div.bp3-card
        [bp3/breadcrumbs
         {:items (into [{:text    "root"
                         :onClick (handle-breadcrumb-click [])}]
                       (for [n (range (count selected-path))]
                         (let [s (nth selected-path n)
                               path (vec (take (inc n) selected-path))]
                           {:text    s
                            :onClick (handle-breadcrumb-click path)})))}]
        (when selected-path
          (let [schema (get-in schema (schema4/schema-path selected-path))]
            [schema-meta {:schema schema}]))]]]]))
