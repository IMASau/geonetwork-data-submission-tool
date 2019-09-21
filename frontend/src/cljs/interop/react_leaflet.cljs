(ns interop.react-leaflet
  (:refer-clojure :exclude [map])
  (:require cljsjs.react-leaflet
            [reagent.core :as r]))

(def leaflet-consumer (r/adapt-react-class js/ReactLeaflet.LeafletConsumer))
(def leaflet-provider (r/adapt-react-class js/ReactLeaflet.LeafletProvider))
(def with-leaflet (r/adapt-react-class js/ReactLeaflet.withLeaflet))
(def use-leaflet (r/adapt-react-class js/ReactLeaflet.useLeaflet))
(def attribution-control (r/adapt-react-class js/ReactLeaflet.AttributionControl))
(def circle (r/adapt-react-class js/ReactLeaflet.Circle))
(def circle-marker (r/adapt-react-class js/ReactLeaflet.CircleMarker))
(def div-overlay (r/adapt-react-class js/ReactLeaflet.DivOverlay))
(def edit-control (r/adapt-react-class js/ReactLeaflet.EditControl))
(def feature-group (r/adapt-react-class js/ReactLeaflet.FeatureGroup))
(def geo-json (r/adapt-react-class js/ReactLeaflet.GeoJSON))
(def grid-layer (r/adapt-react-class js/ReactLeaflet.GridLayer))
(def image-overlay (r/adapt-react-class js/ReactLeaflet.ImageOverlay))
(def layer-group (r/adapt-react-class js/ReactLeaflet.LayerGroup))
(def layers-control (r/adapt-react-class js/ReactLeaflet.LayersControl))
(def controlled-layer (r/adapt-react-class js/ReactLeaflet.ControlledLayer))
(def map (r/adapt-react-class js/ReactLeaflet.Map))
(def map-component (r/adapt-react-class js/ReactLeaflet.MapComponent))
(def map-control (r/adapt-react-class js/ReactLeaflet.MapControl))
(def map-evented (r/adapt-react-class js/ReactLeaflet.MapEvented))
(def map-layer (r/adapt-react-class js/ReactLeaflet.MapLayer))
(def marker (r/adapt-react-class js/ReactLeaflet.Marker))
(def pane (r/adapt-react-class js/ReactLeaflet.Pane))
(def path (r/adapt-react-class js/ReactLeaflet.Path))
(def polygon (r/adapt-react-class js/ReactLeaflet.Polygon))
(def polyline (r/adapt-react-class js/ReactLeaflet.Polyline))
(def popup (r/adapt-react-class js/ReactLeaflet.Popup))
(def rectangle (r/adapt-react-class js/ReactLeaflet.Rectangle))
(def scale-control (r/adapt-react-class js/ReactLeaflet.ScaleControl))
(def tile-layer (r/adapt-react-class js/ReactLeaflet.TileLayer))
(def tooltip (r/adapt-react-class js/ReactLeaflet.Tooltip))
(def video-overlay (r/adapt-react-class js/ReactLeaflet.VideoOverlay))
(def wmstile-layer (r/adapt-react-class js/ReactLeaflet.WMSTileLayer))
(def zoom-control (r/adapt-react-class js/ReactLeaflet.ZoomControl))