(ns metcalf3.fx
  (:require-macros [cljs.core.async.macros :refer [go alt! go-loop]])
  (:require [cljs.core.async :refer [put! <! alts! chan pub sub timeout dropping-buffer]]
            [ajax.core :refer [GET POST DELETE]]
            [goog.net.XhrIo :as xhrio]
            [goog.structs :as structs]
            [re-frame.core :as rf]
            [cljs.spec.alpha :as s]
            [metcalf3.logic :as logic]
            [re-frame.db :refer [app-db]])
  (:import [goog.net Cookies]))

(rf/reg-fx
  :xhrio/get-json
  (fn [{:keys [uri resp-v]}]

    (s/assert string? uri)
    (s/assert vector? resp-v)

    (letfn [(callback [e]
              (let [json (.. e -target getResponseJson)]
                (rf/dispatch (conj resp-v json))))]
      (xhrio/send uri callback))))

(def post-json-header
  (structs/Map. (clj->js {:Content-Type "application/json" :Accept "application/json"})))

(rf/reg-fx
 :xhrio/post-json
 (fn [{:keys [uri data resp-v]}]
   (s/assert string? uri)
   (s/assert string? data)
   (s/assert vector? resp-v)

   (letfn [(callback [e]
             (let [json (.. e -target getResponseJson)]
               (rf/dispatch (conj resp-v json))))]
     (xhrio/send uri callback "POST" data post-json-header))))

(rf/reg-fx
  :fx/set-location-href
  (fn [url] (aset js/location "href" url)))

(rf/reg-fx
  :fx/create-document
  (fn [{:keys [url form success-v error-v]}]
    (POST url
          {:params          (logic/extract-data form)
           :format          :json
           :response-format :json
           :keywords?       true
           :handler         #(rf/dispatch (conj success-v %))
           :error-handler   #(rf/dispatch (conj error-v %))
           :headers         {"X-CSRFToken" (.get (Cookies. js/document) "csrftoken")}})))

(rf/reg-fx
  :fx/clone-document
  (fn [{:keys [url success-v error-v]}]
    (POST url {:handler         #(rf/dispatch (conj success-v %))
               :error-handler   #(rf/dispatch (conj error-v %))
               :headers         {"X-CSRFToken" (.get (Cookies. js/document) "csrftoken")}
               :format          :json
               :response-format :json
               :keywords?       true})))

(rf/reg-fx
  :fx/transition-current-document
  (fn [{:keys [url transition success-v error-v]}]
    (POST url {:handler         #(rf/dispatch (conj success-v %))
               :error-handler   #(rf/dispatch (conj error-v %))
               :headers         {"X-CSRFToken" (.get (Cookies. js/document) "csrftoken")}
               :params          #js {:transition transition}
               :format          :json
               :response-format :json
               :keywords?       true})))

(rf/reg-fx
  :fx/submit-current-document
  (fn [{:keys [url success-v error-v]}]
    (POST url
          {:params          #js {:transition "submit"}
           :handler         #(rf/dispatch (conj success-v %))
           :error-handler   #(rf/dispatch (conj error-v %))
           :headers         {"X-CSRFToken" (.get (Cookies. js/document) "csrftoken")}
           :format          :json
           :response-format :json
           :keywords?       true})))

(rf/reg-fx
  :fx/save-current-document
  (fn [{:keys [url data success-v error-v]}]
    (POST url
          {:params          (clj->js data)
           :format          :json
           :response-format :json
           :keywords?       true
           :handler         #(rf/dispatch (conj success-v %))
           :error-handler   #(rf/dispatch (conj error-v %))
           :headers         {"X-CSRFToken" (.get (Cookies. js/document) "csrftoken")}})))

(rf/reg-fx
  :fx/archive-current-document
  (fn [{:keys [url success-v error-v]}]
    (POST url
          {:params          #js {:transition "archive"}
           :handler         #(rf/dispatch (conj success-v %))
           :error-handler   #(rf/dispatch (conj error-v %))
           :headers         {"X-CSRFToken" (.get (Cookies. js/document) "csrftoken")}
           :format          :json
           :response-format :json
           :keywords?       true})))

(rf/reg-fx
  :window/open
  (fn [{:keys [url windowName]}]
    (s/assert string? url)
    (s/assert (s/nilable string?) windowName)
    (js/window.open url windowName)))
