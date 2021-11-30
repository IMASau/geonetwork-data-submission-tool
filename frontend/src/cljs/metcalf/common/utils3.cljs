(ns metcalf.common.utils3)

(defn vec-remove [v i]
  {:pre [(vector? v) (nat-int? i) (contains? v i)]}
  (reduce conj (vec (subvec v 0 i)) (subvec v (inc i) (count v))))

(defn vec-insert [v idx x]
  {:pre [(vector? v) (nat-int? idx)]}
  (into (conj (subvec v 0 idx) x) (subvec v idx)))