(ns metcalf4.utils
  (:require [cljs.spec.alpha :as s]))


(s/def ::form-id vector?)
(s/def ::data-path (s/coll-of (s/or :i int? :s string?) :kind vector?))
(s/def ::ctx (s/keys :req-un [::form-id ::data-path]))


(defn str-value
  [data]
  (binding [*print-level* 3
            *print-length* 5]
    (pr-str data)))


(defn console-value
  [data]
  (if goog/DEBUG
    data
    (str-value data)))


(defn console-error
  [msg data]
  (js/console.error msg (console-value data)))


(defn console-warning
  [msg data]
  (js/console.warn msg (console-value data)))


(defn update-keys
  [m f]
  (zipmap (map f (keys m)) (vals m)))


(defn contains-path?
  [m ks]
  (not= ::not-found (get-in m ks ::not-found)))


(defn contains-every?
  [m keyseqs]
  (not-any? #{::not-found} (for [ks keyseqs] (get-in m ks ::not-found))))


(def as-path (comp vec flatten))


(defn massage-data-path-value [x]
  (cond
    (simple-keyword? x) (name x)
    (simple-symbol? x) (name x)
    :else x))


(defn massage-data-path
  [data-path]
  (mapv massage-data-path-value (as-path data-path)))


(defn if-contains-update
  [m k xform]
  (if (contains? m k)
    (update m k xform)
    m))


(defn get-ctx
  [{:keys [form-id data-path]}]
  (when (and form-id data-path)
    {:form-id form-id :data-path (massage-data-path data-path)}))


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
    {"northBoundLatitude" lat
     "southBoundLatitude" lat
     "eastBoundLongitude" lng
     "westBoundLongitude" lng}))
(defmethod geometry->box-value "Polygon"
  [{:keys [coordinates]}]
  (let [[rect] coordinates
        lngs (map first rect)
        lats (map second rect)]
    (s/assert some? rect)
    {"northBoundLatitude" (s/assert number? (apply max lats))
     "southBoundLatitude" (s/assert number? (apply min lats))
     "eastBoundLongitude" (s/assert number? (apply max lngs))
     "westBoundLongitude" (s/assert number? (apply min lngs))}))


(defn schema-object-with-keys
  [ks]
  {:type "object" :properties (zipmap (map name ks) (repeat {}))})