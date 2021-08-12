(ns metcalf3.fx
  (:require [cljs.spec.alpha :as s]
            [goog.net.XhrIo :as xhrio]
            [goog.structs :as structs]
            [interop.cljs-ajax :refer [POST]]
            [metcalf3.logic :as logic]
            [re-frame.core :as rf])
  (:import [goog.net Cookies]))

(def get-json-header
  (structs/Map. (clj->js {:Accept "application/json" :X-CSRFToken (.get (Cookies. js/document) "csrftoken")})))

(def post-json-header
  (structs/Map. (clj->js {:Content-Type "application/json" :Accept "application/json" :X-CSRFToken (.get (Cookies. js/document) "csrftoken")})))

(defn xhrio-get-json
  [{:keys [uri resp-v]}]

  (s/assert string? uri)
  (s/assert vector? resp-v)

  (letfn [(callback [^js e]
            (let [json (.. e -target getResponseJson)]
              (rf/dispatch (conj resp-v json))))]
    (xhrio/send uri callback "GET" nil get-json-header)))

(defn xhrio-post-json
  [{:keys [uri data resp-v]}]
  (s/assert string? uri)
  (s/assert string? data)
  (s/assert vector? resp-v)

  (letfn [(callback [^js e]
            (let [json (.. e -target getResponseJson)]
              (rf/dispatch (conj resp-v json))))]
    (xhrio/send uri callback "POST" data post-json-header)))

(defn set-location-href
  [url] (aset js/location "href" url))

(defn create-document
  [{:keys [url form success-v error-v]}]
  (POST url
        {:params          (logic/extract-data form)
         :format          :json
         :response-format :json
         :keywords?       true
         :handler         #(rf/dispatch (conj success-v %))
         :error-handler   #(rf/dispatch (conj error-v %))
         :headers         {"X-CSRFToken" (.get (Cookies. js/document) "csrftoken")}}))

(defn clone-document
  [{:keys [url success-v error-v]}]
  (POST url {:handler         #(rf/dispatch (conj success-v %))
             :error-handler   #(rf/dispatch (conj error-v %))
             :headers         {"X-CSRFToken" (.get (Cookies. js/document) "csrftoken")}
             :format          :json
             :response-format :json
             :keywords?       true}))

(defn transition-current-document
  [{:keys [url transition success-v error-v]}]
  (POST url {:handler         #(rf/dispatch (conj success-v %))
             :error-handler   #(rf/dispatch (conj error-v %))
             :headers         {"X-CSRFToken" (.get (Cookies. js/document) "csrftoken")}
             :params          #js {:transition transition}
             :format          :json
             :response-format :json
             :keywords?       true}))

(defn submit-current-document
  [{:keys [url success-v error-v]}]
  (POST url
        {:params          #js {:transition "submit"}
         :handler         #(rf/dispatch (conj success-v %))
         :error-handler   #(rf/dispatch (conj error-v %))
         :headers         {"X-CSRFToken" (.get (Cookies. js/document) "csrftoken")}
         :format          :json
         :response-format :json
         :keywords?       true}))

(defn save-current-document
  [{:keys [url data success-v error-v]}]
  (POST url
        {:params          (clj->js data)
         :format          :json
         :response-format :json
         :keywords?       true
         :handler         #(rf/dispatch (conj success-v %))
         :error-handler   #(rf/dispatch (conj error-v %))
         :headers         {"X-CSRFToken" (.get (Cookies. js/document) "csrftoken")}}))

(defn archive-current-document
  [{:keys [url success-v error-v]}]
  (POST url
        {:params          #js {:transition "archive"}
         :handler         #(rf/dispatch (conj success-v %))
         :error-handler   #(rf/dispatch (conj error-v %))
         :headers         {"X-CSRFToken" (.get (Cookies. js/document) "csrftoken")}
         :format          :json
         :response-format :json
         :keywords?       true}))

(defn window-open
  [{:keys [url windowName]}]
  (s/assert string? url)
  (s/assert (s/nilable string?) windowName)
  (js/window.open url windowName))
