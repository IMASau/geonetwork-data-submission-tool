(ns metcalf.imas.views 
  (:require [interop.blueprint :as bp3]
            [metcalf.common.views4 :refer [userDisplay]]))


(defn navbar
  [{:keys [context]}]
  (let [{:keys [user urls site]} context
        {:keys [Dashboard account_profile account_logout]} urls
        {:keys [title tag_line guide_pdf]} site]
    [bp3/navbar {:className "bp3-dark"}
     [:div.container
      [bp3/navbar-group {:align (:LEFT bp3/alignment)}
       [:a.bp3-button.bp3-minimal {:href Dashboard} [bp3/navbar-heading (str title " " tag_line)]]]
      [bp3/navbar-group {:align (:RIGHT bp3/alignment)}
       (if account_profile
         [:a.bp3-button.bp3-minimal {:href account_profile}
          [:span.fa.fa-user " " (userDisplay user)]]
         [:span.fa.fa-user {:style {:padding "5px 10px 5px 10px"}} " " (userDisplay user)])
       [:a.bp3-button.bp3-minimal {:href Dashboard}
        [:span.fa.fa-folder-open " My Records"]]
       [:a.bp3-button.bp3-minimal {:href guide_pdf :target "_blank"}
        [:span.fa.fa-info " Help"]]
       [:a.bp3-button.bp3-minimal {:href account_logout}
        [:span.fa.fa-sign-out " Sign Out"]]]]]))
