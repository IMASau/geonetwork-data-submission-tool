(ns metcalf3.components
  (:require [re-frame.core :as rf]
            [clojure.walk :as walk]))

(def ^:dynamic component-registry {})
(def ^:dynamic hiccup-registry {})
(def ^:dynamic not-found-hiccup '[:div "not found"])

(defn render-hiccup
  [hiccup-id]
  (let [hiccup (get hiccup-registry hiccup-id not-found-hiccup)]
    (walk/postwalk-replace component-registry hiccup)))
