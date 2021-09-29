(ns metcalf4.utils
  (:require [cljs.spec.alpha :as s]))


(s/def ::form-id vector?)
(s/def ::data-path vector?)
(s/def ::ctx (s/keys :req-un [::form-id ::data-path]))


(defn get-ctx
  [{:keys [form-id data-path]}]
  {:form-id form-id :data-path data-path})

