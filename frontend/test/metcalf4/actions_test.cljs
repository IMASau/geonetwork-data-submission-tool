(ns metcalf4.actions-test
  (:require [clojure.test :refer [deftest is testing]])
  (:require [metcalf4.actions :as actions]))

(deftest init-snapshots-action-test
  (is (= nil
         (-> {:db {}}
             (get-in [:db :form :snapshots])))))

(deftest save-snapshot-action-test

  (is (= '(1)
         (-> {:db {}}
             (assoc-in [:db :form :state] 1)
             (actions/save-snapshot-action [:form])
             (get-in [:db :form :snapshots]))))

  (is (= '(2 1)
         (-> {:db {}}
             (assoc-in [:db :form :state] 1)
             (actions/save-snapshot-action [:form])
             (assoc-in [:db :form :state] 2)
             (actions/save-snapshot-action [:form])
             (get-in [:db :form :snapshots])))))

(deftest discard-snapshot-action-test
  (is (= (-> {:db {}}
             (get-in [:db :form :snapshots]))
         nil))

  (is (= '()
         (-> {:db {}}
             (assoc-in [:db :form :state] 1)
             (actions/save-snapshot-action [:form])
             (actions/discard-snapshot-action [:form])
             (get-in [:db :form :snapshots]))))

  (is (= '(1)
         (-> {:db {}}
             (assoc-in [:db :form :state] 1)
             (actions/save-snapshot-action [:form])
             (assoc-in [:db :form :state] 2)
             (actions/save-snapshot-action [:form])
             (actions/discard-snapshot-action [:form])
             (get-in [:db :form :snapshots])))))

(deftest restore-snapshot-action-test
  (is (= 2
         (-> {:db {}}
             (assoc-in [:db :form :state] 1)
             (actions/save-snapshot-action [:form])
             (assoc-in [:db :form :state] 2)
             (actions/save-snapshot-action [:form])
             (assoc-in [:db :form :state] 3)
             (actions/restore-snapshot-action [:form])
             (get-in [:db :form :state]))))

  (is (= 1
         (-> {:db {}}
             (assoc-in [:db :form :state] 1)
             (actions/save-snapshot-action [:form])
             (assoc-in [:db :form :state] 2)
             (actions/save-snapshot-action [:form])
             (assoc-in [:db :form :state] 3)
             (actions/restore-snapshot-action [:form])
             (actions/restore-snapshot-action [:form])
             (get-in [:db :form :state])))))
