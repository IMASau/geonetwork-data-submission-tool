(ns interop.ui
  (:require ["/ui/components/Button" :as Button]
            [reagent.core :as r]))

(assert Button/Button)

(defn Button [props] [:> Button/Button props])
