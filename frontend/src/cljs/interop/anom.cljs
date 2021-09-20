(ns interop.anom
  (:require [clojure.spec.alpha :as s]))


; Ref https://github.com/cognitect-labs/anomalies

(s/def :anom/category #{:anom/unavailable
                        :anom/interrupted
                        :anom/incorrect
                        :anom/forbidden
                        :anom/unsupported
                        :anom/not-found
                        :anom/conflict
                        :anom/fault
                        :anom/busy})

(s/def :anom/message string?)

(s/def :anom/anomaly (s/keys :req [:anom/category]
                             :opt [:anom/message]))


; Ref https://github.com/fmnoise/anomalies-tools

(def ^:dynamic *default-category* :anom/fault)

(defn valid-category?
  "Checks if given category exists in the list of categories"
  {:added "0.1.0"}
  [cat]
  (s/valid? :anom/category cat))

(defn anomaly?
  "Checks if given value is anomaly"
  {:added "0.1.0"}
  [x]
  (s/valid? :anom/anomaly x))

(defn anomaly
  "Creates new anomaly with given category(defaults to :anom/fault) message(optional) and data(optional)"
  {:added "0.1.0"}
  ([] (anomaly *default-category* nil nil))
  ([cat-msg-data]
   (cond
     (valid-category? cat-msg-data) (anomaly cat-msg-data nil nil)
     (string? cat-msg-data) (anomaly *default-category* cat-msg-data nil)
     :else (anomaly *default-category* nil cat-msg-data)))
  ([cat-msg msg-data]
   (cond
     (and (valid-category? cat-msg) (string? msg-data)) (anomaly cat-msg msg-data nil)
     (valid-category? cat-msg) (anomaly cat-msg nil msg-data)
     (string? cat-msg) (anomaly *default-category* cat-msg msg-data)
     :else (anomaly *default-category* cat-msg msg-data)))
  ([cat msg data]
   {:pre [(valid-category? cat)
          (or (nil? msg) (string? msg))]}
   (cond-> {:anom/category cat}
     (some? msg) (assoc :anom/message msg)
     (some? data) (assoc :anom/data data))))

(def busy (partial anomaly :anom/busy))
(def busy? #(= (:anom/category %) :anom/busy))
(def conflict (partial anomaly :anom/conflict))
(def conflict? #(= (:anom/category %) :anom/conflict))
(def fault (partial anomaly :anom/fault))
(def fault? #(= (:anom/category %) :anom/fault))
(def forbidden (partial anomaly :anom/forbidden))
(def forbidden? #(= (:anom/category %) :anom/forbidden))
(def incorrect (partial anomaly :anom/incorrect))
(def incorrect? #(= (:anom/category %) :anom/incorrect))
(def interrupted (partial anomaly :anom/interrupted))
(def interrupted? #(= (:anom/category %) :anom/interrupted))
(def not-found (partial anomaly :anom/not-found))
(def not-found? #(= (:anom/category %) :anom/not-found))
(def unavailable (partial anomaly :anom/unavailable))
(def unavailable? #(= (:anom/category %) :anom/unavailable))
(def unsupported (partial anomaly :anom/unsupported))
(def unsupported? #(= (:anom/category %) :anom/unsupported))

(def message :anom/message)
(def data :anom/data)
(def category :anom/category)


; Ref https://github.com/cognitect-labs/aws-api

(def status-codes->anomalies
  {403 :anom/forbidden
   404 :anom/not-found
   503 :anom/busy
   504 :anom/unavailable})

(defn status-code->anomaly [code]
  (or (get status-codes->anomalies code)
      (if (<= 400 code 499)
        :anom/incorrect
        :anom/fault)))
