(ns metcalf4.utils
  (:require [cljs.spec.alpha :as s]
            [goog.object :as gobject]
            [goog.string :as gstring]
            [goog.uri.utils :as uri]))


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


(defn contains-path?
  [m ks]
  (not= ::not-found (get-in m ks ::not-found)))


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


(defn get-csrf
  []
  (let [payload (js->clj (aget js/window "payload") :keywordize-keys true)]
    (get-in payload [:context :csrf])))


(defn js-params [m]
  (let [o (js-obj)]
    (doseq [[k v] m]
      (gobject/set o (gstring/urlEncode (name k)) (gstring/urlEncode v)))
    o))

(defn append-params-from-map
  [uri params]
  (uri/appendParamsFromMap uri (js-params params)))


(defn fetch-get
  [{:keys [uri]}]
  (-> (js/fetch uri #js {:method  "GET"
                         :headers #js {:Content-Type "application/json"
                                       :Accept       "application/json"
                                       :X-CSRFToken  (get-csrf)}})
      (.then (fn [resp] (.json resp)))))

(defn fetch-get-with-results-path
  [{:keys [uri results-path]}]
  (letfn [(get-obj-path
            ([path] #(get-obj-path % path))
            ([o path]
             (let [path (if (string? path) [path] path)]
               (apply gobject/getValueByKeys o path))))]
    (-> (js/fetch uri #js {:method  "GET"
                           :headers #js {:Content-Type "application/json"
                                         :Accept       "application/json"
                                         :X-CSRFToken  (get-csrf)}})
        (.then (fn [resp] (.json resp)))
        (.then (fn [json]
                 (js/console.log "Te"
                                 {:json json :result (get-obj-path json results-path)})
                 (get-obj-path json results-path))))))


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
  (let [ed (merge (assoc (s/explain-data* spec path [] [] form)
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

(defn memoize-to-atom
  "clojure.core/memoize but backed by explicit atom"
  [f mem]
  (fn [& args]
    (let [v (get @mem args lookup-sentinel)]
      (if (identical? v lookup-sentinel)
        (let [ret (apply f args)]
          (swap! mem assoc args ret)
          ret)
        v))))