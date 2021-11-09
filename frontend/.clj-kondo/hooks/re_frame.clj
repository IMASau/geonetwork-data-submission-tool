(ns hooks.re-frame
  (:require [clj-kondo.hooks-api :as api]))

(defn use-qualified-keyword
  [{:keys [:node]}]
  (let [sexpr (api/sexpr node)
        event (second sexpr)
        kw (first event)]
    (when (and (vector? event)
               (keyword? kw)
               (not (qualified-keyword? kw)))
      (let [{:keys [:row :col]} (some-> node :children second :children first meta)]
        (api/reg-finding! {:message "keyword should be fully qualified!"
                           :type    :re-frame/keyword
                           :row     row
                           :col     col})))))

(defn reg-qualified-keyword
  [{:keys [:node]}]
  (let [c (:children node)
        sexpr (api/sexpr node)
        kw (second sexpr)
        {:keys [:row :col]} (some-> node :children second meta)]
    (when (and (keyword? kw)
               (not (qualified-keyword? kw)))
      (api/reg-finding! {:message "keyword should be fully qualified!"
                         :type    :re-frame/keyword
                         :row     row
                         :col     col}))))
