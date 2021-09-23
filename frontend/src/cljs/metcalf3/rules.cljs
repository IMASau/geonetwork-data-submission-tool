(ns metcalf3.rules)

(def ^:dynamic rule-registry {})
(defn get-rule-handler [{:keys [ruleId]}] (get rule-registry ruleId identity))
(defn apply-rule [field rule] ((get-rule-handler rule) field rule))
(defn apply-rules [field] (reduce apply-rule field (:rules field)))
