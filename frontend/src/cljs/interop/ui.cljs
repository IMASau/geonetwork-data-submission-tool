(ns interop.ui
  (:require ["/ui/components/boxmap/BoxMap" :as BoxMap]
            [cljs.spec.alpha :as s]))

(assert BoxMap/BoxMap)

(s/def ::northBoundLatitude number?)
(s/def ::westBoundLongitude number?)
(s/def ::southBoundLatitude number?)
(s/def ::eastBoundLongitude number?)
(s/def ::element (s/keys :req-un [::northBoundLatitude ::westBoundLongitude ::southBoundLatitude ::eastBoundLongitude]))
(s/def ::elements (s/coll-of ::element))

(defn box-map
  [{:keys [elements map-width tick-id on-change]}]
  (s/assert fn? on-change)
  [:> BoxMap/BoxMap
   {:elements (s/assert ::elements elements)
    :mapWidth (s/assert pos? map-width)
    :tickId   (s/assert number? tick-id)
    :onChange (fn [geojson] (on-change (js->clj geojson :keywordize-keys true)))}])
