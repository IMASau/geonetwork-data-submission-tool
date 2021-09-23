(ns metcalf3.rules)

(def ^:dynamic rule-registry {})
(defn get-rule-handler [{:keys [ruleId]}] (get rule-registry ruleId identity))
(defn apply-rule [field rule] ((get-rule-handler rule) field rule))
(defn apply-rules [field] (reduce apply-rule field (:rules field)))

(defn required-field
  [field rule]
  (let [{:keys [value]} field
        empty? (contains? #{nil "" [] {} #{}} value)]
    (-> field
        (assoc :required true)
        (cond-> empty?
                (update field :errors "This field is required")))))

(defn max-length-rule
  [field rule]
  (let [{:keys [maxLength]} rule]
    (assoc field :maxLength maxLength)))
