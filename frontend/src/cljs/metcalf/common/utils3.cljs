(ns metcalf.common.utils3
  (:require [clojure.string :as string]))

(defn map-keys [f m]
  (persistent! (reduce-kv (fn [z k v] (assoc! z (f k) v)) (transient {}) m)))

(defn vec-remove [v i]
  {:pre [(vector? v) (nat-int? i) (contains? v i)]}
  (reduce conj (vec (subvec v 0 i)) (subvec v (inc i) (count v))))

(defn vec-insert [v idx x]
  {:pre [(vector? v) (nat-int? idx)]}
  (into (conj (subvec v 0 idx) x) (subvec v idx)))

(defn userDisplay
  [user]
  (if (and (string/blank? (:lastName user))
           (string/blank? (:firstName user)))
    (if (string/blank? (:email user))
      (:username user)
      (:email user))
    (str (:firstName user) " " (:lastName user))))