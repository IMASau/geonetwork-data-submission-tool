(ns metcalf.common.rules
  (:require [cljs.spec.alpha :as s]
            [clojure.string :as string]
            [interop.date :as date]))

;NOTE: No rule ordering just now

(def ^:dynamic rule-registry {})
(defn get-rule-handler [ruleId] (get-in rule-registry [ruleId :handler] identity))
(defn apply-rule [block ruleId ruleData] ((get-rule-handler ruleId) block ruleData))
(defn req-rules [rules] (if (map? rules) (seq rules) (mapcat seq rules)))
(defn apply-rules [block] (reduce-kv apply-rule block (req-rules (:rules block))))

(def empty-values #{nil "" [] {} #{}})

(defn required-field
  [block required]
  (s/assert #{true} required)
  (let [value (get-in block [:properties :value])]
    (-> block
        (assoc-in [:properties :required] true)
        (cond-> (contains? empty-values value)
                (update-in [:properties :errors] conj "This field is required")))))

(defn max-length
  [block maxLength]
  (assoc-in block [:properties :maxLength] maxLength))

(defn date-order
  "Start date should be fore end date"
  [block {:keys [prop0 prop1]}]
  (let [k0 (keyword prop0)                                  ; FIXME: ugly converions to keyword
        k1 (keyword prop1)                                  ; FIXME: ugly converions to keyword
        d0 (get-in block [:content k0 :properties :value])
        d1 (get-in block [:content k1 :properties :value])
        r [d0 d1]
        out-of-order? (and (not (string/blank? d0))         ; FIXME: payload includes "" instead of null
                           (not (string/blank? d1))         ; FIXME: payload includes "" instead of null
                           (not= r (sort r)))]
    (cond-> block
      out-of-order?
      (update-in [:content k1 :properties :errors] conj
                 (str "Must be after " (date/to-string (date/from-value d0)))))))
