(ns metcalf4.low-code
  (:require [clojure.walk :as walk]))

(def ^:dynamic component-registry {})
(def ^:dynamic template-registry {})
(def ^:dynamic not-found-hiccup '[:div "not found"])

(defn render-template
  [{:keys [template-id variables]}]
  (let [form (get template-registry template-id not-found-hiccup)
        smap (merge component-registry variables)]
    (walk/postwalk-replace smap form)))
