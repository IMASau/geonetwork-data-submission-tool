(ns metcalf.common.viz4
  (:require [interop.blueprint :as bp3]
            [reagent.core :as r]
            [goog.object :as gobj]
            [metcalf.common.schema4 :as schema4]
            [clojure.string :as string]
            [metcalf.common.rules4 :as rules4]))

(defn data-path->schema-nav-path
  "Translate a data-path into a path into the schema"
  [data-path]
  (filterv int? data-path))

(defn schema-type
  [{:keys [type items]}]
  (if (= type "array")
    (if-let [t (:type items)]
      (str "array of " t "s")
      (str "array"))
    (or type "unknown")))

(defn schema-child-node
  [ctx {:keys [schema path]}]
  (let [{:keys [expanded-paths selected-path]} ctx
        {:keys [type items properties]} schema]
    (case type
      "object" (into-array
                 (for [[k m] properties]
                   (let [path (conj path k)]
                     #js {:key        (name k)
                          :label      (str k " (" (schema-type m) ")")
                          :isExpanded (contains? expanded-paths path)
                          :isSelected (= selected-path path)
                          :nodeData   {:path path}
                          :childNodes (schema-child-node ctx {:schema m :path path})})))
      "array" (schema-child-node ctx {:schema items :path path})
      nil)))

(defn schema-meta
  [{:keys [schema handle-property-click]}]
  (let [{:keys [type items properties rules]} schema]
    [:dl
     [:<> [:dt "type"] [:dd (pr-str type)]]
     (when items
       [:<> [:dt "items"] [:dd (schema-type items)
                           (when (= "object" (:type items))
                             [:div (let [properties (:properties items)]
                                     (interpose " " (map (fn [k] [:span.bp3-button.bp3-minimal {:on-click #(handle-property-click k)} (name k)]) (keys properties))))])]])
     (when properties
       [:<> [:dt "properties"] [:dd (interpose " " (map (fn [k] [:span.bp3-button.bp3-minimal {:on-click #(handle-property-click k)} (name k)]) (keys properties)))]])
     (for [[k v] (dissoc schema :items :properties :type :rules)]
       [:<> [:dt (name k)] [:dd (pr-str v)]])
     (when-let [rules (rules4/seq-rules rules)]
       [:<> [:dt "rules"]
        [:dd [:table.bp3-html-table
              [:tbody
               [:<> (for [rule rules]
                      [:tr
                       [:th (pr-str (key rule))]
                       [:td (pr-str (val rule))]])]]]]])]))

(defn schema-viz
  [{:keys [schema expanded-paths selected-path handle-node-expand handle-node-collapse handle-node-click handle-breadcrumb-click handle-property-click]}]
  (let [path []
        child-nodes (filterv some? (schema-child-node {:expanded-paths expanded-paths :selected-path selected-path} {:schema schema :path path}))]
    [:div {:style {:display "flex" :flex-direction "row"}}

     [:div {:style {:flex 1}}
      [bp3/tree
       {:onNodeExpand   handle-node-expand
        :onNodeCollapse handle-node-collapse
        :onNodeClick    handle-node-click
        :contents       [#js {:label      (str "root (" (schema-type schema) ")")
                              :isExpanded (contains? expanded-paths path)
                              :isSelected (= selected-path path)
                              :nodeData   {:path path}
                              :childNodes (into-array child-nodes)}]}]]
     [:div {:style {:flex 2}}
      [:div.bp3-card
       [bp3/breadcrumbs
        {:items (into [{:key     "-1"
                        :text    "root"
                        :onClick #(handle-breadcrumb-click [])}]
                      (for [n (range (count selected-path))]
                        (let [s (nth selected-path n)
                              path (vec (take (inc n) selected-path))]
                          {:key     (str n)
                           :text    s
                           :onClick #(handle-breadcrumb-click path)})))}]
       (when selected-path
         (let [schema (get-in schema (schema4/schema-path selected-path))]
           [schema-meta {:schema schema :handle-property-click #(handle-property-click (conj selected-path %))}]))]]]))


(defn reagent-schema-viz
  [{:keys [initial-selected-path]}]
  (let [path (or initial-selected-path [])
        *expanded-paths (r/atom #{[]})
        *selected-path (r/atom path)]
    (when (seq (butlast path))
      (swap! *expanded-paths conj (butlast path)))
    (letfn [(handle-node-expand [node] (swap! *expanded-paths conj (:path (gobj/get node "nodeData"))))
            (handle-node-collapse [node] (swap! *expanded-paths disj (:path (gobj/get node "nodeData"))))
            (handle-node-click [node]
              (swap! *expanded-paths conj (:path (gobj/get node "nodeData")))
              (reset! *selected-path (:path (gobj/get node "nodeData"))))
            (handle-breadcrumb-click [path] (reset! *selected-path path))
            (handle-property-click [path]
              (when (seq (butlast path))
                (swap! *expanded-paths conj (butlast path)))
              (reset! *selected-path path))]
      (fn [{:keys [schema]}]
        (let [selected-path @*selected-path
              expanded-paths @*expanded-paths]
          [schema-viz {:schema                  schema
                       :expanded-paths          expanded-paths
                       :selected-path           selected-path
                       :handle-node-expand      handle-node-expand
                       :handle-node-collapse    handle-node-collapse
                       :handle-node-click       handle-node-click
                       :handle-breadcrumb-click handle-breadcrumb-click
                       :handle-property-click   handle-property-click}])))))
