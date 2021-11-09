(ns metcalf.common.fx3
  (:require [cljs.spec.alpha :as s]
            [goog.net.cookies]
            [goog.net.XhrIo :as xhrio]
            [goog.structs :as structs]
            [interop.cljs-ajax :refer [POST]]
            [re-frame.core :as rf]))

;; FIXME: Temporary work around. Should be able to get client cookies instead of asking the server.
(defn get-csrf
  []
  (let [payload (js->clj (aget js/window "payload") :keywordize-keys true)]
    #_(get-csrf)
    (get-in payload [:context :csrf])))

(defn ^:export get-cookie
  [_name]
  #_(.get goog.net.cookies _name))

(def get-json-header
  (structs/Map. (clj->js {:Accept "application/json" :X-CSRFToken (get-csrf)})))

(defn xhrio-get-json
  [{:keys [uri resp-v]}]

  (s/assert string? uri)
  (s/assert vector? resp-v)

  (letfn [(callback [^js e]
            (let [json (.. e -target getResponseJson)]
              (rf/dispatch (conj resp-v json))))]
    (xhrio/send uri callback "GET" nil get-json-header)))

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