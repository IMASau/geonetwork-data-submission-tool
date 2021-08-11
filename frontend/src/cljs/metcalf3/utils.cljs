(ns metcalf3.utils
  (:require clojure.string
            [goog.object :as gobject]))

(defn error [& args]
  (.apply (.-error js/console) js/console (to-array args))
  (last args))

(defn warn [& args]
  (.apply (.-warn js/console) js/console (to-array args))
  (last args))

(defn info [& args]
  (.apply (.-info js/console) js/console (to-array args))
  (last args))

(defn log [& args]
  (.apply (.-log js/console) js/console (to-array args))
  (last args))

(defn debug [& args]
  (.apply (.-debug js/console) js/console (to-array args))
  (last args))

(defn on-edge [m0 m1 ks on-set on-clr]
  (let [v0 (get-in m0 ks)
        v1 (get-in m1 ks)]
    (when (and v0 (not v1)) (on-clr))
    (when (and (not v0) v1) (on-set))))

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

(defn title-case [s]
  (-> s clojure.string/lower-case
      (clojure.string/replace #"\b." #(.toUpperCase %1))))

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
  (modified-assoc-in m ks v #(if (integer? %) [])))

(defn vec-remove [v i]
  (reduce conj (vec (subvec v 0 i)) (subvec v (inc i) (count v))))

(defn zip [& colls]
  (apply map vector colls))

(defn enum [coll]
  (zip (range) coll))

(defn same-keyword-string?
  "Check if a keyword parameter coerced to a string is the same as the string parameter."
  [keyword string]
  (= (str (clojure.core/name keyword)) string))