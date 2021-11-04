(ns metcalf3.utils
  (:require [cljs.spec.alpha :as s]
            [clojure.string :as string]
            [goog.object :as gobject]))

(defn on-change [m0 m1 ks f]
  (let [v0 (get-in m0 ks)
        v1 (get-in m1 ks)]
    (when-not (= v0 v1)
      (f v1))))

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

(defn userDisplay
  [user]
  (if (and (string/blank? (:lastName user))
           (string/blank? (:firstName user)))
    (if (string/blank? (:email user))
      (:username user)
      (:email user))
    (str (:firstName user) " " (:lastName user))))

(defn validation-state
  [{:keys [errors show-errors]}]
  (when (and show-errors (seq errors))
    "has-error"))

(defn filter-table
  "Default search for local datasource: case-insensitive substring match"
  [simple? table query]
  (s/assert string? query)
  (let [col-match? (if simple?
                     #(string/starts-with? (-> % str string/lower-case) (string/lower-case query))
                     #(string/includes? (-> % str string/lower-case) (string/lower-case query)))]
    (filter
      (fn [row]
        (some col-match? (rest row)))
      table)))

(defn other-term?
  [term vocabularyTermURL]
  (and (:value term) (empty? (:value vocabularyTermURL))))
