(ns metcalf3.utils
  (:require [goog.object :as gobject]
            [cljs.spec.alpha :as s]))

(defn on-change [m0 m1 ks f]
  (let [v0 (get-in m0 ks)
        v1 (get-in m1 ks)]
    (when-not (= v0 v1)
      (f v1))))

(defn clj->js*
  "Recursively transforms ClojureScript values to JavaScript, but not deeper than `depth`.
  Sets/Vectors/Lists become Arrays, Keywords and Symbols become Strings,
  Maps become Objects. Arbitrary keys are encoded to by `key->js`."
  [x depth]
  (when-not (nil? x)
    (if (satisfies? IEncodeJS x)
      (-clj->js x)
      (cond
        (keyword? x) (name x)
        (symbol? x) (str x)

        (and (pos? depth) (map? x))
        (let [m (js-obj)]
          (doseq [[k v] x]
            (aset m (key->js k) (clj->js* v (dec depth))))
          m)

        (and (pos? depth) (coll? x))
        (let [arr (array)]
          (doseq [x x]
            (.push arr (clj->js* x (dec depth))))
          arr)

        :else x))))

(defn js-lookup
  "Helper for destructuring.  Returns an ILookup for fetching values out of a js-obj by keyword."
  [js-obj]
  (reify
    ILookup
    (-lookup [o k] (gobject/get js-obj (name k)))
    (-lookup [o k not-found] (gobject/get js-obj (name k) not-found))))

(defn fmap [f m]
  (into (empty m) (for [[k v] m] [k (f v)])))

(defn map-keys [f m]
  (persistent! (reduce-kv (fn [z k v] (assoc! z (f k) v)) (transient {}) m)))

(defn keys-in
  "
  Generate a list of paths presented in a nested map

  Ref: http://stackoverflow.com/a/21769786/176453
  "
  [m]
  (cond
    (map? m) (vec
               (mapcat (fn [[k v]]
                         (let [sub (keys-in v)
                               nested (map #(into [k] %) (filter (comp not empty?) sub))]
                           (if (seq nested)
                             nested
                             [[k]])))
                       m))
    (coll? m) (vec
                (mapcat (fn [v k]
                          (let [sub (keys-in v)
                                nested (map #(into [k] %) (filter (comp not empty?) sub))]
                            (if (seq nested)
                              nested
                              [[k]])))
                        m
                        (range)))
    :else []))

(defn modified-assoc-in
  "Modified assoc which allows missing value initialisation based on the key"
  [m [k & ks] v init-value-fn]
  (let [m (or m (init-value-fn k))]
    (if ks
      (assoc m k (modified-assoc-in (get m k) ks v init-value-fn))
      (assoc m k v))))

(defn int-assoc-in
  "Helper based on assoc.  Initialises empty values as arrays if key is an integer"
  [m ks v]
  (modified-assoc-in m ks v #(when (integer? %) [])))

(defn vec-remove [v i]
  {:pre [(vector? v) (nat-int? i) (contains? v i)]}
  (reduce conj (vec (subvec v 0 i)) (subvec v (inc i) (count v))))

(defn vec-insert [v idx x]
  {:pre [(vector? v) (nat-int? idx)]}
  (into (conj (subvec v 0 idx) x) (subvec v idx)))

(defn zip [& colls]
  (apply map vector colls))

(defn enum [coll]
  (zip (range) coll))

(defn geometry-type [{:keys [type]}] type)

(defmulti geometry->box-value geometry-type)

(defmethod geometry->box-value "Point"
  [{:keys [coordinates]}]
  (let [[lng lat] coordinates]
    (s/assert number? lng)
    (s/assert number? lat)
    {:northBoundLatitude {:value lat}
     :southBoundLatitude {:value lat}
     :eastBoundLongitude {:value lng}
     :westBoundLongitude {:value lng}}))

(defmethod geometry->box-value "Polygon"
  [{:keys [coordinates]}]
  (let [[rect] coordinates
        lngs (map first rect)
        lats (map second rect)]
    (s/assert some? rect)
    {:northBoundLatitude {:value (s/assert number? (apply max lats))}
     :southBoundLatitude {:value (s/assert number? (apply min lats))}
     :eastBoundLongitude {:value (s/assert number? (apply max lngs))}
     :westBoundLongitude {:value (s/assert number? (apply min lngs))}}))

(defn boxes->elements
  [boxes]
  (for [box (:value boxes)]
    {:northBoundLatitude (get-in box [:value :northBoundLatitude :value])
     :southBoundLatitude (get-in box [:value :southBoundLatitude :value])
     :eastBoundLongitude (get-in box [:value :eastBoundLongitude :value])
     :westBoundLongitude (get-in box [:value :westBoundLongitude :value])}))
