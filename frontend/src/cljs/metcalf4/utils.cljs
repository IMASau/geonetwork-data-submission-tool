(ns metcalf4.utils
  (:require [cljs.spec.alpha :as s]))


(s/def ::form-id vector?)
(s/def ::data-path (s/coll-of (s/or :i int? :s string?) :kind vector?))
(s/def ::ctx (s/keys :req-un [::form-id ::data-path]))


(defn massage-data-path-value [x]
  (cond
    (simple-keyword? x) (name x)
    (simple-symbol? x) (name x)
    :else x))


(defn get-ctx
  [{:keys [form-id data-path]}]
  (let [data-path (mapv massage-data-path-value data-path)]
    (s/assert ::data-path data-path)
    {:form-id form-id :data-path data-path}))


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


(defn spec-error-at-path
  [spec form path]
  (let [ed (merge (assoc (s/explain-data* spec [] path [] form)
                    ::s/failure :assertion-failed))]
    (str "Spec assertion failed\n" (with-out-str (s/explain-out ed)))))

(defn geometry-type [{:keys [type]}] type)
(defmulti geometry->box-value geometry-type)
(defmethod geometry->box-value "Point"
  [{:keys [coordinates]}]
  (let [[lng lat] coordinates]
    (s/assert number? lng)
    (s/assert number? lat)
    {:northBoundLatitude lat
     :southBoundLatitude lat
     :eastBoundLongitude lng
     :westBoundLongitude lng}))
(defmethod geometry->box-value "Polygon"
  [{:keys [coordinates]}]
  (let [[rect] coordinates
        lngs (map first rect)
        lats (map second rect)]
    (s/assert some? rect)
    {:northBoundLatitude (s/assert number? (apply max lats))
     :southBoundLatitude (s/assert number? (apply min lats))
     :eastBoundLongitude (s/assert number? (apply max lngs))
     :westBoundLongitude (s/assert number? (apply min lngs))}))


(defn schema-object-with-keys
  [ks]
  {:type "object" :properties (zipmap (map name ks) (repeat {}))})