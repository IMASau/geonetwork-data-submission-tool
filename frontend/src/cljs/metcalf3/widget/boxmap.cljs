(ns metcalf3.widget.boxmap
  (:require [goog.object :as gobj]
            [interop.blueprint :as bp3]
            [interop.react-leaflet :as react-leaflet]
            [oops.core]
            [reagent.core :as r]
            [metcalf3.utils :as utils]
            [cljs.spec.alpha :as s]))

(s/def ::northBoundLatitude number?)
(s/def ::westBoundLongitude number?)
(s/def ::southBoundLatitude number?)
(s/def ::eastBoundLongitude number?)
(s/def ::element (s/keys :req-un [::northBoundLatitude ::westBoundLongitude ::southBoundLatitude ::eastBoundLongitude]))
(s/def ::elements (s/coll-of ::element))

;;; http://blog.jayfields.com/2011/01/clojure-select-keys-select-values-and.html
(defn map->bounds [{:keys [west south east north]}]
  [[south west]
   [north east]])

(defn elements->extents
  [elements]
  (s/assert ::elements elements)
  (let [north (apply max (remove nil? (map :northBoundLatitude elements)))
        west (apply min (remove nil? (map :westBoundLongitude elements)))
        south (apply min (remove nil? (map :southBoundLatitude elements)))
        east (apply max (remove nil? (map :eastBoundLongitude elements)))]
    (when (and north west south east)
      {:north north :west west :east east :south south})))

(defn fg->geojson [fg]
  (react-leaflet/to-geojson (react-leaflet/leaflet-element fg)))

; TODO: dispatch on change

(defn box-map2
  [_]
  (let [*fg (atom nil)]
    (fn [{:keys [elements map-width tick-id on-change]}]
      (s/assert ::elements elements)
      (s/assert pos? map-width)
      (s/assert number? tick-id)
      (s/assert fn? on-change)
      (let [extents (elements->extents elements)
            base-layer [react-leaflet/tile-layer
                        {:url         "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                         :attribution "&copy; <a href=&quot;http://osm.org/copyright&quot;>OpenStreetMap</a> contributors"}]]
        (letfn [(handle-change []
                  (on-change (fg->geojson @*fg)))]

          [:div.map-wrapper
           [react-leaflet/map (merge
                                {;:crs                  (gget "L.CRS.EPSG4326")
                                 :id                   "map"
                                 :style                {:height 500 :width map-width}
                                 :use-fly-to           true
                                 :center               [-28 134]
                                 :zoom                 4
                                 :keyboard             false ; handled externally
                                 :close-popup-on-click false ; We'll handle that ourselves
                                 }
                                (when extents
                                  (let [bounds (map->bounds extents)
                                        is-point? (= (first bounds) (second bounds))]
                                    (if (not is-point?)
                                      {:bounds bounds}
                                      {:center (first bounds)}))))

            base-layer
            (into [react-leaflet/feature-group
                   {:ref #(reset! *fg %)
                    :key (str "feature-group" tick-id)}
                   [react-leaflet/edit-control
                    {:position   "topright"
                     :draw       {:polyline     false
                                  :polygon      false
                                  :rectangle    {}
                                  :circle       false
                                  :marker       {}
                                  :circlemarker false}
                     :edit       {:edit   {}
                                  :remove {}
                                  :poly   {}}
                     :on-edited  handle-change
                     :on-deleted handle-change
                     :on-created handle-change}]]
                  (for [{:keys [northBoundLatitude southBoundLatitude eastBoundLongitude westBoundLongitude]} elements
                        :when (and northBoundLatitude southBoundLatitude eastBoundLongitude westBoundLongitude)]
                    (if (and (= northBoundLatitude southBoundLatitude)
                             (= eastBoundLongitude westBoundLongitude))
                      [react-leaflet/marker
                       {:position [southBoundLatitude westBoundLongitude]}]
                      [react-leaflet/rectangle
                       {:bounds [[southBoundLatitude westBoundLongitude]
                                 [northBoundLatitude eastBoundLongitude]]}])))]])))))

(defn box-map2-fill
  []
  (let [*width (r/atom nil)]
    (fn [props]
      (let [width @*width]
        [bp3/resize-sensor {:onResize #(reset! *width (-> % (aget 0) (gobj/getValueByKeys "contentRect" "width")))}
         (r/as-element [:div (when width [box-map2 (assoc props :map-width width)])])]))))

