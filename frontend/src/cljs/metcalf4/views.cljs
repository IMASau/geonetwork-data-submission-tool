(ns metcalf4.views
  (:require [clojure.string :as string]
            [goog.object :as gobj]
            [interop.react-imask :as react-imask]
            [metcalf3.utils :as utils3]
            [metcalf3.widget.modal :as modal]
            [metcalf3.views :as views3]))

; For pure views only, no re-frame subs/handlers

(defn KeywordsThemeCell [rowData]
  (let [rowData (take-while (complement empty?) rowData)]
    [:div.topic-cell
     [:div.topic-path (string/join " > " (drop-last (rest rowData)))]
     [:div.topic-value (last rowData)]]))

(defn m4-modal-dialog-table-modal-edit-form
  [{:keys [form path title on-delete-click on-close-click on-save-click]}]
  [modal/Modal {:ok-copy      "Done"
                :modal-header [:span [:span.glyphicon.glyphicon-list] " Edit " title]
                :modal-body   [form path]
                :modal-footer [:div
                               [:a.btn.text-danger.pull-left
                                {:on-click #(do (.preventDefault %) (on-delete-click))}
                                [:span.glyphicon.glyphicon-remove] " Delete"]
                               [:button.btn.btn-primary {:on-click on-close-click} "Done"]]
                :on-dismiss   on-close-click
                :on-save      on-save-click}])

(defn m4-modal-dialog-table-modal-add-form
  [{:keys [form path title on-close-click on-save-click]}]
  [modal/Modal {:ok-copy      "Done"
                :modal-header [:span [:span.glyphicon.glyphicon-list] " Add " title]
                :modal-body   [form path]
                :on-dismiss   on-close-click
                :on-cancel    on-close-click
                :on-save      on-save-click}])

(defn modal-dialog-confirm
  [{:keys [message title on-dismiss on-cancel on-save]}]
  [modal/Modal
   {:modal-header [:span [:span.glyphicon.glyphicon-question-sign] " " title]
    :dialog-class "modal-sm"
    :modal-body   message
    :on-dismiss   on-dismiss
    :on-cancel    on-cancel
    :on-save      on-save}])

(defn modal-dialog-alert
  [{:keys [message on-dismiss on-save]}]
  [modal/Modal
   {:modal-header [:span [:span.glyphicon.glyphicon-exclamation-sign]
                   " " "Alert"]
    :dialog-class "modal-sm"
    :modal-body   message
    :on-dismiss   on-dismiss
    :on-save      on-save}])

(defn dashboard
  [{:keys [dashboard-props
           dashboard-create-click
           dashboard-show-all-click
           dashboard-toggle-status-filter]}]
  (let [{:keys [filtered-docs status-filter has-documents? status-freq status relevant-status-filter]} dashboard-props]
    [:div
     [views3/navbar]
     [:div.container
      [:span.pull-right {:style {:margin-top 18}} [views3/NewDocumentButton]]
      [:h1 "My Records"]
      [:div.row
       [:div.col-sm-9
        (-> [:div.list-group]
            (into (for [filtered-doc filtered-docs]
                    ^{:key (:url filtered-doc)} [views3/DocumentTeaser filtered-doc]))
            (conj (if has-documents?
                    [:a.list-group-item {:on-click dashboard-create-click}
                     [:span.glyphicon.glyphicon-star.pull-right]
                     [:p.lead.list-group-item-heading " My first record "]
                     [:p.list-group-item-text "Welcome! Since you’re new here, "
                      [:span {:style {:text-decoration "underline"}} "Click here"] " to get started."]]
                    (when (empty? filtered-docs)
                      (if (= status-filter #{"Draft" "Submitted"})
                        [:div
                         [:p "You don't have any active records: "
                          [:a {:on-click dashboard-show-all-click}
                           "show all documents"] "."]
                         [views3/NewDocumentButton]]
                        [:div
                         [:p "No documents match your filter: "
                          [:a {:on-click dashboard-show-all-click}
                           "show all documents"] "."]
                         [views3/NewDocumentButton]])))))]
       [:div.col-sm-3
        (when-not (empty? status-freq)
          (into [:div]
                (for [[status-id sname] status]
                  (let [freq (get status-freq status-id)]
                    [:div [:label
                           [:input {:type      "checkbox"
                                    :disabled  (not freq)
                                    :checked   (contains? relevant-status-filter status-id)
                                    :on-change #(dashboard-toggle-status-filter {:status-id status-id :status-filter status-filter})}]
                           " " sname
                           (when freq [:span.freq " (" freq ")"])]]))))]]]]))
