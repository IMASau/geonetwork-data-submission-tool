(ns metcalf.imas.components
  (:require [re-frame.core :as rf]
            [metcalf.common.components4 :as components4]
            [clojure.string :as string]
            [metcalf.common.low-code4 :as low-code4]))

(defn portal-link
  []
  (let [{:keys [site]} @(rf/subscribe [:subs/get-context])
        {:keys [portal_title portal_url]} site]
    (if portal_url
      [:a {:href portal_url :target "_blank"} [:span.portal-title portal_title]]
      [:span.portal-title portal_title])))


(defn note-for-data-manager-settings
  "Settings for note-for-data-manager component"
  [_]
  {::low-code4/req-ks [:form-id :data-path]
   ::low-code4/opt-ks []})

(defn note-for-data-manager
  [config]
  (let [{:keys [document]} @(rf/subscribe [:subs/get-context])
        value @(rf/subscribe [::components4/get-block-data config])]
    [:div
     {:style {:padding-top    5
              :padding-bottom 5}}
     (if (= "Draft" (:status document))
       [components4/form-group config
        [components4/textarea-field config]]
       (when-not (string/blank? value)
         [:div
          [:strong "Note for the data manager:"]
          [:p value]]))]))

(defn lodge-button
  []
  (let [page @(rf/subscribe [:subs/get-page-props])
        ;; FIXME need an m4 saving? value.
        saving (:metcalf3.handlers/saving? page)
        {:keys [document urls]} @(rf/subscribe [:subs/get-context])
        {:keys [errors]} @(rf/subscribe [:subs/get-progress])
        disabled @(rf/subscribe [:subs/get-form-disabled?])
        has-errors? (and errors (> errors 0))
        archived? (= (:status document) "Archived")
        submitted? (= (:status document) "Submitted")]
    (when-not (or archived? submitted?)
      [:button.btn.btn-primary.btn-lg
       {:disabled (or has-errors? saving disabled)
        :on-click #(rf/dispatch [::lodge-button-click])}
       (when saving
         [:img
          {:src (str (:STATIC_URL urls)
                     "metcalf3/img/saving.gif")}])
       "Lodge data"])))

(defn lodge-status-info
  []
  (let [page @(rf/subscribe [:subs/get-page-props])
        ;; FIXME need an m4 saving? value.
        saving (:metcalf3.handlers/saving? page)
        {:keys [document]} @(rf/subscribe [:subs/get-context])
        {:keys [errors]} @(rf/subscribe [:subs/get-progress])
        is-are (if (> errors 1) "are" "is")
        plural (when (> errors 1) "s")
        has-errors? (and errors (> errors 0))]
    (if has-errors?
      [:span.text-danger [:b "Unable to lodge: "]
       "There " is-are " " [:span errors " error" plural
                            " which must be corrected first."]]
      [:span.text-success
       [:b
        (cond
          saving "Submitting..."
          (= (:status document) "Draft") "Ready to lodge"
          (= (:status document) "Submitted") "Your record has been submitted."
          :else (:status document))]])))


(defn xml-export-link-settings
  "Settings for xml-export-link component"
  [_]
  {::low-code4/req-ks [:label]
   ::low-code4/opt-ks [:form-id :data-path]})

(defn xml-export-link
  [config]
  (let [{:keys [label]} @(rf/subscribe [::components4/get-block-props config])
        {:keys [document]} @(rf/subscribe [:subs/get-context])
        dirty @(rf/subscribe [:subs/get-form-dirty])
        download-props {:href     (str (:export_url document) "?download")
                        :on-click #(when dirty
                                     (js/alert "Please save changes before exporting."))}]
    [:a download-props label]))
