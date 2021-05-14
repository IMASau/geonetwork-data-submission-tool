(ns condense.history
  (:require [clojure.browser.event :as event]
            [goog.History :as history]
            [goog.history.Html5History :as history5]))


; Taken fron ClojureScript One Guide
; http://clojurescriptone.com/documentation.html


(extend-type goog.History
  event/EventType
  (event-types [this]
    (into {}
          (map
            (fn [[k v]]
              [(-> k clojure.string/lower-case keyword)
               v])
            (js->clj goog.history.EventType)))))

(defn history
  [callback]
  (let [h (if (history5/isSupported)
            (goog.history.Html5History.)
            (goog.History.))]
    (do (event/listen h "navigate"
                      (fn [e]
                        (callback {:token       (keyword (.-token e))
                                   :type        (.-type e)
                                   :navigation? (.-isNavigation e)})))
        (.setEnabled h true)
        h)))


(defn set-token
  [history token]
  (.setToken history (name token)))