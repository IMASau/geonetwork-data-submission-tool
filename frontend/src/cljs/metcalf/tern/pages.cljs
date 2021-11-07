(ns metcalf.tern.pages
  (:require [metcalf3.views :as views3]
            [re-frame.core :as rf]))

(defn app-root
  []
  (let [page-name @(rf/subscribe [:subs/get-app-root-page-name])
        modal-props @(rf/subscribe [:subs/get-app-root-modal-props])]
    [:div
     (when modal-props
       [views3/modal modal-props])
     (case page-name
       "404" [views3/PageView404 nil]
       "Error" [views3/PageViewError nil]
       "Edit" [views3/PageViewEdit nil]
       "Dashboard" [views3/dashboard nil]
       nil)]))
