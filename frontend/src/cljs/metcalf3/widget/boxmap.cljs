(ns metcalf3.widget.boxmap
  (:require [cljs.spec.alpha :as s]
            [goog.object :as gobj]
            [interop.blueprint :as bp3]
            [interop.ui :as ui]
            [reagent.core :as r]))

(s/def ::northBoundLatitude number?)
(s/def ::westBoundLongitude number?)
(s/def ::southBoundLatitude number?)
(s/def ::eastBoundLongitude number?)
(s/def ::element (s/keys :req-un [::northBoundLatitude ::westBoundLongitude ::southBoundLatitude ::eastBoundLongitude]))
(s/def ::elements (s/coll-of ::element))

(defn box-map2
  [{:keys [elements map-width tick-id on-change]}]
  [ui/box-map
   {:elements (s/assert ::elements elements)
    :mapWidth (s/assert pos? map-width)
    :tickId   (s/assert number? tick-id)
    :onChange (s/assert fn? (fn [geojson] (on-change (js->clj geojson :keywordize-keys true))))}])

(defn box-map2-fill
  []
  (let [*width (r/atom nil)]
    (fn [props]
      (let [width @*width]
        [bp3/resize-sensor {:onResize #(reset! *width (-> % (aget 0) (gobj/getValueByKeys "contentRect" "width")))}
         (r/as-element [:div (when width [box-map2 (assoc props :map-width width)])])]))))

