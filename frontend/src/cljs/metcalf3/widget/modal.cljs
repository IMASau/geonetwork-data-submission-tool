(ns metcalf3.widget.modal
  (:require [goog.dom.classes :as gclasses]
            [goog.labs.userAgent.platform :as gplatform]
            [reagent.core :as r]))

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
        (if loading [:span [:span.fa.fa-spinner.fa-spin] " "])
        (when on-cancel
          [:button.btn.btn-default {:disabled loading
                                    :on-click #(on-cancel %)} "Cancel"])
        (when on-save
          [:button.btn.btn-primary {:disabled loading
                                    :on-click #(on-save %)} (or ok-copy "OK")])]))])

(defn Modal
  [{:keys [modal-header modal-body modal-footer dialog-class hide-footer
           on-save on-cancel on-dismiss ok-copy loading]
    :as   props}
   this]
  (letfn [

          (init-state [this]
            {:key-down-callback
             (fn [e] (if (= ESCAPE-KEY-CODE (.-keyCode e))
                       (if-let [on-dismiss (:on-dismiss (r/props this))]
                         (on-dismiss e))))})

          (did-mount [this]
            (show-modal this))

          (will-unmount [this]
            (hide-modal this))

          (render [this]
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

