(ns condense.watch-state
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require [om.core :as om]
            [cljs.core.async :refer [put! chan dropping-buffer timeout]]
            [clojure.data :refer [diff]]))


; Internal use.  We watch the atom and use a dropping channel to
; feed into the state machine.
(def change-ch (chan (dropping-buffer 1)))

(defn describe-change [o n]
  (if (not= o n)
    (let [[only-in-old only-in-new _] (diff o n)]
      ;(.log js/console "State -" (str only-in-old))
      (.log js/console "State +" (str only-in-new)))))

(defn enable-state-change-reporting
  "Sets up a core async loop to report on counters.  Waits for 1 second of
  silence before reporting aggregates since last report."
  [iref]
  (add-watch iref ::change-listener #(put! change-ch :changed))
  (go (loop [state :idle
             before @iref]
        (case state
          :idle (alt! change-ch (recur :active before))
          :active (let [timeout-ch (timeout 800)
                        after @iref]
                    (alt! timeout-ch (do (describe-change before after)
                                         (recur :idle after))
                          change-ch (do (recur :active before))))))))