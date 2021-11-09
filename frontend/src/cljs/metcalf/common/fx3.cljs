(ns metcalf.common.fx3
  (:require [goog.net.cookies]
            [interop.cljs-ajax :refer [POST]]
            [re-frame.core :as rf]))

;; FIXME: Temporary work around. Should be able to get client cookies instead of asking the server.
(defn get-csrf
  []
  (let [payload (js->clj (aget js/window "payload") :keywordize-keys true)]
    #_(get-csrf)
    (get-in payload [:context :csrf])))

(defn set-location-href
  [url] (aset js/location "href" url))

(defn post
  [{:keys [url success-v error-v]}]
  (POST url {:error-handler   #(rf/dispatch (conj error-v %))
             :format          :json
             :handler         #(rf/dispatch (conj success-v %))
             :headers         {"X-CSRFToken" (get-csrf)}
             :keywords?       true
             :response-format :json}))

(defn post-json-data
  [{:keys [url data success-v error-v]}]
  (POST url {:error-handler   #(rf/dispatch (conj error-v %))
             :format          :json
             :handler         #(rf/dispatch (conj success-v %))
             :headers         {"X-CSRFToken" (get-csrf)}
             :keywords?       true
             :params          (clj->js data)
             :response-format :json}))