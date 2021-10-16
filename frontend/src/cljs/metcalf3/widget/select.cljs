(ns metcalf3.widget.select
  (:require [cljs.spec.alpha :as s]
            [goog.object :as gobj]
            [interop.react-select :refer [ReactSelect* ReactSelectAsync* ReactSelectAsyncCreatable* SelectComponentsOption*]]
            [interop.react-virtualized :refer [ReactWindow*]]
            [metcalf3.utils :as utils]
            [reagent.core :as r]))

(defn normalize-props [props]
  (utils/clj->js* (update props :options utils/clj->js* 1) 1))

(defn ReactSelect [props]
  (ReactSelect* (normalize-props props)))

(defn ReactSelectAsyncCreatable [props]
  (ReactSelectAsyncCreatable* (normalize-props props)))

(defn ReactSelectAsync [props]
  (ReactSelectAsync* (normalize-props props)))