(ns metcalf.tern.handlers-test
  (:require [clojure.test :refer [deftest is]])
  (:require [metcalf.tern.handlers :as tern-handlers]
            [cljs.spec.alpha :as s]
            [clojure.test.check.generators]
            [clojure.test.check :as tc]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]))

(s/def ::page-name #{"Dashboard" "Edit"})
(s/def ::id keyword?)
(s/def ::name string?)
(s/def ::edit-tab (s/keys :req-un [::id ::name]))
(s/def ::edit-tabs (s/coll-of ::edit-tab))
(s/def ::db map?)
(s/def ::event-v (s/cat :eid keyword? :args (s/* any?)))
(s/def ::fx (s/coll-of (s/nilable ::event-v)))
(s/def ::fx-ret
  (s/and (s/keys :opt-un [::db ::fx])
         (fn only-fx-db [ret] (empty? (dissoc ret :fx :db)))))

(def init-db--fx-ret
  (prop/for-all [{:keys [page-name edit-tabs]}
                 (s/gen (s/keys :req-un [::page-name]
                                :opt-un [::edit-tabs]))]
    (let [ui-data (when (seq edit-tabs)
                    {:low-code/edit-tabs edit-tabs})
          payload (cond-> {:page {:name page-name}}
                    (seq ui-data) (assoc :ui_payload (pr-str ui-data)))
          ret (tern-handlers/init-db {:db {}} [:_ payload])]
      (and (s/valid? map? (:db ret))
           (s/valid? ::fx-ret ret)))))

(defn qc-ret
  [{:keys [result] :as data}]
  (if result {:result true} data))

(deftest init-db--gen
  (is (= {:result true} (qc-ret (tc/quick-check 100 init-db--fx-ret)))))

(comment (tc/quick-check 100 init-db--fx-ret)
         (init-db--gen)
         (cljs.test/run-tests))
