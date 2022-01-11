(ns metcalf.common.actions-test
  (:require [clojure.test :refer [deftest is testing]])
  (:require [metcalf.common.actions4 :as actions]))

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

(def s0 (-> {:db {}}
            (actions/load-edit-form-action
              {:form {:data   {}
                      :schema {:type       "object"
                               :properties {"as" {:type  "array"
                                                  :items {:type "string"}}}}}})))

(deftest select-list-item-action-test
  (testing "No action if index is invalid"
    (is (= {:db {}}
           (-> {:db {}}
               (actions/select-list-item-action [:form] [:as] 0)))))

  (testing "Selects if index valid"
    (is (= 0
           (-> s0
               (actions/new-item-action [:form] ["as"])
               (actions/select-list-item-action [:form] ["as"] 0)
               (get-in [:db :form :state :content "as" :props :list-item-selected-idx]))))

    (is (= 1
           (-> s0
               (actions/new-item-action [:form] ["as"])
               (actions/new-item-action [:form] ["as"])
               (actions/select-list-item-action [:form] ["as"] 1)
               (get-in [:db :form :state :content "as" :props :list-item-selected-idx]))))))

(deftest unselect-list-item-action-test
  (testing "No action if not selected"
    (is (= nil
           (-> {:db {}}
               (actions/new-item-action [:form] ["as"])
               (actions/unselect-list-item-action [:form] [:as])
               (get-in [:db :form :state :content "as" :props :list-item-selected-idx])))))
  (testing "Unselect if selected"
    (is (= nil
           (-> {:db {}}
               (actions/new-item-action [:form] ["as"])
               (actions/select-list-item-action [:form] ["as"] 0)
               (actions/unselect-list-item-action [:form] ["as"])
               (get-in [:db :form :state :content "as" :props :list-item-selected-idx]))))))

(deftest select-last-item-action-test
  (testing "No action if no items"
    (is (= nil
           (-> {:db {}}
               (actions/select-last-item-action [:form] ["as"])
               (get-in [:db :form :state :content "as" :props :list-item-selected-idx])))))
  (testing "No action if no items"
    (is (= 1
           (-> {:db {}}
               (actions/new-item-action [:form] ["as"])
               (actions/new-item-action [:form] ["as"])
               (actions/select-last-item-action [:form] ["as"])
               (get-in [:db :form :state :content "as" :props :list-item-selected-idx]))))))
