(ns interop.react-imask
  (:require ["react-imask" :refer (IMaskInput)]
            [reagent.core :as r]))

(assert IMaskInput)

(def masked-input (r/adapt-react-class IMaskInput))
