(ns metcalf4.actions-test
  (:require [clojure.test :refer [deftest is testing]])
  (:require [metcalf4.actions :as actions]))

(deftest init-snapshots-action-test
  (is (= (actions/init-snapshots-action {:db {}})
         {:db {::actions/snapshots '()}})))

(deftest save-snapshot-action-test

  (is (= '(1)
         (-> {:db {}}
             (actions/init-snapshots-action)
             (assoc-in [:db :form :state] 1)
             (actions/save-snapshot-action [:form])
             (get-in [:db ::actions/snapshots]))))

  (is (= '(2 1)
         (-> {:db {}}
             (actions/init-snapshots-action)
             (assoc-in [:db :form :state] 1)
             (actions/save-snapshot-action [:form])
             (assoc-in [:db :form :state] 2)
             (actions/save-snapshot-action [:form])
             (get-in [:db ::actions/snapshots])))))

(deftest discard-snapshot-action-test
  (is (= (actions/init-snapshots-action {:db {}})
         {:db {::actions/snapshots '()}}))

  (is (= '()
         (-> {:db {}}
             (actions/init-snapshots-action)
             (assoc-in [:db :form :state] 1)
             (actions/save-snapshot-action [:form])
             (actions/discard-snapshot-action)
             (get-in [:db ::actions/snapshots]))))

  (is (= '(1)
         (-> {:db {}}
             (actions/init-snapshots-action)
             (assoc-in [:db :form :state] 1)
             (actions/save-snapshot-action [:form])
             (assoc-in [:db :form :state] 2)
             (actions/save-snapshot-action [:form])
             (actions/discard-snapshot-action)
             (get-in [:db ::actions/snapshots])))))

(deftest restore-snapshot-action-test
  (is (= 2
         (-> {:db {}}
             (actions/init-snapshots-action)
             (assoc-in [:db :form :state] 1)
             (actions/save-snapshot-action [:form])
             (assoc-in [:db :form :state] 2)
             (actions/save-snapshot-action [:form])
             (assoc-in [:db :form :state] 3)
             (actions/restore-snapshot-action [:form])
             (get-in [:db :form :state]))))

  (is (= 1
         (-> {:db {}}
             (actions/init-snapshots-action)
             (assoc-in [:db :form :state] 1)
             (actions/save-snapshot-action [:form])
             (assoc-in [:db :form :state] 2)
             (actions/save-snapshot-action [:form])
             (assoc-in [:db :form :state] 3)
             (actions/restore-snapshot-action [:form])
             (actions/restore-snapshot-action [:form])
             (get-in [:db :form :state])))))
