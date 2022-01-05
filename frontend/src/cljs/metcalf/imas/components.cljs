(ns metcalf.imas.components
  (:require [re-frame.core :as rf]))

(defn portal-link
  []
  (let [{:keys [site]} @(rf/subscribe [:subs/get-context])
        {:keys [portal_title portal_url]} site]
    (if portal_url
      [:a {:href portal_url :target "_blank"} [:span.portal-title portal_title]]
      [:span.portal-title portal_title])))
