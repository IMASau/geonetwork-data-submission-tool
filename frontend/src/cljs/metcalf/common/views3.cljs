(ns metcalf.common.views3
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [cljs.core.async :refer [<! chan put!]]
            [goog.dom.classes :as gclasses]
            [goog.events :as gevents]
            [goog.labs.userAgent.platform :as gplatform]
            [metcalf.common.utils3 :as utils3]
            [re-frame.core :as rf]
            [reagent.core :as r])
  (:import [goog.events FileDropHandler]))


(def ESCAPE-KEY-CODE 27)

(defn show-modal [this]
  (gclasses/add js/document.body "modal-open")
  (.addEventListener js/window "keydown" (:key-down-callback (r/state this))))

(defn hide-modal [this]
  (gclasses/remove js/document.body "modal-open")
  (.removeEventListener js/window "keydown" (:key-down-callback (r/state this))))

(defn ModalContent
  [{:keys [loading modal-header modal-body modal-footer hide-footer ok-copy
           on-dismiss on-cancel on-save]}]
  [:div.modal-content
   [:div.modal-header
    [:button.close {:disabled loading :on-click #(on-dismiss %)}
     [:span {:dangerouslySetInnerHTML {:__html "&times;"}}]]
    [:h4.modal-title modal-header]]
   [:div.modal-body modal-body]
   (when-not hide-footer
     (if modal-footer
       [:div.modal-footer modal-footer]
       [:div.modal-footer
        (when loading [:span [:span.fa.fa-spinner.fa-spin] " "])
        (when on-cancel
          [:button.btn.btn-default {:disabled loading
                                    :on-click on-cancel} "Cancel"])
        (when on-save
          [:button.btn.btn-primary {:disabled loading
                                    :on-click on-save} (or ok-copy "OK")])]))])

(defn Modal
  [{:keys [modal-header modal-body modal-footer dialog-class hide-footer
           on-save on-cancel on-dismiss ok-copy loading]}]
  (letfn [

          (init-state [this]
            {:key-down-callback
             (fn [e] (when (= ESCAPE-KEY-CODE (.-keyCode e))
                       (when-let [on-dismiss (:on-dismiss (r/props this))]
                         (on-dismiss e))))})

          (did-mount [this]
            (show-modal this))

          (will-unmount [this]
            (hide-modal this))

          (render [_]
            [:div.modal-open
             [:div.modal.in {:style {:display "block"}      ;:tabIndex -1
                             }
              [:div.modal-dialog {:class dialog-class}
               [ModalContent
                {:loading      loading
                 :modal-header modal-header
                 :modal-body   modal-body
                 :modal-footer modal-footer
                 :hide-footer  hide-footer
                 :ok-copy      ok-copy
                 :on-dismiss   on-dismiss
                 :on-cancel    on-cancel
                 :on-save      on-save}]]]
             [:div.modal-backdrop.in {:style    (if (gplatform/isIos)
                                                  {:position "sticky" :top 0} ; NOTE: attempt to avoid keyboard bug
                                                  {:position "fixed"} ;GOTCHA: Large modals / scrolling is messy
                                                  )
                                      :disabled loading
                                      :on-click #(on-dismiss %)}]])]
    (r/create-class
      {:get-initial-state      init-state
       :component-did-mount    did-mount
       :component-will-unmount will-unmount
       :render                 render})))

; TODO: Build a react component for uploading
(defn handle-file [this file]
  (let [{:keys [reset-ch max-filesize]} (r/props this)]
    (if (or (not max-filesize)
            (<= (.-size file) (* 1024 1024 max-filesize)))
      (r/set-state this {:file file})
      (when max-filesize
        (rf/dispatch [:app/upload-max-filesize-exceeded
                      {:type    :modal.type/alert
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

(defn update-keys [f m]
  (reduce-kv (fn [z k v] (assoc z (f k) v)) {} m))

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
                          (rf/dispatch [:app/upload-data-confirm-upload-click-add-attachment (update-keys keyword (js->clj (.parse js/JSON (.-response xhr))))])
                          (rf/dispatch [:app/upload-data-file-upload-failed]))
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
                  {:keys [disabled] :as attachments} @(rf/subscribe [:subs/get-attachments attachments-path])
                  upload-form @(rf/subscribe [:subs/get-upload-form])]
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
                           {:on-click #(rf/dispatch [:app/delete-attachment-click
                                                     {:attachments-path attachments-path
                                                      :attachment-idx   attachment-idx}])
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
