(ns metcalf3.utils-spec
  (:require [cljs.spec.alpha :as s]
            [metcalf3.utils3 :as utils]))

(s/fdef utils/clj->js*
  :args (s/cat :x ::s/any
               :depth (s/and integer? (complement neg?))))

(s/fdef utils/int-assoc-in
  :args (s/cat :m (s/or :nil nil?
                        :associative? associative?)
               :ks coll?
               :v ::s/any))

(s/fdef utils/vec-remove
  :args (s/and (s/cat :v vector? :i nat-int?)
               #(< (:i %) (count (:v %))))
  :ret vector?)

(s/fdef utils/zip
  :args (s/cat :colls (s/* coll?))
  :ret (s/every vector?))

(s/fdef utils/enum
  :args (s/cat :coll coll?)
  :ret (s/every (s/tuple nat-int? ::s/any)))