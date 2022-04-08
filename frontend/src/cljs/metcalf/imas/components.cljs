(ns metcalf.imas.components
  (:require [clojure.string :as string]
            [metcalf.common.components4 :as components4]
            [metcalf.common.low-code4 :as low-code4]
            [re-frame.core :as rf]))


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
  [config]
  (let [config (assoc config :data-path [])
        page @(rf/subscribe [:subs/get-page-props])
        ;; FIXME need an m4 saving? value.
        saving (:metcalf3.handlers/saving? page)
        {:keys [document urls]} @(rf/subscribe [:subs/get-context])
        disabled @(rf/subscribe [:subs/get-form-disabled?])
        has-errors? @(rf/subscribe [::components4/has-block-errors? config])
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
  [config]
  (let [config (assoc config :data-path [])
        page @(rf/subscribe [:subs/get-page-props])
        ;; FIXME need an m4 saving? value.
        saving (:metcalf3.handlers/saving? page)
        {:keys [document]} @(rf/subscribe [:subs/get-context])
        has-errors? @(rf/subscribe [::components4/has-block-errors? config])]
    (if has-errors?
      [:span.text-danger [:b "Unable to lodge: "]
       "There are still errors which must be corrected first."]
      [:span.text-success
       [:b
        (cond
          saving "Submitting..."
          (= (:status document) "Draft") "Ready to lodge"
          (= (:status document) "Submitted") "Your record has been submitted."
          :else (:status document))]])))

(defn mailto-data-manager-link
  []
  (let [{:keys [site]} @(rf/subscribe [:subs/get-context])
        {:keys [email]} site]
    [:a {:href (str "mailto:" email)} email]))

(defn add-clone-person-settings
  [_]
  {::low-code4/req-ks [:form-id :data-path :button-text :value-path :added-path :source-path :label-path]
   ::low-code4/opt-ks [:random-uuid-value?]})

(defn add-clone-person
  [config]
  (let [{:keys [button-text source-path label-path]} config
        options @(rf/subscribe [::components4/get-block-data (assoc config :data-path source-path)])
        option-items (if options
                       (map
                        (fn [option]
                          {:text    (get-in option label-path)
                           :onClick #(rf/dispatch [::components4/list-option-picker-change config option])})
                        options))
        menu-items (concat
                    [{:text "Add new"
                      :icon "add"
                      :onClick #(rf/dispatch [::components4/list-add-with-defaults-click-handler3 config])}]
                    (if option-items
                      [{:divider true}])
                    option-items)]
    [components4/dropdown-menu
     {:text       button-text
      :placement  "right"
      :menu-items menu-items
      :class      "bp3-button bp3-intent-primary"}]))