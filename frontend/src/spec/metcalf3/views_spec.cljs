 (ns metcalf3.views-spec
    (:require [cljs.spec.alpha :as s]
              [metcalf3.views3 :as views]))

(s/fdef views/format-columns
   :args (s/cat :flex (s/nilable coll?)
                :fixed (s/nilable coll?)
                :columns (s/nilable coll?)))