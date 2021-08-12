(ns interop.react-imask
  (:require ["react-imask" :refer (IMaskInput)]
            [reagent.core :as r]))

(def masked-input (r/adapt-react-class IMaskInput))
