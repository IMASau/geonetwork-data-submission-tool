(ns metcalf.common.routing3)

(defn route
  ([] (subs js/window.location.hash 1))
  ([new-hash] (set! js/window.location.hash (str "#" new-hash))))

(defn ensure-coll [x]
  (if (coll? x) x [x]))

(defn start! [{:keys [iref path ->hash <-hash]
               :or   {->hash identity <-hash identity}}]
  (let [path (ensure-coll path)
        f #(swap! iref assoc-in path (<-hash (route)))]
    (add-watch iref f #(when (not= (get-in %3 path) (get-in %4 path))
                         (route (->hash (get-in %4 path)))))
    (f)
    f))