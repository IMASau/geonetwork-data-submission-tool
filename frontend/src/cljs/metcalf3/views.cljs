(ns metcalf3.views
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [cljs.core.async :as async :refer [<! chan put!]]
            [clojure.edn :as edn]
            [clojure.set :as set]
            [clojure.string :as string :refer [blank?]]
            [goog.events :as gevents]
            [goog.object :as gobj]
            [goog.style :as gstyle]
            [goog.userAgent :as guseragent]
            [interop.blueprint :as bp3]
            [interop.cljs-ajax :as ajax]
            [interop.cuerdas :as cuerdas]
            [interop.fixed-data-table-2 :refer [Cell Column Table]]
            [interop.moment :as moment]
            [interop.react-imask :as react-imask]
            [metcalf3.content :refer []]
            [metcalf3.handlers :as handlers3]
            [metcalf3.logic :as logic3]
            [metcalf3.utils :as utils3]
            [metcalf3.widget.modal :refer [Modal]]
            [metcalf3.widget.tree :refer []]
            [metcalf4.low-code :as low-code]
            [re-frame.core :as rf]
            [reagent.core :as r])
  (:import [goog.dom ViewportSizeMonitor]
           [goog.events FileDropHandler]
           [goog.events EventType]))

(defn label-template
  [{:keys [label required]}]
  (when label
    [:label label (when required " *")]))

(defn masked-text-widget
  [{:keys [mask value placeholder disabled on-change on-blur]}]
  [react-imask/masked-input
   {:mask        mask
    :disabled    disabled
    :value       value
    :class       "form-control"
    :placeholder placeholder
    :on-change   on-change
    :on-blur     on-blur}])

; TODO: Build a react component for uploading
(defn handle-file [this file]
  (let [{:keys [reset-ch max-filesize]} (r/props this)]
    (if (or (not max-filesize)
            (<= (.-size file) (* 1024 1024 max-filesize)))
      (r/set-state this {:file file})
      (when max-filesize
        (rf/dispatch [::open-modal
                      {:type    :alert
                       :message (str "Please, choose file less than " max-filesize "mb")}])
        (put! reset-ch true)))))

; TODO: Build a react component for uploading
(defn FileDrop [{:keys [on-change placeholder reset-ch]}]
  (letfn [(init-state [_]
            {:file-id (name (gensym "file"))})
          (did-mount [this]
            (gevents/listen
              (FileDropHandler. js/document)
              goog.events.FileDropHandler.EventType.DROP
              (fn [^js e] (handle-file this (.. e getBrowserEvent -dataTransfer -files (item 0)))))
            (go-loop []
                     (when (<! reset-ch)
                       (r/set-state this {:file nil})
                       (recur))))
          (did-update [this [_ _ prev-state]]
            (let [{:keys [file]} (r/state this)]
              (when (and on-change (not= file (:file prev-state)))
                (on-change file))))
          (render [this]
            (let [{:keys [file file-id]} (r/state this)]
              [:div
               {:style {:position "relative"}}
               [:div.text-center.dropzone {:on-click #(.click (js/document.getElementById file-id))}
                [:h3
                 (or (and file (.-name file)) placeholder
                     "Drop file here or click here to upload")]
                [:span.help-block "Maximum file size 100 MB"]]
               [:input
                {:id        file-id
                 :type      "file"
                 :on-change #(handle-file this (.. % -target -files (item 0)))
                 :style     {:position "absolute"
                             :z-index  1
                             :opacity  0
                             :left     0
                             :top      0
                             :width    "100%"
                             :height   "100%"}}]]))]
    (r/create-class
      {:get-initial-state    init-state
       :component-did-mount  did-mount
       :component-did-update did-update
       :render               render})))

(defn delete-attachment!
  "Quick and dirty delete function"
  [attachments-path attachment-idx]
  (rf/dispatch [::open-modal
                {:type       :confirm
                 :title      "Delete?"
                 :message    "Are you sure you want to delete this file?"
                 :on-confirm #(rf/dispatch [::del-value attachments-path attachment-idx])}]))

; TODO: Build a react component for uploading
(defn UploadData
  [_]
  (letfn [(confirm-upload-click
            [this {:keys [url fields]} file reset-file-drop]
            (r/set-state this {:uploading true})
            (let [fd (js/FormData.)
                  xhr (js/XMLHttpRequest.)]
              (.open xhr "POST" url true)
              (set! (.-onreadystatechange xhr)
                    (fn []
                      (when (= (.-readyState xhr) 4)
                        (if (#{200 201} (.-status xhr))
                          (rf/dispatch [::upload-data-confirm-upload-click-add-attachment (utils3/map-keys keyword (js->clj (.parse js/JSON (.-response xhr))))])
                          (rf/dispatch [::open-modal
                                        {:type    :alert
                                         :message "File upload failed. Please try again or contact administrator."}]))
                        (r/set-state this {:uploading false})
                        (put! reset-file-drop true))))
              (doto fd
                (.append "csrfmiddlewaretoken" (get-in fields [:csrfmiddlewaretoken :initial]))
                (.append "document" (get-in fields [:document :initial]))
                (.append "name" (.-name file))
                (.append "file" file))
              (.send xhr fd)))
          (init-state [_]
            {:reset-file-drop (chan)})
          (render [this]
            (let [{:keys [attachments-path]} (r/props this)
                  {:keys [file reset-file-drop uploading]} (r/state this)
                  {:keys [disabled] :as attachments} @(rf/subscribe [:subs/get-derived-path attachments-path])
                  upload-form @(rf/subscribe [:subs/get-derived-path [:upload_form]])]
              [:div.UploadData {:class (when disabled "disabled")}
               (if-not (empty? (:value attachments))
                 [:div
                  [:table.table.table-hover
                   [:thead
                    [:tr [:th "Name"]]]
                   [:tbody
                    (for [[attachment-idx attachment] (map-indexed vector (:value attachments))]
                      (let [{:keys [file name]} (:value attachment)]
                        [:tr
                         [:td
                          [:a {:href (:value file) :target "blank"} (:value name)]
                          [:button.btn.btn-warn.btn-xs.pull-right
                           {:on-click #(delete-attachment! attachments-path attachment-idx)
                            :disabled disabled}
                           [:span.glyphicon.glyphicon-minus]]]]))]]]
                 [:p "There are no data files attached to this record"])
               (when-not disabled
                 [:div
                  [FileDrop
                   {:name         "file"
                    :max-filesize 100
                    :reset-ch     reset-file-drop
                    :on-change    #(r/set-state this {:file %})}]
                  [:button.btn.btn-primary
                   {:on-click #(confirm-upload-click this upload-form file reset-file-drop)
                    :disabled (or uploading (not file))}
                   "Confirm Upload"]])]))]
    (r/create-class
      {:get-initial-state init-state
       :render            render})))
