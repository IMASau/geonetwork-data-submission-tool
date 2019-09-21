(ns metcalf3.widget.boxmap
  (:require [clojure.data :refer [diff]]
            [metcalf3.utils :as utils]
            [re-frame.core :as rf]
            [interop.react-leaflet :as react-leaflet]
            [oops.core :refer [ocall oget gget]]
            [reagent.core :as r]))

;;; http://blog.jayfields.com/2011/01/clojure-select-keys-select-values-and.html
(defn select-values [map ks]
  (reduce #(conj %1 (map %2)) [] ks))

(defn bounds->map [bounds]
  {:north (ocall bounds :getNorth)
   :south (ocall bounds :getSouth)
   :east  (ocall bounds :getEast)
   :west  (ocall bounds :getWest)})

(defn bounds->geojson [{:keys [north south east west]}]
  {:type        "Polygon"
   :coordinates [[[west south] [west north] [east north] [east south] [west south]]]})

(defn map->bounds [{:keys [west south east north] :as bounds}]
  [[south west]
   [north east]])

(defn geographicElement->bounds [{:keys [northBoundLatitude southBoundLatitude eastBoundLongitude westBoundLongitude] :as bounds}]
  (js/console.log "DFSDFSDFSDF" {:bounds bounds})
  [[(:value southBoundLatitude) (:value westBoundLongitude)]
   [(:value northBoundLatitude) (:value eastBoundLongitude)]])

(defn point->latlng [[x y]] {:lat y :lng x})

(defn point-distance [[x1 y1 :as p1] [x2 y2 :as p2]]
  (let [xd (- x2 x1) yd (- y2 y1)]
    (js/Math.sqrt (+ (* xd xd) (* yd yd)))))

(defn latlng->vec [ll]
  (-> ll
      js->clj
      (select-values ["lat" "lng"])))

(defn mouseevent->coords [e]
  (merge
    (-> e
        ;; Note need to round; fractional offsets (eg as in wordpress
        ;; navbar) cause fractional x/y which causes geoserver to
        ;; return errors in GetFeatureInfo
        (ocall "containerPoint.round")
        (js->clj :keywordize-keys true)
        (select-keys [:x :y]))
    (-> e
        (oget "latlng")
        (js->clj :keywordize-keys true)
        (select-keys [:lat :lng]))))

(defn boxes->extents [boxes]
  (let [north (apply max (remove nil? (map (comp :value :northBoundLatitude :value) (:value boxes))))
        west (apply min (remove nil? (map (comp :value :westBoundLongitude :value) (:value boxes))))
        south (apply min (remove nil? (map (comp :value :southBoundLatitude :value) (:value boxes))))
        east (apply max (remove nil? (map (comp :value :eastBoundLongitude :value) (:value boxes))))
        map-extents {:north north :west west :east east :south south}]
    map-extents))

(defn box-map
  [{:keys [map-props]}]
  (let [initial-props map-props
        map-props @(rf/subscribe [:map/props])
        map-props (merge initial-props map-props)
        {:keys [boxes]} map-props
        extents (boxes->extents boxes)
        base-layer [react-leaflet/tile-layer
                    {:url         "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                     :attribution "&copy; <a href=&quot;http://osm.org/copyright&quot;>OpenStreetMap</a> contributors"}]]
    [:div.map-wrapper
     (into
       [react-leaflet/map (merge
                            {;:crs                  (gget "L.CRS.EPSG4326")
                             :id                   "map"
                             :style                {:height "400px" :width "400px"}
                             :use-fly-to           true
                             :center               [-42 147]
                             :zoom                 5
                             :keyboard             false    ; handled externally
                             :close-popup-on-click false    ; We'll handle that ourselves
                             }
                            (when (not (some nil? (vals extents))) {:bounds (map->bounds extents)}))

        base-layer]
       (for [box (:value boxes)]
         (when (not (some nil? (map :value (vals (:value box)))))
           [react-leaflet/rectangle
            {:bounds (geographicElement->bounds (:value box))}])
         ))]))


(defn fg->data [fg]
  (let [leaflet-element (.-leafletElement fg)
        geo-json (.toGeoJSON leaflet-element)
        data (js->clj geo-json :keywordize-keys true)
        geometries (mapv :geometry (:features data))]
    (for [{:keys [type coordinates]} geometries]
      (case type
        "Point" (let [[lat lng] coordinates]
                  {:northBoundLatitude lat
                   :southBoundLatitude lat
                   :eastBoundLongitude lng
                   :westBoundLongitude lng})
        "Polygon" (let [[rect] coordinates
                        lats (map first rect)
                        lngs (map second rect)]
                    {:northBoundLatitude (apply max lats)
                     :southBoundLatitude (apply min lats)
                     :eastBoundLongitude (apply max lngs)
                     :westBoundLongitude (apply min lngs)})))))

; TODO: dispatch on change
; TODO: reset when props change

(defn box-map2
  [_]
  (let [*fg (atom nil)]
    (fn [{:keys [map-props]}]
      (let [initial-props map-props
            map-props @(rf/subscribe [:map/props])
            map-props (merge initial-props map-props)
            {:keys [boxes]} map-props
            extents (boxes->extents boxes)
            base-layer [react-leaflet/tile-layer
                        {:url         "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                         :attribution "&copy; <a href=&quot;http://osm.org/copyright&quot;>OpenStreetMap</a> contributors"}]
            initial-elements (for [box (:value boxes)]
                               (when (not (some nil? (map :value (vals (:value box)))))
                                 [react-leaflet/rectangle
                                  {:bounds (geographicElement->bounds (:value box))}]))]
        (letfn [(handle-change []
                  (js/console.log ::new-boxes (fg->data @*fg)))]

          [:div.map-wrapper
           [react-leaflet/map (merge
                                {;:crs                  (gget "L.CRS.EPSG4326")
                                 :id                   "map"
                                 :style                {:height "400px" :width "400px"}
                                 :use-fly-to           true
                                 :center               [-42 147]
                                 :zoom                 5
                                 :keyboard             false ; handled externally
                                 :close-popup-on-click false ; We'll handle that ourselves
                                 }
                                (when (not (some nil? (vals extents))) {:bounds (map->bounds extents)}))

            base-layer
            (into [react-leaflet/feature-group
                   {:ref #(reset! *fg %)}
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



