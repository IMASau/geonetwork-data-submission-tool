(ns metcalf3.utils3
  (:require [clojure.string :as string]))

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

(defn userDisplay
  [user]
  (if (and (string/blank? (:lastName user))
           (string/blank? (:firstName user)))
    (if (string/blank? (:email user))
      (:username user)
      (:email user))
    (str (:firstName user) " " (:lastName user))))