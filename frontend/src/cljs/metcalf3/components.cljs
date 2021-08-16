(ns metcalf3.components
  (:require [re-frame.core :as rf]
            [clojure.walk :as walk]))

(def ^:dynamic component-registry {})
(def ^:dynamic hiccup-registry {})
(def ^:dynamic not-found-hiccup '[:div "not found"])

(defn PageTabView
  [page]
  (let [tab (get page :tab :data-identification)
        hiccup (get hiccup-registry tab not-found-hiccup)]
    (walk/postwalk-replace component-registry hiccup)))
