(ns metcalf3.widget.boxmap
  (:require [goog.object :as gobj]
            [interop.blueprint :as bp3]
            [interop.ui :as ui]
            [reagent.core :as r]))

(defn box-map2-fill
  []
  (let [*width (r/atom nil)]
    (fn [props]
      (let [width @*width]
        [bp3/resize-sensor {:onResize #(reset! *width (-> % (aget 0) (gobj/getValueByKeys "contentRect" "width")))}
         (r/as-element [:div (when width [ui/box-map (assoc props :map-width width)])])]))))

