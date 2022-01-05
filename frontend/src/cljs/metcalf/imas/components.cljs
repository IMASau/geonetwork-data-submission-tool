(ns metcalf.imas.components
  (:require [re-frame.core :as rf]
            [metcalf.common.components4 :as components4]
            [clojure.string :as string]))

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
