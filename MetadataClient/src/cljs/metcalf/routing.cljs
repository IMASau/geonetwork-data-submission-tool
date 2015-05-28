(ns metcalf.routing
  (:require [goog.events :as e]
            [goog.events.EventType :as et]))

(defn route
  ([] (subs js/window.location.hash 1))
  ([new-hash] (set! js/window.location.hash (str "#" new-hash))))

(defn ensure-coll [x]
  (if (coll? x) x [x]))

(defn start! [{:keys [iref path ->hash <-hash]
               :or   {->hash identity <-hash identity}}]
  (let [path (ensure-coll path)
        f #(swap! iref assoc-in path (<-hash (route)))]
    (e/listen js/window et/HASHCHANGE f)
    (add-watch iref f #(when (not= (get-in %3 path) (get-in %4 path))
                        (route (->hash (get-in %4 path)))))
    (f)
    f))

(defn stop! [a f]
  (remove-watch a f)
  (e/unlisten js/window et/HASHCHANGE f))