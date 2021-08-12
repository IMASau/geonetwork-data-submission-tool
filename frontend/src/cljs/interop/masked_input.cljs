(ns interop.masked-input
  (:require [cljsjs.react-imask]
            [reagent.core :as r]))

(def masked-input (r/adapt-react-class js/IMaskInput))
