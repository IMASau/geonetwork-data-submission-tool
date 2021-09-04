(ns interop.cuerdas
  (:require [cuerdas.core :as cuerdas]))

(def starts-with? cuerdas/starts-with?)
(def includes? cuerdas/includes?)
(def lower cuerdas/lower)
(def human cuerdas/human)
