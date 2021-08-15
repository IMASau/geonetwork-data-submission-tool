(ns interop.ui
  (:require ["/ui/components/boxmap/BoxMap" :as BoxMap]
            [reagent.core :as r]))

(assert BoxMap/BoxMap)

(def box-map (r/adapt-react-class BoxMap/BoxMap))
