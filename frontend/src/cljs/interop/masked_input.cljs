(ns interop.masked-input
  (:require [reagent.core :as r]
            [cljsjs.react-imask]))

(def masked-input (r/adapt-react-class js/IMaskInput))
