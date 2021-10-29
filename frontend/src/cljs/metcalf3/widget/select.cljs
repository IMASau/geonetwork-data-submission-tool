(ns metcalf3.widget.select
  (:require [interop.react-select :refer [ReactSelect* ReactSelectAsync*]]
            [metcalf3.utils :as utils]))

(defn normalize-props [props]
  (utils/clj->js* (update props :options utils/clj->js* 1) 1))
