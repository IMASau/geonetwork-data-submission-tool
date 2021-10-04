(ns metcalf4.utils
  (:require [cljs.spec.alpha :as s]))


(s/def ::form-id vector?)
(s/def ::data-path vector?)
(s/def ::ctx (s/keys :req-un [::form-id ::data-path]))


(defn get-ctx
  [{:keys [form-id data-path]}]
  {:form-id form-id :data-path data-path})


(defn get-csrf
  []
  (let [payload (js->clj (aget js/window "payload") :keywordize-keys true)]
    (get-in payload [:context :csrf])))


(defn fetch-post
  [{:keys [uri body]}]
  (-> (js/fetch uri #js {:method  "POST"
                         :body    (js/JSON.stringify (clj->js body))
                         :headers #js {:Content-Type "application/json"
                                       :Accept       "application/json"
                                       :X-CSRFToken  (get-csrf)}})
      (.then (fn [resp] (.json resp)))))


(def as-path (comp vec flatten))
