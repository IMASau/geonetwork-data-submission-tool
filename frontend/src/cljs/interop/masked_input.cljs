(ns interop.masked-input
  (:require [reagent.core :as r]))

(def masked-input (r/adapt-react-class js/MaskedInput))
