(ns metcalf3.widget.boxmap
  (:require [re-frame.core :as rf]
            [interop.react-leaflet :as react-leaflet]
            [oops.core :refer [ocall oget]]
            [reagent.core :as r]
            [interop.blueprint :as bp3]
            [goog.object :as gobj]))

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

(defn geographicElement->point [{:keys [northBoundLatitude southBoundLatitude eastBoundLongitude westBoundLongitude] :as bounds}]
  [(:value southBoundLatitude) (:value westBoundLongitude)])

(defn geographicElement->point?
  [{:keys [northBoundLatitude southBoundLatitude eastBoundLongitude westBoundLongitude] :as bounds}]
  (and (= (:value southBoundLatitude) (:value northBoundLatitude))
       (= (:value westBoundLongitude) (:value eastBoundLongitude))))

(defn geographicElement->bounds [{:keys [northBoundLatitude southBoundLatitude eastBoundLongitude westBoundLongitude] :as bounds}]
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


(defn fg->data [fg]
  (let [leaflet-element (react-leaflet/leaflet-element fg)
        geo-json (react-leaflet/to-geojson leaflet-element)
        data (js->clj geo-json :keywordize-keys true)
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

; TODO: dispatch on change

(defn box-map2
  [_]
  (let [*fg (atom nil)]
    (fn [{:keys [map-props boxes-path map-width]}]
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
                                 (if (geographicElement->point? (:value box))
                                   [react-leaflet/marker
                                    {:position (geographicElement->point (:value box))}]
                                   [react-leaflet/rectangle
                                    {:bounds (geographicElement->bounds (:value box))}])))]
        (letfn [(handle-change []
                  (rf/dispatch [:handlers/update-boxes boxes-path (fg->data @*fg)]))]

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
                    :key (str "feature-group" @(rf/subscribe [:subs/get-form-tick]))}
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

