(ns metcalf3.preload
  (:require [cljs.spec.alpha :as s]))

(when ^boolean js/goog.DEBUG
  (enable-console-print!)
  (s/check-asserts true))