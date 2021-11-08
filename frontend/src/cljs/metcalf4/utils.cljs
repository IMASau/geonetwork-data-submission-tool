(ns metcalf4.utils
  (:require [cljs.spec.alpha :as s]
            [goog.object :as gobject]
            [goog.string :as gstring]
            [goog.uri.utils :as uri]
            [re-frame.core :as rf]
            [lambdaisland.fetch :as fetch]))


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

(defn post-data
  [{:keys [url data]}]
  (s/assert string? url)
  (fetch/post url {:accept       :json
                   :content-type :json
                   :headers      {:X-CSRFToken (get-csrf)}
                   :body         data}))


(defn get-value-by-keys [o path]
  (s/assert (s/coll-of string?) path)
  (gobject/getValueByKeys o (into-array (map name path))))


(defn load-options
  "Helper for common load options pattern"
  [{:keys [uri results-path search-param]
    :or   {results-path ["results"]
           search-param "query"}}
   query]
  (s/assert string? uri)
  (s/assert string? search-param)
  (s/assert (s/coll-of string?) results-path)
  (.then (fetch-get {:uri (append-params-from-map uri {search-param query})})
         (fn [json]
           (or (get-value-by-keys json results-path) #js []))))


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

(defn dispatchify
  "Helper for promise-fx.  Massages re-frame fx props to rf/dispatch when :resolve, :reject or :finally is a vector.
   * Supports data oriented :fx calls
   * Avoids promise based fx being cluttered with re-frame plumbing"
  [{:keys [resolve reject finally] :as m}]
  (cond-> m
    (vector? resolve) (assoc :resolve #(rf/dispatch (conj resolve %)))
    (vector? reject) (assoc :reject #(rf/dispatch (conj reject %)))
    (vector? finally) (assoc :finally #(rf/dispatch (conj finally %)))))

(defn promise-fx
  "
  Wrap a promise for use as a re-frame fx.
  Looks for :resolve :reject & :finally callback props.
  "
  [f]
  (fn [fx-args]
    (let [{:keys [resolve reject finally]} (dispatchify fx-args)
          args (dissoc fx-args :resolve :reject :finally)]
      (cond-> (f args)
        resolve (.then resolve)
        reject (.catch reject)
        finally (.finally finally)))))

(defn path-vals
  "Returns vector of tuples containing path vector to the value and the value."
  {:from [:metosin/potpuri "0.5.3"]}
  [m]
  (letfn
    [(pvals [l p m]
       (reduce
         (fn [l [k v]]
           (if (map? v)
             (pvals l (conj p k) v)
             (cons [(conj p k) v] l)))
         l m))]
    (pvals [] [] m)))
