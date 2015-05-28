(ns condense.utils
  (:require clojure.string))

(defn fmap [f m]
  (into (empty m) (for [[k v] m] [k (f v)])))

(defn map-keys [f m]
  (persistent! (reduce-kv (fn [z k v] (assoc! z (f k) v)) (transient {}) m)))

(defn memoize-last
  "Returns a memoized version of a referentially transparent function.

  The memoized version of the function keeps a cache of the *most recent call*
  from arguments to result.
  "
  [f]
  (let [mem (atom {})
        lookup-sentinel (js-obj)]
    (fn [& args]
      (let [v (get @mem args lookup-sentinel)]
        (if (identical? v lookup-sentinel)
          (let [ret (apply f args)]
            (reset! mem {args ret})
            ret)
          v)))))

(defn title-case [s]
  (-> s clojure.string/lower-case
      (clojure.string/replace #"\b." #(.toUpperCase %1))))


(defn keys-in [m]
  "
  Generate a list of paths presented in a nested map

  Ref: http://stackoverflow.com/a/21769786/176453
  "
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
  "Helper based on assoc.  Initialises empty values as arrays if key ins an integer"
  [m korks v]
  (modified-assoc-in m korks v #(if (integer? %) [])))

(defn vec-remove [v i]
  (persistent!
    (reduce conj! (transient (vec (subvec v 0 i))) (subvec v (inc i) (count v)))))

(defn zip [& colls]
  (apply map vector colls))

(defn enum [coll]
  (zip (range) coll))

