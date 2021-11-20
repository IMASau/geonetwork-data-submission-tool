(ns metcalf.tern.handlers-test
  (:require [clojure.test :refer [deftest is testing]])
  (:require [metcalf.tern.handlers :as tern-handlers]
            [cljs.spec.alpha :as s]
            [cljs.spec.test.alpha :as stest]
            [clojure.test.check.generators]
            [clojure.test.check :as tc]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [metcalf.common.low-code4 :as low-code]
            [cljs.pprint :as pprint]))

(s/def ::page-name #{"Dashboard" "Edit"})
(s/def ::id keyword?)
(s/def ::name string?)
(s/def ::edit-tab (s/keys :req-un [::id ::name]))
(s/def ::edit-tabs (s/coll-of ::edit-tab))
(s/def ::init-db--args-map (s/keys :req-un [::page-name]
                                   :opt-un [::edit-tabs]))
(s/def ::db map?)
(s/def ::event-v (s/cat :eid keyword? :args (s/* any?)))
(s/def ::fx (s/coll-of (s/nilable ::event-v)))
(s/def ::fx-ret
  (s/and (s/keys :opt-un [::db ::fx])
         (fn [ret] (empty? (dissoc ret :fx :db)))))

(def init-db--fx-ret
  (prop/for-all [{:keys [page-name edit-tabs]} (s/gen ::init-db--args-map)]
    (let [ui-data (when (seq edit-tabs)
                    {:low-code/edit-tabs edit-tabs})
          payload (cond-> {:page {:name page-name}}
                    (seq ui-data) (assoc :ui_payload (pr-str ui-data)))
          ret (tern-handlers/init-db {:db {}} [:_ payload])]
      (println ret)
      (and (s/valid? map? (:db ret))
           (s/valid? ::fx-ret ret)))))

(deftest init-db--gen
  (is (= true (:pass? (tc/quick-check 100 init-db--fx-ret)))))

(comment (tc/quick-check 100 init-db--fx-ret))
