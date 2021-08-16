(ns metcalf3.low-code
  (:require [re-frame.core :as rf]
            [clojure.walk :as walk]))

(def ^:dynamic component-registry {})
(def ^:dynamic template-registry {})
(def ^:dynamic not-found-hiccup '[:div "not found"])

(defn render-template
  [{:keys [template-id]}]
  (let [hiccup (get template-registry template-id not-found-hiccup)]
    (walk/postwalk-replace component-registry hiccup)))
