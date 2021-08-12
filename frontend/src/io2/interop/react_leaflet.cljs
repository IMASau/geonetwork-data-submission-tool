(ns interop.react-leaflet
  (:refer-clojure :exclude [map])
  (:require cljsjs.react-leaflet
            [reagent.core :as r]
            ["leaflet" :default L]
            ["leaflet-draw"]
            ["react-leaflet" :as ReactLeaflet]
            ["react-leaflet-draw" :refer (EditControl)]))

(defn to-geojson [leaflet]
  (js->clj (.toGeoJSON leaflet) :keywordize-keys true))

(defn leaflet-element [fg]
  (.-leafletElement fg))

(def leaflet-consumer (r/adapt-react-class ReactLeaflet/LeafletConsumer))
(def leaflet-provider (r/adapt-react-class ReactLeaflet/LeafletProvider))
(def with-leaflet (r/adapt-react-class ReactLeaflet/withLeaflet))
(def use-leaflet (r/adapt-react-class ReactLeaflet/useLeaflet))
(def attribution-control (r/adapt-react-class ReactLeaflet/AttributionControl))
(def circle (r/adapt-react-class ReactLeaflet/Circle))
(def circle-marker (r/adapt-react-class ReactLeaflet/CircleMarker))
(def div-overlay (r/adapt-react-class ReactLeaflet/DivOverlay))
(def edit-control (r/adapt-react-class ReactLeaflet/EditControl))
(def feature-group (r/adapt-react-class ReactLeaflet/FeatureGroup))
(def geo-json (r/adapt-react-class ReactLeaflet/GeoJSON))
(def grid-layer (r/adapt-react-class ReactLeaflet/GridLayer))
(def image-overlay (r/adapt-react-class ReactLeaflet/ImageOverlay))
(def layer-group (r/adapt-react-class ReactLeaflet/LayerGroup))
(def layers-control (r/adapt-react-class ReactLeaflet/LayersControl))
(def controlled-layer (r/adapt-react-class ReactLeaflet/ControlledLayer))
(def map (r/adapt-react-class ReactLeaflet/Map))
(def map-component (r/adapt-react-class ReactLeaflet/MapComponent))
(def map-control (r/adapt-react-class ReactLeaflet/MapControl))
(def map-evented (r/adapt-react-class ReactLeaflet/MapEvented))
(def map-layer (r/adapt-react-class ReactLeaflet/MapLayer))
(def marker (r/adapt-react-class ReactLeaflet/Marker))
(def pane (r/adapt-react-class ReactLeaflet/Pane))
(def path (r/adapt-react-class ReactLeaflet/Path))
(def polygon (r/adapt-react-class ReactLeaflet/Polygon))
(def polyline (r/adapt-react-class ReactLeaflet/Polyline))
(def popup (r/adapt-react-class ReactLeaflet/Popup))
(def rectangle (r/adapt-react-class ReactLeaflet/Rectangle))
(def scale-control (r/adapt-react-class ReactLeaflet/ScaleControl))
(def tile-layer (r/adapt-react-class ReactLeaflet/TileLayer))
(def tooltip (r/adapt-react-class ReactLeaflet/Tooltip))
(def video-overlay (r/adapt-react-class ReactLeaflet/VideoOverlay))
(def wmstile-layer (r/adapt-react-class ReactLeaflet/WMSTileLayer))
(def zoom-control (r/adapt-react-class ReactLeaflet/ZoomControl))
