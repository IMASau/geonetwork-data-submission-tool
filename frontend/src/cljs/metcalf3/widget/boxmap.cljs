(ns metcalf3.widget.boxmap
  (:require [goog.object :as gobj]
            [interop.blueprint :as bp3]
            [interop.react-leaflet :as react-leaflet]
            [oops.core]
            [reagent.core :as r]))

;;; http://blog.jayfields.com/2011/01/clojure-select-keys-select-values-and.html
(defn map->bounds [{:keys [west south east north]}]
  [[south west]
   [north east]])

(defn geographicElement->point [{:keys [southBoundLatitude westBoundLongitude]}]
  [(:value southBoundLatitude) (:value westBoundLongitude)])

(defn geographicElement->point?
  [{:keys [northBoundLatitude southBoundLatitude eastBoundLongitude westBoundLongitude]}]
  (and (= (:value southBoundLatitude) (:value northBoundLatitude))
       (= (:value westBoundLongitude) (:value eastBoundLongitude))))

(defn geographicElement->bounds [{:keys [northBoundLatitude southBoundLatitude eastBoundLongitude westBoundLongitude]}]
  [[(:value southBoundLatitude) (:value westBoundLongitude)]
   [(:value northBoundLatitude) (:value eastBoundLongitude)]])

(defn boxes->extents [boxes]
  (let [north (apply max (remove nil? (map (comp :value :northBoundLatitude :value) (:value boxes))))
        west (apply min (remove nil? (map (comp :value :westBoundLongitude :value) (:value boxes))))
        south (apply min (remove nil? (map (comp :value :southBoundLatitude :value) (:value boxes))))
        east (apply max (remove nil? (map (comp :value :eastBoundLongitude :value) (:value boxes))))
        map-extents {:north north :west west :east east :south south}]
    map-extents))


(defn fg->data [fg]
  (let [leaflet-element (react-leaflet/leaflet-element fg)
        data (react-leaflet/to-geojson leaflet-element)
        geometries (mapv :geometry (:features data))]
    (for [{:keys [type coordinates]} geometries]
      (case type
        "Point" (let [[lng lat] coordinates]
                  {:northBoundLatitude {:value lat}
                   :southBoundLatitude {:value lat}
                   :eastBoundLongitude {:value lng}
                   :westBoundLongitude {:value lng}})
        "Polygon" (let [[rect] coordinates
                        lngs (map first rect)
                        lats (map second rect)]
                    {:northBoundLatitude {:value (apply max lats)}
                     :southBoundLatitude {:value (apply min lats)}
                     :eastBoundLongitude {:value (apply max lngs)}
                     :westBoundLongitude {:value (apply min lngs)}})))))

(defn boxes->elements
  [boxes]
  (for [box (:value boxes)]
    (when (not (some nil? (map :value (vals (:value box)))))
      (if (geographicElement->point? (:value box))
        [react-leaflet/marker
         {:position (geographicElement->point (:value box))}]
        [react-leaflet/rectangle
         {:bounds (geographicElement->bounds (:value box))}]))))

; TODO: dispatch on change

(defn box-map2
  [_]
  (let [*fg (atom nil)]
    (fn [{:keys [boxes map-width tick-id on-change]}]
      (let [extents (boxes->extents boxes)
            base-layer [react-leaflet/tile-layer
                        {:url         "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                         :attribution "&copy; <a href=&quot;http://osm.org/copyright&quot;>OpenStreetMap</a> contributors"}]
            initial-elements (boxes->elements boxes)]
        (letfn [(handle-change []
                  (on-change (fg->data @*fg)))]

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
                                (when (not (some nil? (vals extents)))
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
                  initial-elements)]])))))

(defn box-map2-fill
  []
  (let [*width (r/atom nil)]
    (fn [props]
      (let [width @*width]
        [bp3/resize-sensor {:onResize #(reset! *width (-> % (aget 0) (gobj/getValueByKeys "contentRect" "width")))}
         (r/as-element [:div (when width [box-map2 (assoc props :map-width width)])])]))))

