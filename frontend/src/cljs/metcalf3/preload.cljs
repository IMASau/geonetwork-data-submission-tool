(ns metcalf3.preload
  (:require [cljs.spec.alpha :as s]
            [devtools.core :as devtools]))

(when ^boolean js/goog.DEBUG
  (enable-console-print!)
  (devtools/install!)
  (s/check-asserts true))