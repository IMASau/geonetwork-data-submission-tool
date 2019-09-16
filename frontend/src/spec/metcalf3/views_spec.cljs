 (ns metcalf3.views-spec
    (:require [metcalf3.views :as views]
              [cljs.spec.alpha :as s]))

(s/fdef views/format-columns
   :args (s/cat :flex (s/nilable coll?)
                :fixed (s/nilable coll?)
                :columns (s/nilable coll?)))