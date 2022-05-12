(ns metcalf.common.utils4
  (:require [cljs.spec.alpha :as s]
            [goog.object :as gobject]
            [goog.string :as gstring]
            [goog.uri.utils :as uri]
            [lambdaisland.fetch :as fetch]
            [re-frame.core :as rf]
            [clojure.string :as string]))


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


(defn log
  "Log to console with level.  Uses pprint with level/length set.
   Intended for production logging."
  [{:keys [level msg data]}]
  (case level
    :error (js/console.error msg (console-value data))
    :info (js/console.info msg (console-value data))
    :log (js/console.log msg (console-value data))
    :debug (js/console.debug msg (console-value data))
    :warn (js/console.warn msg (console-value data))))


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
      (gobject/set o (gstring/urlEncode (name k)) v))
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

(defn post-json
  [{:keys [url data]}]
  (s/assert string? url)
  (fetch/post url {:accept       :json
                   :content-type :json
                   :headers      {:X-CSRFToken (get-csrf)}
                   :body         data}))

(defn get-json
  [{:keys [url]}]
  (s/assert string? url)
  (fetch/get url {:accept       :json
                  :content-type :json
                  :headers      {:X-CSRFToken (get-csrf)}}))

(defn post-multipart-form
  [{:keys [url data]}]
  (let [body (js/FormData.)]
    (doseq [[k v] data]
      (.append body (name k) v))
    (-> (js/fetch url #js {:headers #js {:X-CSRFToken (get-csrf)}
                           :method  "post"
                           :body    body})
        (.then (fn [resp]
                 (-> (.json resp)
                     (.then (fn [json] {:status (.-status resp)
                                        :body   json}))))))))

(defn get-value-by-keys
  "Like get-in but for js objects."
  [o path]
  (s/assert (s/coll-of string?) path)
  (gobject/getValueByKeys o (into-array (map name path))))

(defn set-value-by-keys
  "Like assoc-in but for js object"
  [o path v]
  (s/assert (s/coll-of string? :min-count 1) path)
  (let [p (reduce
            (fn [o k]
              (gobject/setIfUndefined o k (js-obj))
              (gobject/get o k))
            o
            (butlast path))]
    (gobject/set p (last path) v))
  o)

(goog-define load-options-api-root "")

(defn js-xformer
  "Returns a function for massaging js-object data"
  [data-mapper]
  (s/assert (s/coll-of (s/keys :req-un [::get-path ::set-path])) data-mapper)
  (fn [js-option]
    (let [js-ret (js-obj)]
      (doseq [{:keys [get-path set-path]} data-mapper]
        (set-value-by-keys js-ret set-path (get-value-by-keys js-option get-path)))
      js-ret)))

(defn load-options
  "Helper for common load options pattern.
   Gets option data from :results-path.
   Maps over results with :option-xform (if set)."
  [{:keys [uri results-path search-param option-xform]
    :or   {results-path ["results"]
           search-param "query"}}
   query]
  (s/assert string? uri)
  (s/assert string? search-param)
  (s/assert (s/coll-of string?) results-path)
  (.then (fetch-get {:uri (append-params-from-map (str load-options-api-root uri) {search-param query})})
         (fn [json]
           (cond-> (or (get-value-by-keys json results-path) #js [])
             option-xform (.map option-xform)))))

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
    (let [v (get @mem args ::not-found)]
      (if (= v ::not-found)
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


(defn update-vals
  [m f]
  (zipmap (keys m) (map f (vals m))))


(defn show-error-analysis
  "Postwalk analysis.  Set show-errors? prop if field should display error."
  [{:keys [props] :as block}]
  (let [{:keys [touched errors disabled]} props
        show-errors? (and (not disabled) touched (seq errors))]
    (if show-errors?
      (assoc-in block [:props :show-errors?] true)
      block)))

(defn resolve-select-mode
  "Provide a default where select-mode isn't defined."
  [{:keys [select-mode added-path]}]
  (when-not select-mode
    (js/console.warn (str ::resolve-selected-mode ": Please specify select-mode")))
  (when (and (= select-mode :added-only) (empty? added-path))
    (js/console.error (str ::resolve-selected-mode ": added-path required for :added-only")))
  (or select-mode
      (when added-path :added-only)
      :all-items))
