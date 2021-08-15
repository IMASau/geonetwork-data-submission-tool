(ns metcalf3.components
  (:require [re-frame.core :as rf]
            [clojure.walk :as walk]))

(def ^:dynamic component-registry {})

(defn PageTabView
  [page]
  (let [tab (get page :tab :data-identification)
        hiccup @(rf/subscribe [:subs/get-template-tab tab])]
    (walk/postwalk-replace component-registry hiccup)))
