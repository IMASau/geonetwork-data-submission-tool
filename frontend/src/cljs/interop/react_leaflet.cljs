(ns interop.react-leaflet
  (:refer-clojure :exclude [map])
  (:require ["leaflet"]
            ["leaflet-draw"]
            ["react-leaflet" :as ReactLeaflet]
            ["react-leaflet-draw" :refer (EditControl)]
            [reagent.core :as r]))
